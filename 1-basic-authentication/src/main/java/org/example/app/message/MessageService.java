package org.example.app.message;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public String public_message() {
        return "Hello User, this is a public endpoint";
    }

    public String private_message(String user) {
        return String.format("Hello [%s], this is a private endpoint", user);
    }

    public String admin(String user) {
        return String.format("Hello [%s], this is the administrative endpoint", user);
    }
}
