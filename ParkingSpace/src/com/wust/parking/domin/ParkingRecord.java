package com.wust.parking.domin;

public class ParkingRecord {
	


	private int id;
	private int userId;
	private int parkingLotId;
	private long date;
	private long duration;
	private double grade;
	private String evaluation;
	private boolean isPay;
	public ParkingRecord(){
		duration = 0;
		grade = 0;
		evaluation = "";
	}
	
	public ParkingRecord(int id,int userId,int parkingLotId,long date,long duration,double grade,String evaluation,boolean isPay)
	{
		this.id = id;
		this.userId = userId;
		this.parkingLotId = parkingLotId;
		this.date = date;
		this.duration = duration;
		this.grade = grade;
		this.evaluation = evaluation;
		this.isPay=isPay;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getParkingLotId() {
		return parkingLotId;
	}
	public void setParkingLotId(int parkingLotId) {
		this.parkingLotId = parkingLotId;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public double getGrade() {
		return grade;
	}
	public void setGrade(double grade) {
		this.grade = grade;
	}


	public String getEvaluation() {
		return evaluation;
	}


	public void setEvaluation(String evaluation) {
		this.evaluation = evaluation;
	}
	
	public boolean getIsPay() {
		return isPay;
	}

	public void setIsPay(boolean isPay) {
		this.isPay = isPay;
	}
	
}
