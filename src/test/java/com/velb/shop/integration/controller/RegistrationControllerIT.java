package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.UserRegistrationDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class RegistrationControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Test
    void createNewUser() throws Exception {
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .lastName("ТЕСТ")
                .firstName("Тест")
                .middleName("")
                .email("test@mail.ru")
                .rawPassword("testPassword")
                .build();
        Optional<User> userWithTestEmail = userRepository.findByEmail(registrationDto.getEmail());
        assertTrue(userWithTestEmail.isEmpty());

        mockMvc.perform(post("/api/v1/registration")
                        .content(objectMapper.writeValueAsString(registrationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isCreated());

        Optional<User> userWithTestEmailAfterRegistration = userRepository.findByEmail(registrationDto.getEmail());
        assertTrue(userWithTestEmailAfterRegistration.isPresent());
    }

}
