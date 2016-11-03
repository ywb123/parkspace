package com.wust.parkingspace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wust.parking.domin.ParkingRecord;
import com.wust.parking.domin.UserInfo;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.RefreshableView;
import com.wust.parking.view.RefreshableView.PullToRefreshListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecordOfParkingLotActivity extends Activity {

	protected static final int SHOW = 0;
	
	private ListView lvRecordOfParkingLot;
	private RefreshableView refreshRecord;
	
	private ArrayList<ParkingRecord> records=new ArrayList<ParkingRecord>();
	private ArrayList<UserInfo> users=new ArrayList<UserInfo>();
	private ArrayList<Double> pays=new ArrayList<Double>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_of_parking_lot);
		lvRecordOfParkingLot=(ListView) findViewById(R.id.lvRecordOfParkingLot);
		refreshRecord=(RefreshableView) findViewById(R.id.refreshRecord);
		show();
		refreshRecord.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				getRecords();
				handler.sendEmptyMessage(SHOW);
			}
		}, 0);
	}

	public void show(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				getRecords();
			}
			
		}).start();
	}
	
	public class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return records.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return records.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v=null;
			if(convertView==null){
				v=LayoutInflater.from(RecordOfParkingLotActivity.this).inflate(R.layout.record_of_parkinglot_item, null);
			}else{
				v=convertView;
			}
			TextView tvLicenseNumber=(TextView) v.findViewById(R.id.tvLicenseNumber);
			TextView tvPayment=(TextView) v.findViewById(R.id.tvPayment);
			Button btIsPay=(Button) v.findViewById(R.id.btIsPay);
			TextView tvdate=(TextView) v.findViewById(R.id.tvDate);
			
			tvLicenseNumber.setText(users.get(position).getLicenseNumber()+"");
			SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日HH时");
			String date=format.format(new Date(records.get(position).getDate()));
			tvdate.setText(date);
			tvPayment.setText("总价:"+pays.get(position)+"元");
			if(records.get(position).getIsPay()){
				//已付款状态
				btIsPay.setBackground(getResources().getDrawable(R.drawable.background_button_blue));
				btIsPay.setClickable(false);
				btIsPay.setText("已付款");
			}else{
				//未付款
				btIsPay.setBackground(getResources().getDrawable(R.drawable.background_button_red));
				btIsPay.setText("现金支付");
				btIsPay.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//付款成功，将此次停车记录的isCharged更新为true
						new AlertDialog.Builder(RecordOfParkingLotActivity.this).setTitle("操作")
						.setMessage("确认该用户已付款？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setIsChargedTrue(records.get(position).getId());
							}
						})
						.setNegativeButton("取消", null).show();
						
				        
					}
				});
			}
			
			return v;
		}
		
	}
	
	public void getRecords() {
		SharedPreferences sp = RecordOfParkingLotActivity.this.getSharedPreferences(
				"LoginAdmin", RecordOfParkingLotActivity.this.MODE_PRIVATE);
		final int pid = sp.getInt("parkingLotId", 0);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "queryByParkingLotId"));
		params.add(new BasicNameValuePair("parkingLotId", Integer.toString(pid)));
		String jsonStr = HttpRequestUtil.postRequest(params,
				"ParkingRecordServlet");
		try {
			records = new ArrayList<ParkingRecord>();
			users = new ArrayList<UserInfo>();
			// 封装每一条记录
			JSONArray jsonArray = new JSONArray(jsonStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = new JSONObject(jsonArray.get(i)
						.toString());
				Log.v("Json" + i, jsonArray.get(i).toString());
				records.add(new ParkingRecord(jsonObject.getInt("id"),
						jsonObject.getInt("userId"), jsonObject
								.getInt("parkingLotId"), jsonObject
								.getLong("date"), jsonObject
								.getLong("duration"), jsonObject
								.getDouble("grade"), jsonObject
								.getString("evaluation"), jsonObject
								.getBoolean("isCharged")));
				UserInfo u = new UserInfo();
				u.setCarModel(jsonObject.getString("carModel"));
				u.setId(jsonObject.getInt("userId"));
				u.setLicenseNumber(jsonObject.getString("licenseNumber"));
				u.setPayAcount(jsonObject.getString("payAcount"));
				u.setUserName(jsonObject.getString("userName"));
				users.add(u);
				pays.add(jsonObject.getDouble("pay"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Collections.reverse(records);
		Collections.reverse(pays);
		Collections.reverse(users);
		handler.sendEmptyMessage(SHOW);
		

	}

	public Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				lvRecordOfParkingLot.setAdapter(new MyAdapter());
				refreshRecord.finishRefreshing();
				if (records.size() == 0) {
					Toast.makeText(RecordOfParkingLotActivity.this, "没有停车记录", Toast.LENGTH_LONG)
							.show();
				}
				break;
			
			default:
				break;
			}
		}
	};
	
	 /*****
     * 将服务器端的停车记录ischarged设为true
     * ********/
    private void setIsChargedTrue(final int rid){
    	new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 将停车请求发送给服务器端
				ArrayList<NameValuePair> paras = new ArrayList<NameValuePair>();
				paras.add(new BasicNameValuePair("action", "sureCharge"));
				paras.add(new BasicNameValuePair("rid", Integer.toString(rid)));
				String jsonStr = HttpRequestUtil.postRequest(paras,
						"ParkingRecordServlet");
				Log.v("Json", jsonStr);
				// 提取json字段
				JSONObject json = null;
				String state = null;
				try {
					json = new JSONObject(jsonStr);
					state = json.getString("result");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (state.equals("success")) {
					Log.d("charge", "ok");
				} else {
					Log.d("charge", "error");
				}
			}
    		
    	}).start();
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record_of_parking_lot, menu);
		return true;
	}

}
