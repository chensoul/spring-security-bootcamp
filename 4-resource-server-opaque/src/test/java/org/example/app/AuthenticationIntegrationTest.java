package org.example.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthenticationIntegrationTest {

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
        this.mvc.perform(get("/actuator/env").with(opaqueToken().attributes(c -> c.put("name", "user")).authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))))
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @DisplayName("call protected actuator environment endpoint with invalid creds")
    @Test
    void requestActuatorEnvUnauthorized() throws Exception {
        this.mvc.perform(get("/actuator/env").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @DisplayName("call protected private message endpoint with valid creds")
    @Test
    void requestPrivateMessageSuccess() throws Exception {
        this.mvc.perform(get("/api/private/message").with(opaqueToken().attributes(c -> c.put("name", "user")).authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(String.format("Hello [%s], this is a private endpoint", "user")))
                .andReturn();
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
                .andExpect(status().is2xxSuccessful()).andExpect(content().string("Hello unknown user, this is a public endpoint")).andReturn();
    }

    @DisplayName("call protected admin endpoint with valid creds")
    @Test
    void requestAdminSuccess() throws Exception {
        this.mvc.perform(get("/api/admin").with(opaqueToken().attributes(c -> c.put("name", "admin")).authorities(AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN"))))
                .andExpect(status().is2xxSuccessful()).andExpect(content().string(String.format("Hello [%s], this is the administrative endpoint", "admin"))).andReturn();
    }

    @DisplayName("call protected admin endpoint with insufficient rights")
    @Test
    void requestAdminFailInsufficientRights() throws Exception {
        this.mvc.perform(get("/api/admin").with(opaqueToken().attributes(c -> c.put("name", "admin")).authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))))
                .andExpect(status().isForbidden()).andReturn();
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
}
