package com.knowledgenexus.service;

import com.knowledgenexus.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a real Google Calendar event (with Google Meet conference data) on the
 * organizer's primary calendar and invites the other participant as an attendee.
 */
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final String EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1&sendUpdates=all";

    private static final int DEFAULT_DURATION_MINUTES = 30;

    private final GoogleTokenService googleTokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    public CreatedEvent createMeetEvent(User organizer, String attendeeEmail, LocalDateTime scheduledAt, String summary) {
        String accessToken = googleTokenService.getValidAccessToken(organizer);
        String zone = ZoneId.systemDefault().getId();
        LocalDateTime end = scheduledAt.plusMinutes(DEFAULT_DURATION_MINUTES);

        Map<String, Object> body = new HashMap<>();
        body.put("summary", summary);
        body.put("start", Map.of("dateTime", scheduledAt.toString(), "timeZone", zone));
        body.put("end", Map.of("dateTime", end.toString(), "timeZone", zone));
        body.put("attendees", List.of(
                Map.of("email", organizer.getEmail()),
                Map.of("email", attendeeEmail)
        ));
        body.put("conferenceData", Map.of(
                "createRequest", Map.of(
                        "requestId", UUID.randomUUID().toString(),
                        "conferenceSolutionKey", Map.of("type", "hangoutsMeet")
                )
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(EVENTS_URL, new HttpEntity<>(body, headers), Map.class);
        return extractEvent(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private CreatedEvent extractEvent(Map responseBody) {
        if (responseBody == null) {
            throw new IllegalStateException("Empty response from Google Calendar API");
        }

        String eventId = (String) responseBody.get("id");
        Map<String, Object> conferenceData = (Map<String, Object>) responseBody.get("conferenceData");
        if (conferenceData == null) {
            throw new IllegalStateException("Google did not return conference data for this event");
        }

        List<Map<String, Object>> entryPoints = (List<Map<String, Object>>) conferenceData.get("entryPoints");
        String meetLink = entryPoints.stream()
                .filter(ep -> "video".equals(ep.get("entryPointType")))
                .map(ep -> (String) ep.get("uri"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Google did not return a Meet video link"));

        return new CreatedEvent(eventId, meetLink);
    }

    public record CreatedEvent(String eventId, String meetLink) {
    }
}