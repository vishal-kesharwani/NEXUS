package com.knowledgenexus.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String headline;
    private String bio;
    private Integer experience;
    private String role;
    private String company;
    private String location;
    private String linkedIn;
    private String github;
}
