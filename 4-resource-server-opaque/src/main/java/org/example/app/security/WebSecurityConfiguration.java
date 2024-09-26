package org.example.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    public static final String USER_ROLE_NAME = "USER";
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    @Order(1)
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**", "/v3/api-docs", "/swagger-ui.html", "/swagger-ui/**")
                .authorizeHttpRequests(r -> {
                            r.requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll();
                            r.anyRequest().hasRole(ADMIN_ROLE_NAME);
                        }
                ).oauth2ResourceServer(r -> r.opaqueToken(withDefaults()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> {
                            authorize.requestMatchers("/api/public/message/**").permitAll();
                            authorize.requestMatchers("/api/private/message/**").hasAnyRole(USER_ROLE_NAME, ADMIN_ROLE_NAME);
                            authorize.requestMatchers("/api/admin/**").hasAnyRole(ADMIN_ROLE_NAME);
                            authorize.anyRequest().authenticated();
                        }
                ).oauth2ResourceServer(r -> r.opaqueToken(withDefaults()));
        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector introspector(
            OAuth2ResourceServerProperties properties,
            @Value("${spring.security.oauth2.resourceserver.opaquetoken.userinfo-uri}") String userinfoUrl
    ) {
        return new CustomAuthoritiesOpaqueTokenIntrospector(
                properties.getOpaquetoken().getIntrospectionUri(),
                userinfoUrl,
                properties.getOpaquetoken().getClientId(),
                properties.getOpaquetoken().getClientSecret());
    }
}
