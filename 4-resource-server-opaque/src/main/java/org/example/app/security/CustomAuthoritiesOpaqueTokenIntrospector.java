package org.example.app.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomAuthoritiesOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;
    private final WebClient webClient;

    public CustomAuthoritiesOpaqueTokenIntrospector(String introspectionUrl, String userInfoUrl, String clientId, String clientSecret) {
        this.delegate =
                new NimbusOpaqueTokenIntrospector(introspectionUrl, clientId, clientSecret);
        this.webClient = WebClient.builder().baseUrl(userInfoUrl).build();
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal = this.delegate.introspect(token);
        UserInfoResponse userInfo = getUserInfo(token);
        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());
        attributes.put("name", userInfo.getName());
        attributes.put("given_name", userInfo.getGivenName());
        attributes.put("family_name", userInfo.getFamilyName());
        return new DefaultOAuth2AuthenticatedPrincipal(
                principal.getName(), attributes, extractAuthorities(userInfo.getRoles()));
    }

    private UserInfoResponse getUserInfo(String token) {
        return webClient.get()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(UserInfoResponse.class);
                    } else {
                        return response.createError();
                    }
                }).block();
    }

    private Collection<GrantedAuthority> extractAuthorities(List<String> roles) {
        return roles.stream()
                .map(s -> "ROLE_" + s)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
