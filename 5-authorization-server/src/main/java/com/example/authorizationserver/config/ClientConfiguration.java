package com.example.authorizationserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.List;

//@Configuration
public class ClientConfiguration {

    private static final List<String> REDIRECT_URIS = List.of(
            "http://localhost:8080/callback",
            "http://127.0.0.1:8080/callback",
            "https://oauth.pstmn.io/v1/callback"
    );

    //@Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient jwtClient = RegisteredClient
                .withId("jwt")
                .clientId("jwt")
                .clientSecret("{noop}secret")
                .authorizationGrantTypes(gt -> gt.addAll(List.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.CLIENT_CREDENTIALS)))
                .clientAuthenticationMethods(am -> am.addAll(List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, ClientAuthenticationMethod.CLIENT_SECRET_POST, ClientAuthenticationMethod.NONE)))
                .redirectUris(ru -> ru.addAll(REDIRECT_URIS))
                .build();

        return new InMemoryRegisteredClientRepository(jwtClient);
    }

}
