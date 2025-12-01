package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.ProductVersionDTO;
import com.vnpt.mini_project_java.entity.ProductVersion;
import com.vnpt.mini_project_java.service.productVersion.ProductVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/product/version", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductVersionRestController {

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
}
