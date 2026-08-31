package com.farmasol.backend.dto;

public class LoginRequest {
    private String usuario;
    private String password;
    private String rol; // "cliente" o "admin"

    // Getters y Setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}