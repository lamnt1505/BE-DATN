package com.vnpt.mini_project_java.service.storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vnpt.mini_project_java.dto.StorageDTO;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.Storage;
import com.vnpt.mini_project_java.respository.ProductRepository;
import com.vnpt.mini_project_java.respository.StorageRepository;

@Service
public class StorageServiceImpl implements StorageService {

	@Autowired
	private final StorageRepository storageRepository;

	@Autowired
	private final ProductRepository productRepository;

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

	public StorageServiceImpl(StorageRepository storageRepository, ProductRepository productRepository) {
		super();
		this.storageRepository = storageRepository;
		this.productRepository = productRepository;
	}

	@Override
	public Storage findQuatityProduct(long product_id) {
		return storageRepository.findQuatityProduct(product_id);
	}
	@Override
	public List<StorageDTO> getAllStorageDTO() {
		List<Storage> products = storageRepository.findAll();
		return products.stream().map(StorageDTO::new).collect(Collectors.toList());
	}
	@Override
	public <S extends Storage> S save(S entity) {
		return storageRepository.save(entity);
	}

	@Override
	public StorageDTO createStorage(StorageDTO storageDTO) {
		Storage storage = new Storage();
		storage.setIdImport(storageDTO.getIdImport());
		storage.setUsers(storageDTO.getUsers());
		if (storageDTO.getQuantity() == null || storageDTO.getQuantity() < 0) {
			throw new RuntimeException("Quantity cannot be null or negative");
		}
		storage.setQuantity(storageDTO.getQuantity());
		storage.setCreateDate(LocalDate.parse(storageDTO.getCreateDate(), dateTimeFormatter));
		storage.setUpdateDate(LocalDate.parse(storageDTO.getUpdateDate(), dateTimeFormatter));
		if (storageDTO.getProductId() == null) {
			throw new RuntimeException("Product ID cannot be null");
		}
		Product product = productRepository.findById(storageDTO.getProductId())
				.orElseThrow(() -> new RuntimeException("Product not found"));
		storage.setProduct(product);
		Storage savedStorage = storageRepository.save(storage);
		return new StorageDTO(savedStorage);
	}

	@Override
	public Storage updateStorage(long idImport, StorageDTO storageDTO) {

		Optional<Storage> optionalStorage = storageRepository.findById(idImport);
		if (!optionalStorage.isPresent()) {
			throw new RuntimeException("Storage with ID " + idImport + " not found");
		}

		Storage storage = optionalStorage.get();
		storage.setUsers(storageDTO.getUsers());
		storage.setQuantity(storageDTO.getQuantity() != null ? storageDTO.getQuantity() : 0);
		if (storageDTO.getCreateDate() != null) {
			storage.setCreateDate(LocalDate.parse(storageDTO.getCreateDate(), dateTimeFormatter));
		}
		if (storageDTO.getUpdateDate() != null) {
			storage.setUpdateDate(LocalDate.parse(storageDTO.getUpdateDate(), dateTimeFormatter));
		}
		if (storageDTO.getProductId() != null) {
			Long productId = storageDTO.getProductId();
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
			storage.setProduct(product);
		}
		return storageRepository.save(storage);
	}

	@Override
	public Storage getImportById(long idImport) {
		Optional<Storage> result = storageRepository.findById(idImport);
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new RuntimeException("Product not found with ID: " + idImport);
		}
	}

	@Override
	public void deleteStorageById(long idImport) {
		Storage storage = storageRepository.findById(idImport)
				.orElseThrow(() -> new RuntimeException("Storage not found with id: " + idImport));
		storageRepository.deleteById(idImport);
	}

	@Override
	public List<StorageDTO> getLowStockProducts(int threshold) {

		List<Object[]> results = storageRepository.getLowStockProducts(threshold);

		List<StorageDTO> dtoList = new ArrayList<>();

		for (Object[] row : results) {
			StorageDTO dto = new StorageDTO();

			dto.setProductId(((Number) row[0]).longValue());
			dto.setProduct_name((String) row[1]);
			dto.setQuantity(((Number) row[2]).intValue());

			dtoList.add(dto);
		}

		return dtoList;
	}
}
