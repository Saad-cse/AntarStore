package com.project.antarstore.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Orders {
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Products products;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Users users;
	
	@Column(nullable = false)
	private String productName;
	private String description;
	
	@Column(nullable = false)
	private double productPrice;
	
	@Column(nullable = false)
	private double discount;
	
	@Column(nullable = false)
	private double finalPrice;
	
	@Column(nullable = false)
	private String color;
	
	@Column(nullable = false)
	private String size;
	private double shippingCharge;
	
	@Column(nullable = false)
	private double totalAmount;
	
	@Column(nullable = false)
	private String shippingAddress;
	@Column(nullable = false)
	private String pincode;
	
	@Column(nullable = false)
	private String contactNo;
	
	@Column(nullable = false)
	private String customerName; 
	
	@Column(nullable = false)
	private int quantity;
	
	// NOTE: intentionally NOT unique. One Razorpay order (one checkout) can contain
	// multiple cart lines, and each line becomes its own Orders row sharing this
	// same provider order id. Uniqueness previously here caused checkout to fail
	// with a constraint violation for any multi-item cart (see recovery audit F-03).
	@Column(nullable = false)
	private String orderId;
	
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
	

	private String paymentId;
	private String paymentSignature;
	private LocalDateTime orderedAt;
	private LocalDateTime cancelledAt;
	private LocalDateTime deliveredAt;
	
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Products getProducts() {
		return products;
	}

	public void setProducts(Products products) {
		this.products = products;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(double productPrice) {
		this.productPrice = productPrice;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getFinalPrice() {
		return finalPrice;
	}

	public void setFinalPrice(double finalPrice) {
		this.finalPrice = finalPrice;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public double getShippingCharge() {
		return shippingCharge;
	}

	public void setShippingCharge(double shippingCharge) {
		this.shippingCharge = shippingCharge;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentSignature() {
		return paymentSignature;
	}

	public void setPaymentSignature(String paymentSignature) {
		this.paymentSignature = paymentSignature;
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}

	public void setOrderedAt(LocalDateTime orderDate) {
		this.orderedAt = orderDate;
	}

	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public enum OrderStatus{
		Processing,Confirmed,Shipped,Out_For_Delivery,Delivered,Cancelled
	}
    
	public enum PaymentStatus{
		Pending,Success,Refunded,Cancelled
	}

	
	
}
