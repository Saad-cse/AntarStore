package com.project.antarstore.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Cart {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long id;
	 
	 @ManyToOne
	 @JoinColumn(nullable = false)
     private Users user;
	 
	 @ManyToOne
	 @JoinColumn(nullable = false)
     private Products product;
	 
     private int quantity;
     
     
     private double productPrice;
     private double finalPrice;
     
     private String color;
     private String size;
     
     
	 public long getId() {
		 return id;
	 }
	 public void setId(long id) {
		 this.id = id;
	 }
	 public Users getUser() {
		 return user;
	 }
	 public void setUser(Users user) {
		 this.user = user;
	 }
	 public Products getProduct() {
		 return product;
	 }
	 public void setProduct(Products product) {
		 this.product = product;
	 }
	 public int getQuantity() {
		 return quantity;
	 }
	 public void setQuantity(int quantity) {
		 this.quantity = quantity;
	 }
	 public double getProductPrice() {
		 return productPrice;
	 }
	 public void setProductPrice(double productPrice) {
		 this.productPrice = productPrice;
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
     
     
     
     
     
}
