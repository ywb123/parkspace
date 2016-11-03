package com.wust.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wust.parking.domin.ParkingLot;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.util.Phone;
import com.wust.parkingspace.MyApplication;
import com.wust.parkingspace.ParkingLotDetailActivity;
import com.wust.parkingspace.ParkingLotListActivity;
import com.wust.parkingspace.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class FloatListService extends Service {

	protected static final int SHOW = 0;
	private View rootView;
	private ListView lvParkingLot;
	private ImageButton ibIsShow;
	
	private boolean isShow=true;
	private boolean go=true;
	
	private WindowManager wm;
	private LayoutParams wmParams;
	
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private BDLocation myLocation=null;
	
	List<ParkingLot> parkingLots=new ArrayList<ParkingLot>();
	@Override
	public void onCreate() {
		Log.e("FloatListService", "serviceCreate");
		super.onCreate();
		rootView=LayoutInflater.from(this).inflate(R.layout.float_parkinglot, null);
		lvParkingLot=(ListView) rootView.findViewById(R.id.lvParkingLot);
		ibIsShow=(ImageButton) rootView.findViewById(R.id.ibIsShow);
		setlocation();
		isShow=true;
		createView();
		
		
	}

	public void createView(){
		wm=(WindowManager) getApplicationContext().getSystemService("window");
		wmParams=((MyApplication)getApplication()).getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL; // 调整悬浮窗口至右侧
		
		wmParams.x = 0;
		wmParams.y = 0;
		
		wmParams.width = Phone.width/3;
		wmParams.height = Phone.height/2;
		wm.addView(rootView, wmParams);
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				while (go) {
					getParkingLot();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}).start();
		ibIsShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("Log", "click");
				if(isShow){
					
					wmParams.width = 20;
					lvParkingLot.setVisibility(View.GONE);
					ibIsShow.setImageDrawable(getResources().getDrawable(R.drawable.ic_left));
					wm.updateViewLayout(rootView, wmParams);
					
				}else{
					wmParams.width = Phone.width/3;
					lvParkingLot.setVisibility(View.VISIBLE);
					ibIsShow.setImageDrawable(getResources().getDrawable(R.drawable.ic_right));
					wm.updateViewLayout(rootView, wmParams);
				}
				isShow=!isShow;
			}
		});
		lvParkingLot.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(FloatListService.this,
						ParkingLotDetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Bundle extras = new Bundle();
				extras.putSerializable("parkingLot", parkingLots.get(arg2));
				intent.putExtras(extras);
				startActivity(intent);
				
			}
		});
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		go=false;
		wm.removeView(rootView);
		mLocationClient.stop();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return parkingLots.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return parkingLots.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v=null;
			if(convertView!=null){
				v=convertView;
			}else{
				v=LayoutInflater.from(FloatListService.this).inflate(R.layout.float_parkinglot_item, null);
				
			}
			TextView tvParkingLotName=(TextView) v.findViewById(R.id.tvParkingLotName);
			TextView tvAmount=(TextView) v.findViewById(R.id.tvAmount);
			TextView tvDistance=(TextView) v.findViewById(R.id.tvDistance);
			
			tvAmount.setText("车位数："+parkingLots.get(position).getSurplus()+"/"+parkingLots.get(position).getAmount());
			tvParkingLotName.setText(parkingLots.get(position).getParkingLotName());
			if(parkingLots.get(position).getDistance()>=0){
				tvDistance.setText("距离："+parkingLots.get(position).getDistance()+"米");
			}else{
				tvDistance.setText("距离未知");
			}
			
			return v;
		}
		
		
		
	}
	
	
	public void setlocation(){
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	    
	    LocationClientOption option = new LocationClientOption();
	    option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
	    option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
	    option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
	    option.setIsNeedAddress(true);//返回的定位结果包含地址信息
	    option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
	    mLocationClient.setLocOption(option);
	    mLocationClient.start();
	    if (mLocationClient != null && mLocationClient.isStarted()){
	    	mLocationClient.requestLocation();
	    }
	    else{ 
	    	Log.d("LocSDK4", "locClient is null or not started");
	    }
	    
    }
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
			myLocation=location;
		}
	}
	
	public void getParkingLot(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "queryAllParkingLot"));
		String servlet = "ParkingLotServlet";
		String result = HttpRequestUtil.postRequest(params, servlet);
		parkingLots = new ArrayList<ParkingLot>();
		try {
			JSONArray array = new JSONArray(result);
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				
				int distance=-1;
				if(myLocation!=null){
					distance=(int) DistanceUtil.getDistance(new LatLng(json.getDouble("latitude"), json.getDouble("longitude")),
						new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
				}
				if(distance<5000){
					ParkingLot pl = new ParkingLot();
					pl.setAmount(json.getInt("amount"));
					pl.setGrade(json.getDouble("grade"));
					pl.setId(json.getInt("id"));
					pl.setLatitude(json.getDouble("latitude"));
					pl.setLocation(json.getString("location"));
					pl.setLongitude(json.getDouble("longitude"));
					pl.setParkingLotName(json.getString("parkingLotName"));
					pl.setPrice(json.getDouble("price"));
					pl.setSurplus(json.getInt("surplus"));
					pl.setDistance(distance);
					parkingLots.add(pl);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.sort(parkingLots, new Comparator<ParkingLot>(){

			@Override
			public int compare(ParkingLot p1, ParkingLot p2) {
				// TODO Auto-generated method stub
				return (int) (p1.getDistance()-p2.getDistance());
			}});
		handler.sendEmptyMessage(SHOW);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				lvParkingLot.setAdapter(new MyAdapter());
				break;
			default:
				break;
			}
		}

	};
}
