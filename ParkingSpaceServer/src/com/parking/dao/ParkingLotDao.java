package com.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


import java.sql.ResultSet;
import com.parking.domin.ParkingLot;
import com.parking.sql.DBUtil;

public class ParkingLotDao {
	DBUtil db;
	public ParkingLotDao(){
		db=new DBUtil();
	}
	public ArrayList<ParkingLot>  queryAllParkingLot(){
		ArrayList<ParkingLot> pls=new ArrayList<ParkingLot>();
		String sql="select * from ParkingLot;";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()){
				ParkingLot pl=new ParkingLot();
				pl.setAmount(rs.getInt("amount"));
				pl.setGrade(rs.getDouble("grade"));
				pl.setId(rs.getInt("ID"));
				pl.setLatitude(rs.getDouble("latitude"));
				pl.setLongitude(rs.getDouble("longitude"));
				pl.setLocation(rs.getString("location"));
				pl.setParkingLotName(rs.getString("parkingLotName"));
				pl.setPrice(rs.getDouble("price"));
				pl.setSurplus(rs.getInt("surplus"));
				pls.add(pl);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		
		return pls;
	}
	public ParkingLot  queryById(int Id){
		ParkingLot pl=new ParkingLot();
		String sql="select * from ParkingLot where ID="+Id+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()){
				
				pl.setAmount(rs.getInt("amount"));
				pl.setGrade(rs.getDouble("grade"));
				pl.setId(rs.getInt("ID"));
				pl.setLatitude(rs.getDouble("latitude"));
				pl.setLongitude(rs.getDouble("longitude"));
				pl.setLocation(rs.getString("location"));
				pl.setParkingLotName(rs.getString("parkingLotName"));
				pl.setPrice(rs.getDouble("price"));
				pl.setSurplus(rs.getInt("surplus"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		
		return pl;
	}
	public boolean updateSurplus(int id,int surplus){
		boolean b=false;
		String sql="update ParkingLot set surplus="+surplus+" where id="+id+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			int k=pstmt.executeUpdate();
			if(k>0){
				b=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return b;
	}
	
	/**
	 * function:更新停车场评分
	 * @param id 停车场id,
	 * @param grade 评分分数
	 * @return boolean
	 * */
	public boolean updateGrade(int id,double grade){
		boolean b=false;
		String sql="update ParkingLot set grade="+grade+" where id="+id+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			int k=pstmt.executeUpdate();
			if(k>0){
				b=true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return b;
	}
}
