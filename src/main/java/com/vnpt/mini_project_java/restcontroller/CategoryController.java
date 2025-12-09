package com.vnpt.mini_project_java.restcontroller;

import com.vnpt.mini_project_java.dto.CatalogResponseDTO;
import com.vnpt.mini_project_java.dto.CategoryDTO;
import com.vnpt.mini_project_java.entity.Category;
import com.vnpt.mini_project_java.service.category.CategoryService;
import com.vnpt.mini_project_java.service.product.ProductService;
import com.vnpt.mini_project_java.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/category", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("")
    public ResponseEntity<?> listCategory(){
        return ResponseEntity.ok(this.categoryService.findAll());
    }

    @PostMapping("/add")
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO dto) {
        try {
            CategoryDTO createdCategory = categoryService.saveDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }catch (EntityNotFoundException exception){
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy dữ liệu hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/Listgetall")
    public ResponseEntity<List<CategoryDTO>> getList(){
        List<CategoryDTO> categoryDTOS = categoryService.getAllCategoryDTO();
        return ResponseEntity.ok(categoryDTOS);
    }

    @GetMapping("/{id}/get")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable(name = "id") Long id){
        Category category = categoryService.getCategoryById(id);
        CategoryDTO categoryResponse = new CategoryDTO(category);
        return ResponseEntity.ok().body(categoryResponse);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable long id, @RequestBody CategoryDTO categoryDTO){
        try {
            Category category = categoryService.updateCategory(id, categoryDTO);
            CategoryDTO updateDTO = new CategoryDTO(category);
            return ResponseEntity.ok(updateDTO);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping ("/{id}/delete")
    public ResponseEntity<String> deleteCategory(@PathVariable long id){
        try {
            categoryService.deleteCategoryById(id);
            return ResponseEntity.ok().body("{\"status\":\"success\"}");
        }catch (EntityNotFoundException ex){
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importCategory(@RequestParam("file") MultipartFile file) {
        try {
            List<CategoryDTO> categorys = ExcelUtil.readCategoryFromExcel(file);
            categoryService.importCategorysFromExcel(categorys);
            return ResponseEntity.ok("Import successful");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error importing file");
        }
    }
    
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadExcelProductTemplate(){
        try{
            InputStream inputStream = getClass().getResourceAsStream("/templates/API-CATEGORY.xlsx");
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=API-CATEGORY.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/paginated")
    public Page<CategoryDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "categoryID,asc") String[] sort) {

        Sort.Direction sortDirection = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort[0]));

        return categoryService.getPaginatedCategorys(pageable);
    }
    @GetMapping("/catalog/{categoryID}")
    public ResponseEntity<List<CatalogResponseDTO>> getProductsByCategoryId(@PathVariable("categoryID") long categoryID) {
        try {
            List<CatalogResponseDTO> products = productService
                    .showListCategoryByIdCategory(categoryID)
                    .stream()
                    .map(p -> new CatalogResponseDTO(
                            p.getProductName(),
                            p.getPrice(),
                            p.getImage(),
                            p.getProductID()
                    ))
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
