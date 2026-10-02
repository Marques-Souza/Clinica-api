package com.marquesdev.clinica.jwt;

import com.marquesdev.clinica.enums.Perfil;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

public class JwtUserDetails extends User {

    com.marquesdev.clinica.entity.User user;

    public JwtUserDetails(com.marquesdev.clinica.entity.User user) {
        super(user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getPerfil().name()));
        this.user = user;
    }

    public UUID getId(){
        return this.user.getId();
    }

    public Perfil getPerfil(){
        return this.user.getPerfil();
    }
}
