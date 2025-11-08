package com.vnpt.mini_project_java.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class FavoriteDTO {

    private Long favoriteId;

    private Long id;

    private String name;

    private String image;

    private double price;

    private String dateProduct;

    public FavoriteDTO( Long id, String name, String image, double price) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
    }
}
