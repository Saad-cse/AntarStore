package com.project.antarstore.Dto;

import com.project.antarstore.Model.Category;

public class ProductDto {
	
	 // product details
      private String productName;
      private String productDescription;
      private Category category;
      private String brandName;
      private String gender;   //kids ,men,women
      
      
      //price details
      
      private double costPrice;
      private double sellingPrice;
      private int discount;
      private double finalprice;
     
      //product variants
      
      private String colors;
      private String sizes;
      
      
      //inventory
      
      private long quantity;
      private double shippingCharge;
      private String deliveryTime;  //2-5 days
      
      private boolean returnPolicy;

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
      
      
      
      
            
      
      
      
      
}
