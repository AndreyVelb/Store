package com.velb.shop.model.security;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Long id;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
        super(username, password, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
