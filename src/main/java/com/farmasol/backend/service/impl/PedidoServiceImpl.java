package com.farmasol.backend.service.impl;

import com.farmasol.backend.dto.pedido.CheckoutRequest;
import com.farmasol.backend.dto.pedido.PedidoDetalleResponse;
import com.farmasol.backend.dto.pedido.PedidoResponse;
import com.farmasol.backend.dto.pedido.PedidoResumenResponse;
import com.farmasol.backend.dto.producto.PrecioCalculadoDTO;
import com.farmasol.backend.exception.BusinessException;
import com.farmasol.backend.exception.ResourceNotFoundException;
import com.farmasol.backend.model.*;
import com.farmasol.backend.model.enums.EstadoPedido;
import com.farmasol.backend.repository.*;
import com.farmasol.backend.service.PedidoService;
import com.farmasol.backend.service.PrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final DireccionRepository direccionRepository;
    private final ProductoRepository productoRepository;
    private final PersonalRepository personalRepository;
    private final PrecioService precioService;

    @Value("${farmasol.pedido.costo-envio:0}")
    private BigDecimal costoEnvio;

    @Override
    @Transactional
    public PedidoResponse checkout(Long idCliente, CheckoutRequest request) {
        Carrito carrito = carritoRepository.findByCliente_Id(idCliente)
                .orElseThrow(() -> new BusinessException("El carrito está vacío"));
        if (carrito.getDetalles().isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        Direccion direccion = direccionRepository.findByIdAndCliente_Id(request.getIdDireccion(), idCliente)
                .filter(Direccion::getActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada con ID: " + request.getIdDireccion()));

        Pedido pedido = Pedido.builder()
                .cliente(carrito.getCliente())
                .direccion(direccion)
                .estado(EstadoPedido.PENDIENTE)
                .costoEnvio(costoEnvio != null ? costoEnvio : BigDecimal.ZERO)
                .envioQuienRecibe(direccion.getQuienRecibe())
                .envioTelefono(direccion.getTelefonoContacto())
                .envioDireccion(direccion.getDireccion())
                .envioDistrito(direccion.getDistrito())
                .envioReferencia(direccion.getReferencia())
                .notas(request.getNotas())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        for (CarritoDetalle linea : carrito.getDetalles()) {
            Producto producto = linea.getProducto();
            if (linea.getCantidad() > producto.getStock()) {
                throw new BusinessException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }
            PrecioCalculadoDTO precio = precioService.calcular(producto);
            BigDecimal lineaSubtotal = precio.getPrecioFinal().multiply(BigDecimal.valueOf(linea.getCantidad()));

            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .nombreProducto(producto.getNombre())
                    .precioUnitario(precio.getPrecioBase())
                    .descuentoUnitario(precio.getDescuentoUnitario())
                    .cantidad(linea.getCantidad())
                    .subtotal(lineaSubtotal)
                    .build();
            pedido.getDetalles().add(detalle);

            producto.setStock(producto.getStock() - linea.getCantidad());

            subtotal = subtotal.add(precio.getPrecioBase().multiply(BigDecimal.valueOf(linea.getCantidad())));
            descuentoTotal = descuentoTotal.add(precio.getDescuentoUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
        }

        BigDecimal total = subtotal.subtract(descuentoTotal).add(pedido.getCostoEnvio()).max(BigDecimal.ZERO);
        pedido.setSubtotal(subtotal);
        pedido.setDescuentoTotal(descuentoTotal);
        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);
        guardado.setCodigoPedido("PED-" + Year.now() + "-" + String.format("%06d", guardado.getId()));
        guardado = pedidoRepository.save(guardado);

        carrito.getDetalles().clear();
        carritoRepository.save(carrito);

        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarDeCliente(Long idCliente) {
        return pedidoRepository.findByCliente_IdOrderByFechaPedidoDesc(idCliente)
                .stream().map(this::toResumen).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerDeCliente(Long idCliente, Long idPedido) {
        Pedido pedido = pedidoRepository.findByIdAndCliente_Id(idPedido, idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + idPedido));
        return toResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarTodos(EstadoPedido filtroEstado) {
        List<Pedido> pedidos = (filtroEstado != null)
                ? pedidoRepository.findByEstadoOrderByFechaPedidoDesc(filtroEstado)
                : pedidoRepository.findAllByOrderByFechaPedidoDesc();
        return pedidos.stream().map(this::toResumen).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerAdmin(Long idPedido) {
        return toResponse(buscar(idPedido));
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long idPersonal, Long idPedido, EstadoPedido nuevoEstado) {
        Pedido pedido = buscar(idPedido);
        if (!pedido.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new BusinessException("No se puede pasar de " + pedido.getEstado() + " a " + nuevoEstado);
        }

        Personal personal = personalRepository.findById(idPersonal).orElse(null);
        if (pedido.getAtendidoPor() == null) {
            pedido.setAtendidoPor(personal);
        }

        switch (nuevoEstado) {
            case CONFIRMADO -> {
                pedido.setFechaConfirmacion(LocalDateTime.now());
                pedido.setAtendidoPor(personal);
            }
            case ENTREGADO -> pedido.setFechaEntrega(LocalDateTime.now());
            case CANCELADO -> reponerStock(pedido);
            default -> { }
        }

        pedido.setEstado(nuevoEstado);
        return toResponse(pedidoRepository.save(pedido));
    }

    private void reponerStock(Pedido pedido) {
        for (PedidoDetalle d : pedido.getDetalles()) {
            if (d.getProducto() != null) {
                Producto p = d.getProducto();
                p.setStock(p.getStock() + d.getCantidad());
                productoRepository.save(p);
            }
        }
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findWithDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    private PedidoResumenResponse toResumen(Pedido p) {
        int items = p.getDetalles().stream().mapToInt(PedidoDetalle::getCantidad).sum();
        return PedidoResumenResponse.builder()
                .id(p.getId())
                .codigoPedido(p.getCodigoPedido())
                .nombreCliente(p.getCliente().getNombres() + " " + p.getCliente().getApellidos())
                .estado(p.getEstado())
                .total(p.getTotal())
                .cantidadItems(items)
                .fechaPedido(p.getFechaPedido())
                .build();
    }

    private PedidoResponse toResponse(Pedido p) {
        List<PedidoDetalleResponse> detalles = new ArrayList<>();
        for (PedidoDetalle d : p.getDetalles()) {
            detalles.add(PedidoDetalleResponse.builder()
                    .idProducto(d.getProducto() != null ? d.getProducto().getId() : null)
                    .nombreProducto(d.getNombreProducto())
                    .precioUnitario(d.getPrecioUnitario())
                    .descuentoUnitario(d.getDescuentoUnitario())
                    .cantidad(d.getCantidad())
                    .subtotal(d.getSubtotal())
                    .build());
        }
        return PedidoResponse.builder()
                .id(p.getId())
                .codigoPedido(p.getCodigoPedido())
                .idCliente(p.getCliente().getId())
                .nombreCliente(p.getCliente().getNombres() + " " + p.getCliente().getApellidos())
                .estado(p.getEstado())
                .subtotal(p.getSubtotal())
                .descuentoTotal(p.getDescuentoTotal())
                .costoEnvio(p.getCostoEnvio())
                .total(p.getTotal())
                .envioQuienRecibe(p.getEnvioQuienRecibe())
                .envioTelefono(p.getEnvioTelefono())
                .envioDireccion(p.getEnvioDireccion())
                .envioDistrito(p.getEnvioDistrito())
                .envioReferencia(p.getEnvioReferencia())
                .fechaPedido(p.getFechaPedido())
                .fechaConfirmacion(p.getFechaConfirmacion())
                .fechaEntrega(p.getFechaEntrega())
                .atendidoPor(p.getAtendidoPor() != null ? p.getAtendidoPor().getUsuario() : null)
                .notas(p.getNotas())
                .detalles(detalles)
                .build();
    }
}
