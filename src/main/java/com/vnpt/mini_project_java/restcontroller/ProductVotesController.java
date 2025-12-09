package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.ProductVoteDTO;
import com.vnpt.mini_project_java.service.productvotes.ProductVotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/votes", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductVotesController {

    @Autowired
    ProductVotesService productVotesService;

    @GetMapping("/List--get--all")
    public ResponseEntity<List<ProductVoteDTO>> getList(){
        List<ProductVoteDTO> categoryDTOS = productVotesService.getAllvoteDTO();
        return ResponseEntity.ok(categoryDTOS);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVoteDTO>> getVotesByProductId(@PathVariable Long productId) {
        List<ProductVoteDTO> votes = productVotesService.getVotesByProductId(productId);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/paginated")
    public Page<ProductVoteDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "productID,asc") String[] sort) {

        Sort.Direction sortDirection = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort[0]));

        return productVotesService.getPaginatedProducts(pageable);
    }
}
