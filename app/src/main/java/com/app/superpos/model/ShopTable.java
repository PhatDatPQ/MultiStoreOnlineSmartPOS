package com.app.superpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShopTable {


    public String getShop_gst_no() {
        return shop_gst_no;
    }

    public void setShop_gst_no(String shop_gst_no) {
        this.shop_gst_no = shop_gst_no;
    }

    @SerializedName("shop_gst_no")
    @Expose
    private String shop_gst_no;

    public String getTax_No() {
        return Tax_No;
    }

    public void setTax_No(String tax_No) {
        Tax_No = tax_No;
    }

    @SerializedName("Tax_No")
    @Expose
    private String Tax_No;

    public String getShop_logo() {
        return shop_logo;
    }

    public void setShop_logo(String shop_logo) {
        this.shop_logo = shop_logo;
    }

    @SerializedName("shop_logo")
    @Expose
    private String shop_logo;


    public String getShop_currency() {
        return shop_currency;
    }

    public void setShop_currency(String shop_currency) {
        this.shop_currency = shop_currency;
    }

    @SerializedName("shop_currency")
    @Expose
    private String shop_currency;

    public String getShop_state() {
        return shop_state;
    }

    public void setShop_state(String shop_state) {
        this.shop_state = shop_state;
    }

    @SerializedName("shop_state")
    @Expose
    private String shop_state;

    public String getShop_city() {
        return shop_city;
    }

    public void setShop_city(String shop_city) {
        this.shop_city = shop_city;
    }

    @SerializedName("shop_city")
    @Expose
    private String shop_city;

    public String getShop_fssai_no() {
        return shop_fssai_no;
    }

    public void setShop_fssai_no(String shop_fssai_no) {
        this.shop_fssai_no = shop_fssai_no;
    }

    @SerializedName("shop_fssai_no")
    @Expose
    private String shop_fssai_no;

    @SerializedName("message")
    @Expose
    private String message;

    public String getShop_owner_id() {
        return shop_owner_id;
    }

    public void setShop_owner_id(String shop_owner_id) {
        this.shop_owner_id = shop_owner_id;
    }

    @SerializedName("shop_owner_id")
    @Expose
    private String shop_owner_id;


    @SerializedName("shop_id ")
    @Expose
    private String shopId;
    @SerializedName("shop_name")
    @Expose
    private String shopName;
    @SerializedName("shop_contact")
    @Expose
    private String shopContact;
    @SerializedName("shop_email")
    @Expose
    private String shopEmail;
    @SerializedName("shop_address")
    @Expose
    private String shopAddress;
    @SerializedName("shop_status")
    @Expose
    private String shopStatus;
    @SerializedName("shop_owner_id")
    @Expose
    private String shopOwnerId;
    @SerializedName("shop_gst_no")
    @Expose
    private String shopGstNo;
    @SerializedName("shop_fssai_no")
    @Expose
    private String shopFssaiNo;
    @SerializedName("shop_city")
    @Expose
    private String shopCity;
    @SerializedName("shop_state")
    @Expose
    private String shopState;
    @SerializedName("shop_currency")
    @Expose
    private String shopCurrency;
    @SerializedName("tax")
    @Expose
    private String tax;
    @SerializedName("Tax_No")
    @Expose
    private String taxNo;
    @SerializedName("shop_logo")
    @Expose
    private String shopLogo;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopContact() {
        return shopContact;
    }

    public void setShopContact(String shopContact) {
        this.shopContact = shopContact;
    }

    public String getShopEmail() {
        return shopEmail;
    }

    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopStatus() {
        return shopStatus;
    }

    public void setShopStatus(String shopStatus) {
        this.shopStatus = shopStatus;
    }

    public String getShopOwnerId() {
        return shopOwnerId;
    }

    public void setShopOwnerId(String shopOwnerId) {
        this.shopOwnerId = shopOwnerId;
    }

    public String getShopGstNo() {
        return shopGstNo;
    }

    public void setShopGstNo(String shopGstNo) {
        this.shopGstNo = shopGstNo;
    }

    public String getShopFssaiNo() {
        return shopFssaiNo;
    }

    public void setShopFssaiNo(String shopFssaiNo) {
        this.shopFssaiNo = shopFssaiNo;
    }

    public String getShopCity() {
        return shopCity;
    }

    public void setShopCity(String shopCity) {
        this.shopCity = shopCity;
    }

    public String getShopState() {
        return shopState;
    }

    public void setShopState(String shopState) {
        this.shopState = shopState;
    }

    public String getShopCurrency() {
        return shopCurrency;
    }

    public void setShopCurrency(String shopCurrency) {
        this.shopCurrency = shopCurrency;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getTaxNo() {
        return taxNo;
    }

    public void setTaxNo(String taxNo) {
        this.taxNo = taxNo;
    }

    public String getShopLogo() {
        return shopLogo;
    }

    public void setShopLogo(String shopLogo) {
        this.shopLogo = shopLogo;
    }

}