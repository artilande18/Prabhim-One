package com.example.registeration.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProductRequest {

    private String name;
    private String sku;

    @JsonProperty("product_code")
    private String productCode;

    private String barcode;

    @JsonProperty("product_type")
    private String productType;

    private UUID category;
    private UUID brand;

    @JsonProperty("unit_of_measurement")
    private UUID unitOfMeasurement;

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
    private Boolean trackInventory;

    @JsonProperty("opening_stock")
    private BigDecimal openingStock;

    @JsonProperty("reorder_level")
    private BigDecimal reorderLevel;

    private String description;
    private String status;

    public UpdateProductRequest() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
