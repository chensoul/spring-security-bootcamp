package org.example.app.message;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public String public_message() {
        return "Hello User, this is a public endpoint";
    }

    public String private_message() {
        return "Hello User, this is a private endpoint";
    }

    public String admin() {
        return "Hello Admin, this is a administrative endpoint";
    }
}
