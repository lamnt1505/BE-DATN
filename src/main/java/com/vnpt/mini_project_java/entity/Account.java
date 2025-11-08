package com.vnpt.mini_project_java.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Data;
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
@Table(name = "Account")
@Getter
@Setter
@AllArgsConstructor
public class Account {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long   accountID;

    @Column(name = "account_name")
    private String  accountName;

    @Column(name = "image")
    private String image;

    @Column(name = "account_pass")
    private String  accountPass;
    
    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "local")
    private String local;

    private String captcha;

    @Column(name = "date_of_birth")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "type_account")
    private String typeAccount;

    public static final String ADMIN = "ADMIN";

    public static final String EMPLOYEE = "EMPLOYEE";

    public static final String USER = "USER";

    public Account() {
    }

    public Account(Long accountID, String accountName, String accountPass, String image,
                   String username, String email, String phoneNumber, String local, LocalDate dateOfBirth, Set<Cart> carts, Set<ProductVote> productVotes,
                   Set<Order> orders, String typeAccount) {
        this.accountID = accountID;
        this.accountName = accountName;
        this.accountPass = accountPass;
        this.image = image;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.local = local;
        this.dateOfBirth = dateOfBirth;
        this.carts = carts;
        this.productVotes = productVotes;
        this.orders = orders;
        this.typeAccount = typeAccount;
    }

	@JsonManagedReference
    @JsonBackReference
    @OneToMany(mappedBy ="account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Cart> carts = new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy ="account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ProductVote> productVotes = new HashSet<>();

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy ="account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Order> orders = new HashSet<>();


    private String getImagesDir() {
        return System.getProperty("user.dir") + "/src/main/resources/static/images";
    }

    public String getImageBase64() {
        if (this.image == null || this.image.isEmpty()) {
            return "";
        }

        if (this.image.startsWith("http")) {
            return this.image; // Trả về trực tiếp link Cloudinary
        }

        Path imagePath = Paths.get(getImagesDir(), this.image);
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setImageBase64(String imageBase64) {
        try {
            this.image = imageBase64;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAdmin() {
        return ADMIN.equals(typeAccount);
    }

    public boolean isUser() {
        return USER.equals(typeAccount);
    }
}
