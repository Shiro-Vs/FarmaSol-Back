package com.farmasol.backend.service.impl;

import com.farmasol.backend.dto.ProductoDTO;
import com.farmasol.backend.exception.ResourceNotFoundException;
import com.farmasol.backend.model.Producto;
import com.farmasol.backend.repository.ProductoRepository;
import com.farmasol.backend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return convertirADTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductoDTO guardar(ProductoDTO productoDTO) {
        Producto producto = convertirAEntidad(productoDTO);
        Producto productoGuardado = productoRepository.save(producto);
        return convertirADTO(productoGuardado);
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(Long id, ProductoDTO productoDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setCategoria(productoDTO.getCategoria());
        productoExistente.setImagenUrl(productoDTO.getImagenUrl());
        productoExistente.setRequiereReceta(productoDTO.getRequiereReceta() != null ? productoDTO.getRequiereReceta() : false);

        Producto productoActualizado = productoRepository.save(productoExistente);
        return convertirADTO(productoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    // Métodos auxiliares de mapeo
    private ProductoDTO convertirADTO(Producto entity) {
        return ProductoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .precio(entity.getPrecio())
                .stock(entity.getStock())
                .categoria(entity.getCategoria())
                .imagenUrl(entity.getImagenUrl())
                .requiereReceta(entity.getRequiereReceta())
                .build();
    }

    private Producto convertirAEntidad(ProductoDTO dto) {
        return Producto.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .categoria(dto.getCategoria())
                .imagenUrl(dto.getImagenUrl())
                .requiereReceta(dto.getRequiereReceta() != null ? dto.getRequiereReceta() : false)
                .build();
    }
}
