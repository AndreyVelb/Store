package com.velb.shop.service;

import com.velb.shop.exception.UserAlreadyExistsException;
import com.velb.shop.model.dto.UserDto;
import com.velb.shop.model.dto.UserRegistrationDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.model.mapper.UserDtoMapper;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    public Long createUser(UserRegistrationDto userRegistrationDto) {
        UserDto userDto = userDtoMapper.map(userRegistrationDto);
        Optional<User> userWithSameEmail = userRepository.findByEmail(userRegistrationDto.getEmail());
        if (userWithSameEmail.isEmpty()) {
            User user = User.builder()
                    .lastName(userDto.getLastName())
                    .firstName(userDto.getFirstName())
                    .middleName(userDto.getMiddleName())
                    .role(Role.CONSUMER)
                    .email(userDto.getEmail())
                    .password(userDto.getEncryptedPassword())
                    .build();
            return userRepository.save(user).getId();
        } else throw new UserAlreadyExistsException("На этот адрес электронной почты уже зарегистрирован аккаунт; ");
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> new CustomUserDetails(
                        user.getEmail(),
                        user.getPassword(),
                        List.of(Role.values()),
                        user.getId()))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с почтовым адресом " +
                        username + " не найден"));
    }
}
