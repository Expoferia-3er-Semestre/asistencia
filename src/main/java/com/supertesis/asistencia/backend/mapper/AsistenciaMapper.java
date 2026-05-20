package com.supertesis.asistencia.backend.mapper;

import com.supertesis.asistencia.backend.dto.asistencia.AsistenciaResponseDto;
import com.supertesis.asistencia.backend.entity.Asistencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AsistenciaMapper {

    @Mapping(source = "personal.id", target = "personalId")
    @Mapping(target = "nombrePersonal", expression = "java(entity.getPersonal().getNombre() + \" \" + entity.getPersonal().getApellido())")
    AsistenciaResponseDto toResponseDto(Asistencia entity);
}