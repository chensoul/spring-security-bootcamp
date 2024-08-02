package org.example.app;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
                .apply(springSecurity())
                .build();
    }

    @DisplayName("call unprotected actuator health endpoint without creds")
    @Test
    void requestActuatorHealthSuccess() throws Exception {
        this.mvc.perform(get("/actuator/health"))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @DisplayName("call protected actuator environment endpoint with valid creds")
    @Test
    void requestActuatorEnvSuccess() throws Exception {
        this.mvc.perform(get("/actuator/env").with(httpBasic("user", "secret")))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @DisplayName("call protected actuator environment endpoint with invalid creds")
    @Test
    void requestActuatorEnvUnauthorized() throws Exception {
        this.mvc.perform(get("/actuator/env").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @DisplayName("call protected private message endpoint with valid creds")
    @ParameterizedTest
    @MethodSource("users")
    void requestPrivateMessageSuccess(Pair<String,String> user) throws Exception {
        this.mvc.perform(get("/api/private/message").with(httpBasic(user.getLeft(), user.getRight())))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string(String.format("Hello [%s], this is a private endpoint", user.getLeft()))).andReturn();
    }

    @DisplayName("call protected private message endpoint with invalid creds")
    @Test
    void requestPrivateMessageUnauthorized() throws Exception {
        this.mvc.perform(get("/api/private/message").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @DisplayName("call protected private message endpoint without creds")
    @Test
    void requestPrivateMessageNoAuthentication() throws Exception {
        this.mvc.perform(get("/api/private/message"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @DisplayName("call unprotected public message endpoint without creds")
    @Test
    void requestPublicMessageNoAuthentication() throws Exception {
        this.mvc.perform(get("/api/public/message"))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string("Hello User, this is a public endpoint")).andReturn();
    }

    @DisplayName("call protected admin endpoint with valid creds")
    @ParameterizedTest
    @MethodSource("users")
    void requestAdminSuccess(Pair<String,String> user) throws Exception {
        this.mvc.perform(get("/api/admin").with(httpBasic(user.getLeft(), user.getRight())))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string(String.format("Hello [%s], this is the administrative endpoint", user.getLeft()))).andReturn();
    }

    @DisplayName("call protected admin endpoint with invalid creds")
    @Test
    void requestAdminUnauthorized() throws Exception {
        this.mvc.perform(get("/api/admin").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @DisplayName("call protected admin endpoint without creds")
    @Test
    void requestAdminNoAuthentication() throws Exception {
        this.mvc.perform(get("/api/admin"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    static List<Pair<String,String>> users() {
        List<Pair<String, String>> users = new ArrayList<>();
        users.add(Pair.of("user", "secret"));
        users.add(Pair.of("admin", "admin"));
        return users;
    }
}
