package com.example.registeration.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDetailsResponse {

    private UUID id;
    private CategoryInfo category;
    private BrandInfo brand;

    @JsonProperty("unit_of_measurement")
    private UnitInfo unitOfMeasurement;

    private WarehouseInfo warehouse;

    @JsonProperty("created_by")
    private String createdBy;

    private String organization;
    private String name;
    private String sku;

    @JsonProperty("product_code")
    private String productCode;

    private String barcode;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty("hsn_sac_code")
    private String hsnSacCode;

    @JsonProperty("gst_tax_rate")
    private String gstTaxRate;

    @JsonProperty("selling_price")
    private String sellingPrice;

    @JsonProperty("purchase_price")
    private String purchasePrice;

    @JsonProperty("track_inventory")
    private Boolean trackInventory;

    @JsonProperty("opening_stock")
    private String openingStock;

    @JsonProperty("reorder_level")
    private String reorderLevel;

    @JsonProperty("current_stock")
    private String currentStock;

    private String description;
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    public ProductDetailsResponse() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
    }

    public BrandInfo getBrand() {
        return brand;
    }

    public void setBrand(BrandInfo brand) {
        this.brand = brand;
    }

    public UnitInfo getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UnitInfo unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public WarehouseInfo getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseInfo warehouse) {
        this.warehouse = warehouse;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getHsnSacCode() {
        return hsnSacCode;
    }

    public void setHsnSacCode(String hsnSacCode) {
        this.hsnSacCode = hsnSacCode;
    }

    public String getGstTaxRate() {
        return gstTaxRate;
    }

    public void setGstTaxRate(String gstTaxRate) {
        this.gstTaxRate = gstTaxRate;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(String sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Boolean getTrackInventory() {
        return trackInventory;
    }

    public void setTrackInventory(Boolean trackInventory) {
        this.trackInventory = trackInventory;
    }

    public String getOpeningStock() {
        return openingStock;
    }

    public void setOpeningStock(String openingStock) {
        this.openingStock = openingStock;
    }

    public String getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(String reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(String currentStock) {
        this.currentStock = currentStock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Static nested class for Category
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String description;

        public CategoryInfo() {}

        public CategoryInfo(UUID id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Static nested class for Brand
    public static class BrandInfo {
        private UUID id;
        private String name;
        private String description;

        public BrandInfo() {}

        public BrandInfo(UUID id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Static nested class for UnitOfMeasurement
    public static class UnitInfo {
        private UUID id;
        private String name;
        private String abbreviation;

        public UnitInfo() {}

        public UnitInfo(UUID id, String name, String abbreviation) {
            this.id = id;
            this.name = name;
            this.abbreviation = abbreviation;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAbbreviation() { return abbreviation; }
        public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
    }

    // Static nested class for Warehouse
    public static class WarehouseInfo {
        private UUID id;
        private String name;
        private String address;
        private String status;

        public WarehouseInfo() {}

        public WarehouseInfo(UUID id, String name, String address, String status) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.status = status;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
