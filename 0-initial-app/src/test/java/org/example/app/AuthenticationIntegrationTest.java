package org.example.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @DisplayName("call unprotected actuator health endpoint")
    @Test
    void requestActuatorHealthSuccess() throws Exception {
        this.mvc.perform(get("/actuator/health"))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @DisplayName("call unprotected actuator environment endpoint")
    @Test
    void requestActuatorEnvSuccess() throws Exception {
        this.mvc.perform(get("/actuator/env"))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @DisplayName("call unprotected private message endpoint")
    @Test
    void requestPrivateMessageSuccess() throws Exception {
        this.mvc.perform(get("/api/private/message"))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string(String.format("Hello [%s], this is a private endpoint", "user"))).andReturn();
    }

    @DisplayName("call unprotected public message endpoint")
    @Test
    void requestPublicMessageNoAuthentication() throws Exception {
        this.mvc.perform(get("/api/public/message"))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string("Hello unknown user, this is a public endpoint")).andReturn();
    }

    @DisplayName("call unprotected admin endpoint")
    @Test
    void requestAdminSuccess() throws Exception {
        this.mvc.perform(get("/api/admin"))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string(String.format("Hello [%s], this is the administrative endpoint", "user"))).andReturn();
    }
}
