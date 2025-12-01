package com.vnpt.mini_project_java.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.ProductVote;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class ProductVoteDTO {

    private Long productVoteID;

    private int rating;

    private String comment;

    private Long accountID;

    private Long productID;

    private LocalDate createdAt;

    private LocalDate updatedAt;

    public ProductVoteDTO() {
    }

    public ProductVoteDTO(ProductVote productVote) {
        this.productVoteID = productVote.getProductVoteID();
        this.rating = productVote.getRating();
        this.comment = productVote.getComment();
        this.accountID = (productVote.getAccount() != null) ? productVote.getAccount().getAccountID() : null;
        this.productID = productVote.getProduct().getProductID();
        this.createdAt = productVote.getCreatedAt();
        this.updatedAt = productVote.getUpdatedAt();
    }
}
