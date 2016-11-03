package com.wust.parkingspace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wust.parking.domin.ParkingLot;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.RefreshableView;
import com.wust.parking.view.ShowAmountView;
import com.wust.parking.view.RefreshableView.PullToRefreshListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParkingLotListActivity extends Activity {

	protected static final int SHOW = 0;
	private ListView lvParkingLot;
	private RefreshableView refreshParkingLot;
	ArrayList<ParkingLot> parkingLots = new ArrayList<ParkingLot>();
	private Spinner spGroup;
	private final String[] strSp=new String[]{"距离","车位","价格"};
	// Bundle userInfo;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_lot_list);
		lvParkingLot = (ListView) findViewById(R.id.lvParkingLot);
		refreshParkingLot = (RefreshableView) findViewById(R.id.refreshParkingLot);
		spGroup=(Spinner) findViewById(R.id.spGroup);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, strSp);
		spGroup.setAdapter(adapter);
		spGroup.setOnItemSelectedListener(new MyOnItemSelectedListener());
		double latitude = getIntent().getExtras().getDouble("latitude");
		double longitude = getIntent().getExtras().getDouble("longitude");
		LatLng position = new LatLng(latitude, longitude);
		for (ParkingLot pl : (ArrayList<ParkingLot>) getIntent().getExtras()
				.getSerializable("parkingLot")) {
			double distance = DistanceUtil.getDistance(position,
					new LatLng(pl.getLatitude(), pl.getLongitude()));
			pl.setDistance(distance);
			if (distance < 10000) {
				parkingLots.add(pl);
			}
		}
		Collections.sort(parkingLots, new Comparator<ParkingLot>(){

			@Override
			public int compare(ParkingLot p1, ParkingLot p2) {
				// TODO Auto-generated method stub
				return (int) (p1.getDistance()-p2.getDistance());
			}});
		lvParkingLot.setAdapter(new MyAdapter());
		lvParkingLot.setOnItemClickListener(new MyOnItemClickListener());
		if (parkingLots.size() == 0) {
			Toast.makeText(ParkingLotListActivity.this, "未发现附近有停车场",
					Toast.LENGTH_LONG).show();
		}

		refreshParkingLot.setOnRefreshListener(new PullToRefreshListener() {

			@Override
			public void onRefresh() {
				getParkingLotList();
			}
		}, 0);
	}

	/**
	 * 推荐选项选择监听
	 * */
	private class MyOnItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			switch (arg2) {
			case 0:
				Collections.sort(parkingLots, new Comparator<ParkingLot>(){
					@Override
					public int compare(ParkingLot p1, ParkingLot p2) {
						return (int) (p1.getDistance()-p2.getDistance());
					}});
				break;
			case 1:
				Collections.sort(parkingLots, new Comparator<ParkingLot>(){
					@Override
					public int compare(ParkingLot p1, ParkingLot p2) {
						// TODO Auto-generated method stub
						return (int) (p2.getSurplus()-p1.getSurplus());
					}});
				break;
			case 2:
				Collections.sort(parkingLots, new Comparator<ParkingLot>(){
					@Override
					public int compare(ParkingLot p1, ParkingLot p2) {
						// TODO Auto-generated method stub
						return (int) (p1.getPrice()-p2.getPrice());
					}});
				break;
			default:
				break;
			}
			lvParkingLot.setAdapter(new MyAdapter());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
		
	}
	
	private class MyOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// Toast.makeText(ParkingLotListActivity.this, "click",
			// Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(ParkingLotListActivity.this,
					ParkingLotDetailActivity.class);
			Bundle extras = new Bundle();
			extras.putSerializable("parkingLot", parkingLots.get(arg2));
			// extras.putBundle("UserInfo",userInfo);
			intent.putExtras(extras);
			startActivity(intent);

		}
	};

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return parkingLots.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return parkingLots.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = null;
			if (convertView == null) {
				v = LayoutInflater.from(ParkingLotListActivity.this).inflate(
						R.layout.parkinglotlist, null);
			} else {
				v = convertView;
			}
			
			TextView tvParkingLotDistance = (TextView) v
					.findViewById(R.id.tvParkingLotDistance);
			TextView tvParkingLotName = (TextView) v
					.findViewById(R.id.tvParkingLotName);
			TextView tvParkingLotLocation = (TextView) v
					.findViewById(R.id.tvParkingLotLocation);
			ShowAmountView sav = (ShowAmountView) v
					.findViewById(R.id.savAmount);
			tvParkingLotName.setText(parkingLots.get(position)
					.getParkingLotName());

			tvParkingLotLocation.setText("地址："
					+ parkingLots.get(position).getLocation());
			sav.setSum(parkingLots.get(position).getAmount());
			sav.setSur(parkingLots.get(position).getSurplus());
			tvParkingLotDistance.setText("距离："
					+ (int) parkingLots.get(position).getDistance() + "米");
			return v;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parking_lot_list, menu);
		return true;
	}

	public void getParkingLotList() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "queryAllParkingLot"));
		String servlet = "ParkingLotServlet";
		String result = HttpRequestUtil.postRequest(params, servlet);
		parkingLots = new ArrayList<ParkingLot>();
		try {
			JSONArray array = new JSONArray(result);
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				SharedPreferences sp=ParkingLotListActivity.this.getSharedPreferences("location", MODE_PRIVATE);
				int distance=(int) DistanceUtil.getDistance(new LatLng(json.getDouble("latitude"), json.getDouble("longitude")),
						new LatLng(sp.getFloat("searchLatitude", 0.0f), sp.getFloat("searchLongitude", 0.0f)));
				if(distance<10000){
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
		
		
		handler.sendEmptyMessage(SHOW);

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				spGroup.setSelection(spGroup.getSelectedItemPosition());
				//lvParkingLot.setAdapter(new MyAdapter());
				refreshParkingLot.finishRefreshing();
				Toast.makeText(ParkingLotListActivity.this, "更新成功",
						Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}

	};
}
