package com.knowledgenexus.controller;

import com.knowledgenexus.dto.DashboardResponse;
import com.knowledgenexus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse dashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return dashboardService.getDashboard(
                userDetails.getUsername()
        );
    }
}
