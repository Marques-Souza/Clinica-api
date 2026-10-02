package com.marquesdev.clinica.jwt;

import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email);
        return new JwtUserDetails(user);
    }

    public JwtToken getTokenAuthenticated(String email) {
        Perfil perfil = userService.findPerfilByEmail(email);
        return JwtUtils.createToken(email, perfil.name());
    }
}
