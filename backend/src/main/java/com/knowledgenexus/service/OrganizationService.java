package com.knowledgenexus.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class OrganizationService {

    private static final List<String> ORGANIZATIONS = List.of(
            "Google",
            "Apple",
            "Microsoft",
            "Amazon",
            "Meta",
            "Netflix",
            "NVIDIA",
            "Tesla",
            "Adobe",
            "Salesforce",
            "Oracle",
            "IBM",
            "Intel",
            "SteepGraph",
            "Tata Consultancy Services",
            "Infosys",
            "Wipro",
            "HCLTech",
            "Accenture",
            "Capgemini"
    );

    public List<String> search(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();

        return ORGANIZATIONS.stream()
                .filter(name -> normalized.isBlank() || name.toLowerCase().contains(normalized))
                .sorted(Comparator.naturalOrder())
                .limit(10)
                .toList();
    }
}
