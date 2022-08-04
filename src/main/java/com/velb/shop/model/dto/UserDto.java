package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String lastName;

    private String firstName;

    private String middleName;

    private String email;

    private String encryptedPassword;

}
