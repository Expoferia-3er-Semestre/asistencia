package com.supertesis.asistencia.backend.service;

import com.supertesis.asistencia.backend.entity.Usuario;
import com.supertesis.asistencia.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    public Usuario findByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con nombre " + nombreUsuario));
    }

    public List<Usuario> findByRolId(Integer rolId) {
        return usuarioRepository.findByRolId(rolId);
    }

    public List<Usuario> findActiveUsers() {
        return usuarioRepository.findByActivoTrue();
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null && usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con nombre de usuario " + usuario.getNombreUsuario());
        }
        if (usuario.getId() == null && usuario.getPersonal() != null && usuario.getPersonal().getId() != null && usuarioRepository.existsByPersonalId(usuario.getPersonal().getId())) {
            throw new IllegalArgumentException("Ya existe un usuario asociado al personal id " + usuario.getPersonal().getId());
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Integer id, Usuario updatedUsuario) {
        Usuario existing = findById(id);
        existing.setNombreUsuario(updatedUsuario.getNombreUsuario());
        existing.setPasswordHash(updatedUsuario.getPasswordHash());
        existing.setRol(updatedUsuario.getRol());
        existing.setActivo(updatedUsuario.getActivo());
        existing.setUltimoAcceso(updatedUsuario.getUltimoAcceso());
        return usuarioRepository.save(existing);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
