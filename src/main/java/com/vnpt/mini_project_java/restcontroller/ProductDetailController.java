package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.ProductDetailDTO;
import com.vnpt.mini_project_java.entity.ProductDetail;
import com.vnpt.mini_project_java.service.productDetail.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/productdetail", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductDetailController {
    @Autowired
    private ProductDetailService productDetailService;

    @GetMapping("/getall")
    public ResponseEntity<?> getListProductDetail(){
        return ResponseEntity.ok(productDetailService.getAllProductDetailDTO());
    }

    @GetMapping("/Listgetall")
    public ResponseEntity<List<ProductDetailDTO>> getList(){
        List<ProductDetailDTO> productDetailDTOS = productDetailService.getAllProductDetailDTO();
        return ResponseEntity.ok(productDetailDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductDetailById(@PathVariable(name = "id") Long productDetailID){
        ProductDetail productDetail = productDetailService.getProductDetailById(productDetailID);
        ProductDetailDTO productDetailResponse = new ProductDetailDTO(productDetail);
        return ResponseEntity.ok().body(productDetailResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createProductDetail(@RequestBody ProductDetailDTO dto){
        try {
            System.out.println("Request body: " + dto);
            ProductDetailDTO createdProductDetail = productDetailService.createProductDetail(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProductDetail);
        } catch (RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi hệ thống: " + ex.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProductDetail(
            @PathVariable(name = "id") Long productDetailID,
            @RequestBody ProductDetailDTO dto) {
        try {
            System.out.println("Update request for ID: " + productDetailID);
            ProductDetailDTO updatedProductDetail = productDetailService.updateProductDetail(productDetailID, dto);
            return ResponseEntity.ok(updatedProductDetail);
        } catch (RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi hệ thống: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductDetail(@PathVariable(name = "id") Long productDetailID) {
        try {
            System.out.println("Delete request for ID: " + productDetailID);
            productDetailService.deleteProductDetail(productDetailID);
            return ResponseEntity.ok(Collections.singletonMap("message", "Xóa chi tiết sản phẩm thành công"));
        } catch (RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi hệ thống: " + ex.getMessage()));
        }
    }
}
