package com.vnpt.mini_project_java.service.productVersion;

import com.vnpt.mini_project_java.dto.ProductVersionDTO;
import com.vnpt.mini_project_java.entity.ProductVersion;

import java.util.List;

public interface ProductVersionService {
    List<ProductVersionDTO> getAllProductVersionDTO();

    ProductVersion getProductVersionById(long versionID);

    //ProductVersion findByIdProduct(long productID);

    List<ProductVersion> findAll();

    List<ProductVersion> findAllByProductId(long productID);

    ProductVersionDTO createProductVersion(ProductVersionDTO productVersionDTO);

    ProductVersionDTO updateProductVersion(Long versionID, ProductVersionDTO productVersionDTO);

    void deleteProductVersion(Long versionID);
}
