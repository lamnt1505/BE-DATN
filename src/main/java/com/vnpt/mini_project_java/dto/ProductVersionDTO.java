package com.vnpt.mini_project_java.dto;

import com.vnpt.mini_project_java.entity.ProductVersion;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ProductVersionDTO {
    private Long versionID;
    private String memory;
    private String image1;
    private String color;
    private Long productID;

    public ProductVersionDTO() {
    }

    public ProductVersionDTO(ProductVersion productVersion){
        this.versionID = productVersion.getVersionID();
        this.memory = productVersion.getMemory();
        this.image1 = productVersion.getImage1();
        this.color = productVersion.getColor();
        this.productID = productVersion.getProduct().getProductID();
    }
}
