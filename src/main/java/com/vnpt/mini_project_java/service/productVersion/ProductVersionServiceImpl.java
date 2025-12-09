package com.vnpt.mini_project_java.service.productVersion;

import com.vnpt.mini_project_java.dto.ProductVersionDTO;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.ProductVersion;
import com.vnpt.mini_project_java.respository.ProductRepository;
import com.vnpt.mini_project_java.respository.ProductVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductVersionServiceImpl implements ProductVersionService{

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private  ProductVersionRepository productVersionRepository;

    public ProductVersionServiceImpl(ProductVersionRepository productVersionRepository, ProductRepository productRepository) {
        this.productVersionRepository = productVersionRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductVersionDTO> getAllProductVersionDTO(){
        List<ProductVersion> productVersions = productVersionRepository.findAll();
        return productVersions.stream().map(ProductVersionDTO::new).collect(Collectors.toList());
    }

    @Override
    public ProductVersion getProductVersionById(long versionID){
        Optional<ProductVersion> result = productVersionRepository.findById(versionID);
        if(result.isPresent()){
            return result.get();
        }else {
            throw new RuntimeException("ProductVersion not found with ID:" + versionID);
        }
    }

    @Override
    public List<ProductVersion> findAll(){
        return productVersionRepository.findAll();
    }

    @Override
    public List<ProductVersion> findAllByProductId(long productID){
        return productVersionRepository.findAllByProductId(productID);
    }

    @Override
    public ProductVersionDTO createProductVersion(ProductVersionDTO productVersionDTO) {
        try {
            Product product = productRepository.findById(productVersionDTO.getProductID())
                    .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + productVersionDTO.getProductID()));

            ProductVersion productVersion = new ProductVersion();
            productVersion.setMemory(productVersionDTO.getMemory());
            productVersion.setImage1(productVersionDTO.getImage1());
            productVersion.setColor(productVersionDTO.getColor());
            productVersion.setProduct(product);

            ProductVersion savedVersion = productVersionRepository.save(productVersion);
            return new ProductVersionDTO(savedVersion);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo ProductVersion: " + e.getMessage());
        }
    }

    @Override
    public ProductVersionDTO updateProductVersion(Long versionID, ProductVersionDTO productVersionDTO) {
        try {
            ProductVersion productVersion = productVersionRepository.findById(versionID)
                    .orElseThrow(() -> new RuntimeException("ProductVersion không tồn tại với ID: " + versionID));

            if (productVersionDTO.getMemory() != null) {
                productVersion.setMemory(productVersionDTO.getMemory());
            }
            if (productVersionDTO.getImage1() != null) {
                productVersion.setImage1(productVersionDTO.getImage1());
            }
            if (productVersionDTO.getColor() != null) {
                productVersion.setColor(productVersionDTO.getColor());
            }

            if (productVersionDTO.getProductID() != null) {
                Product product = productRepository.findById(productVersionDTO.getProductID())
                        .orElseThrow(() -> new RuntimeException("Product không tồn tại với ID: " + productVersionDTO.getProductID()));
                productVersion.setProduct(product);
            }

            ProductVersion updatedVersion = productVersionRepository.save(productVersion);
            return new ProductVersionDTO(updatedVersion);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật ProductVersion: " + e.getMessage());
        }
    }

    @Override
    public void deleteProductVersion(Long versionID) {
        try {
            ProductVersion productVersion = productVersionRepository.findById(versionID)
                    .orElseThrow(() -> new RuntimeException("ProductVersion không tồn tại với ID: " + versionID));
            productVersionRepository.delete(productVersion);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa ProductVersion: " + e.getMessage());
        }
    }
}
