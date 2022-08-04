package com.velb.shop.integration.service;

import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.UserRegistrationDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.repository.UserRepository;
import com.velb.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
class UserServiceIT extends IntegrationTestBase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Test
    void createUser() {
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .lastName("Андреев")
                .firstName("Андрей")
                .middleName("Андреевич")
                .email("andreev@mail.ru")
                .rawPassword("andreev")
                .build();

        assertTrue((userRepository.findByEmail("andreev@mail.ru").isEmpty()));

        userService.createUser(registrationDto);

        Optional<User> savedUser = userRepository.findByEmail("andreev@mail.ru");

        assertTrue(savedUser.isPresent());
        assertEquals(savedUser.get().getLastName(), registrationDto.getLastName());
        assertEquals(savedUser.get().getFirstName(), registrationDto.getFirstName());
        assertEquals(savedUser.get().getMiddleName(), registrationDto.getMiddleName());
    }

    @Test
    void loadUserByUsername() {
        String userLogin = "petrov@gmail.com";

        CustomUserDetails customUserDetails = (CustomUserDetails) userService.loadUserByUsername(userLogin);

        assertEquals(customUserDetails.getId(), 2L);
        assertEquals(customUserDetails.getUsername(), "petrov@gmail.com");
        assertTrue(passwordEncoder.matches("petrov", customUserDetails.getPassword()));
    }
}