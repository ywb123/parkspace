package com.parking.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.parking.dao.UserInfoDao;
import com.parking.domin.UserInfo;

public class UserInfoServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UserInfoServlet() {
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

		UserInfoDao userInfoDao=new UserInfoDao();
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String action=request.getParameter("action");
		if(action.equals("login")){
			PrintWriter out = response.getWriter();
			UserInfo user=new UserInfo();
			String licenseNumber=URLDecoder.decode(request.getParameter("licenseNumber"),"utf-8");
			//licenseNumber =new String(licenseNumber.getBytes("iso-8859-1"));	//get«Î«Û¬“¬Î
			System.out.println("≥µ≈∆∫≈£∫"+licenseNumber);
			String password=request.getParameter("password");
			user=userInfoDao.login(licenseNumber, password);
			JSONObject json=new JSONObject();
			if(!"".equals(user.getUserName())&&user.getUserName()!=null){
				try {
					json.put("result", "success");
					json.put("id", user.getId());
					json.put("userName", user.getUserName());
					json.put("password", user.getPassword());
					json.put("licenseNumber", user.getLicenseNumber());
					json.put("carModel", user.getCarModel());
					json.put("payAcount", user.getPayAcount());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				try {
					json.put("result", "error");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			response.setContentType("text/plain");  
            response.setCharacterEncoding("UTF-8");
            out.print(json.toString());
            System.out.println(json.toString()+"aaaa");
            out.flush();  
            out.close();  
		}
		else if(action.equals("register")){
			PrintWriter out = response.getWriter();
			UserInfo user=new UserInfo();
			user.setUserName(request.getParameter("userName"));
			user.setCarModel(request.getParameter("carModel"));
			user.setLicenseNumber(request.getParameter("licenseNumber"));
			user.setPassword(request.getParameter("password"));
			user.setPayAcount(request.getParameter("payAcount"));
			boolean b=userInfoDao.register(user);
			JSONObject json=new JSONObject();
			if(b){
				try {
					json.put("result", "success");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				try {
					json.put("result", "error");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			response.setContentType("text/plain");  
            response.setCharacterEncoding("UTF-8");
            out.print(json.toString());
            System.out.println(json.toString()+"aaaa");
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

}
