package com.example.registeration.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    @JsonProperty("product_code")
    private String productCode;

    private String barcode;

    @NotBlank(message = "Product type is required")
    @JsonProperty("product_type")
    private String productType;

    @NotNull(message = "Category ID is required")
    private UUID category;

    @NotNull(message = "Brand ID is required")
    private UUID brand;

    @NotNull(message = "Unit of measurement ID is required")
    @JsonProperty("unit_of_measurement")
    private UUID unitOfMeasurement;

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouse;

    @JsonProperty("hsn_sac_code")
    private String hsnSacCode;

    @JsonProperty("gst_tax_rate")
    private BigDecimal gstTaxRate;

    @JsonProperty("selling_price")
    private BigDecimal sellingPrice;

    @JsonProperty("purchase_price")
    private BigDecimal purchasePrice;

    @JsonProperty("track_inventory")
    private Boolean trackInventory = true;

    @JsonProperty("opening_stock")
    private BigDecimal openingStock;

    @JsonProperty("reorder_level")
    private BigDecimal reorderLevel;

    private String description;

    public CreateProductRequest() {
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

    public UUID getCategory() {
        return category;
    }

    public void setCategory(UUID category) {
        this.category = category;
    }

    public UUID getBrand() {
        return brand;
    }

    public void setBrand(UUID brand) {
        this.brand = brand;
    }

    public UUID getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(UUID unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public UUID getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(UUID warehouse) {
        this.warehouse = warehouse;
    }

    public String getHsnSacCode() {
        return hsnSacCode;
    }

    public void setHsnSacCode(String hsnSacCode) {
        this.hsnSacCode = hsnSacCode;
    }

    public BigDecimal getGstTaxRate() {
        return gstTaxRate;
    }

    public void setGstTaxRate(BigDecimal gstTaxRate) {
        this.gstTaxRate = gstTaxRate;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Boolean getTrackInventory() {
        return trackInventory;
    }

    public void setTrackInventory(Boolean trackInventory) {
        this.trackInventory = trackInventory;
    }

    public BigDecimal getOpeningStock() {
        return openingStock;
    }

    public void setOpeningStock(BigDecimal openingStock) {
        this.openingStock = openingStock;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
