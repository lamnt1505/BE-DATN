package com.vnpt.mini_project_java.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailiDDTO {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String image;
    private LocalDate dateProduct;
    private String category;
    private String trademark;

    private ProductDetailDTO detail;
    private List<ProductVersionDTO> versions;
}
