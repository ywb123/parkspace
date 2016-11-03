package com.wust.parking.fragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOvelray;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wust.parking.domin.ParkingLot;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parkingspace.ParkingLotDetailActivity;
import com.wust.parkingspace.ParkingLotListActivity;
import com.wust.parkingspace.R;
import com.wust.service.FloatListService;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainFragment extends Fragment {
	
	protected static final int SHOW_PARKINGLOT = 0;
	private MapView mMapView=null;
	private BaiduMap mBaiduMap=null;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private GeoCoder mSearch;
	private RoutePlanSearch routeSearch;
	private double searchLatitude=0;
	private double searchLongitude=0;
	private BDLocation myLocation=null;
	private DrivingRouteOvelray routeOverlay=null;
	
	private ArrayList<String> strings=new ArrayList<String>();
	private ArrayList<String> provinces=new ArrayList<String>();
	private ArrayList<String> provincesId=new ArrayList<String>();
	private ArrayList<String> citys=new ArrayList<String>();
	
	private String currentProvince="";
	private String currentCity="";
	private String selectedCity="";
	
	private Spinner spProvince;
	private Spinner spCity;
	private EditText etLocation;
	private Button btSetMapType;
	private ImageButton btSearch;
	private Button btLocation;
	private Button btShowParkingLot;
	
	private boolean isFirstLocation=true;
	private boolean isTypeNormal=true;
	
	private ArrayList<ParkingLot> parkingLots=new ArrayList<ParkingLot>();

    //private Bundle userInfo;

	
	
	public MainFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext()); 
       
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		
		spProvince=(Spinner) rootView.findViewById(R.id.province);
		spCity=(Spinner) rootView.findViewById(R.id.city);
		etLocation=(EditText) rootView.findViewById(R.id.etLocation);
		btSetMapType=(Button) rootView.findViewById(R.id.btSetMapType);
		btSearch=(ImageButton) rootView.findViewById(R.id.btSearch);
		btLocation=(Button) rootView.findViewById(R.id.btLocation);
		btShowParkingLot=(Button) rootView.findViewById(R.id.btShowParkingLot);
		mMapView = (MapView) rootView.findViewById(R.id.bmapsView);
		mBaiduMap=mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		new InitProvinceSpinner().execute("");
		setMapView(30.4472,114.271);
		setlocation();
		getParkingLotList();
        
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(listener);
        
		mBaiduMap.setOnMapClickListener(myMapClickListener);
		routeSearch=RoutePlanSearch.newInstance();
		routeSearch.setOnGetRoutePlanResultListener(myRouteListener);
		
		
		
        return rootView;
    }
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		btLocation.setOnClickListener(l);
		btSearch.setOnClickListener(l);
		btSetMapType.setOnClickListener(l);
		btShowParkingLot.setOnClickListener(l);
	}
	private OnClickListener l=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btLocation:
				if (mLocationClient != null && mLocationClient.isStarted()){
					isFirstLocation=true;
			    	mLocationClient.requestLocation();
			    	//mLocationClient.requestOfflineLocation();//离线请求
			    }
			    else{ 
			    	Log.d("LocSDK4", "locClient is null or not started");
			    	Toast.makeText(getActivity(), "locClient is null or not started", Toast.LENGTH_SHORT).show();
			    }
				break;
			case R.id.btSearch:
				
				String strlocation=etLocation.getText().toString().trim();
				mSearch.geocode(new GeoCodeOption()
				.city(selectedCity).address(strlocation));	
				
				PlanNode stNode = PlanNode.withCityNameAndPlaceName(selectedCity, myLocation.getAddrStr());
		        PlanNode enNode = PlanNode.withCityNameAndPlaceName(selectedCity, strlocation);

		        
		        routeSearch.drivingSearch((new DrivingRoutePlanOption())
		                    .from(stNode)
		                    .to(enNode));
				break;
			case R.id.btSetMapType:
				BaiduMap mBaiduMap = mMapView.getMap(); 
				isTypeNormal=!isTypeNormal;
				if(isTypeNormal){
					//普通地图
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); 
					btSetMapType.setTextColor(Color.WHITE);
					Drawable background=getResources().getDrawable(R.drawable.sat);
					btSetMapType.setBackground(background);
				}else{
					//卫星地图
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
					btSetMapType.setTextColor(Color.BLACK);
					Drawable background=getResources().getDrawable(R.drawable.map);
					btSetMapType.setBackground(background);
				}
				break;
			case R.id.btShowParkingLot:
				Intent intent=new Intent(getActivity(),ParkingLotListActivity.class);
				Bundle extras=new Bundle();
				extras.putSerializable("parkingLot", parkingLots);
				extras.putDouble("latitude", searchLatitude);
				extras.putDouble("longitude", searchLongitude);
		        //extras.putBundle("UserInfo",userInfo);
				intent.putExtras(extras);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
	};
	
	
	//
	public void setlocation(){
		mLocationClient = new LocationClient(getActivity().getApplicationContext());     //声明LocationClient类
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
	    	//mLocationClient.requestOfflineLocation();//离线请求
	    }
	    else{ 
	    	Log.d("LocSDK4", "locClient is null or not started");
	    }
	    
	   /* //位置提醒相关代码
        NotifyLister mNotifyer = new NotifyLister();
        mNotifyer.SetNotifyLocation(30.4472,114.271,3000,"gps");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
        mLocationClient.registerNotify(mNotifyer);
        //注册位置提醒监听事件后，可以通过SetNotifyLocation 来修改位置提醒设置，修改后立刻生效。

        //取消位置提醒
        // mLocationClient.removeNotifyEvent(mNotifyer);
        */
    }
	//BDNotifyListner实现
    public class NotifyLister extends BDNotifyListener{
       public void onNotify(BDLocation mlocation, float distance){
    	   Vibrator mVibrator01=(Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
    	   mVibrator01.vibrate(500);//振动提醒已到设定位置附近
       }
    }
    //设置地图
	public void setMapView(double loclatitude,double loclongitude){
		SharedPreferences sp=getActivity().getSharedPreferences("location", getActivity().MODE_PRIVATE);
		Editor edtior=sp.edit();
		edtior.putFloat("searchLatitude", (float) loclatitude);
		edtior.putFloat("searchLongitude", (float) loclongitude);
		edtior.commit();
		searchLatitude=loclatitude;
		searchLongitude=loclongitude;
		BaiduMap mBaiduMap = mMapView.getMap();  
		if(isTypeNormal){
			//矢量地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  
		}else{
			//卫星地图
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
		}

		MapStatus status=new MapStatus.Builder()
							.zoom(17)
							.target(new LatLng(loclatitude, loclongitude)).build();
		MapStatusUpdate sUpdate=MapStatusUpdateFactory.newMapStatus(status);
		mBaiduMap.setMapStatus(sUpdate);


		Button btLoc=new Button(getActivity());
		btLoc.setText("定位");
		btLoc.setId(1);
		//创建布局参数
	   	 MapView.LayoutParams layoutParam  = new MapView.LayoutParams(
	   	       //控件宽,继承自ViewGroup.LayoutParams
	   	       MapView.LayoutParams.WRAP_CONTENT,
	   	        //控件高,继承自ViewGroup.LayoutParams
	   	       MapView.LayoutParams.WRAP_CONTENT
	   	      );
		mMapView.addView(btLoc, layoutParam);
		
        //mBaiduMap.clear();

        //定义Maker坐标点
		/*LatLng point = new LatLng(loclatitude, loclongitude);
		//构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
		    .fromResource(R.drawable.ico_marka);
		//构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions()
		    .position(point)
		    .icon(bitmap);
		//在地图上添加Marker，并显示
		 mBaiduMap.addOverlay(option);
		
		mBaiduMap.setOnMarkerClickListener(new MyOnMarkerClickListener());*/
		
	}
	//MyLocationListener
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
			myLocation=location;
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
			Log.d("TAG",sb.toString());
			MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
			// 此处设置开发者获取到的方向信息，顺时针0-360
			.latitude(location.getLatitude())
			.longitude(location.getLongitude())
			.build();
			mBaiduMap.setMyLocationData(locData);
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
					com.baidu.mapapi.map.MyLocationConfigeration.LocationMode.NORMAL, true, null));
			
			/*if(searchLatitude!=0 && searchLongitude!=0){
				LatLng searchPoint=new LatLng(searchLatitude,searchLongitude);
				LatLng locationPoint=new LatLng(location.getLatitude(),location.getLongitude());
				List<LatLng> points=new ArrayList<LatLng>();
				points.add(locationPoint);
				points.add(searchPoint);
				if(ooPolyline!=null){
					ooPolyline.
				}
				OverlayOptions ooline = new PolylineOptions().width(10)
						.color(0xAAFF0000).points(points);
				mBaiduMap.addOverlay(ooline);
				
			}*/
			if(isFirstLocation){
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				
				setMapView(location.getLatitude(), location.getLongitude());
				currentProvince=location.getProvince();
				currentCity=location.getCity();
				
				isFirstLocation=false;
				for(int i=0;i<provinces.size();i++){
					if(currentProvince.equals(provinces.get(i)+"省")){
						spProvince.setSelection(i);
						break;
					}
				}
				
			}
			
 
			//Toast.makeText(getActivity(), "time:"+location.getTime()+"\nspeed:"+location.getSpeed(), Toast.LENGTH_SHORT).show();
			System.out.println("speed:"+location.getSpeed()+"m/s");
			Log.d("TAG",sb.toString());
		}
	}
	//MyOnMarkerClickListener实现
	public class MyOnMarkerClickListener implements OnMarkerClickListener{

		@Override
		public boolean onMarkerClick(Marker arg0) {
			// TODO Auto-generated method stub
			mBaiduMap=mMapView.getMap();
			final ParkingLot pl=(ParkingLot)arg0.getExtraInfo().getSerializable("parkingLot");
			//创建InfoWindow展示的view
			Button button = new Button(getActivity().getApplicationContext());  
			button.setBackgroundResource(R.drawable.popup);
			button.setAlpha(0.6f);
			//button.setBackgroundColor(Color.rgb(0x15, 0xA0, 0xF5));
			button.setTextColor(Color.rgb(0x15, 0xA0, 0xF5));
			button.setPadding(20, 10, 20, 30);
			button.setText(pl.getParkingLotName()+"\n"+pl.getLocation()
					+"\n车位数："+pl.getAmount()+"/"+pl.getSurplus());
			//定义用于显示该InfoWindow的坐标点
			final LatLng pt = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude); 
			
			//创建InfoWindow的点击事件监听者
			OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {  
			    public void onInfoWindowClick() {  
			    //添加点击后的事件响应代码
			    	mBaiduMap.hideInfoWindow();
			    	int distance=(int) DistanceUtil.getDistance(pt, new LatLng(searchLatitude,searchLongitude));
			    	pl.setDistance(distance);
			    	Intent intent=new Intent(getActivity(),ParkingLotDetailActivity.class);
					Bundle extras=new Bundle();
					extras.putSerializable("parkingLot", pl);
			        //extras.putBundle("UserInfo",userInfo);
					intent.putExtras(extras);
					startActivity(intent);
			    }  
			}; 
			OnLongClickListener l=new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					//Toast.makeText(MainActivity.this, "长按进入详细界面", Toast.LENGTH_SHORT).show();
					
					return false;
				}
			};
			button.setOnLongClickListener(l);
			//创建InfoWindow
			InfoWindow mInfoWindow = new InfoWindow(button, pt, listener); 
			
			//显示InfoWindow
			mBaiduMap.showInfoWindow(mInfoWindow);
			return false;
		}
		
	}
	
	private OnMapClickListener myMapClickListener=new OnMapClickListener() {
		
		@Override
		public boolean onMapPoiClick(MapPoi arg0) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public void onMapClick(LatLng arg0) {
			// TODO Auto-generated method stub
			mBaiduMap.hideInfoWindow();
		}
	};
	
	private OnGetRoutePlanResultListener myRouteListener=new OnGetRoutePlanResultListener() {
		@Override
		public void onGetWalkingRouteResult(WalkingRouteResult result) {}
		@Override
		public void onGetTransitRouteResult(TransitRouteResult result) {}
		@Override
		public void onGetDrivingRouteResult(DrivingRouteResult result) {
			// TODO Auto-generated method stub
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
	            Toast.makeText(getActivity(), "抱歉，未找到合适路线", Toast.LENGTH_SHORT).show();
	        }
	        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
	            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
	            //result.getSuggestAddrInfo()
	            return;
	        }
	        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
	            /*nodeIndex = -1;
	            mBtnPre.setVisibility(View.VISIBLE);
	            mBtnNext.setVisibility(View.VISIBLE);
	            route = result.getRouteLines().get(0);*/
	        	if(routeOverlay!=null){routeOverlay.removeFromMap();}
	            DrivingRouteOvelray overlay = new MyDrivingRouteOverlay(mBaiduMap);
	            routeOverlay = overlay;
	            mBaiduMap.setOnMarkerClickListener(overlay);
	            overlay.setData(result.getRouteLines().get(0));
	            overlay.addToMap();
	            overlay.zoomToSpan();
	        }
		}
	};
	//OnGetGeoCoderResultListener对象
	private OnGetGeoCoderResultListener listener=new OnGetGeoCoderResultListener() {
		
		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			// TODO Auto-generated method stub
			Log.d("aaa", "888");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
				Toast.makeText(getActivity(), "未查询到信息", Toast.LENGTH_SHORT).show();
			}else{
				setMapView(result.getLocation().latitude, result.getLocation().longitude);
				//Toast.makeText(MainActivity.this,"onGetGeoCodeResult:"+ result.getLocation().latitude+"-"+result.getLocation().longitude, Toast.LENGTH_SHORT).show();
			}
			Log.d("aaa", "999");
		}
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			// TODO Auto-generated method stub
			Log.d("aaa", "666");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
				Toast.makeText(getActivity(), "未查询到信息", Toast.LENGTH_SHORT).show();
			}
			else{
				//Toast.makeText(MainActivity.this, "onGetReverseGeoCodeResult:"+result.getLocation().latitude+"-"+result.getLocation().longitude, Toast.LENGTH_SHORT).show();
			}
			Log.d("aaa", "777");
		}
		
	};
	
	//定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOvelray {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        }
    }
	//初始化省份spinner
	private class InitProvinceSpinner extends AsyncTask<String, Integer, String>{

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			ArrayAdapter<String> aa=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item, provinces);
			
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
			final String r=result;
			ArrayAdapter<String> aa=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item, citys);
			
			spCity.setAdapter(aa);
			spCity.setPrompt("请选择城市");
			spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					selectedCity=citys.get(arg2);
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
	
	//
	public void getParkingLotList(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<NameValuePair> params=new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("action", "queryAllParkingLot"));
				String servlet="ParkingLotServlet";
				String result=HttpRequestUtil.postRequest(params, servlet);
				try {
					JSONArray array=new JSONArray(result);
					for(int i=0;i<array.length();i++){
						JSONObject json=array.getJSONObject(i);
						ParkingLot pl=new ParkingLot();
						pl.setAmount(json.getInt("amount"));
						pl.setGrade(json.getDouble("grade"));
						pl.setId(json.getInt("id"));
						pl.setLatitude(json.getDouble("latitude"));
						pl.setLocation(json.getString("location"));
						pl.setLongitude(json.getDouble("longitude"));
						pl.setParkingLotName(json.getString("parkingLotName"));
						pl.setPrice(json.getDouble("price"));
						pl.setSurplus(json.getInt("surplus"));
						parkingLots.add(pl);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(SHOW_PARKINGLOT);
			}
		}).start();
		
		
	}
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_PARKINGLOT:
				markParkingLot();
				break;

			default:
				break;
			}
		}
		
	};
	//在地图上标记停车场位置
	public void markParkingLot(){
		mBaiduMap = mMapView.getMap();  
		//定义Maker坐标点
		for(ParkingLot pl:parkingLots){
			LatLng point = new LatLng(pl.getLatitude(), pl.getLongitude());  
			//构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory  
			    .fromResource(R.drawable.ico_marka);  
			//构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions()
			    .position(point)  
			    .icon(bitmap); 
			//在地图上添加Marker，并显示
			Marker marker=(Marker) mBaiduMap.addOverlay(option);
			Bundle data=new Bundle();
			data.putSerializable("parkingLot", pl);
			marker.setExtraInfo(data);
		}
		mBaiduMap.setOnMarkerClickListener(new MyOnMarkerClickListener());
	}
	
	
	

	 @Override
	public void onDestroy() {  
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stop();
        mSearch.destroy();
    } 
	 
    @Override
	public void onResume() {  
        super.onResume();
        mMapView.onResume(); 
        mLocationClient.start();
        Intent i=new Intent(getActivity(),FloatListService.class);
		getActivity().startService(i);
    } 
    
    @Override
	public void onPause() {  
        super.onPause();
        mMapView.onPause();  
        mLocationClient.stop();
        Intent i=new Intent(getActivity(),FloatListService.class);
		getActivity().stopService(i);
    } 
}
