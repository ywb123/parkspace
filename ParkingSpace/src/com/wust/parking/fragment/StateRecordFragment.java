package com.wust.parking.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wust.parking.domin.ParkingRecord;
import com.wust.parking.domin.ParkingLot;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.RefreshableView;
import com.wust.parking.view.RefreshableView.PullToRefreshListener;
import com.wust.parkingspace.ParkingLotDetailActivity;
import com.wust.parkingspace.ParkingRecordDataActivity;
import com.wust.parkingspace.PayActivity;
import com.wust.parkingspace.R;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StateRecordFragment extends Fragment {
	protected static final int SHOW = 0;

	private ListView lvRecord;
	private Button btShowData;
	private RefreshableView refreshRecord;
	
	private ArrayList<ParkingRecord> records=new ArrayList<ParkingRecord>();
	private ArrayList<ParkingLot> parkingLots=new ArrayList<ParkingLot>();
	private ArrayList<Double> pays=new ArrayList<Double>();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_staterecord, container, false);
		lvRecord=(ListView) rootView.findViewById(R.id.lvRecord);
		refreshRecord=(RefreshableView) rootView.findViewById(R.id.refreshRecord);
		btShowData=(Button) rootView.findViewById(R.id.btShowData);
		btShowData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(),ParkingRecordDataActivity.class);
				startActivity(intent);
			}
		});
		show();
		refreshRecord.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				getRecords();
				
			}
		}, 0);
		lvRecord.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(),ParkingLotDetailActivity.class);
				Bundle extras=new Bundle();
				SharedPreferences sp=getActivity().getSharedPreferences("location", getActivity().MODE_PRIVATE);
				int distance=(int) DistanceUtil.getDistance(new LatLng(parkingLots.get(arg2).getLatitude(), parkingLots.get(arg2).getLongitude()),
						new LatLng(sp.getFloat("searchLatitude", 0.0f), sp.getFloat("searchLongitude", 0.0f)));
				parkingLots.get(arg2).setDistance(distance);
				extras.putSerializable("parkingLot", parkingLots.get(arg2));
				intent.putExtras(extras);
				startActivity(intent);
				
			}
		});
		return rootView;
		
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
				v=LayoutInflater.from(getActivity()).inflate(R.layout.record_item, null);
			}else{
				v=convertView;
			}
			TextView tvPName=(TextView) v.findViewById(R.id.tvPName);
			TextView tvPayment=(TextView) v.findViewById(R.id.tvPayment);
			Button btIsPay=(Button) v.findViewById(R.id.btIsPay);
			TextView tvdate=(TextView) v.findViewById(R.id.tvDate);
			
			tvPName.setText(parkingLots.get(position).getParkingLotName());
			SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日HH时");
			String date=format.format(new Date(records.get(position).getDate()));
			tvdate.setText(date);
			tvPayment.setText("总价:"+pays.get(position)+"元");
			if(records.get(position).getIsPay()){
				//已付款状态
				btIsPay.setBackground(getResources().getDrawable(R.drawable.background_button_blue));
				btIsPay.setClickable(false);
				btIsPay.setText("付款成功");
			}else{
				//未付款
				btIsPay.setBackground(getResources().getDrawable(R.drawable.background_button_red));
				btIsPay.setText("立即支付");
				btIsPay.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// 跳转到结算界面
						Intent intent = new Intent(getActivity(),PayActivity.class);
						Bundle bundle = new Bundle();
						bundle.putInt("rid", records.get(position).getId());
						bundle.putInt("pid", parkingLots.get(position).getId());
						bundle.putLong("duration", records.get(position).getDuration());
						bundle.putDouble("price", parkingLots.get(position).getPrice());
						bundle.putDouble("pay", pays.get(position));
						// bundle.putBundle("UserInfo",userInfo);
						intent.putExtras(bundle);
						startActivity(intent);
						//Toast.makeText(getActivity(), "跳转到付款界面", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			return v;
		}
		
	}
	
	public void getRecords() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				"LoginUserInfo", getActivity().MODE_PRIVATE);
		final int uid = sp.getInt("id", 0);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "queryByUserId"));
		params.add(new BasicNameValuePair("userId", Integer.toString(uid)));
		String jsonStr = HttpRequestUtil.postRequest(params,
				"ParkingRecordServlet");
		try {
			records = new ArrayList<ParkingRecord>();
			parkingLots = new ArrayList<ParkingLot>();
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
				ParkingLot p = new ParkingLot();
				p.setAmount(jsonObject.getInt("amount"));
				p.setGrade(jsonObject.getDouble("grade"));
				p.setId(jsonObject.getInt("parkingLotId"));
				p.setLatitude(jsonObject.getDouble("latitude"));
				p.setLongitude(jsonObject.getDouble("longitude"));
				p.setLocation(jsonObject.getString("location"));
				p.setParkingLotName(jsonObject.getString("parkingLotName"));
				p.setPrice(jsonObject.getDouble("price"));
				p.setSurplus(jsonObject.getInt("surplus"));
				parkingLots.add(p);
				pays.add(jsonObject.getDouble("pay"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Collections.reverse(records);
		Collections.reverse(pays);
		Collections.reverse(parkingLots);

		handler.sendEmptyMessage(SHOW);
	}

	public Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				lvRecord.setAdapter(new MyAdapter());
				refreshRecord.finishRefreshing();
				if (records.size() == 0) {
					Toast.makeText(getActivity(), "没有停车记录", Toast.LENGTH_LONG)
							.show();
				}else{
					Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG)
					.show();
				}
				break;
			default:
				break;
			}
		}
	};
}
