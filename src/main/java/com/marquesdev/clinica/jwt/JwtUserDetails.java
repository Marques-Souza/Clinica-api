package com.marquesdev.clinica.jwt;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

public class JwtUserDetails extends User {

    com.marquesdev.clinica.entity.User user;

    public JwtUserDetails(com.marquesdev.clinica.entity.User user) {
        super(user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getPerfil().name()));
    }

    public UUID getId(){
        return this.user.getId();
    }

    public String getPerfil(){
        return this.user.getPerfil().name();
    }
}
