package com.sica.usuario.domain;

/**
 * Representa un usuario del sistema SICA.
 * Un usuario siempre debe estar asociado a un rol (rolId).
 */
public class Usuario {

    private Long id;
    private String nombre;
    private String documento;
    private String username;
    private String password;
    private Long rolId;
    private boolean activo;

    public Usuario(String nombre, String documento, String username, String password, Long rolId) {
        this.nombre = nombre;
        this.documento = documento;
        this.username = username;
        this.password = password;
        this.rolId = rolId;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Long getRolId() {
        return rolId;
    }

    public boolean isActivo() {
        return activo;
    }
}