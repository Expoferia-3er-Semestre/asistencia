package com.supertesis.asistencia.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class RolPermisoDirectoId implements Serializable {

    private Integer rol;
    private Integer permiso;

    public RolPermisoDirectoId() {}

    public RolPermisoDirectoId(Integer rol, Integer permiso) {
        this.rol = rol;
        this.permiso = permiso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolPermisoDirectoId that = (RolPermisoDirectoId) o;
        return Objects.equals(rol, that.rol) && Objects.equals(permiso, that.permiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rol, permiso);
    }
}