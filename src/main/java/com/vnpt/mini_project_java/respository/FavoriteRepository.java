package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.entity.Favorite;
import com.vnpt.mini_project_java.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByAccountAndProduct(Account account, Product product);

    List<Favorite> findByAccount_AccountID(Long accountId);

    Optional<Favorite> findByAccount_AccountIDAndProduct_ProductID(Long accountId, Long productId);
}
