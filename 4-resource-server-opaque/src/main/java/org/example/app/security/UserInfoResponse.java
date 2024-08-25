package org.example.app.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserInfoResponse {

    @JsonProperty("sub")
    private String subject;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String name;

    private List<String> roles;

    public UserInfoResponse(String subject, String givenName, String familyName, String name, List<String> roles) {
        this.subject = subject;
        this.givenName = givenName;
        this.familyName = familyName;
        this.name = name;
        this.roles = roles;
    }

    public UserInfoResponse() {
    }

    public String getSubject() {
        return subject;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }
}
