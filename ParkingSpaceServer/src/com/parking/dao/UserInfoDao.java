package com.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.parking.domin.UserInfo;
import com.parking.sql.DBUtil;


public class UserInfoDao {
	DBUtil db;
	public UserInfoDao(){
		db=new DBUtil();
	}
	public UserInfo login(String licenseNumber,String password){
		UserInfo user=new UserInfo();
		String sql = "select * from UserInfo where licenseNumber='"+licenseNumber+"' and password='"+password+"';";
		Connection conn = db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				user.setId(rs.getInt("ID"));
				user.setCarModel(rs.getString("carModel"));
				user.setLicenseNumber(rs.getString("licenseNumber"));
				user.setPayAcount(rs.getString("payAcount"));
				user.setUserName(rs.getString("userName"));
				user.setPassword(rs.getString("password"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return user;
	}
	public boolean register(UserInfo user){
		boolean b=false;
		String sql="insert into UserInfo(userName,password,carModel,LicenseNumber,payAcount)" +
				"values('"+user.getUserName()+"','"+user.getPassword()+"','"+user.getCarModel()+"','"+
				user.getLicenseNumber()+"','"+user.getPayAcount()+"');";
		Connection conn = db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			int i=pstmt.executeUpdate();
			if(i!=0){b=true;}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{db.closeConn(conn);}
		System.out.println(""+b);
		return b;
	}
	public UserInfo queryByLicenseNumber(String licenseNumber){
		UserInfo user=new UserInfo();
		String sql = "select * from UserInfo where licenseNumber='"+licenseNumber+"';";
		Connection conn = db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				user.setId(rs.getInt("ID"));
				user.setCarModel(rs.getString("carModel"));
				user.setLicenseNumber(rs.getString("licenseNumber"));
				user.setPayAcount(rs.getString("payAcount"));
				user.setUserName(rs.getString("userName"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return user;
	}
	

}
