package com.supertesis.asistencia.backend.repository;

import com.supertesis.asistencia.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Búsqueda por nombre de usuario (único)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Búsqueda por personal (único)
    Optional<Usuario> findByPersonalId(Integer personalId);

    // Búsqueda por rol
    List<Usuario> findByRolId(Integer rolId);

    // Usuarios activos
    List<Usuario> findByActivoTrue();

    // Usuarios inactivos
    List<Usuario> findByActivoFalse();

    // Consulta con JOIN para obtener usuario con información del personal
    @Query("SELECT u FROM Usuario u JOIN FETCH u.personal p WHERE u.nombreUsuario = :nombreUsuario")
    Optional<Usuario> findByNombreUsuarioWithPersonal(@Param("nombreUsuario") String nombreUsuario);

    // Consulta con JOIN para obtener usuarios con información del rol
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol r WHERE u.activo = true")
    List<Usuario> findActiveUsersWithRoles();

    // Verificar si existe nombre de usuario
    boolean existsByNombreUsuario(String nombreUsuario);

    // Verificar si existe personal
    boolean existsByPersonalId(Integer personalId);

    // Búsqueda por nombre del personal (usando JOIN)
    @Query("SELECT u FROM Usuario u JOIN u.personal p WHERE p.nombre LIKE %:nombre% OR p.apellido LIKE %:nombre%")
    List<Usuario> findByPersonalNombreContaining(@Param("nombre") String nombre);
}