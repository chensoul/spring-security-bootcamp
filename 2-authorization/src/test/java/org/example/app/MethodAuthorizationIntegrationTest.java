package org.example.app;

import org.example.app.message.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class MethodAuthorizationIntegrationTest {

    private final MessageService messageService;

    public MethodAuthorizationIntegrationTest(@Autowired MessageService messageService) {
        this.messageService = messageService;
    }

    @DisplayName("call public message service without creds")
    @Test
    void requestPublicMessageSuccess() {
        String message = messageService.publicMessage();
        assertThat(message).isNotNull().isEqualTo("Hello unknown user, this is a public endpoint");
    }

    @DisplayName("call private message service with insufficient rights")
    @WithMockUser(roles = {"INVALID"})
    @Test
    void requestPrivateMessageAccessDeniedForWrongRole() {
        assertThatThrownBy(() -> {
            messageService.privateMessage("invalid");
        }, "Insufficient role").isInstanceOf(AccessDeniedException.class).hasMessage("Access Denied");
    }

    @DisplayName("call private message service with sufficient rights")
    @WithMockUser
    @Test
    void requestPrivateMessageAccessGrantedForUserRole() {
        String message = messageService.privateMessage("user");
        assertThat(message).isNotNull().isEqualTo("Hello [user], this is a private endpoint");
    }

    @DisplayName("call private message service with multiple roles")
    @WithMockUser(roles = {"USER", "ADMIN"})
    @Test
    void requestPrivateMessageAccessGrantedForAdminRole() {
        String message = messageService.privateMessage("admin");
        assertThat(message).isNotNull().isEqualTo("Hello [admin], this is a private endpoint");
    }

    @DisplayName("call admin message service without administrative rights")
    @WithMockUser(roles = "USER")
    @Test
    void requestAdminMessageAccessDeniedForUserRole() {
        assertThatThrownBy(() -> {
            messageService.admin("user");
        }, "Insufficient role").isInstanceOf(AccessDeniedException.class).hasMessage("Access Denied");
    }

    @DisplayName("call admin message service with administrative rights")
    @WithMockUser(roles = "ADMIN")
    @Test
    void requestAdminMessageAccessGrantedForAdminRole() {
        String message = messageService.privateMessage("admin");
        assertThat(message).isNotNull().isEqualTo("Hello [admin], this is a private endpoint");
    }
}
