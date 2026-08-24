package com.farmasol.backend.service;

import com.farmasol.backend.dto.ProductoDTO;

import java.util.List;

public interface ProductoService {

    List<ProductoDTO> listarTodos();

    ProductoDTO obtenerPorId(Long id);

    List<ProductoDTO> buscarPorNombre(String nombre);

    List<ProductoDTO> buscarPorCategoria(String categoria);

    ProductoDTO guardar(ProductoDTO productoDTO);

    ProductoDTO actualizar(Long id, ProductoDTO productoDTO);

    void eliminar(Long id);
}
