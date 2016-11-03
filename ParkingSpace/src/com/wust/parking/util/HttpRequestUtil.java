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
			//�����ַ���
			HttpEntity httpEntity=new UrlEncodedFormEntity(params,"UTF-8");
			//����post����
			post.setEntity(httpEntity);
			//��ʼ��HttpClient
			HttpClient client=new DefaultHttpClient();
			//ִ������
			HttpResponse response=client.execute(post);
			if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
				result=EntityUtils.toString(response.getEntity());
				Log.d("http", result);
			}else{
				Log.d("http", "�������"+response.getStatusLine().getStatusCode());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
