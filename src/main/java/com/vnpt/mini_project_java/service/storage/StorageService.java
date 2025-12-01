package com.vnpt.mini_project_java.service.storage;

import java.util.List;

import com.vnpt.mini_project_java.dto.StorageDTO;
import com.vnpt.mini_project_java.entity.Storage;

public interface StorageService {

    Storage findQuatityProduct(long product_id);

    List<StorageDTO> getAllStorageDTO();

	<S extends Storage> S save(S entity);

	StorageDTO createStorage(StorageDTO dto);

	Storage updateStorage(long idImport, StorageDTO storageDTO);

	Storage getImportById(long idImport);

	void deleteStorageById(long id);

    List<StorageDTO> getLowStockProducts(int threshold);
}
