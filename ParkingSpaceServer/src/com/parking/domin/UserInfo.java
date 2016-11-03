package com.parking.domin;

public class UserInfo {

	private int id;
	private String userName;
	private String password;
	private String carModel;
	private String licenseNumber;
	private String payAcount;
	
	public UserInfo(){}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCarModel() {
		return carModel;
	}
	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}
	public String getLicenseNumber() {
		return licenseNumber;
	}
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}
	public String getPayAcount() {
		return payAcount;
	}
	public void setPayAcount(String payAcount) {
		this.payAcount = payAcount;
	}
	
	
}
