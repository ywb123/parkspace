package com.wust.parking.util;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpRequestUtil {

	public static String serverUrl="http://parkingspacewust.sinaapp.com/servlet/";//"http://192.168.173.1:8080/ParkingSpaceServer/servlet/";
	public static String postRequest(List<NameValuePair> params,String servlet){
		String result="";
		String url=serverUrl+servlet;
		HttpPost post=new HttpPost(url);
		try {
			//设置字符集
			HttpEntity httpEntity=new UrlEncodedFormEntity(params,"UTF-8");
			//设置post参数
			post.setEntity(httpEntity);
			//初始化HttpClient
			HttpClient client=new DefaultHttpClient();
			//执行请求
			HttpResponse response=client.execute(post);
			if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
				result=EntityUtils.toString(response.getEntity());
				Log.d("http", result);
			}else{
				Log.d("http", "请求错误"+response.getStatusLine().getStatusCode());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
