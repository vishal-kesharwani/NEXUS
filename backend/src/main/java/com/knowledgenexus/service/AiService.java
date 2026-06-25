package com.knowledgenexus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenexus.model.Conversation;
import com.knowledgenexus.model.Message;
import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import com.knowledgenexus.repository.ConversationRepository;
import com.knowledgenexus.repository.MessageRepository;
import com.knowledgenexus.repository.UserRepository;
import com.knowledgenexus.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;

    @Value("${OPENROUTER_API_KEY:}")
    private String openrouterApiKey;

    @Value("${OPENROUTER_API_KEYS:}")
    private String openrouterApiKeys;

    @Value("${OPENROUTER_MODEL:deepseek/deepseek-v4-pro}")
    private String openrouterModel;

    @Value("${OPENAI_API_KEY:}")
    private String openaiApiKey;

    @Value("${OPENAI_MODEL:gpt-4o-mini}")
    private String openaiModel;

    public String getMatchReason(UUID mentorId, UUID menteeId) {
        User mentor = userRepository.findById(mentorId).orElseThrow();
        User mentee = userRepository.findById(menteeId).orElseThrow();

        List<UserSkill> mentorSkills = userSkillRepository.findByUserId(mentorId);
        List<UserSkill> menteeSkills = userSkillRepository.findByUserId(menteeId);

        String mSkillsStr = mentorSkills.stream().map(s -> s.getSkill().getName()).collect(Collectors.joining(", "));
        String tSkillsStr = menteeSkills.stream().map(s -> s.getSkill().getName()).collect(Collectors.joining(", "));

        // Check if there are overlapping skills
        Set<String> overlapping = new HashSet<>();
        mentorSkills.forEach(ms -> {
            menteeSkills.forEach(ts -> {
                if (ms.getSkill().getName().equalsIgnoreCase(ts.getSkill().getName())) {
                    overlapping.add(ms.getSkill().getName());
                }
            });
        });

        String prompt = String.format(
                "Explain why %s %s (Mentor) is a good match for %s %s (Mentee).\n" +
                "Mentor details:\n" +
                "- Headline: %s\n" +
                "- Company: %s\n" +
                "- Experience: %d years\n" +
                "- Skills: %s\n\n" +
                "Mentee details:\n" +
                "- Headline: %s\n" +
                "- Skills: %s\n\n" +
                "Focus on the overlapping skills: %s. Write a concise, professional paragraph matching them (max 4 sentences).",
                mentor.getFirstName(), mentor.getLastName(),
                mentee.getFirstName(), mentee.getLastName(),
                mentor.getHeadline() != null ? mentor.getHeadline() : "Mentor",
                mentor.getCompany() != null ? mentor.getCompany() : "N/A",
                mentor.getExperienceYears() != null ? mentor.getExperienceYears() : 3,
                mSkillsStr,
                mentee.getHeadline() != null ? mentee.getHeadline() : "Mentee",
                tSkillsStr,
                overlapping.isEmpty() ? "general interest in tech" : String.join(", ", overlapping)
        );

        if (!hasAnyApiKey()) {
            return generateMockMatchReason(mentor, mentee, overlapping);
        }

        return callAiEndpoint(prompt, "You are a professional mentorship advisor.");
    }

    public String askAiAssistant(UUID conversationId, String userPrompt) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        List<Message> history = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        StringBuilder historyStr = new StringBuilder();
        for (Message msg : history) {
            historyStr.append(String.format("%s: %s\n", 
                msg.getSender().getFirstName() + " " + msg.getSender().getLastName(), 
                msg.getContent()
            ));
        }

        String prompt = String.format(
                "You are an AI mentorship assistant. Here is the conversation history between mentor %s and mentee %s:\n" +
                "\"\"\"\n%s\n\"\"\"\n" +
                "The user is asking: \"%s\"\n" +
                "Based on their history, provide a helpful, constructive response. Format in clean markdown.",
                conversation.getMentor().getFirstName(),
                conversation.getMentee().getFirstName(),
                historyStr.toString(),
                userPrompt
        );

        if (!hasAnyApiKey()) {
            return generateMockAiResponse(userPrompt, conversation, history);
        }

        return callAiEndpoint(prompt, "You are an assistant inside a chat application helping mentors and mentees.");
    }

    private boolean hasAnyApiKey() {
        return !getOpenRouterKeys().isEmpty() || getOpenAiKey() != null;
    }

    private List<String> getOpenRouterKeys() {
        List<String> keys = new ArrayList<>();
        addKeys(keys, openrouterApiKeys);
        addKeys(keys, openrouterApiKey);
        addKeys(keys, System.getenv("OPENROUTER_API_KEYS"));
        addKeys(keys, System.getenv("OPENROUTER_API_KEY"));
        return keys.stream()
                .map(String::trim)
                .filter(key -> !key.isBlank())
                .distinct()
                .toList();
    }

    private String getOpenAiKey() {
        String key = firstNonBlank(openaiApiKey, System.getenv("OPENAI_API_KEY"));
        return key == null ? null : key.trim();
    }

    private void addKeys(List<String> keys, String rawKeys) {
        if (rawKeys == null || rawKeys.isBlank()) {
            return;
        }
        Arrays.stream(rawKeys.split("[,;\\s]+"))
                .map(String::trim)
                .filter(key -> !key.isBlank())
                .forEach(keys::add);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private String callAiEndpoint(String prompt, String systemInstruction) {
        List<String> openRouterKeys = getOpenRouterKeys();
        AiCallResult lastOpenRouterResult = null;
        for (int i = 0; i < openRouterKeys.size(); i++) {
            AiCallResult result = callChatCompletions(
                    "https://openrouter.ai/api/v1/chat/completions",
                    openrouterModel,
                    openRouterKeys.get(i),
                    prompt,
                    systemInstruction,
                    true
            );
            if (result.success()) {
                return result.content();
            }
            lastOpenRouterResult = result;
        }

        String openAiKey = getOpenAiKey();
        if (openAiKey != null) {
            AiCallResult result = callChatCompletions(
                    "https://api.openai.com/v1/chat/completions",
                    openaiModel,
                    openAiKey,
                    prompt,
                    systemInstruction,
                    false
            );
            if (result.success()) {
                return result.content();
            }
            return "Failed to query OpenAI model (Status: " + result.statusCode() + "). " +
                    "Fallback to local advisor: please align goals with your mentor.";
        }

        if (lastOpenRouterResult != null) {

            return """
OpenRouter Error

Status : %d

Response :

%s
""".formatted(
                    lastOpenRouterResult.statusCode(),
                    lastOpenRouterResult.content()
            );
        }

        return "No AI API key configured. Fallback to local advisor: please align goals with your mentor.";
    }

    private AiCallResult callChatCompletions(
            String url,
            String model,
            String apiKey,
            String prompt,
            String systemInstruction,
            boolean openRouter
    ) {
        try {
            Map<String, Object> messageSystem = Map.of("role", "system", "content", systemInstruction);
            Map<String, Object> messageUser = Map.of("role", "user", "content", prompt);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(messageSystem, messageUser)
            );

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)));

            if (openRouter) {
                builder.header("HTTP-Referer", "http://localhost:5173")
                        .header("X-Title", "Knowledge Nexus");
            }

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                return new AiCallResult(true, response.statusCode(),
                        jsonNode.path("choices").path(0).path("message").path("content").asText().trim());
            }

            return new AiCallResult(false, response.statusCode(), response.body());
        } catch (Exception e) {
            return new AiCallResult(false, 500, e.getMessage());
        }
    }

    private record AiCallResult(boolean success, int statusCode, String content) {
    }

    private String generateMockMatchReason(User mentor, User mentee, Set<String> overlapping) {
        String matchSkills = overlapping.isEmpty() ? "skills in engineering and design" : String.join(", ", overlapping);
        return String.format(
                "%s and %s are an excellent match. Both share deep focus on %s. " +
                "%s has over %d years of valuable experience as a %s at %s, " +
                "which perfectly positions them to help %s navigate their career path and master these technologies.",
                mentee.getFirstName(), mentor.getFirstName(), matchSkills,
                mentor.getFirstName(), 
                mentor.getExperienceYears() != null ? mentor.getExperienceYears() : 3,
                mentor.getCurrentRole() != null ? mentor.getCurrentRole() : "Professional Developer",
                mentor.getCompany() != null ? mentor.getCompany() : "Knowledge Nexus",
                mentee.getFirstName()
        );
    }

    private String generateMockAiResponse(String userPrompt, Conversation conversation, List<Message> history) {
        String mentorName = conversation.getMentor().getFirstName();
        String menteeName = conversation.getMentee().getFirstName();
        

        if (userPrompt.toLowerCase().contains("roadmap")) {
            return String.format(
                    "### 🗺️ Learning Roadmap Suggestion (AI Simulation)\n" +
                    "Here is a recommended 4-week roadmap based on your profile details:\n\n" +
                    "1. **Week 1: Foundations & Goal Setting**\n" +
                    "   - Discuss career aspirations and review core coding concepts.\n" +
                    "2. **Week 2: Advanced Design Patterns**\n" +
                    "   - Pair-program on an active framework feature (e.g. Spring Boot or React hooks).\n" +
                    "3. **Week 3: Deployment & Cloud Integration**\n" +
                    "   - Run local deployments, Docker containers, or configure server setups.\n" +
                    "4. **Week 4: Review & Showcase**\n" +
                    "   - Code reviews, resume polishing, and mockup interview prep.\n\n" +
                    "**Action Item**: Click **Schedule Session** to set up a Google Meet to discuss this roadmap with %s!",
                    mentorName
            );
        } else if (userPrompt.toLowerCase().contains("summarize")) {
            return String.format(
                    "### 📝 Chat Summary (AI Simulation)\n" +
                    "Here is a summary of the conversation history between **%s** (Mentee) and **%s** (Mentor):\n\n" +
                    "- Total messages: **%d**\n" +
                    "- **Key Topics**: Exchanged initial greetings and discussed core project collaboration goals.\n" +
                    "- **Current Status**: Mentorship request is active, messages are loaded in real-time.\n",
                    menteeName, mentorName, history.size()
            );
        } else {
            return String.format(
                    "### 💡 Mentorship Helper (AI Simulation)\n" +
                    "You asked: *\"%s\"*\n\n" +
                    "Here are some helpful tips to guide the discussion between %s and %s:\n" +
                    "- Define structured milestones.\n" +
                    "- Schedule a regular 30-minute touchpoint weekly.\n" +
                    "- Share github links and conduct review reviews.\n\n" +
                    "*Set your AI API Key in environment variables to unlock full LLM answers!*",
                    userPrompt, menteeName, mentorName
            );
        }
    }
}
