package com.parking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.ResultSet;

import com.parking.domin.ParkingRecord;
import com.parking.sql.DBUtil;

public class ParkingRecordDao {

	DBUtil db;
	public ParkingRecordDao(){
		db=new DBUtil();
	}
	public ArrayList<ParkingRecord> queryAllRecord(){
		ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
		String sql="select * from ParkingRecord;";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()){
				ParkingRecord pr=new ParkingRecord();
				pr.setDate(rs.getLong("date"));
				pr.setDuration(rs.getDouble("duration"));
				pr.setGrade(rs.getDouble("grade"));
				pr.setId(rs.getInt("ID"));
				pr.setParkingLotId(rs.getInt("parkingLotId"));
				pr.setUserId(rs.getInt("userId"));
				prs.add(pr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		return prs;
	}
	public ArrayList<ParkingRecord> queryByUserId(int userId){
		ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
		String sql="select * from ParkingRecord where userId="+userId+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()){
				ParkingRecord pr=new ParkingRecord();
				pr.setDate(rs.getLong("date"));
				pr.setDuration(rs.getDouble("duration"));
				pr.setGrade(rs.getDouble("grade"));
				pr.setId(rs.getInt("ID"));
				pr.setParkingLotId(rs.getInt("parkingLotId"));
				pr.setUserId(rs.getInt("userId"));
				prs.add(pr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		return prs;
	}
	public ArrayList<ParkingRecord> queryByParkingLotId(int parkingLotId){
		ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
		String sql="select * from ParkingRecord where parkingLotId="+parkingLotId+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			while(rs.next()){
				ParkingRecord pr=new ParkingRecord();
				pr.setDate(rs.getLong("date"));
				pr.setDuration(rs.getDouble("duration"));
				pr.setGrade(rs.getDouble("grade"));
				pr.setId(rs.getInt("ID"));
				pr.setParkingLotId(rs.getInt("parkingLotId"));
				pr.setUserId(rs.getInt("userId"));
				prs.add(pr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		return prs;
	}
	public ParkingRecord queryById(int id){
		ParkingRecord pr=new ParkingRecord();
		String sql="select * from ParkingRecord where ID="+id+";";
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()){
				
				pr.setDate(rs.getLong("date"));
				pr.setDuration(rs.getDouble("duration"));
				pr.setGrade(rs.getDouble("grade"));
				pr.setId(rs.getInt("ID"));
				pr.setParkingLotId(rs.getInt("parkingLotId"));
				pr.setUserId(rs.getInt("userId"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		
		return pr;
	}
	public boolean addRecord(ParkingRecord pr){
		boolean b=false;
		String sql="insert into ParkingRecord(userId,parkingLotId,date,duration,grade)" +
				"values("+pr.getUserId()+","+pr.getParkingLotId()+",'"+pr.getDate()+"',"+pr.getDuration()+","+pr.getGrade()+");";
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
	 * function：更新停车时间，根据用户的id和停车时间进行更改
	 * @param id
	 * @param duration
	 * @return
	 */
	public boolean updateDuration(int rid,long duration)
	{
		boolean b=false;
		String sql = "update ParkingRecord set duration="+duration+" where id="+rid;
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			int k=pstmt.executeUpdate();
			if(k>0){
				b=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return b;
	}
	
	/**
	 * function:获取停车记录（通过用户id和开始停车时间）
	 * @param uid
	 * @param date
	 * @return
	 */
	public int getIdByUidAndDate(int uid,long date)
	{
		int id = 0;
		String sql = "select id from parkingrecord where userId="+uid+" and date="+date;
		System.out.println(sql);
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()){
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return id;
			
	}
	
	public boolean updateEvaluation(int rid,float grade,String evaluation)
	{
		boolean b = false;
		String sql = "update parkingrecord set grade="+grade+",evaluation='"+evaluation+"' where id="+rid;
		System.out.println(sql);
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			int k=pstmt.executeUpdate();
			if(k>0){
				b=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return b;
	}
	
	public int queryRecordCount(){
		int count=0;
		String sql = "select count(*) from parkingrecord ;";
		System.out.println(sql);
		Connection conn=db.openConnection();
		PreparedStatement pstmt;
		try {
			pstmt=conn.prepareStatement(sql);
			ResultSet rs=pstmt.executeQuery();
			if(rs.next()){
				count=rs.getInt("count(*)");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			db.closeConn(conn);
		}
		return count;
	}
}
