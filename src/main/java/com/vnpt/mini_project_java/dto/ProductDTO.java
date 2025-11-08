package com.vnpt.mini_project_java.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vnpt.mini_project_java.entity.Product;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import javax.persistence.Transient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@ToString
public class ProductDTO {

    private Long id;

    private String name;

    private String description;

    private String date_product;

    private Long categoryID;

    private String categoryname;
    
    private String memory;
    
    private String color;

    private String productBluetooth;

    private String productScreen;

    private String productWifi;

    private String productCamera;
    
    private Long tradeID;

    private Long productDetailID;

    private Long versionID;

    private String tradeName;

    private String image;
    // link Cloudinary
    private String imageBase64; // base64 từ client khi upload

    private double price;

    private Set<ProductVersionDTO> productVersions;

    private Set<ProductDetailDTO> productDetails;

    @Transient
    private int amount;

    public ProductDTO() {
    }

    public ProductDTO(Product product){
        this.id = product.getProductID();
        this.name = product.getProductName();
        this.description = product.getDescription();
        this.date_product = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(product.getDateProduct());
        this.price = product.getPrice();
        this.amount = product.getAmount();

        if(product.getTrademark() != null){
            this.tradeName = product.getTrademark().getTradeName();
            this.tradeID = product.getTrademark().getTradeID();
        }

        if(product.getCategory() != null){
            this.categoryname = product.getCategory().getCategoryName();
            this.categoryID = product.getCategory().getCategoryID();
        }

        this.image = product.getImage();

        this.imageBase64 = null;

        if (product.getProductVersions() != null) {
            this.productVersions = product.getProductVersions().stream()
                    .map(ProductVersionDTO::new)
                    .collect(Collectors.toSet());
        } else {
            this.productVersions = Collections.emptySet();
        }

        if (product.getProductDetails() != null) {
            this.productDetails = product.getProductDetails().stream()
                    .map(ProductDetailDTO::new)
                    .collect(Collectors.toSet());
        } else {
            this.productDetails = Collections.emptySet();
        }
    }
}