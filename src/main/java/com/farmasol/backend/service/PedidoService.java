package com.farmasol.backend.service;

import com.farmasol.backend.dto.pedido.CheckoutRequest;
import com.farmasol.backend.dto.pedido.PedidoResponse;
import com.farmasol.backend.dto.pedido.PedidoResumenResponse;
import com.farmasol.backend.model.enums.EstadoPedido;

import java.util.List;

public interface PedidoService {

    PedidoResponse checkout(Long idCliente, CheckoutRequest request);

    List<PedidoResumenResponse> listarDeCliente(Long idCliente);

    PedidoResponse obtenerDeCliente(Long idCliente, Long idPedido);

    List<PedidoResumenResponse> listarTodos(EstadoPedido filtroEstado);

    PedidoResponse obtenerAdmin(Long idPedido);

    PedidoResponse cambiarEstado(Long idPersonal, Long idPedido, EstadoPedido nuevoEstado);
}
