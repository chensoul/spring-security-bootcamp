package com.example.authorizationserver;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class AuthorizationServerUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserDetails> usernameToUsers = new HashMap<>();

    public AuthorizationServerUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        usernameToUsers.put(
                "user",
                new AuthorizationServerUser(
                        "user",
                        passwordEncoder.encode("secret"),
                        "Ursula",
                        "User",
                        "usrula.user@example.com",
                        singletonList("USER")));
        usernameToUsers.put(
                "admin",
                new AuthorizationServerUser(
                        "admin",
                        passwordEncoder.encode("secret"),
                        "Andreas",
                        "Administrator",
                        "andreas.administrator@example.com",
                        List.of("USER", "ADMIN")));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (usernameToUsers.containsKey(username)) {
            return usernameToUsers.get(username);
        } else throw new UsernameNotFoundException(username);
    }
}
