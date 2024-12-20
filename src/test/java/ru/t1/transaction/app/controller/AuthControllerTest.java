package ru.t1.transaction.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.t1.transaction.app.model.dto.LoginRequest;
import ru.t1.transaction.app.model.dto.SignupRequest;
import ru.t1.transaction.app.util.AbstractIntegrationTestInitializer;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthControllerTest extends AbstractIntegrationTestInitializer {

    @Autowired
    private AuthController authController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ExceptionController()).build();
    }

    @AfterEach
    public void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_roles");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "role");
    }

    @Test
    @Sql(scripts = "classpath:db/create-role.sql")
    void registerUser_whenValidSignupRequest_returnUserRegisteredMessage() throws Exception {

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");

        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("BradPitt");
        signUpRequest.setEmail("Pitt12@gmaile.com");
        signUpRequest.setPassword("12345678");
        signUpRequest.setRole(roles);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signUpRequest)))
                        .andExpect(status().isOk())
                        .andExpect(content()
                        .string("{\"message\":\"User registered successfully!\"}"));
    }

    @Test
    @Sql(scripts = "classpath:db/create-role.sql")
    void authenticateUser_whenValidLoginRequest_returnJwtResponse() throws Exception {

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");

        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("JohnDoe");
        signUpRequest.setEmail("John12@gmaile.com");
        signUpRequest.setPassword("12345Password");
        signUpRequest.setRole(roles);

        authController.registerUser(signUpRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("JohnDoe");
        loginRequest.setPassword("12345Password");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("JohnDoe"))
                .andExpect(jsonPath("$.email").value("John12@gmaile.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.accessToken").value(Matchers.matchesRegex("^[A-Za-z0-9.\\-_]+\\.[A-Za-z0-9.\\-_]+\\.[A-Za-z0-9.\\-_]+$")))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @Sql(scripts = "classpath:db/create-role.sql")
    void registerUser_whenUserExists_returnMessageError() throws Exception {

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("Brad");
        signupRequest.setEmail("Pitt12@gmaile.com");
        signupRequest.setPassword("12345678");
        signupRequest.setRole(roles);

        authController.registerUser(signupRequest);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("{\"message\":\"Error: Username is already taken!\"}"));
    }

    @Test
    @Sql(scripts = "classpath:db/create-role.sql")
    void registerUser_whenEmailExists_returnMessageError() throws Exception {

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("Brad");
        signupRequest.setEmail("Pitt12@gmaile.com");
        signupRequest.setPassword("12345678");
        signupRequest.setRole(roles);

        authController.registerUser(signupRequest);

        SignupRequest signupRequest2 = new SignupRequest();
        signupRequest2.setUsername("James");
        signupRequest2.setEmail("Pitt12@gmaile.com");
        signupRequest2.setPassword("12345678");
        signupRequest2.setRole(roles);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("{\"message\":\"Error: Email is already in use!\"}"));
    }

    @Test
    @Sql(scripts = "classpath:db/create-user-role.sql")
    void registerUser_whenSetRoleNull_returnUserRegisteredMessage() throws Exception {

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("Brad");
        signupRequest.setEmail("Pitt12@gmaile.com");
        signupRequest.setPassword("12345678");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("{\"message\":\"User registered successfully!\"}"));
    }

    @Test
    void registerUser_whenRoleNotExists_returnMessageError() throws Exception {

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("Brad");
        signupRequest.setEmail("Pitt12@gmaile.com");
        signupRequest.setPassword("12345678");
        signupRequest.setRole(roles);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                        .value("Error: Role is not found."));
    }
}
