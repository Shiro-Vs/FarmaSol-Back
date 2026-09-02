package com.farmasol.backend.dto.pedido;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotNull(message = "Debe elegir una dirección de envío")
    private Long idDireccion;

    @Size(max = 255)
    private String notas;
}
