package com.parking.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parking.dao.ParkingLotDao;
import com.parking.dao.ParkingRecordDao;
import com.parking.dao.UserInfoDao;
import com.parking.domin.ParkingLot;
import com.parking.domin.ParkingRecord;
import com.parking.domin.UserInfo;

public class ParkingLotServlet extends HttpServlet {

	HashMap<String, Date> startTime;
	HashMap<String, Integer> parkingPlace;
	/**
	 * Constructor of the object.
	 */
	public ParkingLotServlet() {
		super();
		startTime=new HashMap<String, Date>();
		parkingPlace=new HashMap<String, Integer>();
		System.out.println("mmm");
		
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
//		startTime.clear();
//		parkingPlace.clear();
		System.out.println("mmm1");
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
		System.out.println("aa"+action);
		ParkingLotDao pld=new ParkingLotDao();
		if(action.equals("queryAllParkingLot")){
			ArrayList<ParkingLot> pls=new ArrayList<ParkingLot>();
			pls=pld.queryAllParkingLot();
			JSONArray array=new JSONArray();
			for(int i=0;i<pls.size();i++){
				JSONObject obj=new JSONObject();
				try {
					obj.put("id", pls.get(i).getId());
					obj.put("amount", pls.get(i).getAmount());
					obj.put("grade", pls.get(i).getGrade());
					obj.put("latitude", pls.get(i).getLatitude());
					obj.put("location",pls.get(i).getLocation());
					obj.put("longitude",pls.get(i).getLongitude());
					obj.put("parkingLotName",pls.get(i).getParkingLotName());
					obj.put("price",pls.get(i).getPrice());
					obj.put("surplus",pls.get(i).getSurplus());
					array.put(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			response.setContentType("text/plain");
            out.print(array.toString()+"aaaa");
            System.out.println(array.toString()+"aaaa");
            out.flush();  
            out.close(); 
		}else if(action.equals("startParking")){
			//停车场剩余车位数减一
			int id=Integer.parseInt(request.getParameter("id"));
			int uid=Integer.parseInt(request.getParameter("uid"));
			String licenseNumber=request.getParameter("licenseNumber");
			
			ParkingLot pl=pld.queryById(id);
			JSONObject json=new JSONObject();
			if(pl.getSurplus()>0){
				boolean b=pld.updateSurplus(id, pl.getSurplus()-1);
				
				if(b){
					try {
						json.put("result", "success");
						json.put("test", "中文");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					try {
						json.put("test", "中文");
						json.put("result", "error");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
			}else{
				try {
					json.put("result", "error");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			
			response.setContentType("text/plain");
			out.print(json.toString());
			System.out.println("StartParking json:"+json);
			//保存当前系统时间
			Date start=new Date();
			System.out.println(licenseNumber);
			startTime.put(licenseNumber, start);
			parkingPlace.put(licenseNumber, id);
			
			
			//将停车记录插入数据库
			ParkingRecord pr = new ParkingRecord();
			pr.setUserId(uid);		//user id
			pr.setParkingLotId(id);	//parking plot id
			pr.setDate(startTime.get(licenseNumber).getTime());	//date time
			ParkingRecordDao recordDao = new ParkingRecordDao();
			recordDao.addRecord(pr);
			
			out.flush();  
            out.close();
		}else if(action.equals("stopParking")){
			int uid = Integer.parseInt(request.getParameter("uid"));
			int id=Integer.parseInt(request.getParameter("id"));
			String licenseNumber=request.getParameter("licenseNumber");
			ParkingLot pl=pld.queryById(id);
			if(pl.getSurplus()>0){
				boolean b=pld.updateSurplus(id, pl.getSurplus()+1);
				Date currentTime=new Date();
				long time=currentTime.getTime()-startTime.get(licenseNumber).getTime();
				
				//根据用户id、停车时间，查询停车记录id
				//修改停车时间
				ParkingRecordDao recordDao = new ParkingRecordDao();
				int rid = recordDao.getIdByUidAndDate(uid,startTime.get(licenseNumber).getTime());
				JSONObject json=new JSONObject();
				if(b){
					try {
						json.put("rid", rid);
						json.put("result", "success");
						json.put("duration", time);//返回停车时间
						json.put("price", pl.getPrice());	//单价
						json.put("pay",MiliSencToHours(time)*pl.getPrice());
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
				startTime.remove(licenseNumber);
				parkingPlace.remove(licenseNumber);
				
				response.setContentType("text/plain");
				out.print(json.toString());
				
				recordDao.updateDuration(rid, time);
			}
			//当前系统时间
			out.flush();  
            out.close();
			
		}else if(action.equals("getParkingTime")){
			
			int id=Integer.parseInt(request.getParameter("id"));
			String licenseNumber=request.getParameter("licenseNumber");
			System.out.println(licenseNumber);
			ParkingLot pl=pld.queryById(id);
			Date currentTime=new Date();
			long time=0;
			int parkingId=0;
			JSONObject json=new JSONObject();
			try {
				
				if((startTime.containsKey(licenseNumber))&& (parkingPlace.containsKey(licenseNumber))){
					time=currentTime.getTime()-startTime.get(licenseNumber).getTime();
					parkingId=parkingPlace.get(licenseNumber);
					ParkingLot plot=pld.queryById(parkingId);
					json.put("time", time);
					json.put("parkingName", plot.getParkingLotName());
					json.put("parkingAmount", plot.getAmount());
					json.put("parkingGrade", plot.getGrade());
					json.put("parkingLatitude", plot.getLatitude());
					json.put("parkingLongitude", plot.getLongitude());
					json.put("parkingPrice", plot.getPrice());
					json.put("parkingSurplus", plot.getSurplus());
					json.put("parkingLocation", plot.getLocation());
				}
				json.put("parkingLotName", pl.getParkingLotName());
				json.put("amount", pl.getAmount());
				json.put("grade", pl.getGrade());
				json.put("id", pl.getId());
				json.put("latitude", pl.getLatitude());
				json.put("longitude", pl.getLongitude());
				json.put("price", pl.getPrice());
				json.put("surplus", pl.getSurplus());
				json.put("location", pl.getLocation());
				
				json.put("parkingId", parkingId);//返回客户端判断是否为0,若为0则不取time和parkingName的值
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(json.toString());
			response.setContentType("text/plain");
			out.print(json.toString());
			out.flush();  
            out.close();
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
	private float MiliSencToHours(long miliSenc)
	{
		return (float)miliSenc/(1000*60*60);
	}


}
