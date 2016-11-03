package com.wust.parkingspace;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ReleaseParkingLotActivity extends Activity {

	private MapView showMapView=null;
	private BaiduMap mBaiduMap=null;
	private GeoCoder mSearch;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	BitmapDescriptor bitmap;
	//ActionBar action;
	
	private EditText etParkingLotName;
	private EditText etParkingPrice;
	private EditText etParkingAmount;
	private EditText etSearch;
	private Spinner spProvince;
	private Spinner spCity;
	
	private ArrayList<String> strings=new ArrayList<String>();
	private ArrayList<String> provinces=new ArrayList<String>();
	private ArrayList<String> provincesId=new ArrayList<String>();
	private ArrayList<String> citys=new ArrayList<String>();
	
	private String currentProvince="";
	private String currentCity="";
	private String selectedCity="";
	private String sLocation="";
	private double sLatitude=-1;
	private double sLongitude=-1;
	
	
	private boolean isFirstLocation=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext()); 
		setContentView(R.layout.activity_release_parking_lot);
		etParkingLotName=(EditText) findViewById(R.id.etParkingLotName);
		etParkingPrice=(EditText) findViewById(R.id.etParkingPrice);
		etParkingAmount=(EditText) findViewById(R.id.etParkingAmount);
		etSearch=(EditText) findViewById(R.id.etSearch);
		showMapView=(MapView) findViewById(R.id.showMapView);
		spProvince=(Spinner) findViewById(R.id.sProvince);
		spCity=(Spinner) findViewById(R.id.sCity);
		//构建Marker图标
		bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ico_marka);
		setMapView(30,114,17);
		
		//action=getActionBar();
		//action.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		new InitProvinceSpinner().execute("");
		setlocation();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.release_parking_lot, menu);
		return true;
	}
	
	/***
	 *添加actionbar
	 * **/
	public void addActionBar(){
		
	}
	//显示地图按钮
	public void btShowMap(View v){
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(listener);
		String strlocation=etSearch.getText().toString().trim();
		mSearch.geocode(new GeoCodeOption()
		.city(selectedCity).address(strlocation));		
	}
	//提交按钮
	public void btSubmit(View v){
		String strParkingLotName=etParkingLotName.getText().toString().trim();
		String strParkingPrice=etParkingPrice.getText().toString().trim();
		String strParkingAmount=etParkingAmount.getText().toString().trim();
		if("".equals(strParkingLotName)||"".equals(strParkingPrice)||"".equals(strParkingAmount)||"".equals(sLocation)||sLatitude==-1||sLongitude==-1){
			Toast.makeText(ReleaseParkingLotActivity.this, "信息不全", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(ReleaseParkingLotActivity.this, "停车场名称："+strParkingLotName+
					"\n停车场价格："+strParkingPrice+"元/小时\n停车位数目："+strParkingAmount+
					"个\nlatitude:"+sLatitude+"\nlongitude:"+sLongitude+
					"\nlacation:"+sLocation, Toast.LENGTH_SHORT).show();			
		}
	}
	 //设置地图
	public void setMapView(double loclatitude,double loclongitude,int zoom){
		
		mBaiduMap = showMapView.getMap();  
		
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  
		

		MapStatus status=new MapStatus.Builder()
							.zoom(zoom)
							.target(new LatLng(loclatitude, loclongitude)).build();
		MapStatusUpdate sUpdate=MapStatusUpdateFactory.newMapStatus(status);
		mBaiduMap.setMapStatus(sUpdate);

		 
		//mBaiduMap.setOnMapLongClickListener(new MyOnMapLongClickListener());
		mBaiduMap.setOnMapClickListener(new MyMapClickListener());
	}

	/*public class MyOnMapLongClickListener implements OnMapLongClickListener{

		@Override
		public void onMapLongClick(LatLng point) {
			// TODO Auto-generated method stub
			//构建MarkerOption，用于在地图上添加Marker
			mBaiduMap.clear();
			OverlayOptions option = new MarkerOptions()
			    .position(point)
			    .icon(bitmap);
			//在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			clickAgain=false;
		}
	}*/
	//单击地图事件
	public class MyMapClickListener implements OnMapClickListener{

		@Override
		public void onMapClick(LatLng point) {
			//构建MarkerOption，用于在地图上添加Marker
			mBaiduMap.clear();
			OverlayOptions option = new MarkerOptions()
			    .position(point)
			    .icon(bitmap);
			//在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			//地理反编
			mSearch = GeoCoder.newInstance();
			mSearch.setOnGetGeoCodeResultListener(listener);
			// 反Geo搜索
			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
					.location(point));	
		}

		@Override
		public boolean onMapPoiClick(MapPoi point) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	//MyOnMarkerClickListener实现
	public class MyOnMarkerClickListener implements OnMarkerClickListener{
		@Override
		public boolean onMarkerClick(Marker arg0) {
			// TODO Auto-generated method stub
			mBaiduMap.clear();
			return false;
		}
	}
	
	//
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
	    if(mLocationClient == null){
	    	Log.d("LocSDK4", "locClient is null ");
	    }
	    if (mLocationClient != null && mLocationClient.isStarted()){
	    	mLocationClient.requestLocation();
	    	mLocationClient.requestOfflineLocation();//离线请求
	    }
	    else{ 
	    	Log.d("LocSDK4", "locClient is null or not started");
	    }
	}
	//MyLocationListener
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			} 
			if(isFirstLocation){
				
				setMapView(location.getLatitude(), location.getLongitude(),17);
				currentProvince=location.getProvince();
				currentCity=location.getCity();
				
				isFirstLocation=false;
				for(int i=0;i<provinces.size();i++){
					if(currentProvince.equals(provinces.get(i)+"省")){
						spProvince.setSelection(i);
						//action.setSelectedNavigationItem(i);
						break;
					}
				}
				
			}
			
 
			Log.d("TAG",sb.toString());
		}
	}
	//地理解析监听事件
	private OnGetGeoCoderResultListener listener=new OnGetGeoCoderResultListener() {
		
		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			// TODO Auto-generated method stub
			Log.d("aaa", "888");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
				Toast.makeText(ReleaseParkingLotActivity.this, "未查询到信息", Toast.LENGTH_SHORT).show();
			}else{
				setMapView(result.getLocation().latitude, result.getLocation().longitude,12);
			}
			Log.d("aaa", "999");
		}
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			// TODO Auto-generated method stub
			Log.d("aaa", "666");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
				Toast.makeText(ReleaseParkingLotActivity.this, "未查询到信息", Toast.LENGTH_SHORT).show();
			}
			else{
				sLocation=result.getAddress();
				sLatitude=result.getLocation().latitude;
				sLongitude=result.getLocation().longitude;
				Toast.makeText(ReleaseParkingLotActivity.this, "反向编码:"+result.getLocation().latitude+"-"+result.getLocation().longitude+"\nlocation:"+result.getAddress(), Toast.LENGTH_SHORT).show();
			}
			Log.d("aaa", "777");
		}
		
	};
	
	//初始化省份spinner
	private class InitProvinceSpinner extends AsyncTask<String, Integer, String>{

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			ArrayAdapter<String> aa=new ArrayAdapter<String>(ReleaseParkingLotActivity.this,android.R.layout.simple_spinner_dropdown_item, provinces);
			
			
			/*action.setListNavigationCallbacks(aa, new OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					// TODO Auto-generated method stub
					new InitCitySpinner().execute(provincesId.get(itemPosition),provinces.get(itemPosition));
					return false;
				}
			});*/
			
			spProvince.setAdapter(aa);
			spProvince.setPrompt("请选择省份");
			spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					new InitCitySpinner().execute(provincesId.get(arg2),provinces.get(arg2));
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			getProvinces();
			return null;
		}
	}
	//初始化城市spinner
	private class InitCitySpinner extends AsyncTask<String, Integer, String>{

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			ArrayAdapter<String> aa=new ArrayAdapter<String>(ReleaseParkingLotActivity.this,android.R.layout.simple_spinner_dropdown_item, citys);
			
			
			/*action.setListNavigationCallbacks(aa, new OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					// TODO Auto-generated method stub
					selectedCity=citys.get(itemPosition);
					mSearch = GeoCoder.newInstance();
					mSearch.setOnGetGeoCodeResultListener(listener);
					mSearch.geocode(new GeoCodeOption()
					.city(selectedCity).address(selectedCity));	
					return false;
				}
			});
*/			
			
			spCity.setAdapter(aa);
			spCity.setPrompt("请选择城市");
			spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					selectedCity=citys.get(arg2);
					selectedCity=citys.get(arg2);
					mSearch = GeoCoder.newInstance();
					mSearch.setOnGetGeoCodeResultListener(listener);
					mSearch.geocode(new GeoCodeOption()
					.city(selectedCity).address(selectedCity));	
					//Toast.makeText(MainActivity.this, r+"-"+citys.get(arg2), Toast.LENGTH_SHORT).show();
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
				}
			});
			for(int i=0;i<citys.size();i++){
				if(currentCity.equals(citys.get(i)+"市")){
					spCity.setSelection(i);
					//action.setSelectedNavigationItem(i);
					break;
				}
				//Log.d(currentCity, citys.get(i));
			}
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			getCitys(params[0]);
			return params[1];
		}
	}
	//获取省份
	private void getProvinces() {
		String Url="http://webservice.webxml.com.cn/WebServices/WeatherWS.asmx/getRegionProvince";
		//String result="";
		InputStream resultInput=null;

		HttpGet request=new HttpGet(Url);
		HttpClient client=new DefaultHttpClient();
		try {
			HttpResponse response=client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的数据
				Log.d("aaa", "ok");
				resultInput=response.getEntity().getContent();
				//result = EntityUtils.toString(response.getEntity());
				
			}else{Log.d("aaa", "nothing");}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		parsexml(resultInput);
		provinces=new ArrayList<String>();
		provincesId=new ArrayList<String>();
		for(String s:strings){
			String[] str=s.split(",");
			//Log.d("aaa", str[0]);
			//Log.d("aaa", str[1]);
			provinces.add(str[0]);
			provincesId.add(str[1]);
			//Log.d("aaa", str[1]);
		}
		
	}
	//解析省份和城市信息
	public void parsexml(InputStream resultInput){
		XmlPullParser xp=Xml.newPullParser();
		try {
			xp.setInput(resultInput, "UTF-8");
			int eventType=xp.getEventType();
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					strings=new ArrayList<String>();
					break;
				case XmlPullParser.START_TAG:
					if(xp.getName().equals("string")){
						eventType=xp.next();
						strings.add(xp.getText());
					}
					break;
				case XmlPullParser.END_TAG:
	
					break;
				default:
					break;
				}
				eventType=xp.next();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//获取对应的城市
	private void getCitys(String id) {
		Log.d("111",id);
		String Url="http://webservice.webxml.com.cn/WebServices/WeatherWS.asmx/getSupportCityString?theRegionCode="+id;
		//String result="";
		InputStream resultInput=null;

		HttpGet request=new HttpGet(Url);
		HttpClient client=new DefaultHttpClient();
		try {
			HttpResponse response=client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的数据
				Log.d("aaa", "ok");
				resultInput=response.getEntity().getContent();
				//result = EntityUtils.toString(response.getEntity());
				
			}else{Log.d("aaa", "nothing");}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		parsexml(resultInput);
		citys=new ArrayList<String>();
		for(String s:strings){
			String[] str=s.split(",");
			Log.d("aaa", str[0]);
			Log.d("aaa", str[1]);
			citys.add(str[0]);
		}
	}
	
	 @Override
	public void onDestroy() {  
       super.onDestroy();
       showMapView.onDestroy();
       mLocationClient.stop();
       mSearch.destroy();
   } 
	 
   @Override
	public void onResume() {  
       super.onResume();
       showMapView.onResume(); 
       mLocationClient.start();
   } 
   
   @Override
	public void onPause() {  
       super.onPause();
       showMapView.onPause();  
       mLocationClient.stop();
   } 
}
