package com.supertesis.asistencia.backend.mapper;

import com.supertesis.asistencia.backend.dto.eventoacceso.EventoAccesoResponseDto;
import com.supertesis.asistencia.backend.entity.EventoAcceso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventoAccesoMapper {

    @Mapping(target = "nombrePersonal", expression = "java(entity.getPersonal().getNombre() + \" \" + entity.getPersonal().getApellido())")
    @Mapping(source = "entity.personal.cedula", target = "cedula")
    @Mapping(target = "horaRegistro", expression = "java(entity.getTimestampEvento().toLocalTime())")
    @Mapping(source = "mensaje", target = "mensaje") // Toma el String pasado por parámetro
    EventoAccesoResponseDto toResponseDto(EventoAcceso entity, String mensaje);

}