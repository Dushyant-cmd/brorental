package com.brorental.brorental.models;

import java.util.Objects;

public class RentItemModel {
    public String id;
    private String address, adsImageUrl, broPartnerId, category, docId, extraCharge, ownerDescription, perHourCharge, state, status, vehicleNumber, timings, year, productHealth, productColor, name;

    public RentItemModel(String id, String address, String adsImageUrl, String broPartnerId, String category, String docId, String extraCharge, String ownerDescription, String perHourCharge, String state, String status, String vehicleNumber, String timings, String year, String productHealth, String productColor, String name) {
        this.id = id;
        this.address = address;
        this.adsImageUrl = adsImageUrl;
        this.broPartnerId = broPartnerId;
        this.category = category;
        this.docId = docId;
        this.extraCharge = extraCharge;
        this.ownerDescription = ownerDescription;
        this.perHourCharge = perHourCharge;
        this.state = state;
        this.status = status;
        this.vehicleNumber = vehicleNumber;
        this.timings = timings;
        this.year = year;
        this.productHealth = productHealth;
        this.productColor = productColor;
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getProductHealth() {
        return productHealth;
    }

    public void setProductHealth(String productHealth) {
        this.productHealth = productHealth;
    }

    public String getProductColor() {
        return productColor;
    }

    public void setProductColor(String productColor) {
        this.productColor = productColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdsImageUrl() {
        return adsImageUrl;
    }

    public void setAdsImageUrl(String adsImageUrl) {
        this.adsImageUrl = adsImageUrl;
    }

    public String getBroPartnerId() {
        return broPartnerId;
    }

    public void setBroPartnerId(String broPartnerId) {
        this.broPartnerId = broPartnerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getExtraCharge() {
        return extraCharge;
    }

    public void setExtraCharge(String extraCharge) {
        this.extraCharge = extraCharge;
    }

    public String getOwnerDescription() {
        return ownerDescription;
    }

    public void setOwnerDescription(String ownerDescription) {
        this.ownerDescription = ownerDescription;
    }

    public String getPerHourCharge() {
        return perHourCharge;
    }

    public void setPerHourCharge(String perHourCharge) {
        this.perHourCharge = perHourCharge;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getTimings() {
        return timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentItemModel that = (RentItemModel) o;
        return id == that.id && Objects.equals(category, that.category) && Objects.equals(timings, that.timings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, timings);
    }
}
