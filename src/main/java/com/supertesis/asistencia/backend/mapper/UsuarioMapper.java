package com.supertesis.asistencia.backend.mapper;

import com.supertesis.asistencia.backend.dto.AuthResponseDto;
import com.supertesis.asistencia.backend.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "username", source = "usuario.nombreUsuario")
    // Concatenamos nombre y apellido del Personal directamente usando una expresión de Java
    @Mapping(target = "nombre", expression = "java(usuario.getPersonal().getNombre() + \" \" + usuario.getPersonal().getApellido())")
    @Mapping(target = "rol", source = "usuario.rol.nombreRol")
    @Mapping(target = "mensaje", source = "mensajePersonalizado")
    AuthResponseDto toAuthResponseDto(Usuario usuario, String mensajePersonalizado);
}