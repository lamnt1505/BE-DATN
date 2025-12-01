package com.vnpt.mini_project_java.service.productDetail;

import com.vnpt.mini_project_java.dto.ProductDetailDTO;
import com.vnpt.mini_project_java.entity.ProductDetail;
import com.vnpt.mini_project_java.entity.ProductVersion;

import java.util.List;

public interface ProductDetailService {
    List<ProductDetailDTO> getAllProductDetailDTO();

    ProductDetail getProductDetailById(long productDetailID);

    ProductDetailDTO createProductDetail(ProductDetailDTO dto);

    ProductDetailDTO updateProductDetail(Long id, ProductDetailDTO dto);

    void deleteProductDetail(Long id);

    List<ProductDetail> findByIdProduct(long productID);
}
