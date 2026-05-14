package com.supertesis.asistencia.backend.mapper;

import com.supertesis.asistencia.backend.dto.cargo.CargoRequestDto;
import com.supertesis.asistencia.backend.dto.cargo.CargoResponseDto;
import com.supertesis.asistencia.backend.entity.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CargoMapper {

    // Mapeamos el nombre del departamento al record anidado del Response
    @Mapping(source = "departamento.id", target = "departamento.id")
    @Mapping(source = "departamento.nombre", target = "departamento.nombre")
    CargoResponseDto toResponseDto(Cargo entity);

    // Ignoramos el ID y la entidad completa del departamento al crear
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    Cargo toEntity(CargoRequestDto dto);
}