package com.marquesdev.clinica.security;

import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.jwt.JwtUserDetails;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAuthorization")
@RequiredArgsConstructor
public class UserAuthorization {

    private final UserRepository userRepository;

    public boolean canUpdatePassword(UUID id, Authentication authentication){
        JwtUserDetails principal = (JwtUserDetails) authentication.getPrincipal();

        UUID authenticatedUserId = principal.getId();
        Perfil authenticatedPerfil = principal.getPerfil();

        if (authenticatedPerfil == Perfil.USER){
            return authenticatedUserId.equals(id);
        }

        if (authenticatedPerfil == Perfil.DOCTOR){
            return false;
        }

        if (authenticatedPerfil == Perfil.ADMIN){

            if (authenticatedUserId.equals(id)){
                return true;
            }

            return userRepository.findById(id)
                    .map(user -> user.getPerfil() == Perfil.DOCTOR)
                    .orElse(false);
        }
        return  false;
    }
}
