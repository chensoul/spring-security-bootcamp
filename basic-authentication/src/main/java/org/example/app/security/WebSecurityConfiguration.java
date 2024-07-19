package org.example.app.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfiguration {

    @Order(1)
    @Bean
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests( r -> {
                            r.requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll();
                            r.requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated();
                        }
                ).httpBasic(Customizer.withDefaults()).formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests( r -> {
                            r.requestMatchers("/public/message").permitAll();
                            r.requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated();
                        }
                ).httpBasic(Customizer.withDefaults()).formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.builder().username("user").password(passwordEncoder().encode("secret")).roles("USER").build(),
                User.builder().username("admin").password(passwordEncoder().encode("admin")).roles("USER", "ADMIN").build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
