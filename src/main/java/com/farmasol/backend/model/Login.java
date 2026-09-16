package com.farmasol.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    private String nombre;
    private String apellido;
    private String usuario;
    
    @Column(name = "correo")
    private String correo;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento")
    private java.time.LocalDate fechaNacimiento;

    private String telefono;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private Boolean activo;
    private LocalDateTime fecha_creacion;

    public enum Rol {
        Admin,
        Usuario,
        Trabajador        
    }

    // Getters y Setters (actualizados para el nuevo nombre)
    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) { this.fecha_creacion = fecha_creacion; }
}