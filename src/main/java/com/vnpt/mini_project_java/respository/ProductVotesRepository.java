package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.entity.ProductVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVotesRepository extends JpaRepository<ProductVote, Long> {
    List<ProductVote> findByProduct_ProductID(Long productId);

    @Query("SELECT v FROM ProductVote v WHERE v.product.productID = :productId")
    List<ProductVote> findByProductId(@Param("productId") Long productId);
}
