package com.farmasol.backend.controller;

import com.farmasol.backend.dto.LoginRequest;
import com.farmasol.backend.dto.LoginResponse;
import com.farmasol.backend.dto.RegisterRequest;
import com.farmasol.backend.model.Login;
import com.farmasol.backend.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class LoginController {

    @Autowired
    private LoginRepository loginRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Buscamos al usuario por su nombre de usuario (el rol ya no viene en el request)
        Optional<Login> user = loginRepository.findByUsuario(request.getUsuario());

        if (user.isPresent()) {
            Login login = user.get();

            // Comparamos la contraseña ingresada con la guardada en la BD
            if (login.getPasswordHash().equals(request.getPassword())) {
                // Devolvemos el rol que tiene el usuario en la BD (Admin o Usuario)
                return ResponseEntity.ok(new LoginResponse(true, "Login exitoso", login.getNombre(), login.getRol().name()));
            } else {
                return ResponseEntity.status(401).body(new LoginResponse(false, "Usuario o contraseña incorrectos", null, null));
            }
        } else {
            return ResponseEntity.status(401).body(new LoginResponse(false, "Usuario o contraseña incorrectos", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Verificar si el usuario ya existe
        if (loginRepository.findByUsuario(request.getUsuario()).isPresent()) {
            return ResponseEntity.status(409).body("El usuario ya existe");
        }

        // Crear el nuevo usuario
        Login nuevoUsuario = new Login();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setUsuario(request.getUsuario());
        nuevoUsuario.setCorreo(request.getCorreo());
        nuevoUsuario.setPasswordHash(request.getPassword()); // NOTA: En producción se usa BCrypt
        nuevoUsuario.setActivo(true);

        // ASIGNAR EL ROL SEGÚN LO QUE ENVIÓ EL FRONTEND
        if ("admin".equalsIgnoreCase(request.getRol())) {
            nuevoUsuario.setRol(Login.Rol.Admin);
        } else {
            nuevoUsuario.setRol(Login.Rol.Usuario); // Cliente por defecto
        }

        loginRepository.save(nuevoUsuario);

        return ResponseEntity.ok("Registro exitoso");
    }

    @PostMapping("/recuperar")
    public ResponseEntity<?> recuperarContrasena(@RequestBody java.util.Map<String, String> body) {
        String correo = body.get("correo");
        
        if (correo == null || correo.isEmpty()) {
            return ResponseEntity.status(400).body("El correo es obligatorio");
        }

        // Buscar por correo
        java.util.Optional<Login> usuario = loginRepository.findByCorreo(correo);

        if (usuario.isPresent()) {
            // Devolver la contraseña (NO HACER ESTO EN PRODUCCIÓN)
            return ResponseEntity.ok("Tu contraseña es: " + usuario.get().getPasswordHash());
        } else {
            return ResponseEntity.status(404).body("No se encontró ningún usuario con ese correo");
        }
    }

    @PostMapping("/verificar-correo")
    public ResponseEntity<?> verificarCorreo(@RequestBody java.util.Map<String, String> body) {
        String correo = body.get("correo");
        
        if (correo == null || correo.isEmpty()) {
            return ResponseEntity.badRequest().body("El correo es obligatorio");
        }

        // Verificar si ya existe en la base de datos
        if (loginRepository.findByCorreo(correo).isPresent()) {
            return ResponseEntity.status(409).body("Este correo ya está registrado");
        }

    return ResponseEntity.ok("Correo disponible para registro");
}
}