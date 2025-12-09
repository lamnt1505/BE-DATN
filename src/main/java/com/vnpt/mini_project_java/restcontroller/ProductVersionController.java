package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.ProductVersionDTO;
import com.vnpt.mini_project_java.entity.ProductVersion;
import com.vnpt.mini_project_java.service.productVersion.ProductVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/product/version", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductVersionController {
    @Autowired
    private ProductVersionService productVersionService;

    @GetMapping("/getall")
    public ResponseEntity<?> getListProduct() {
        return ResponseEntity.ok(productVersionService.getAllProductVersionDTO());
    }

    @GetMapping("/Listgetall")
    public ResponseEntity<List<ProductVersionDTO>> getList() {
        List<ProductVersionDTO> productVersionDTOS = productVersionService.getAllProductVersionDTO();
        return ResponseEntity.ok(productVersionDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVersionDTO> getProductById(@PathVariable(name = "id") Long versionID) {
        ProductVersion productVersion = productVersionService.getProductVersionById(versionID);
        ProductVersionDTO prodouctVersionResponse = new ProductVersionDTO(productVersion);
        return ResponseEntity.ok().body(prodouctVersionResponse);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProductVersion(@RequestBody ProductVersionDTO productVersionDTO) {
        try {
            ProductVersionDTO createdVersion = productVersionService.createProductVersion(productVersionDTO);
            return ResponseEntity.status(201).body(createdVersion);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("{versionID}/update")
    public ResponseEntity<?> updateProductVersion(@PathVariable Long versionID, @RequestBody ProductVersionDTO productVersionDTO) {
        try {
            ProductVersionDTO updatedVersion = productVersionService.updateProductVersion(versionID, productVersionDTO);
            return ResponseEntity.ok(updatedVersion);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("{versionID}/delete")
    public ResponseEntity<?> deleteProductVersion(@PathVariable Long versionID) {
        try {
            productVersionService.deleteProductVersion(versionID);
            return ResponseEntity.ok("✅ Xóa ProductVersion thành công");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
