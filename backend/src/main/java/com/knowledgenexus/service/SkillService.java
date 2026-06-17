package com.knowledgenexus.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knowledgenexus.dto.CreateSkillRequest;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.stackexchange.com/2.3")
            .build();

    private final Map<String, CachedCatalog> catalogCache = new ConcurrentHashMap<>();

    public Skill create(CreateSkillRequest request) {

        String normalizedName = normalizeSkillName(request.getName());
        if (skillRepository.existsByNameIgnoreCase(normalizedName)) {
            return skillRepository.findByNameIgnoreCase(normalizedName)
                    .orElseThrow(() -> new RuntimeException("Skill already exists"));
        }

        Skill skill = Skill.builder()
                .name(normalizedName)
                .slug(slugify(normalizedName))
                .build();

        return skillRepository.save(skill);
    }

    public List<Skill> getCatalog(String query) {
        String key = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        CachedCatalog cached = catalogCache.get(key);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.skills();
        }

        List<String> externalNames = fetchStackOverflowTags(query);
        LinkedHashMap<String, Skill> catalog = new LinkedHashMap<>();

        if (!externalNames.isEmpty()) {
            for (String externalName : externalNames) {
                Skill skill = ensureSkill(externalName);
                catalog.putIfAbsent(skill.getSlug(), skill);
            }
        }

        if (catalog.isEmpty()) {
            List<Skill> fallback = (query == null || query.isBlank())
                    ? skillRepository.findTop20ByOrderByNameAsc()
                    : skillRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
            fallback.forEach(skill -> catalog.putIfAbsent(skill.getSlug(), skill));
        }

        List<Skill> results = new ArrayList<>(catalog.values());
        catalogCache.put(key, new CachedCatalog(results, Instant.now().plus(Duration.ofMinutes(10))));
        return results;
    }

    private Skill ensureSkill(String rawName) {
        String normalized = normalizeSkillName(rawName);
        return skillRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> skillRepository.save(
                        Skill.builder()
                                .name(normalized)
                                .slug(slugify(normalized))
                                .category("Imported")
                                .description("Imported from Stack Overflow tag catalog")
                                .build()
                ));
    }

    private List<String> fetchStackOverflowTags(String query) {
        try {
            StackExchangeTagResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder = uriBuilder.path("/tags")
                                .queryParam("site", "stackoverflow")
                                .queryParam("sort", "popular")
                                .queryParam("order", "desc")
                                .queryParam("pagesize", 20);
                        if (query != null && !query.isBlank()) {
                            uriBuilder = uriBuilder.queryParam("inname", query.trim());
                        }
                        return uriBuilder.build();
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(StackExchangeTagResponse.class);

            if (response == null || response.items() == null) {
                return List.of();
            }

            return response.items().stream()
                    .map(StackExchangeTagItem::name)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isBlank())
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String normalizeSkillName(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.isBlank()) {
            return "";
        }

        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "c#" -> "C#";
            case "c++" -> "C++";
            case "node.js" -> "Node.js";
            case "asp.net-core" -> "ASP.NET Core";
            case "asp.net" -> "ASP.NET";
            case "reactjs" -> "React";
            case "next.js" -> "Next.js";
            default -> {
                String[] parts = trimmed.split("[-_.\\s]+");
                StringBuilder builder = new StringBuilder();
                for (String part : parts) {
                    if (part.isBlank()) {
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append(' ');
                    }
                    builder.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        builder.append(part.substring(1).toLowerCase(Locale.ROOT));
                    }
                }
                yield builder.length() > 0 ? builder.toString() : trimmed;
            }
        };
    }

    private String slugify(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{Alnum}]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StackExchangeTagResponse(List<StackExchangeTagItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StackExchangeTagItem(String name, Integer count) {}

    record CachedCatalog(List<Skill> skills, Instant expiresAt) {}
}
