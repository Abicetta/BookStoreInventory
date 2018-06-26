package com.abicetta.bookstoreinventory;

public class Suppliers {
    private String supId;
    private String supName;
    private String supPhone;

    public String getId() {
        return supId;
    }

    public String getName() {
        return supName;
    }

    public String getPhone() {
        return supPhone;
    }

    public void setId(String supplierId) {
        this.supId = supplierId;
    }

    public void setName(String supplierName) {
        this.supName = supplierName;
    }

    public void setPhone(String supplierPhone) {
        this.supPhone = supplierPhone;
    }

}
