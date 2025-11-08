package com.vnpt.mini_project_java.service.product;

import com.vnpt.mini_project_java.dto.*;

import com.vnpt.mini_project_java.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<ProductDTO> getAllProductDTO();

    List<CompareProductDTO> getAllCompare();

    ProductDTO createProduct(ProductDTO dto);

    void deleteProductById(long id);

    Product updateProduct(long id, ProductDTO dto);

    Product getProductById(long productID);

    Page<ProductDTO> getPaginatedProducts(Pageable pageable);

    List<ProductDTO> getProductsByCategoryId(Long categoryID);

    List<Product> findBycategoryId(Long categoryID);

    Optional<Product> findById(Long productID);

    void importProductsFromExcel(List<ProductDTO> productDTOs);

    List<Product> findAll();

    List<Product> showListCategoryByIdCategory(long categoryID);

    Product findByIdProduct(long productID);

    List<Product> showListProductByIdCategoryFilter(long categoryID);

    List<Product> listProductNewBest();

    List<Product> listProductPriceDesc();

    List<Product> listProductPriceAsc();

    List<Product> compareList = new ArrayList<>();

    List<Product> searchProducts(ProductSearchCriteriaDTO criteria);

	Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    List<Product> searchListProductByIdCategory(String productName);

    Page<ProductDTO> getPaginatedProduct(Pageable pageable);

    ProductDetailiDDTO getProductDetailWithVersionsDetails(Long id);

    List<ProductDTO> getRelatedProducts(Long id);
}
