package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.*;
import com.vnpt.mini_project_java.entity.*;
import com.vnpt.mini_project_java.respository.DiscountRepository;
import com.vnpt.mini_project_java.respository.DiscountUsageRepository;
import com.vnpt.mini_project_java.service.favorite.FavoriteService;
import com.vnpt.mini_project_java.service.product.ProductService;
import com.vnpt.mini_project_java.service.productvotes.ProductVotesService;
import com.vnpt.mini_project_java.spec.ProductSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    ProductVotesService productVotesService;

    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    DiscountUsageRepository discountUsageRepository;

    private void logToConsoleAndFile(String message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(message);
    }

    @GetMapping(value ="/dossier-statistic/list--Product")
    public ResponseEntity<?> showListProduct() {
        return ResponseEntity.ok(this.productService.getAllProductDTO());
    }

    @PostMapping(value ="/dossier-statistic/list--ProductById--Category--Filter/{categoryID}")
    @ResponseBody
    public List<ProductDTO> showListProductByIdCategory(@PathVariable("categoryID") long id) {
        List<Product> productList = this.productService.showListProductByIdCategoryFilter(id);
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setDescription(product.getDescription());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTO.setCategoryID(product.getCategory().getCategoryID());
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    @GetMapping(value ="/dossier-statistic/list--Product--PriceDesc")
    public ResponseEntity<List<ProductDTO>> showListProductPriceDesc() {
        List<Product> productList = this.productService.listProductPriceDesc();
        List<ProductDTO> productDTOList = convertToDTOList(productList);
        return ResponseEntity.ok(productDTOList);
    }

    @GetMapping(value ="/dossier-statistic/list--Product--PriceAsc")
    public ResponseEntity<List<ProductDTO>> showListProductPriceAsc() {
        List<Product> productList = this.productService.listProductPriceAsc();
        List<ProductDTO> productDTOList = convertToDTOList(productList);
        return ResponseEntity.ok(productDTOList);
    }

    private List<ProductDTO> convertToDTOList(List<Product> productList) {
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    @GetMapping(value ="/dossier-statistic/list--Product--NewBest")
    public ResponseEntity<List<ProductDTO>> showListProductNewBest() {
        List<Product> productList = this.productService.listProductNewBest();

        List<ProductDTO> productDTOList = new ArrayList<>();

        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());

            productDTOList.add(productDTO);
        }

        return ResponseEntity.ok(productDTOList);
    }

    @PostMapping(value ="/dossier-statistic/search")
    public ResponseEntity<?> searchProducts(@RequestBody ProductSearchCriteriaDTO criteria, Pageable pageable) {
        try {
            Specification<Product> spec = ProductSpecifications.searchByCriteria(criteria);

            Page<Product> products = productService.findAll(spec, pageable);

            Page<ProductDTO> productDTOs = products.map(ProductDTO::new);

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping(value ="/dossier-statistic/add--favorite")
    public ResponseEntity<String> addToFavorite(@RequestParam(required = false) Long accountID,
                                                @RequestParam(required = false) Long productID) {
        if (accountID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("⚠️ Bạn cần đăng nhập để thêm sản phẩm yêu thích!");
        }
        if (productID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Thiếu mã sản phẩm!");
        }
        String result = favoriteService.addProductToFavorite(accountID, productID);
        HttpStatus status = result.contains("Đã thêm") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(result, status);
    }

    @GetMapping(value ="/dossier-statistic/list--favorite")
    public ResponseEntity<List<FavoriteDTO>> getFavoriteList(@RequestParam Long accountID) {
        List<FavoriteDTO> favoriteProducts = favoriteService.getFavoritesByAccountId(accountID);
        if (favoriteProducts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(favoriteProducts, HttpStatus.OK);
    }

    @DeleteMapping("/dossier-statistic/{accountId}/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long accountId, @PathVariable Long productId) {
        try {
            boolean removed = favoriteService.removeFavorite(accountId, productId);
            Map<String, Object> response = new HashMap<>();
            if (removed) {
                response.put("success", true);
                response.put("message", "Đã xóa sản phẩm khỏi danh sách yêu thích");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm trong danh sách yêu thích");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/dossier-statistic/add--vote")
    public ResponseEntity<?> createVote(@RequestBody ProductVoteDTO voteDTO) {
        try {
            ProductVote vote = productVotesService.saveVote(voteDTO);
            ProductVoteDTO responseDTO = convertToDTO(vote);
            return ResponseEntity.ok(responseDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể lưu đánh giá.");
        }
    }

    public ProductVoteDTO convertToDTO(ProductVote vote) {
        ProductVoteDTO dto = new ProductVoteDTO();
        dto.setProductVoteID(vote.getProductVoteID());
        dto.setRating(vote.getRating());
        dto.setComment(vote.getComment());
        if (vote.getAccount() != null) {
            dto.setAccountID(vote.getAccount().getAccountID());
        } else {
            dto.setAccountID(null);
        }
        dto.setProductID(vote.getProduct().getProductID());
        dto.setCreatedAt(vote.getCreatedAt());
        dto.setUpdatedAt(vote.getUpdatedAt());
        return dto;
    }

    @GetMapping("/dossier-statistic/products")
    public ResponseEntity<Page<ProductDTO>> getPaginatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "productID,desc") String[] sort) {

        String[] sortParams = sort.clone();
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<ProductDTO> products = productService.getPaginatedProducts(pageable);
        return ResponseEntity.ok(products);
    }
}

