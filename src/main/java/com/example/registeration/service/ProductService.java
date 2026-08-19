package com.example.registeration.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.registeration.dto.CreateProductRequest;
import com.example.registeration.dto.UpdateProductRequest;
import com.example.registeration.dto.ProductListResponse;
import com.example.registeration.dto.ProductDetailsResponse;
import com.example.registeration.entity.Brand;
import com.example.registeration.entity.Category;
import com.example.registeration.entity.Product;
import com.example.registeration.entity.UnitOfMeasurement;
import com.example.registeration.entity.Warehouse;
import com.example.registeration.exception.ResourceNotFoundException;
import com.example.registeration.repository.BrandRepository;
import com.example.registeration.repository.CategoryRepository;
import com.example.registeration.repository.ProductRepository;
import com.example.registeration.repository.UnitOfMeasurementRepository;
import com.example.registeration.repository.UserRepository;
import com.example.registeration.repository.WarehouseRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitOfMeasurementRepository unitOfMeasurementRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            UnitOfMeasurementRepository unitOfMeasurementRepository,
            WarehouseRepository warehouseRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitOfMeasurementRepository = unitOfMeasurementRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
    }

    private Category getOrCreateCategory(UUID id) {
        return categoryRepository.findById(id).orElseGet(() -> {
            Category category = new Category(id, "Electronics", "Electronic gadgets and devices");
            return categoryRepository.save(category);
        });
    }

    private Brand getOrCreateBrand(UUID id) {
        return brandRepository.findById(id).orElseGet(() -> {
            Brand brand = new Brand(id, "Logitech", "Peripherals");
            return brandRepository.save(brand);
        });
    }

    private UnitOfMeasurement getOrCreateUnitOfMeasurement(UUID id) {
        return unitOfMeasurementRepository.findById(id).orElseGet(() -> {
            UnitOfMeasurement uom = new UnitOfMeasurement(id, "Pieces", "pcs");
            return unitOfMeasurementRepository.save(uom);
        });
    }

    private Warehouse getOrCreateWarehouse(UUID id) {
        return warehouseRepository.findById(id).orElseGet(() -> {
            Warehouse wh = new Warehouse(id, "Main Warehouse", "123 Main St", "active");
            return warehouseRepository.save(wh);
        });
    }

    @Transactional
    public ProductDetailsResponse createProduct(CreateProductRequest request, UUID createdByUserId) {
        if (!userRepository.existsById(createdByUserId)) {
            throw new ResourceNotFoundException("User not found with id: " + createdByUserId);
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setProductCode(request.getProductCode());
        product.setBarcode(request.getBarcode());
        product.setProductType(request.getProductType());

        // Resolve lookups
        product.setCategory(getOrCreateCategory(request.getCategory()));
        product.setBrand(getOrCreateBrand(request.getBrand()));
        product.setUnitOfMeasurement(getOrCreateUnitOfMeasurement(request.getUnitOfMeasurement()));
        product.setWarehouse(getOrCreateWarehouse(request.getWarehouse()));

        product.setHsnSacCode(request.getHsnSacCode());
        product.setGstTaxRate(request.getGstTaxRate() != null ? request.getGstTaxRate() : BigDecimal.ZERO);
        product.setSellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : BigDecimal.ZERO);
        product.setPurchasePrice(request.getPurchasePrice() != null ? request.getPurchasePrice() : BigDecimal.ZERO);

        boolean track = request.getTrackInventory() != null && request.getTrackInventory();
        product.setTrackInventory(track);

        BigDecimal openStock = request.getOpeningStock() != null ? request.getOpeningStock() : BigDecimal.ZERO;
        product.setOpeningStock(openStock);
        product.setCurrentStock(openStock); // current stock initially equals opening stock
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : BigDecimal.ZERO);

        product.setDescription(request.getDescription());
        product.setStatus("active");

        // Audit/System fields
        product.setCreatedBy(createdByUserId);
        product.setOrganization(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")); // default org UUID
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsDeleted(false);
        product.setDeletedAt(null);

        Product saved = productRepository.save(product);
        return mapToProductDetailsResponse(saved, createdByUserId);
    }

    public ProductDetailsResponse getProductDetails(UUID id, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getIsDeleted() != null && product.getIsDeleted()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        return mapToProductDetailsResponse(product, product.getCreatedBy());
    }

    public Page<ProductListResponse> listProducts(
            String search,
            String productType,
            UUID categoryId,
            UUID brandId,
            String status,
            Boolean trackInventory,
            String stockAvailability,
            String startDateStr,
            String endDateStr,
            String ordering,
            int page,
            int pageSize,
            UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Sort sort = getSort(ordering);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Specification<Product> spec = buildSpecification(
                search, productType, categoryId, brandId, status, trackInventory, stockAvailability, startDateStr, endDateStr
        );

        Page<Product> products = productRepository.findAll(spec, pageable);

        List<ProductListResponse> content = products.getContent().stream()
                .map(this::mapToProductListResponse)
                .toList();

        return new PageImpl<>(content, pageable, products.getTotalElements());
    }

    @Transactional
    public ProductDetailsResponse updateProduct(UUID id, UpdateProductRequest request, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getIsDeleted() != null && product.getIsDeleted()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getProductCode() != null) product.setProductCode(request.getProductCode());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());
        if (request.getProductType() != null) product.setProductType(request.getProductType());

        if (request.getCategory() != null) product.setCategory(getOrCreateCategory(request.getCategory()));
        if (request.getBrand() != null) product.setBrand(getOrCreateBrand(request.getBrand()));
        if (request.getUnitOfMeasurement() != null) product.setUnitOfMeasurement(getOrCreateUnitOfMeasurement(request.getUnitOfMeasurement()));
        if (request.getWarehouse() != null) product.setWarehouse(getOrCreateWarehouse(request.getWarehouse()));

        if (request.getHsnSacCode() != null) product.setHsnSacCode(request.getHsnSacCode());
        if (request.getGstTaxRate() != null) product.setGstTaxRate(request.getGstTaxRate());
        if (request.getSellingPrice() != null) product.setSellingPrice(request.getSellingPrice());
        if (request.getPurchasePrice() != null) product.setPurchasePrice(request.getPurchasePrice());

        if (request.getTrackInventory() != null) product.setTrackInventory(request.getTrackInventory());
        if (request.getOpeningStock() != null) {
            BigDecimal prevOpening = product.getOpeningStock() != null ? product.getOpeningStock() : BigDecimal.ZERO;
            product.setOpeningStock(request.getOpeningStock());
            // Adjust current stock relatively
            BigDecimal diff = request.getOpeningStock().subtract(prevOpening);
            BigDecimal current = product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO;
            product.setCurrentStock(current.add(diff));
        }
        if (request.getReorderLevel() != null) product.setReorderLevel(request.getReorderLevel());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        return mapToProductDetailsResponse(saved, saved.getCreatedBy());
    }

    @Transactional
    public void deleteProduct(UUID id, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getIsDeleted() != null && product.getIsDeleted()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public ProductDetailsResponse restoreProduct(UUID id, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(false);
        product.setDeletedAt(null);
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return mapToProductDetailsResponse(saved, saved.getCreatedBy());
    }

    @Transactional
    public ProductDetailsResponse toggleProductStatus(UUID id, boolean active, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getIsDeleted() != null && product.getIsDeleted()) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        product.setStatus(active ? "active" : "inactive");
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return mapToProductDetailsResponse(saved, saved.getCreatedBy());
    }

    @Transactional
    public int bulkDeleteProducts(List<UUID> ids, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Product> products = productRepository.findAllById(ids);
        int count = 0;
        for (Product product : products) {
            if (product.getIsDeleted() == null || !product.getIsDeleted()) {
                product.setIsDeleted(true);
                product.setDeletedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                count++;
            }
        }
        if (count > 0) {
            productRepository.saveAll(products);
        }
        return count;
    }

    @Transactional
    public int bulkToggleProductStatus(List<UUID> ids, boolean active, UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Product> products = productRepository.findAllById(ids);
        int count = 0;
        String targetStatus = active ? "active" : "inactive";
        for (Product product : products) {
            if ((product.getIsDeleted() == null || !product.getIsDeleted()) && !targetStatus.equals(product.getStatus())) {
                product.setStatus(targetStatus);
                product.setUpdatedAt(LocalDateTime.now());
                count++;
            }
        }
        if (count > 0) {
            productRepository.saveAll(products);
        }
        return count;
    }


    private ProductListResponse mapToProductListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setProductCode(product.getProductCode());
        response.setBarcode(product.getBarcode());
        response.setProductType(product.getProductType());

        if (product.getCategory() != null) {
            response.setCategory(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            response.setBrand(product.getBrand().getId());
            response.setBrandName(product.getBrand().getName());
        }
        if (product.getUnitOfMeasurement() != null) {
            response.setUnitOfMeasurement(product.getUnitOfMeasurement().getId());
            response.setUnitAbbreviation(product.getUnitOfMeasurement().getAbbreviation());
        }
        if (product.getWarehouse() != null) {
            response.setWarehouse(product.getWarehouse().getId());
            response.setWarehouseName(product.getWarehouse().getName());
        }

        response.setSellingPrice(product.getSellingPrice() != null ? String.format("%.2f", product.getSellingPrice()) : "0.00");
        response.setPurchasePrice(product.getPurchasePrice() != null ? String.format("%.2f", product.getPurchasePrice()) : "0.00");
        response.setCurrentStock(product.getCurrentStock() != null ? String.format("%.2f", product.getCurrentStock()) : "0.00");
        response.setTrackInventory(product.getTrackInventory());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());

        return response;
    }

    private ProductDetailsResponse mapToProductDetailsResponse(Product product, UUID createdByUserId) {
        ProductDetailsResponse response = new ProductDetailsResponse();
        response.setId(product.getId());

        if (product.getCategory() != null) {
            response.setCategory(new ProductDetailsResponse.CategoryInfo(
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCategory().getDescription()
            ));
        }

        if (product.getBrand() != null) {
            response.setBrand(new ProductDetailsResponse.BrandInfo(
                    product.getBrand().getId(),
                    product.getBrand().getName(),
                    product.getBrand().getDescription()
            ));
        }

        if (product.getUnitOfMeasurement() != null) {
            response.setUnitOfMeasurement(new ProductDetailsResponse.UnitInfo(
                    product.getUnitOfMeasurement().getId(),
                    product.getUnitOfMeasurement().getName(),
                    product.getUnitOfMeasurement().getAbbreviation()
            ));
        }

        if (product.getWarehouse() != null) {
            response.setWarehouse(new ProductDetailsResponse.WarehouseInfo(
                    product.getWarehouse().getId(),
                    product.getWarehouse().getName(),
                    product.getWarehouse().getAddress(),
                    product.getWarehouse().getStatus()
            ));
        }

        // created_by resolved to user email
        String userEmail = userRepository.findById(createdByUserId)
                .map(user -> user.getEmail())
                .orElse("unknown@companya.com");
        response.setCreatedBy(userEmail);

        response.setOrganization(product.getOrganization() != null ? product.getOrganization().toString() : "org-uuid-here");
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setProductCode(product.getProductCode());
        response.setBarcode(product.getBarcode());
        response.setProductType(product.getProductType());
        response.setHsnSacCode(product.getHsnSacCode());

        response.setGstTaxRate(product.getGstTaxRate() != null ? String.format("%.2f", product.getGstTaxRate()) : "0.00");
        response.setSellingPrice(product.getSellingPrice() != null ? String.format("%.2f", product.getSellingPrice()) : "0.00");
        response.setPurchasePrice(product.getPurchasePrice() != null ? String.format("%.2f", product.getPurchasePrice()) : "0.00");

        response.setTrackInventory(product.getTrackInventory());
        response.setOpeningStock(product.getOpeningStock() != null ? String.format("%.2f", product.getOpeningStock()) : "0.00");
        response.setReorderLevel(product.getReorderLevel() != null ? String.format("%.2f", product.getReorderLevel()) : "0.00");
        response.setCurrentStock(product.getCurrentStock() != null ? String.format("%.2f", product.getCurrentStock()) : "0.00");

        response.setDescription(product.getDescription());
        response.setStatus(product.getStatus());

        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setIsDeleted(product.getIsDeleted());
        response.setDeletedAt(product.getDeletedAt());

        return response;
    }

    private Sort getSort(String ordering) {
        if (ordering == null || ordering.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        boolean desc = ordering.startsWith("-");
        String field = desc ? ordering.substring(1) : ordering;

        String mappedField = switch (field) {
            case "name" -> "name";
            case "sku" -> "sku";
            case "selling_price" -> "sellingPrice";
            case "purchase_price" -> "purchasePrice";
            case "current_stock" -> "currentStock";
            case "created_at" -> "createdAt";
            default -> "createdAt";
        };

        return Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, mappedField);
    }

    private Specification<Product> buildSpecification(
            String search,
            String productType,
            UUID categoryId,
            UUID brandId,
            String status,
            Boolean trackInventory,
            String stockAvailability,
            String startDateStr,
            String endDateStr) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted products
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";

                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                Join<Product, Brand> brandJoin = root.join("brand", JoinType.LEFT);

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("sku")), searchPattern),
                        cb.like(cb.lower(root.get("barcode")), searchPattern),
                        cb.like(cb.lower(categoryJoin.get("name")), searchPattern),
                        cb.like(cb.lower(brandJoin.get("name")), searchPattern)
                ));
            }

            if (productType != null && !productType.isEmpty()) {
                predicates.add(cb.equal(root.get("productType"), productType));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (trackInventory != null) {
                predicates.add(cb.equal(root.get("trackInventory"), trackInventory));
            }

            if (stockAvailability != null && !stockAvailability.isEmpty()) {
                if ("in_stock".equalsIgnoreCase(stockAvailability)) {
                    predicates.add(cb.greaterThan(root.get("currentStock"), root.get("reorderLevel")));
                } else if ("out_of_stock".equalsIgnoreCase(stockAvailability)) {
                    predicates.add(cb.le(root.get("currentStock"), BigDecimal.ZERO));
                } else if ("low_stock".equalsIgnoreCase(stockAvailability)) {
                    predicates.add(cb.and(
                            cb.le(root.get("currentStock"), root.get("reorderLevel")),
                            cb.greaterThan(root.get("currentStock"), BigDecimal.ZERO)
                    ));
                }
            }

            if (startDateStr != null && !startDateStr.isEmpty()) {
                try {
                    java.time.LocalDate startDate = java.time.LocalDate.parse(startDateStr);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
                } catch (Exception e) {
                    // Ignore date parse errors
                }
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                try {
                    java.time.LocalDate endDate = java.time.LocalDate.parse(endDateStr);
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
                } catch (Exception e) {
                    // Ignore date parse errors
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
