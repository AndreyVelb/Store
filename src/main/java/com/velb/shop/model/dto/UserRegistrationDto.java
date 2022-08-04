package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationDto {
    @NotBlank(message = " Поле ФАМИЛИЯ не может быть пустым; ")
    @Size(min = 2, max = 30, message = " Поле ФАМИЛИЯ не может быть меньше 2 символов и более 30; ")
    private String lastName;

    @NotBlank(message = " Поле ИМЯ не может быть пустым; ")
    @Size(min = 2, max = 30, message = " Поле ИМЯ не может быть меньше 2 символов и более 30; ")
    private String firstName;

    @Size(max = 30, message = " Поле ОТЧЕСТВО не может быть более 30 символов; ")
    private String middleName;

    @Email(message = " Поле ЭЛЕКТРОННАЯ ПОЧТА должно быть заполнено в действительным адресом; ")
    @Size(max = 255, message = " Поле ЭЛЕКТРОННАЯ ПОЧТА не может быть более 255 символов; ")
    private String email;

    @NotBlank(message = " Поле ПАРОЛЬ не должно быть пустым; ")
    @Size(min = 6, max = 255, message = " Поле ПАРОЛЬ не может быть менее 6 символов и более 255")
    private String rawPassword;

}
