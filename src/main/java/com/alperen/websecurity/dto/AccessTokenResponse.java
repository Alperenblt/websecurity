package com.alperen.websecurity.dto;

public class AccessTokenResponse {
    private String accessToken;
    private String tokenType;

    public AccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
