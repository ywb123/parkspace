package com.parking.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parking.dao.ParkingLotDao;
import com.parking.dao.ParkingRecordDao;
import com.parking.domin.ParkingLot;
import com.parking.domin.ParkingRecord;

public class ParkingRecordServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public ParkingRecordServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String action=request.getParameter("action");
		ParkingRecordDao prd=new ParkingRecordDao();
		if(action.equals("queryAllRecord")){
			ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
			prs=prd.queryAllRecord();
			JSONArray array=new JSONArray();
			for(ParkingRecord pr:prs){
				JSONObject json=new JSONObject();
				try {
					json.put("id", pr.getId());
					json.put("date", pr.getDate());
					json.put("duration", pr.getDuration());
					json.put("grade", pr.getGrade());
					json.put("parkingLotId", pr.getParkingLotId());
					json.put("userId", pr.getUserId());
					array.put(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			response.setContentType("text/plain");
            out.print(array.toString());
            System.out.println(array.toString());
            out.flush();  
            out.close(); 
		}else if(action.equals("queryByUserId")){
			ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
			int userId=Integer.parseInt(request.getParameter("userId"));
			prs=prd.queryByUserId(userId);
			JSONArray array=new JSONArray();
			for(ParkingRecord pr:prs){
				JSONObject json=new JSONObject();
				try {
					json.put("id", pr.getId());
					json.put("date", pr.getDate());
					json.put("duration", pr.getDuration());
					json.put("grade", pr.getGrade());
					json.put("parkingLotId", pr.getParkingLotId());
					json.put("userId", pr.getUserId());
					array.put(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			response.setContentType("text/plain");
            out.print(array.toString());
            System.out.println(array.toString());
            out.flush();  
            out.close(); 
		}
		else if(action.equals("queryByParkingLotId")){
			ArrayList<ParkingRecord> prs=new ArrayList<ParkingRecord>();
			int parkingLotId=Integer.parseInt(request.getParameter("parkingLotId"));
			prs=prd.queryByUserId(parkingLotId);
			JSONArray array=new JSONArray();
			for(ParkingRecord pr:prs){
				JSONObject json=new JSONObject();
				try {
					json.put("id", pr.getId());
					json.put("date", pr.getDate());
					json.put("duration", pr.getDuration());
					json.put("grade", pr.getGrade());
					json.put("parkingLotId", pr.getParkingLotId());
					json.put("userId", pr.getUserId());
					array.put(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			response.setContentType("text/plain");
            out.print(array.toString());
            System.out.println(array.toString());
            out.flush();  
            out.close(); 
		}//更新评价内容（评分、评论文字）并更新该停车场的评分分数
		else if(action.equals("evaluate")){
			int rid = Integer.parseInt(request.getParameter("rid"));
			float grade = Float.parseFloat(request.getParameter("grade")); 	//评分
			String evaluation = request.getParameter("evaluation");		//评论文字
			ParkingRecordDao recordDao = new ParkingRecordDao();
			ParkingRecord pr=new ParkingRecord();
			pr=recordDao.queryById(rid);
			boolean b=recordDao.updateEvaluation(rid, grade, evaluation);
			JSONObject json=new JSONObject();
			if(b){
				try {
					json.put("result", "success");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				try {
					json.put("result", "error");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			ParkingLotDao pld=new ParkingLotDao();
			ParkingLot pl=pld.queryById(pr.getParkingLotId());
			double rGrade=pl.getGrade();
			int count=prd.queryRecordCount();
			BigDecimal bg = new BigDecimal((rGrade*count+grade)/(count+1));
	        double fGrade = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			pld.updateGrade(pl.getId(),fGrade );
			response.setContentType("text/plain");
			out.print(json.toString());
			
			out.flush();  
            out.close();
			
			System.out.println("b="+b);
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
