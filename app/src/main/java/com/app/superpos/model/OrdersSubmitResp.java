
package com.app.superpos.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrdersSubmitResp {

    @SerializedName("Message")
    @Expose
    private String message;
    @SerializedName("invoice_id")
    @Expose
    private Integer invoiceId;
    @SerializedName("orderlist")
    @Expose
    private OrderList orderlist;
    @SerializedName("OrderDteails")
    @Expose
    private OrderDetails orderDteails;
    @SerializedName("Products")
    @Expose
    private List<Product> products = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public OrderList getOrderlist() {
        return orderlist;
    }

    public void setOrderlist(OrderList orderlist) {
        this.orderlist = orderlist;
    }

    public OrderDetails getOrderDteails() {
        return orderDteails;
    }

    public void setOrderDteails(OrderDetails orderDteails) {
        this.orderDteails = orderDteails;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}
