package com.vnpt.mini_project_java.service.productDetail;

import com.vnpt.mini_project_java.dto.ProductDetailDTO;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.ProductDetail;
import com.vnpt.mini_project_java.respository.ProductDetailRepository;
import com.vnpt.mini_project_java.respository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {

    @Autowired
    private final ProductDetailRepository productDetailRepository;

    @Autowired
    private final ProductRepository productRepository;

    public ProductDetailServiceImpl(ProductDetailRepository productDetailRepository,ProductRepository productRepository) {
        this.productDetailRepository = productDetailRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDetailDTO> getAllProductDetailDTO(){
        List<ProductDetail> productDetails = productDetailRepository.findAll();
        return productDetails.stream().map(ProductDetailDTO::new).collect(Collectors.toList());
    }

    @Override
    public ProductDetail getProductDetailById(long productDetailID){
        Optional<ProductDetail> result = productDetailRepository.findById(productDetailID);
        if(result.isPresent()){
            return result.get();
        }else {
            throw new RuntimeException("ProductDetail not found with ID:" + productDetailID);
        }
    }

    @Override
    public ProductDetailDTO createProductDetail(ProductDetailDTO dto){

        System.out.println("DTO received: " + dto);
        System.out.println("Product ID: " + dto.getProductID());

        Long productId = dto.getProductID();

        if (productId == null || productId <= 0) {
            throw new RuntimeException("Product ID không được để trống hoặc không hợp lệ!");
        }

        Product product = productRepository.findById(productId).
                orElseThrow(() -> new RuntimeException("Product not found with id:" + productId));

        ProductDetail productDetail = new ProductDetail();
        //productDetail.setProductDetailID(dto.getProductDetailID());
        productDetail.setProductCamera(dto.getProductCamera());
        productDetail.setProductWifi(dto.getProductWifi());
        productDetail.setProductScreen(dto.getProductScreen());
        productDetail.setProductBluetooth(dto.getProductBluetooth());
        productDetail.setProduct(product);

        ProductDetail savedProductDetail = productDetailRepository.save(productDetail);

        return new ProductDetailDTO(savedProductDetail);
    }

    @Override
    public ProductDetailDTO updateProductDetail(Long id, ProductDetailDTO dto) {
        System.out.println("Update DTO received: " + dto);
        System.out.println("ProductDetail ID: " + id);

        ProductDetail productDetail = productDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductDetail not found with id: " + id));

        Long newProductId = dto.getProductID();
        if (newProductId != null && newProductId > 0) {
            if (!newProductId.equals(productDetail.getProduct().getProductID())) {
                Product newProduct = productRepository.findById(newProductId)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + newProductId));
                productDetail.setProduct(newProduct);
            }
        }

        if (dto.getProductCamera() != null && !dto.getProductCamera().isEmpty()) {
            productDetail.setProductCamera(dto.getProductCamera());
        }
        if (dto.getProductWifi() != null && !dto.getProductWifi().isEmpty()) {
            productDetail.setProductWifi(dto.getProductWifi());
        }
        if (dto.getProductScreen() != null && !dto.getProductScreen().isEmpty()) {
            productDetail.setProductScreen(dto.getProductScreen());
        }
        if (dto.getProductBluetooth() != null && !dto.getProductBluetooth().isEmpty()) {
            productDetail.setProductBluetooth(dto.getProductBluetooth());
        }

        ProductDetail updatedProductDetail = productDetailRepository.save(productDetail);

        return new ProductDetailDTO(updatedProductDetail);
    }

    @Override
    public void deleteProductDetail(Long id) {
        ProductDetail productDetail = productDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductDetail not found with id: " + id));

        productDetailRepository.deleteById(id);
        System.out.println("ProductDetail deleted successfully");
    }

    @Override
    public List<ProductDetail> findByIdProduct(long productID) {
        return productDetailRepository.findByIdProduct(productID);
    }
}
