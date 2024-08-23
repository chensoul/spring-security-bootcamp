package org.example.app.message;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public String publicMessage() {
        return "Hello unknown user, this is a public endpoint";
    }

    public String privateMessage(String user) {
        return String.format("Hello [%s], this is a private endpoint", user);
    }

    public String admin(String user) {
        return String.format("Hello [%s], this is the administrative endpoint", user);
    }
}
