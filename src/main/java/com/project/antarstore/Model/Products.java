package com.project.antarstore.Model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Products {
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false)
	 private String productName;
	@Column(nullable = false ,length = 1000)
     private String productDescription;
	@ManyToOne
     private Category category;

     private String brandName;
	

     private String gender;   //kids ,men,women
     
     
     //price details
     
     @Column(nullable = false)
     private double costPrice;
     
     @Column(nullable = false)
     private double sellingPrice;
     
     @Column(nullable = false)
     private int discount;
     
     @Column(nullable = false)
     private double finalprice;
    
     //product variants
     @Column(nullable = false,length = 500)
     private String colors; 
     
     @Column(nullable = false,length=500)
     private String sizes;
     
     
     //inventory
     @Column(nullable = false)
     private long quantity;
     
     
     private double shippingCharge;
     
     @Column(nullable = false)
     private String deliveryTime;  //2-5 days
     
     
     private boolean returnPolicy;
     
     @Column(nullable = false)
     private boolean visibility;
     
     private LocalDateTime addedAt;
     
     @ElementCollection
     private List<String> productImages;
     
     @Enumerated(EnumType.STRING)
     private ProductStatus status;
     
     public enum  ProductStatus {
    	 Available,Out_Of_Stock
     }

	 public long getId() {
		 return id;
	 }

	 public void setId(long id) {
		 this.id = id;
	 }

	 public String getProductName() {
		 return productName;
	 }

	 public void setProductName(String productName) {
		 this.productName = productName;
	 }

	 public String getProductDescription() {
		 return productDescription;
	 }

	 public void setProductDescription(String productDescription) {
		 this.productDescription = productDescription;
	 }

	 public Category getCategory() {
		 return category;
	 }

	 public void setCategory(Category category) {
		 this.category = category;
	 }

	 public String getBrandName() {
		 return brandName;
	 }

	 public void setBrandName(String brandName) {
		 this.brandName = brandName;
	 }

	 public String getGender() {
		 return gender;
	 }

	 public void setGender(String gender) {
		 this.gender = gender;
	 }

	 public double getCostPrice() {
		 return costPrice;
	 }

	 public void setCostPrice(double costPrice) {
		 this.costPrice = costPrice;
	 }

	 public double getSellingPrice() {
		 return sellingPrice;
	 }

	 public void setSellingPrice(double sellingPrice) {
		 this.sellingPrice = sellingPrice;
	 }

	 public int getDiscount() {
		 return discount;
	 }

	 public void setDiscount(int discount) {
		 this.discount = discount;
	 }

	 public double getFinalprice() {
		 return finalprice;
	 }

	 public void setFinalprice(double finalprice) {
		 this.finalprice = finalprice;
	 }

	 public String getColors() {
		 return colors;
	 }

	 public void setColors(String colors) {
		 this.colors = colors;
	 }

	 public String getSizes() {
		 return sizes;
	 }

	 public void setSizes(String sizes) {
		 this.sizes = sizes;
	 }

	 public long getQuantity() {
		 return quantity;
	 }

	 public void setQuantity(long quantity) {
		 this.quantity = quantity;
	 }

	 public double getShippingCharge() {
		 return shippingCharge;
	 }

	 public void setShippingCharge(double shippingCharge) {
		 this.shippingCharge = shippingCharge;
	 }

	 public String getDeliveryTime() {
		 return deliveryTime;
	 }

	 public void setDeliveryTime(String deliveryTime) {
		 this.deliveryTime = deliveryTime;
	 }

	 public boolean isReturnPolicy() {
		 return returnPolicy;
	 }

	 public void setReturnPolicy(boolean returnPolicy) {
		 this.returnPolicy = returnPolicy;
	 }

	 public boolean isVisibility() {
		 return visibility;
	 }

	 public void setVisibility(boolean visibility) {
		 this.visibility = visibility;
	 }

	 public LocalDateTime getAddedAt() {
		 return addedAt;
	 }

	 public void setAddedAt(LocalDateTime addedAt) {
		 this.addedAt = addedAt;
	 }

	 public List<String> getProductImages() {
		 return productImages;
	 }

	 public void setProductImages(List<String> productImages) {
		 this.productImages = productImages;
	 }

	 public ProductStatus getStatus() {
		 return status;
	 }

	 public void setStatus(ProductStatus status) {
		 this.status = status;
	 }
     
     
}
