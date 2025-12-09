package com.vnpt.mini_project_java.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_info", schema = "public")
@Getter
@Setter
@ToString(exclude = {"account", "orderDetails"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderID;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "order_import")
    private LocalDate orderDateImport;

    @Column(name = "status", columnDefinition = "nvarchar(50)")
    private String status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "order_total")
    private double orderTotal;

    @Column(name = "txn_ref")
    private String txnRef;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "shipping_address", columnDefinition = "nvarchar(255)", nullable = false)
    private String shippingAddress;

    @Column(name = "note", columnDefinition = "nvarchar(500)")
    private String note;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    @JsonManagedReference
    @OneToMany(mappedBy ="order" ,fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<OrderDetail> orderDetails = new HashSet<>();

    public Account getVendor(){
        return account;
    }

    public void setVendor(Account account){
        this.account = account;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", orderDateImport=" + orderDateImport +
                ", orderTotal=" + orderTotal +
                ", accountID=" + (account != null ? account.getAccountID() : "null") +
                ", orderDetailsSize=" + (orderDetails != null ? orderDetails.size() : "null") +
                '}';
    }

    public enum UpdateStatus{
        SUCCESS,
        ORDERID_NOT_FOUND,
        INSUFFICIENT_QUANTITY,
        STORAGE_NOT_FOUND
    }
}
