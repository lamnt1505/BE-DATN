package com.vnpt.mini_project_java.respository;

import com.vnpt.mini_project_java.dto.StorageDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vnpt.mini_project_java.entity.Storage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage,Long> {
    @Query(value =
            "SELECT p.product_id AS productId, p.product_name AS productName, SUM(s.quantity) AS totalQuantity " +
                    "FROM storage s " +
                    "JOIN product p ON s.product_id = p.product_id " +
                    "GROUP BY p.product_id, p.product_name " +
                    "HAVING SUM(s.quantity) < :threshold",
            nativeQuery = true)
    List<Object[]> getLowStockProducts(@Param("threshold") int threshold);

    @Query(value = "SELECT * FROM storage WHERE product_id=? ", nativeQuery = true)
    Storage findQuatityProduct(long product_id);
}
