package com.project.antarstore.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Users {
	
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @Column(nullable = false,length = 150)
  private String name;
  
  @Column(nullable = false,length = 20)
  private String contactNo;
  
  @Column(nullable = false, unique =  true)
  private String email;
  
  @Column(nullable = false, length = 20)
  private String gender;
  
  @Column(nullable = false, length = 150)
  private String password;
  
  @Enumerated(EnumType.STRING)
  private UserRole role;
  
  @Enumerated(EnumType.STRING)
  private UserStatus status;
  
  public enum UserRole{
	  User, Admin
  }
  
  public enum UserStatus {
	  Unverified, Verified, Disabled
  }
  
  @Column(nullable = false)
  private LocalDateTime registerAt;
  
  private String otp;
  
  private LocalDateTime generatedAt;
  private LocalDateTime expiryTime;
  
  public long getId() {
	return id;
  }
  public void setId(long id) {
	this.id = id;
  }
  public String getName() {
	return name;
  }
  public void setName(String name) {
	this.name = name;
  }
  public String getContactNo() {
	return contactNo;
  }
  public void setContactNo(String contactNo) {
	this.contactNo = contactNo;
  }
  public String getEmail() {
	return email;
  }
  public void setEmail(String email) {
	this.email = email;
  }
  public String getGender() {
	return gender;
  }
  public void setGender(String gender) {
	this.gender = gender;
  }
  public String getPassword() {
	return password;
  }
  public void setPassword(String password) {
	this.password = password;
  }
  public UserRole getRole() {
	return role;
  }
  public void setRole(UserRole role) {
	this.role = role;
  }
  public UserStatus getStatus() {
	return status;
  }
  public void setStatus(UserStatus status) {
	this.status = status;
  }
  public LocalDateTime getRegisterAt() {
	return registerAt;
  }
  public void setRegisterAt(LocalDateTime registerAt) {
	this.registerAt = registerAt;
  }
  public String getOtp() {
	return otp;
  }
  public void setOtp(String otp) {
	this.otp = otp;
  }
  public LocalDateTime getGeneratedAt() {
	return generatedAt;
  }
  public void setGeneratedAt(LocalDateTime generatedAt) {
	this.generatedAt = generatedAt;
  }
  public LocalDateTime getExpiryTime() {
	return expiryTime;
  }
  public void setExpiryTime(LocalDateTime expiryTime) {
	this.expiryTime = expiryTime;
  }
  
  
}
