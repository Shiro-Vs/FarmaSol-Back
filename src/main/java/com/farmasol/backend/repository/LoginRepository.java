package com.farmasol.backend.repository;

import com.farmasol.backend.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Integer> {
    // Buscamos solo por el nombre de usuario
    Optional<Login> findByUsuario(String usuario);
    Optional<Login> findByCorreo(String correo);
}
