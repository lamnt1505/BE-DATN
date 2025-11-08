package com.vnpt.mini_project_java.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cart", schema = "public")
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartID;

    @Column(name = "date_import")
    private LocalDate dateImport;

    @Column(name = "cart_quantity")
    private Long cartQuantity;

    private Double total;

    private Long productID;

    @Transient
    private int amount;

    @JsonManagedReference
    @OneToMany(mappedBy ="cart" ,fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<CartDetail> cartDetails = new HashSet<>();

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "account_id")
    private Account account;
    
    public enum CartUpdateStatus{
        SUCCESS,
        PRODUCT_NOT_FOUND,
        INVALID_AMOUNT,
        INVALID_OPERATION,
        PRODUCT_REMOVED,
        QUANTITY_UPDATED,
    }

}
