package com.velb.shop.model.entity.auxiliary;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN, CONSUMER;

    @Override
    public String getAuthority() {
        return name();
    }
}
