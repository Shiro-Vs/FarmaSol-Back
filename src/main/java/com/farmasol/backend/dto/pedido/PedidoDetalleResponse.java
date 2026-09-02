package com.farmasol.backend.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDetalleResponse {

    private Long idProducto;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
}
