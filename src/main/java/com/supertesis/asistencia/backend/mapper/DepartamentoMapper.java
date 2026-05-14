package com.supertesis.asistencia.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.supertesis.asistencia.backend.dto.departamento.DepartamentoRequestDto;
import com.supertesis.asistencia.backend.dto.departamento.DepartamentoResponseDto;
import com.supertesis.asistencia.backend.entity.Departamento;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartamentoMapper {
    
    DepartamentoResponseDto toResponseDto(Departamento entity);

    @Mapping(target = "id", ignore = true)
    Departamento toEntity(DepartamentoRequestDto dto);

}
