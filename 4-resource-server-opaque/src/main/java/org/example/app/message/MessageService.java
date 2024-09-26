package org.example.app.message;

import org.example.app.security.IsUserOrAdmin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public String publicMessage() {
        return "Hello unknown user, this is a public endpoint";
    }

    @IsUserOrAdmin
    public String privateMessage(String user) {
        return String.format("Hello [%s], this is a private endpoint", user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String admin(String user) {
        return String.format("Hello [%s], this is the administrative endpoint", user);
    }
}
