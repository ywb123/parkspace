package com.wust.parkingspace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.util.Phone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

	protected static final int HANDLER_OK = 0;
	protected static final int HANDLER_ERROR = 1;
	private EditText etLicenseNumber;
	private EditText etPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		etLicenseNumber=(EditText) findViewById(R.id.etLicenseNumber);
		etPassword=(EditText) findViewById(R.id.etPassword);
		Display display=getWindowManager().getDefaultDisplay();
		Phone.width=display.getWidth();
		Phone.height=display.getHeight();
		SharedPreferences sp=getSharedPreferences("LoginUserInfo", MODE_PRIVATE);
		String strlicenseNumber=sp.getString("licenseNumber", "");
		String strpassword=sp.getString("password", "");
		if(!("".equals(strlicenseNumber))&&!("".equals(strpassword))){
			Intent intent =new Intent(LoginActivity.this,MainActivity.class);
			startActivity(intent);
			finish();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	public void register(View v){
		Intent intent =new Intent(LoginActivity.this,RegisterActivity.class);
		startActivity(intent);
		finish();
		
	}
	public void login(View v){
		final String strLicenseNumber=etLicenseNumber.getText().toString().trim();
		final String strPassword=etPassword.getText().toString().trim();
        final ProgressDialog waitDialog = ProgressDialog.show( this, "正在登陆" , "请稍等...", true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String servlet="UserInfoServlet";
				List<NameValuePair> params=new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "login"));
				params.add(new BasicNameValuePair("licenseNumber", strLicenseNumber));
				params.add(new BasicNameValuePair("password", strPassword));
				String result=HttpRequestUtil.postRequest(params, servlet).toString().trim();
                Log.v("json",result);
                waitDialog.dismiss();
				try {
					String strResult="";
					JSONObject json=new JSONObject(result);
					strResult=json.getString("result");
					Log.d(strResult, strResult);
					if(strResult.equals("success")){
						Message msg=new Message();
						msg.what=HANDLER_OK;
						Bundle data=new Bundle();
						data.putInt("id", json.getInt("id"));
						data.putString("userName", json.getString("userName"));
						data.putString("password", json.getString("password"));
						data.putString("licenseNumber", json.getString("licenseNumber"));
						Log.v("aaa", json.getString("licenseNumber"));
						data.putString("carModel", json.getString("carModel"));
						data.putString("payAcount", json.getString("payAcount"));
						msg.setData(data);
						handler.sendMessage(msg);
					}else if(strResult.equals("error")){
						handler.sendEmptyMessage(HANDLER_ERROR);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_OK:
				SharedPreferences sp=LoginActivity.this.getSharedPreferences("LoginUserInfo",MODE_PRIVATE);
				Editor edtior=sp.edit();
				edtior.putInt("id", msg.getData().getInt("id"));
				edtior.putString("userName", msg.getData().getString("userName"));
				edtior.putString("password", msg.getData().getString("password"));
				edtior.putString("licenseNumber", msg.getData().getString("licenseNumber"));
				edtior.putString("carModel", msg.getData().getString("carModel"));
				edtior.putString("payAcount", msg.getData().getString("payAcount"));
				edtior.commit();
				Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
				Intent intent =new Intent(LoginActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
				break;
			case HANDLER_ERROR:
				Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}
		
	};

}
