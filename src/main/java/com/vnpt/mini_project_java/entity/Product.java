package com.vnpt.mini_project_java.entity;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productID;

    @Column(name = "productName", columnDefinition = "nvarchar(150)")
    private String productName;

    @Column(name = "image")
    private String image;

    @Column(name = "description", columnDefinition = "nvarchar(500)")
    private String description;

    private double price;

    @Transient
    private int amount;

    @Column(name = "date_product")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateProduct;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonManagedReference
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<ProductVersion> productVersions;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ProductDetail> productDetails ;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "trade_id")
    private Trademark trademark;


    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ProductVote> productVotes = new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Discount> discounts = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    private String getImagesDir() {
        return System.getProperty("user.dir") + "/src/main/resources/static/images";
    }

    public String getImageBase64() {
        if (this.getImage() == null) {
            return "";
        } else {
            Path imagePath = Paths.get(getImagesDir(), this.getImage());
            try {
                byte[] imageBytes = Files.readAllBytes(imagePath);
                return Base64.getEncoder().encodeToString(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    public void setImageBase64(String imageBase64) {
        try {
            this.image = imageBase64;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Product() {
    }

    public Product(Long productID, String productName, String image, String description,
                   LocalDate dateProduct, Category category, Set<ProductVersion> productVersions,
                   Trademark trademark, Set<ProductDetail> productDetails, Set<ProductVote> productVotes,
                   Set<Discount> discounts, int amount) {
        this.productID = productID;
        this.productName = productName;
        this.image = image;
        this.description = description;
        this.dateProduct = dateProduct;
        this.category = category;
        this.productVersions = productVersions;
        this.trademark = trademark;
        this.productDetails = productDetails;
        this.productVotes = productVotes;
        this.discounts = discounts;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", productName='" + productName + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", dateProduct=" + dateProduct +
                ", category=" + category +
                ", productVersions=" + productVersions +
                ", trademark=" + trademark +
                ", productDetails=" + productDetails +
                ", productVotes=" + productVotes +
                ", discounts=" + discounts +
                '}';
    }

    public enum CartUpdateStatus{
        SUCCESS,
        PRODUCT_NOT_FOUND,
        INVALID_AMOUNT,
        INVALID_OPERATION,
        PRODUCT_REMOVED,
        QUANTITY_UPDATED,
    }
}
