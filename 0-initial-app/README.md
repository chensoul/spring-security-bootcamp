# Initial Application

This the initial sample java spring boot application which is completely unsecured by now.

## Provided API endpoints

* http://localhost:8080/api/public/message
* http://localhost:8080/api/private/message
* http://localhost:8080/api/admin

### Actuator and Doc endpoints

* http://localhost:8080/actuator/health
* http://localhost:8080/actuator/info
* http://localhost:8080/actuator/env
* http://localhost:8080/actuator/metrics/http.server.requests
* http://localhost:8080/actuator/sbom/application
* http://localhost:8080/v3/api-docs
* http://localhost:8080/swagger-ui.html

## Test the API endpoints

To make it easy for IntelliJ users you find IntelliJ http request clients in the `rest-client` folder.
If you use `Postman` or `Bruno` you may import the requests by specifying the open-api document via http://localhost:8080/v3/api-docs.

## Lab 1: Add basic authentication

In this lab we will add basic authentication mechanisms like `basic authentication` and `form based login`.
Your learnings here are:

* Get to know the concept of security filter chains to configure web security layer
* How to configure users for authentication
* Make sure passwords are stored securely
* Implement automated tests to verify the correctness of authentication

So let's start.

### 1.Configure additional dependencies

To add spring security for production code and add corresponding test support you need to add these dependencies
to your maven `pom.xml`:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```

Do not forget to reload maven dependencies to avoid unexpected errors!!
If you (re)start the application you will notice a generated password on the console. You may login with the username `user` 
and the generated password when you navigate the web browser to http://localhost:8080.

### 2.Configure our own users

This is usually not what you want to have for productive applications.
So let's create some users. To do this create a new java class `WebSecurityConfiguration` 
in a new package `org.example.app.security`:

```java
package org.example.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class WebSecurityConfiguration {

    public static final String USER_ROLE_NAME = "USER";
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    @Bean
    public UserDetailsService userDetailsService() {
        User.UserBuilder users = User.builder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username("user").password(passwordEncoder().encode("secret")).roles(USER_ROLE_NAME).build());
        manager.createUser(users.username("admin").password(passwordEncoder().encode("admin")).roles(USER_ROLE_NAME,ADMIN_ROLE_NAME).build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

We created two users, one administrative user with the `ADMIN` role and one standard user with the `USER` role.
Please notice the `PasswordEncoder` bean. This makes sure that the passwords are hashed securely (by default using the `bcrypt' hashing algorithm).

If you restart the application, the generated password will not appear any more. Now let's try to login with one of the two users.
To force a re-login you can navigate to http:://localhost:8080/logout.

### 3. Fine-tune the authentication

We want to have different security configurations between our custom API endpoints and the general actuator and docs endpoints.
To achieve this we have to replace the spring boot autoconfiguration with our own one. This done by defining one or more beans 
creating a `SecurityFilterChain`. Be warned that this is often a source of misconfiguration.

Let's extend the class `WebSecurityConfiguration`:

```java
package org.example.app.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    public static final String USER_ROLE_NAME = "USER";
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    @Order(1)
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**", "/v3/api-docs", "/swagger-ui.html", "/swagger-ui/**")
                .authorizeHttpRequests(r -> {
                            r.requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll();
                            r.anyRequest().authenticated();
                        }
                ).httpBasic(withDefaults()).formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> {
                            authorize.requestMatchers("/api/public/message/**").permitAll();
                            authorize.anyRequest().authenticated();
                        }
                ).httpBasic(withDefaults()).formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        User.UserBuilder users = User.builder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username("user").password(passwordEncoder().encode("secret")).roles(USER_ROLE_NAME).build());
        manager.createUser(users.username("admin").password(passwordEncoder().encode("admin")).roles(USER_ROLE_NAME,ADMIN_ROLE_NAME).build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

Please do not forget to add the additional annotation `@EnableWebSecurity` above the class definition. 
This annotation enables custom security configuration and switches off spring boot autoconfiguration.

You will see exactly 2(!!) bean definitions of `SecurityFilterChain`. The first one has an additional `@Order` annotation. 
This important as you need to tell spring security in which order the configuration should be applied.
The basic rule is always define the most specific configuration first using an order starting with _1_ and having an additional `securityMatcher()` call.
You don't need to define the order for the last bean configuration as the default order will always be the one with the least priority.
The `securityMatcher()` call is also not required here as this just handles all remaining requests.

We also allow to access the `health` endpoint and the `/api/public/message` endpoint without any authentication.
Finally, we configure `basic authentication` and `form based login` as authentication mechanisms.

### 4.Implement automated tests to verify authentication

To make sure that the authentication works as expected for the endpoints we need to write an automated test.
Luckily there is already a test implemented in class `AuthenticationIntegrationTest`. Try to run this test and 
you will recognize failing tests as these tests do not specify and authentication parameters.

Let's change this as you can see in the following adapted test class:

```java
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
                .andExpect(status().is2xxSuccessful()).andExpect(content().string("Hello unknown user, this is a public endpoint")).andReturn();
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
```

Please notice the security testing support that is activated by adding this line to the init method annotated with `@BeforeEach`:

```java
//...
    .apply(springSecurity())
//...
```

To add authentication to the tests you need to add a snippet like this:

```java
    with(httpBasic("user", "secret"))
```

That's it for the first lab. Now we continue with adding authorization.