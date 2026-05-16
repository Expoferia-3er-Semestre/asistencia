package com.supertesis.asistencia.backend.mapper;

import com.supertesis.asistencia.backend.dto.personal.PersonalCreateRequestDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalResponseDto;
import com.supertesis.asistencia.backend.dto.personal.PersonalUpdateRequestDto;
import com.supertesis.asistencia.backend.entity.Personal;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonalMapper {
    
    // De Entidad a Respuesta (Para mostrar al usuario)
    @Mapping(source = "cargo.id", target = "cargo.id")
    @Mapping(source = "cargo.nombre", target = "cargo.nombre")
    @Mapping(source = "departamento.id", target = "departamento.id")
    @Mapping(source = "departamento.nombre", target = "departamento.nombre")
    PersonalResponseDto toResponseDto(Personal entity);

    // De Petición a Entidad (Para guardar/crear)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cargo", ignore = true)        // Lo manejará el Service
    @Mapping(target = "departamento", ignore = true) // Lo manejará el Service
    @Mapping(target = "fechaEgreso", ignore = true) // No se establece al crear
    @Mapping(target = "activo", ignore = true)     // Se establece automáticamente al crear
    Personal toEntityCreate(PersonalCreateRequestDto dto);

    // De Petición a Entidad (Para actualizar)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cargo", ignore = true)        // Lo manejará el Service
    @Mapping(target = "departamento", ignore = true) // Lo manejará el Service
    void updateEntityFromDto(PersonalUpdateRequestDto dto, @MappingTarget Personal entity);


}
