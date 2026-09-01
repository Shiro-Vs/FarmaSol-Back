package com.farmasol.backend.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String nombre;
    private String rol;

    public LoginResponse(boolean success, String message, String nombre, String rol) {
        this.success = success;
        this.message = message;
        this.nombre = nombre;
        this.rol = rol;
    }

    // Getters y Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}