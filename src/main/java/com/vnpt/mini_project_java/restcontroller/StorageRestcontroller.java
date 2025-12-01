package com.vnpt.mini_project_java.restcontroller;

import javax.persistence.EntityNotFoundException;

import com.vnpt.mini_project_java.dto.CategoryDTO;
import com.vnpt.mini_project_java.dto.ProductDTO;
import com.vnpt.mini_project_java.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vnpt.mini_project_java.dto.StorageDTO;
import com.vnpt.mini_project_java.entity.Storage;
import com.vnpt.mini_project_java.service.storage.StorageService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/storage", produces = MediaType.APPLICATION_JSON_VALUE)
public class StorageRestcontroller {

	private static final Logger logger = LoggerFactory.getLogger(ProductRestController.class);

	@Autowired
	private final StorageService storageService;
	
	public StorageRestcontroller(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/Listgetall")
	public ResponseEntity<List<StorageDTO>> getList(){
		List<StorageDTO> storageDTOS = storageService.getAllStorageDTO();
		return ResponseEntity.ok(storageDTOS);
	}

	@GetMapping("/getall")
    public ResponseEntity<?> getListProductdto() {
        return ResponseEntity.ok(storageService.getAllStorageDTO());
    }

    @PostMapping("/add")
	public ResponseEntity<StorageDTO> createStorage(@RequestBody StorageDTO dto) {
		try {
			StorageDTO createdStorage = storageService.createStorage(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdStorage);
		}catch(EntityNotFoundException ex) {
			return ResponseEntity.badRequest().build();
		}
	}
    
    @PutMapping("/update/{id}")
    public ResponseEntity<StorageDTO> updateStorage(@PathVariable long id,@ModelAttribute  StorageDTO storageDTO){
    	try {
    		Storage storage = storageService.updateStorage(id, storageDTO);
    		
    		StorageDTO updateDTO = new StorageDTO(storage);

    		return ResponseEntity.ok(updateDTO);
    	}catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
            
		}
    }

	@DeleteMapping ("/delete/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable long id) {
		try {
			logger.info("Người dùng {} đang xóa sản phẩm có ID: {}",id);
			storageService.deleteStorageById(id);
			return ResponseEntity.ok().body("{\"status\":\"success\"}");
		} catch (EntityNotFoundException ex) {
			//System.out.println("Error" + ex.getMessage());
			return ResponseEntity.noContent().build();
		}
	}

	@GetMapping("/{id}/get")
	public ResponseEntity<StorageDTO> getStorageById(@PathVariable(name = "id") Long idImport) {
		Storage storage = storageService.getImportById(idImport);
		StorageDTO storageResponse = new StorageDTO(storage);
		return ResponseEntity.ok().body(storageResponse);
	}

	@GetMapping("/low-stock")
	public ResponseEntity<?> getLowStockProducts(
			@RequestParam(defaultValue = "5") int threshold) {
		List<StorageDTO> list = storageService.getLowStockProducts(threshold);
		return ResponseEntity.ok(list);
	}
}
