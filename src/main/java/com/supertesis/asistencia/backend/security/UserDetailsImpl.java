package com.supertesis.asistencia.backend.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.supertesis.asistencia.backend.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean activo;

    public static UserDetailsImpl build(Usuario usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "USER";
        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(rol)),
                usuario.getActivo() != null && usuario.getActivo()
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
