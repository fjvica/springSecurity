package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OidcLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {

        String baseUrl = "https://dev-q4h3r1oxikgie2h8.us.auth0.com/v2/logout";
        String returnTo = "http://localhost:8080/";
        String clientId = "wOu2Y5n6vMWBsM6Gw6SUTJlO0YwiHoBa";

        String logoutUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("client_id", clientId)
                .queryParam("returnTo", returnTo)
                .build()
                .toUriString();

        response.sendRedirect(logoutUrl);
    }
}