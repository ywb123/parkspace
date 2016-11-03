package com.wust.parkingspace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wust.parking.util.HttpRequestUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends Activity {

	protected static final int HANDLER_OK = 0;
	protected static final int HANDLER_ERROR = 1;
	private EditText etUserName;
	private EditText etLicenseNumber;
	private EditText etPassword;
	private EditText etPasswordAgain;
	private EditText etCarModel;
	private EditText etPayAcount;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		etUserName=(EditText) findViewById(R.id.etUserName);
		etLicenseNumber=(EditText) findViewById(R.id.etLicenseNumber);
		etPassword=(EditText) findViewById(R.id.etPassword);
		etPasswordAgain=(EditText) findViewById(R.id.etPasswordAgain);
		etCarModel=(EditText) findViewById(R.id.etCarModel);
		etPayAcount=(EditText) findViewById(R.id.etPayAcount);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	public void registerOK(View v){

		final String strUserName=etUserName.getText().toString().trim();
		final String strLicenseNumber=etLicenseNumber.getText().toString().trim();
		final String strPassword=etPassword.getText().toString().trim();
		final String strPasswordAgain=etPasswordAgain.getText().toString().trim();
		final String strCarModel=etCarModel.getText().toString().trim();
		final String strPayAcount=etPayAcount.getText().toString().trim();
		if(!strPassword.equals(strPasswordAgain)){
			Toast.makeText(RegisterActivity.this, "两次密码不一致", Toast.LENGTH_LONG).show();
		}else if(etLicenseNumber.equals("")){
			Toast.makeText(RegisterActivity.this, "请输入车牌号", Toast.LENGTH_LONG).show();
		}else{
            final ProgressDialog waitDialog = ProgressDialog.show( this, "提交中" , "请稍等...", true);
			new Thread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String servlet="UserInfoServlet";
					List<NameValuePair> params=new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("action", "register"));
					params.add(new BasicNameValuePair("userName", strUserName));
					params.add(new BasicNameValuePair("licenseNumber", strLicenseNumber));
					params.add(new BasicNameValuePair("password", strPassword));
					params.add(new BasicNameValuePair("carModel", strCarModel));
					params.add(new BasicNameValuePair("payAcount", strPayAcount));
					String result=HttpRequestUtil.postRequest(params, servlet).toString().trim();
					try {
						Log.d(result, result);
						String strResult="";
						
						JSONObject json=new JSONObject(result);
						strResult=json.getString("result");
						
						Log.d(strResult, strResult);
						if(strResult.equals("success")){
                            waitDialog.dismiss();
							handler.sendEmptyMessage(HANDLER_OK);
						}else if(strResult.equals("error")){
                            waitDialog.dismiss();
							handler.sendEmptyMessage(HANDLER_ERROR);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			
		}
		
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_OK:
				Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
				Intent intent =new Intent(RegisterActivity.this,LoginActivity.class);
				startActivity(intent);
				finish();
                break;
			case HANDLER_ERROR:
				Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	};
	public void backToLogin(View v){
		Intent intent =new Intent(RegisterActivity.this,LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
