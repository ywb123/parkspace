package com.wust.parkingspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wust.parking.domin.UserInfo;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.RefreshableView;
import com.wust.parking.view.RefreshableView.PullToRefreshListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ParkingCarListActivity extends Activity {

	private static final int SHOW = 0;
	
	private List<UserInfo> users=new ArrayList<UserInfo>();
	private List<Long> times=new ArrayList<Long>();
	
	private ListView lvParkingCar;
	private RefreshableView refreshRecord;
	private boolean isFresh=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_car_list);
		lvParkingCar=(ListView) findViewById(R.id.lvParkingCar);
		refreshRecord=(RefreshableView) findViewById(R.id.refreshRecord);
		refreshRecord.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				updateDate();
			}
		}, 0);
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	getMenuInflater().inflate(R.menu.main, menu);
    	menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				SharedPreferences sp=getSharedPreferences("LoginAdmin",MODE_PRIVATE);
				Editor e=sp.edit();
				e.clear();
				e.commit();
				System.exit(0);
				return false;
			}
		});
        return true;
    }
	/*****
	 * 更新users数据信息
	 * 
	 * ******/
	public void updateDate(){
		SharedPreferences sp=ParkingCarListActivity.this.getSharedPreferences("LoginAdmin", ParkingCarListActivity.this.MODE_PRIVATE);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("parkingLotId", sp.getInt("parkingLotId", 0)+""));//停车场ID
		params.add(new BasicNameValuePair("action", "queryParkingUsers"));
		String servlet = "ParkingLotServlet";
		String result = HttpRequestUtil.postRequest(params, servlet);
		try {
			JSONArray array = new JSONArray(result);
			users=new ArrayList<UserInfo>();
			times=new ArrayList<Long>();
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				UserInfo u = new UserInfo();
				u.setCarModel(json.getString("carModel"));
				u.setId(json.getInt("id"));
				u.setLicenseNumber(json.getString("licenseNumber"));
				u.setUserName(json.getString("userName"));
				times.add(json.getLong("time"));
				users.add(u);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.reverse(users);
		handler.sendEmptyMessage(SHOW);
	}
	
	//listView 的数据适配器
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return users.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return users.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v=null;
			if(convertView!=null){
				v=convertView;
			}else{
				v=LayoutInflater.from(ParkingCarListActivity.this).inflate(R.layout.parking_car_item, null);
			}
			TextView tvCarLicenseNumber=(TextView) v.findViewById(R.id.tvCarLicenseNumber);
			TextView tvCarModel=(TextView) v.findViewById(R.id.tvCarModel);
			Chronometer clockParking=(Chronometer) v.findViewById(R.id.clockParking);
			
			tvCarLicenseNumber.setText("车牌号："+users.get(position).getLicenseNumber());
			tvCarModel.setText("型号："+users.get(position).getCarModel());
			clockParking.setBase(SystemClock.elapsedRealtime()
					- times.get(position));
			clockParking.start();
			return v;
		}
		
	}
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				lvParkingCar.setAdapter(new MyAdapter());
				if(users.size()<=0){
					Toast.makeText(ParkingCarListActivity.this, "暂时没有用户停车", Toast.LENGTH_SHORT).show();
				}
				refreshRecord.finishRefreshing();
				break;

			
			default:
				break;
			}
		}
		
	};
	
	public void showRecord(View v){
		Intent intent=new Intent(ParkingCarListActivity.this,RecordOfParkingLotActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isFresh=false;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isFresh=true;
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(isFresh){
					updateDate();
					try {
						Thread.sleep(30*1000);//每三十秒更新一次
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}}).start();
	}
}
