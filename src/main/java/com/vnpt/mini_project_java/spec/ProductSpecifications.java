package com.vnpt.mini_project_java.spec;

import com.vnpt.mini_project_java.dto.ProductSearchCriteriaDTO;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.ProductDetail;
import com.vnpt.mini_project_java.entity.ProductVersion;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ProductSpecifications {
    public static Specification<Product> searchByCriteria(ProductSearchCriteriaDTO criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Predicate predicate;

            if (criteria.getProductID() != null) {
                predicates.add(criteriaBuilder.equal(root.get("productID"), criteria.getProductID()));
            }

            if (criteria.getCategoryID() != null && !criteria.getCategoryID().isEmpty()) {
                predicates.add(root.get("category").get("categoryID").in(criteria.getCategoryID()));
            }

            if (criteria.getTradeID() != null && !criteria.getTradeID().isEmpty()) {
                predicates.add(root.get("trademark").get("tradeID").in(criteria.getTradeID()));
            }

            if (criteria.getVersionID() != null && !criteria.getVersionID().isEmpty()) {
                Join<Product, ProductVersion> productVersionsJoin = root.join("productVersions", JoinType.LEFT);
                predicates.add(productVersionsJoin.get("versionID").in(criteria.getVersionID()));
            }

            if (criteria.getProductDetailID() != null && !criteria.getProductDetailID().isEmpty()) {
                Join<Product, ProductDetail> productProductDetailJoin = root.join("productDetails", JoinType.LEFT);
                predicates.add(productProductDetailJoin.get("productDetailID").in(criteria.getProductDetailID()));
            }

            if (criteria.getProductName() != null && !criteria.getProductName().isEmpty()) {
                try {
                    Predicate productNamePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("productName")),
                            "%" + criteria.getProductName().toLowerCase() + "%"
                    );
                    predicates.add(productNamePredicate);
                } catch (Exception e) {
                    System.out.println();
                }
            }

            if (criteria.getCategoryName() != null && !criteria.getCategoryName().isEmpty()) {
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("category").get("name")),
                        "%" + criteria.getCategoryName().toLowerCase() + "%");
                predicates.add(predicate);
            }

            if (criteria.getMemory() != null && !criteria.getMemory().isEmpty()) {
                Join<Product, ProductVersion> productVersions = root.join("productVersions", JoinType.LEFT);
                predicate = criteriaBuilder.equal(productVersions.get("memory"), criteria.getMemory());
                predicates.add(predicate);
            }

            if (criteria.getColor() != null && !criteria.getColor().isEmpty()) {
                Join<Product, ProductVersion> productVersions = root.join("productVersions", JoinType.LEFT);
                predicate = criteriaBuilder.equal(productVersions.get("color"), criteria.getColor());
                predicates.add(predicate);
            }

            if (criteria.getProductCamera() != null && !criteria.getProductCamera().isEmpty()) {
                Join<Product, ProductDetail> productDetails = root.join("productDetails");
                predicate = criteriaBuilder.equal(productDetails.get("productCamera"), criteria.getProductCamera());
                predicates.add(predicate);
            }

            if (criteria.getProductBluetooth() != null && !criteria.getProductBluetooth().isEmpty()) {
                Join<Product, ProductDetail> productDetails = root.join("productDetails");
                predicate = criteriaBuilder.equal(productDetails.get("productBluetooth"), criteria.getProductBluetooth());
                predicates.add(predicate);
            }

            if (criteria.getProductScreen() != null && !criteria.getProductScreen().isEmpty()) {
                Join<Product, ProductDetail> productDetails = root.join("productDetails");
                predicate = criteriaBuilder.equal(productDetails.get("productScreen"), criteria.getProductScreen());
                predicates.add(predicate);
            }

            if (criteria.getPrice() != null && !criteria.getPrice().isEmpty()) {
                try {
                    String[] parts = criteria.getPrice().split("-");
                    if (parts.length == 2) {
                        double minPrice = Double.parseDouble(parts[0]);
                        double maxPrice = Double.parseDouble(parts[1]);
                        predicates.add(criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}