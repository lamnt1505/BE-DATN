package com.vnpt.mini_project_java.dto;

import com.vnpt.mini_project_java.entity.ProductDetail;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
public class ProductDetailDTO {

    private Long productDetailID;

    private String productCamera;

    private String productWifi;

    private String productScreen;

    private String productBluetooth;

    private Long productID;

    private DetailDTO detail;

    private List<VersionDTO> versions;

    public ProductDetailDTO() {
    }

    public ProductDetailDTO(ProductDetail productDetail){
        this.productDetailID = productDetail.getProductDetailID();
        this.productCamera = productDetail.getProductCamera();
        this.productWifi = productDetail.getProductWifi();
        this.productScreen = productDetail.getProductScreen();
        this.productBluetooth = productDetail.getProductBluetooth();
        this.productID = productDetail.getProduct().getProductID();
    }

    @Data
    @AllArgsConstructor
    public static class DetailDTO {
        private String camera;
        private String wifi;
        private String screen;
        private String bluetooth;
    }

    @Data
    @AllArgsConstructor
    public static class VersionDTO {
        private Long versionID;
        private String color;
        private String memory;
        private String image1;
    }
}
