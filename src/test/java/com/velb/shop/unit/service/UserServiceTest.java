package com.velb.shop.unit.service;

import com.velb.shop.exception.UserAlreadyExistsException;
import com.velb.shop.model.dto.UserDto;
import com.velb.shop.model.dto.UserRegistrationDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.model.mapper.UserDtoMapper;
import com.velb.shop.repository.UserRepository;
import com.velb.shop.service.UserService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDtoMapper userDtoMapper;
    @InjectMocks
    private UserService userService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createUser() {
        final User[] user = new User[1];
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .lastName("Иванов")
                .firstName("Иван")
                .middleName("Иванович")
                .email("ivanov@mail.ru")
                .rawPassword("testpass")
                .build();

        when(userDtoMapper.map(registrationDto))
                .thenAnswer(invocationOnMock -> {
                    UserRegistrationDto userRegistrationDto = invocationOnMock.getArgument(0);
                    return UserDto.builder()
                            .lastName(userRegistrationDto.getLastName())
                            .firstName(userRegistrationDto.getFirstName())
                            .middleName(userRegistrationDto.getMiddleName())
                            .email(userRegistrationDto.getEmail())
                            .encryptedPassword("encryptedPassword")
                            .build();
                });

        when(userRepository.findByEmail(registrationDto.getEmail()))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocationOnMock -> {
                    user[0] = invocationOnMock.getArgument(0, User.class);
                    user[0].setId(100L);
                    return user[0];
                });

        userService.createUser(registrationDto);

        assertEquals(user[0].getId(), 100L);
        assertEquals(user[0].getLastName(), registrationDto.getLastName());
        assertEquals(user[0].getFirstName(), registrationDto.getFirstName());
        assertEquals(user[0].getMiddleName(), registrationDto.getMiddleName());
        assertEquals(user[0].getEmail(), registrationDto.getEmail());
        assertEquals(user[0].getPassword(), "encryptedPassword");
        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void createUserThrowUserAlreadyExistEx() {
        User consumer = User.builder()
                .lastName("Петров")
                .firstName("Петр")
                .middleName("Петрович")
                .role(Role.CONSUMER)
                .email("test@mail.ru")
                .password("encryptedPassword")
                .build();
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .lastName("Иванов")
                .firstName("Иван")
                .middleName("Иванович")
                .email("test@mail.ru")
                .rawPassword("testpass")
                .build();

        when(userRepository.findByEmail(registrationDto.getEmail()))
                .thenReturn(Optional.of(consumer));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(registrationDto));

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void loadUserByUsername() {
        User consumer = User.builder()
                .lastName("Петров")
                .firstName("Петр")
                .middleName("Петрович")
                .role(Role.CONSUMER)
                .email("test@mail.ru")
                .password("encryptedPassword")
                .build();

        when(userRepository.findByEmail(consumer.getEmail()))
                .thenReturn(Optional.of(consumer));

        UserDetails userDetails = userService.loadUserByUsername(consumer.getEmail());

        assertEquals(consumer.getEmail(), userDetails.getUsername());
        assertEquals(consumer.getPassword(), userDetails.getPassword());
        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    public void loadUserByUsernameThrowUsernameNotFoundEx() {
        User consumer = User.builder()
                .lastName("Петров")
                .firstName("Петр")
                .middleName("Петрович")
                .role(Role.CONSUMER)
                .email("test@mail.ru")
                .password("encryptedPassword")
                .build();

        when(userRepository.findByEmail(consumer.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(consumer.getEmail()));

        verify(userRepository, times(1)).findByEmail(any());
    }
}