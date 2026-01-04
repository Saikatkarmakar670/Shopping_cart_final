package com.product.service;

import com.product.dtos.ProductRequestDTO;
import com.product.dtos.ProductResponseDTO;
import com.product.dtos.ProductResponseReturn;

import java.util.List;

public interface ProductService {
    List<ProductResponseReturn> getAll();
    ProductResponseDTO getByProductId(Long id);
    List<ProductResponseDTO> getById(Long id);
    List<ProductResponseReturn> getByCategories(String categories);
    ProductResponseDTO addProduct(Long sellerId,ProductRequestDTO dto);
    ProductResponseDTO updateProduct(Long sellerId,ProductRequestDTO dto,Long id);
    void deleteById(Long sellerId,Long id);
}
