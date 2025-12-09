package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.InventoryDTO;
import com.vnpt.mini_project_java.entity.Inventory;
import com.vnpt.mini_project_java.service.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/check/{branchID}/{productID}")
    public ResponseEntity<Map<String, Object>> checkStock(@PathVariable Long branchID, @PathVariable Long productID) {
        Optional<Inventory> inv = inventoryService.checkStock(branchID, productID);
        Map<String, Object> response = new HashMap<>();
        if (!inv.isPresent() || inv.get().getQuantity() <= 0) {
            response.put("status", "Hết hàng");
            response.put("message", "Hết hàng tại chi nhánh");
            response.put("quantity", 0);
        } else {
            response.put("status", "in_stock");
            response.put("message", "Còn hàng");
            response.put("quantity", inv.get().getQuantity());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-product/{productID}")
    public ResponseEntity<List<InventoryDTO>> getStockByProduct(@PathVariable Long productID) {
        return ResponseEntity.ok(inventoryService.getAllBranchesStock(productID));
    }
}
