package com.vnpt.mini_project_java.service.product;

import com.vnpt.mini_project_java.dto.*;
import com.vnpt.mini_project_java.entity.*;
import com.vnpt.mini_project_java.respository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.vnpt.mini_project_java.spec.ProductSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final CategoryRepository categoryRepository;

    @Autowired
    private final TrademarkReopsitory trademarkReopsitory;

    @Autowired
    private final ProductDetailRepository  productDetailRepository;

    @Autowired
    private final ProductVersionRepository productVersionRepository;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public ProductServiceImpl(ProductRepository productRepository, TrademarkReopsitory trademarkReopsitory,
                              CategoryRepository categoryRepository, ProductDetailRepository  productDetailRepository, ProductVersionRepository productVersionRepository) {
        super();
        this.productRepository = productRepository;
        this.trademarkReopsitory = trademarkReopsitory;
        this.categoryRepository = categoryRepository;
        this.productDetailRepository = productDetailRepository;
        this.productVersionRepository = productVersionRepository;
    }

    @Override
    public List<ProductDTO> getAllProductDTO() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductDTO::new).collect(Collectors.toList());
    }

    @Override
    public List<CompareProductDTO> getAllCompare(){
        List<Product> products = productRepository.findAll();
        return products.stream().map(CompareProductDTO:: new).collect(Collectors.toList());
    }
    
    @Override
	public Page<Product> findAll(Specification<Product> spec, Pageable pageable) {
        try {
            return productRepository.findAll(spec, pageable);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while searching products", e);
        }
    }
    
    @Override
    public ProductDTO createProduct(ProductDTO dto, MultipartFile image) {

        if (productRepository.existsByproductNameIgnoreCase(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên sản phẩm '" + dto.getName() + "' đã tồn tại!");
        }
        Long categoryId = dto.getCategoryID();

        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        Long tradeId = dto.getTradeID();
        if (tradeId == null) {
            throw new RuntimeException("");
        }
        Trademark trademark = trademarkReopsitory.findById(tradeId).
                orElseThrow(() -> new RuntimeException("Trade mark not found with id: " + tradeId));

        String fileName = image.getOriginalFilename();
        Path uploadDir = Paths.get("src/main/resources/static/images");
        Path filePath = uploadDir.resolve(fileName);

        try {
            image.transferTo(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save image file", ex);
        }

        String imagePath = fileName;

        Product product = new Product();
        product.setProductID(dto.getId());
        product.setImage(imagePath);
        product.setProductName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDateProduct(LocalDate.parse(dto.getDate_product(), dateTimeFormatter));
        product.setCategory(category);
        product.setTrademark(trademark);
        Product savedProduct = productRepository.save(product);
        return new ProductDTO(savedProduct);
    }

    @Override
    public Product updateProduct(long productID, ProductDTO dto,MultipartFile image) {
        Optional<Product> optionalProduct = productRepository.findById(productID);
        if (!optionalProduct.isPresent()) {
            throw new RuntimeException("Product with ID " + productID + " not found");
        }
        Product product = optionalProduct.get();
        product.setProductName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setDateProduct(LocalDate.parse(dto.getDate_product(), dateTimeFormatter));
        product.setPrice(dto.getPrice());
        if (image != null && !image.isEmpty()) {
            String fileName = image.getOriginalFilename();
            Path uploadDir = Paths.get("src/main/resources/static/images");
            Path filePath = uploadDir.resolve(fileName);
            try {
                image.transferTo(filePath);
                String imagePath = fileName;
                product.setImage(imagePath);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to save image file", ex);
            }
        }
        if (dto.getCategoryID() != null) {
            Long categoryId = dto.getCategoryID();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            product.setCategory(category);
        }
        if (dto.getTradeID() != null) {
            Long tradeId = dto.getTradeID();
            Trademark trademark = trademarkReopsitory.findById(tradeId)
                    .orElseThrow(() -> new RuntimeException("Trademark not found with id: " + tradeId));
            product.setTrademark(trademark);
        }
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(long productID) {
        Optional<Product> result = productRepository.findById(productID);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new RuntimeException("Product not found with ID: " + productID);
        }
    }

    @Override
    public void deleteProductById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.deleteById(id);
    }

    @Override
    public Page<ProductDTO> getPaginatedProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, productPage.getTotalElements());
    }

    @Override
    public List<ProductDTO> getProductsByCategoryId(Long categoryID) {
        List<Product> products = productRepository.findByCategoryId(categoryID);
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getProductID());
            productDTO.setName(product.getProductName());
            productDTO.setDescription(product.getDescription());
            productDTO.setDate_product(product.getDateProduct().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            productDTO.setImageBase64(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTO.setTradeID(product.getTrademark().getTradeID());
            productDTO.setTradeName(product.getTrademark().getTradeName());
            productDTO.setCategoryID(product.getCategory().getCategoryID());
            productDTO.setCategoryname(product.getCategory().getCategoryName());
            productDTOS.add(productDTO);
        }
        return productDTOS;
    }

    @Override
    public List<Product> findBycategoryId(Long categoryID) {
        return productRepository.findByCategoryId(categoryID);
    }

    @Override
    public Optional<Product> findById(Long productID) {
        return productRepository.findById(productID);
    }

    @Override
    public void importProductsFromExcel(List<ProductDTO> productDTOs) {
        List<Product> products = new ArrayList<>();
        for (ProductDTO productDTO : productDTOs) {
            Product product = new Product();
            product.setProductName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setDateProduct(LocalDate.parse(productDTO.getDate_product()));
            product.setPrice(productDTO.getPrice());
            product.setCategory(product.getCategory());
            product.setImageBase64(productDTO.getImageBase64());
            Category category = new Category();
            category.setCategoryID(productDTO.getCategoryID());
            category.setCategoryName(productDTO.getCategoryname());
            product.setCategory(category);

            Trademark trademark = new Trademark();
            trademark.setTradeID(productDTO.getTradeID());
            trademark.setTradeName(productDTO.getTradeName());
            product.setTrademark(trademark);

            products.add(product);
        }
        productRepository.saveAll(products);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> showListCategoryByIdCategory(long categoryID) {
        return productRepository.showListProductByIdCategory(categoryID);
    }

    @Override
    public Product findByIdProduct(long productID) {
        Optional<Product> optionalProduct = productRepository.findById(productID);
        return optionalProduct.orElse(null);
    }

    @Override
    public List<Product> showListProductByIdCategoryFilter(long categoryID) {
        return productRepository.showListProductByIdCategoryFilter(categoryID);
    }

    @Override
    public List<Product> listProductNewBest() {
        return productRepository.listProductNewBest();
    }

    @Override
    public List<Product> listProductPriceDesc() {
        return productRepository.listProductPriceDesc();
    }

    @Override
    public List<Product> listProductPriceAsc() {
        return productRepository.listProductPriceAsc();
    }

    @Override
    public List<Product> searchProducts(ProductSearchCriteriaDTO criteria) {
        Specification<Product> spec = ProductSpecifications.searchByCriteria(criteria);
        return productRepository.findAll(spec);
    }

    @Override
    public List<Product> searchListProductByIdCategory(String productName) {
        return productRepository.searchListProductByIdCategory(productName);
    }

    @Override
    public Page<ProductDTO> getPaginatedProduct(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductDTO::new);
    }

    @Override
    public ProductDetailiDDTO getProductDetailWithVersionsDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));

        ProductDetailiDDTO dto = new ProductDetailiDDTO();

        dto.setId(product.getProductID());
        dto.setName(product.getProductName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                Path imagePath = Paths.get("src/main/resources/static/images/", product.getImage());
                byte[] imageBytes = Files.readAllBytes(imagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                dto.setImage(base64Image);
            } catch (IOException e) {
                e.printStackTrace();
                dto.setImage(null);
            }
        }
        dto.setDateProduct(product.getDateProduct());
        dto.setCategory(product.getCategory() != null ? product.getCategory().getCategoryName() : null);
        dto.setTrademark(product.getTrademark() != null ? product.getTrademark().getTradeName() : null);

        if (product.getProductDetails() != null && !product.getProductDetails().isEmpty()) {
            ProductDetail firstDetail = product.getProductDetails().iterator().next();
            dto.setDetail(new ProductDetailDTO(firstDetail));
        }

        if (product.getProductVersions() != null && !product.getProductVersions().isEmpty()) {
            List<ProductVersionDTO> versions = product.getProductVersions().stream()
                    .map(ProductVersionDTO::new)
                    .collect(Collectors.toList());
            dto.setVersions(versions);
        } else {
            dto.setVersions(Collections.emptyList());
        }

        return dto;
    }

    @Override
    public List<ProductDTO> getRelatedProducts(Long id) {
        Product current = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        Long categoryId = Optional.ofNullable(current.getCategory())
                .map(c -> c.getCategoryID())
                .orElse(null);

        if (categoryId == null) return Collections.emptyList();

        List<Product> related = productRepository
                .findTop4ByCategory_CategoryIDAndProductIDNot(categoryId, current.getProductID());

        return related.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }
}
