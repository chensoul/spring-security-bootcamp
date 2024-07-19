package org.example.app.message;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageApi {

    private final MessageService messageService;

    public MessageApi(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/public/message")
    public String public_message() {
        return messageService.public_message();
    }

    @GetMapping("/private/message")
    public String private_message() {
        return messageService.private_message();
    }

    @GetMapping("/admin")
    public String admin() {
        return messageService.admin();
    }
}
