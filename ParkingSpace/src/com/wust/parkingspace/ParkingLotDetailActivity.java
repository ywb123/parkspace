package com.wust.parkingspace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wust.parking.domin.ParkingLot;
import com.wust.parking.domin.UserInfo;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.ShowAmountView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParkingLotDetailActivity extends Activity {

	protected static final int NOT_PARKING = 0;
	protected static final int PARKING = 1;
	protected static final int SUCCESS_CHROM = 2;
	protected static final int FAIL_CHROM = 3;
	protected static final int STOP_CHROM = 4;
	protected static final int PARKING_HERE = 5;
	TextView tvParkingLotDistance;
	TextView tvParkingLotName;
	TextView tvParkingLotLocation;
	TextView tvParkingLotPrice;
	TextView tvParkingLotGrade;
	RatingBar rbGrade;
	Button btParking;
	ShowAmountView savAmount;
	Chronometer chronometer;
	ProgressDialog progressDialog;
	// Bundle userInfo;
	UserInfo userInfo = new UserInfo();

	ParkingLot pl = new ParkingLot();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_lot_detail);
		tvParkingLotDistance = (TextView) findViewById(R.id.tvParkingLotDistance);
		tvParkingLotName = (TextView) findViewById(R.id.tvParkingLotName);
		tvParkingLotLocation = (TextView) findViewById(R.id.tvParkingLotLocation);
		tvParkingLotPrice = (TextView) findViewById(R.id.tvParkingLotPrice);
		tvParkingLotGrade = (TextView) findViewById(R.id.tvParkingLotGrade);
		rbGrade = (RatingBar) findViewById(R.id.rtGrade);
		btParking = (Button) this.findViewById(R.id.btParking);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		progressDialog = new ProgressDialog(this.getApplicationContext());
		savAmount = (ShowAmountView) findViewById(R.id.savAmount);
		
		
		pl = (ParkingLot) getIntent().getExtras().getSerializable("parkingLot");
		tvParkingLotDistance.setText("距离：" + (int) pl.getDistance() + "米");
		tvParkingLotName.setText(pl.getParkingLotName());
		tvParkingLotLocation.setText("地址：" + pl.getLocation());
		tvParkingLotPrice.setText("价格：" + pl.getPrice() + "/元");
		tvParkingLotGrade.setText("评分：" + pl.getGrade());
		rbGrade.setRating((float) pl.getGrade());
		savAmount.setSum(pl.getAmount());
		savAmount.setSur(pl.getSurplus());
		// userInfo = getIntent().getExtras().getBundle("UserInfo");
		// Log.v("aaaaaa", userInfo.getString("licenseNumber"));
		SharedPreferences sp = ParkingLotDetailActivity.this
				.getSharedPreferences("LoginUserInfo", MODE_PRIVATE);

		userInfo.setLicenseNumber(sp.getString("licenseNumber", ""));
		userInfo.setId(sp.getInt("id", 0));
		userInfo.setUserName(sp.getString("userName", ""));
		userInfo.setPayAcount(sp.getString("payAcount", ""));
		userInfo.setCarModel(sp.getString("carModel", ""));
		
		updateParkingLot();
	}

	// 停车按钮
	public void btParking(View v) throws JSONException {
		String clockState = btParking.getText().toString();
		if (clockState.equals("开始停车")) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 将停车请求发送给服务器端
					ArrayList<NameValuePair> paras = new ArrayList<NameValuePair>();
					paras.add(new BasicNameValuePair("action", "startParking"));
					paras.add(new BasicNameValuePair("licenseNumber", userInfo
							.getLicenseNumber()));
					paras.add(new BasicNameValuePair("id", Integer.toString(pl
							.getId())));
					paras.add(new BasicNameValuePair("uid", Integer.toString(userInfo
							.getId())));
					String jsonStr = HttpRequestUtil.postRequest(paras,
							"ParkingLotServlet");
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
					Message msg = new Message();
					if (state.equals("success")) {
						msg.what = SUCCESS_CHROM;
						handler.sendMessage(msg);
					} else {
						msg.what = FAIL_CHROM;
						handler.sendMessage(msg);
					}
				}
			}).start();

		}
		// 如果是停止计时
		else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.what = STOP_CHROM;
					handler.sendMessage(msg);

					// 告知服务器并获取参数
					ArrayList<NameValuePair> paras = new ArrayList<NameValuePair>();
					paras.add(new BasicNameValuePair("action", "stopParking"));
					paras.add(new BasicNameValuePair("licenseNumber", userInfo
							.getLicenseNumber()));
					paras.add(new BasicNameValuePair("id", Integer.toString(pl
							.getId())));
					paras.add(new BasicNameValuePair("uid", Integer.toString(userInfo
							.getId())));
					String jsonStr = HttpRequestUtil.postRequest(paras,
							"ParkingLotServlet");
					// 提取json字段
					JSONObject json = null;
					String state = null;
					try {
						json = new JSONObject(jsonStr);
						state = json != null ? json.getString("result") : null;
					} catch (JSONException e) {
						e.printStackTrace();
					}

					if (state.equals("success")) {
						// 获取要传递的参数
						int rid = 0; // 停车记录id
						long time = 0;
						double price = 0;
						double pay = 0;
						try {
							rid = json.getInt("rid");
							time = json.getLong("duration");
							price = json.getDouble("price");
							pay = json.getDouble("pay");
						} catch (JSONException e) {
						}

						// 跳转到结算界面
						Intent intent = new Intent();
						intent.setClass(getApplicationContext(),
								PayActivity.class);
						Bundle bundle = new Bundle();
						bundle.putInt("rid", rid);
						bundle.putInt("pid", pl.getId());
						bundle.putLong("duration", time);
						bundle.putDouble("price", price);
						bundle.putDouble("pay", pay);
						// bundle.putBundle("UserInfo",userInfo);
						intent.putExtras(bundle);
						startActivity(intent);
						finish();
					}
				}
			}).start();

		}
	}

	// 更新停车场信息
	public void updateParkingLot() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 将请求发送给服务器端
				ArrayList<NameValuePair> paras = new ArrayList<NameValuePair>();
				paras.add(new BasicNameValuePair("action", "getParkingTime"));
				paras.add(new BasicNameValuePair("id", Integer.toString(pl
						.getId())));
				paras.add(new BasicNameValuePair("licenseNumber", userInfo
						.getLicenseNumber()));
				String jsonStr = HttpRequestUtil.postRequest(paras,"ParkingLotServlet");
				Log.v("Json", jsonStr);
				// 提取json字段
				JSONObject json = null;
				int parkingId = 0;
				Message msg = new Message();
				try {
					json = new JSONObject(jsonStr);

					parkingId = json.getInt("parkingId");
					pl.setAmount(json.getInt("amount"));
					pl.setGrade(json.getDouble("grade"));
					pl.setSurplus(json.getInt("surplus"));
					if (parkingId == 0) {
						// 该车暂时没找到车位
						msg.what = NOT_PARKING;
					} else if (parkingId == pl.getId()) {
						msg.what = PARKING_HERE;
						Bundle data = new Bundle();
						data.putLong("time", json.getLong("time"));
						data.putInt("parkingAmount", json.getInt("parkingAmount"));
						data.putInt("parkingSurplus", json.getInt("parkingSurplus"));
						msg.setData(data);
					} else {
						msg.what = PARKING;
						Bundle data = new Bundle();
						data.putInt("parkingId", json.getInt("parkingId"));
						data.putString("parkingName",json.getString("parkingName"));
						data.putLong("time", json.getLong("time"));
						data.putDouble("parkingLatitude", json.getDouble("parkingLatitude"));
						data.putDouble("parkingLongitude", json.getDouble("parkingLongitude"));
						data.putInt("parkingAmount", json.getInt("parkingAmount"));
						data.putInt("parkingSurplus", json.getInt("parkingSurplus"));
						data.putDouble("parkingGrade", json.getDouble("parkingGrade"));
						data.putDouble("parkingPrice", json.getDouble("parkingPrice"));
						data.putString("parkingLocation", json.getString("parkingLocation"));
						Log.v("a", json.getDouble("parkingLatitude")+"");
						Log.v("a", json.getString("parkingName")+"");
						msg.setData(data);
					}
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	// handler的初始化
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			Animation animation;
			switch (msg.what) {
			
			case SUCCESS_CHROM:
				Toast.makeText(getApplicationContext(), "开始计时",
						Toast.LENGTH_SHORT).show();
				animation=AnimationUtils.loadAnimation(ParkingLotDetailActivity.this, R.anim.translateshow);
				btParking.startAnimation(animation);
				btParking.setText("结束计时");
				// 开始计时器
				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.start();
				break;
			case FAIL_CHROM:
				Toast.makeText(getApplicationContext(), "车位满了",
						Toast.LENGTH_LONG).show();
				break;
			case STOP_CHROM:
				// 停止计时器
				animation=AnimationUtils.loadAnimation(ParkingLotDetailActivity.this, R.anim.translategone);
				btParking.startAnimation(animation);
				chronometer.stop();
				chronometer.setBase(SystemClock.elapsedRealtime());
				break;
			case NOT_PARKING:
				tvParkingLotGrade.setText("评分：" + (float)pl.getGrade());
				rbGrade.setRating((float) pl.getGrade());
				savAmount.setSum(pl.getAmount());
				savAmount.setSur(pl.getSurplus());
				break;
			case PARKING_HERE:
				chronometer.setBase(SystemClock.elapsedRealtime()
						- msg.getData().getLong("time"));
				Log.v("time", msg.getData().getLong("time") + "");
				btParking.setText("结束停车");
				savAmount.setSur(msg.getData().getInt("parkingSurplus"));
				savAmount.setSum(msg.getData().getInt("parkingAmount"));
				chronometer.start();
				break;
			case PARKING:
				//btParking.setClickable(false);
				chronometer.setBase(SystemClock.elapsedRealtime()
						- msg.getData().getLong("time"));

				btParking.setText("您的车正停在"
						+ msg.getData().getString("parkingName"));
				chronometer.start();
				
				SharedPreferences sp=ParkingLotDetailActivity.this.getSharedPreferences("location", MODE_PRIVATE);
				int distance=(int) DistanceUtil.getDistance(new LatLng(msg.getData().getDouble("parkingLatitude"), msg.getData().getDouble("parkingLongitude")),
						new LatLng(sp.getFloat("searchLatitude", 0.0f), sp.getFloat("searchLongitude", 0.0f)));
				final ParkingLot plot=new ParkingLot();
				plot.setAmount(msg.getData().getInt("parkingAmount"));
				plot.setDistance(distance);
				plot.setGrade(msg.getData().getDouble("parkingGrade"));
				plot.setId(msg.getData().getInt("parkingId"));
				plot.setLatitude(msg.getData().getDouble("parkingLatitude"));
				plot.setLocation(msg.getData().getString("parkingLocation"));
				plot.setLongitude(msg.getData().getDouble("parkingLongitude"));
				plot.setParkingLotName(msg.getData().getString("parkingName"));
				plot.setPrice(msg.getData().getDouble("parkingPrice"));
				plot.setSurplus(msg.getData().getInt("parkingSurplus"));
				Log.v("time", msg.getData().getString("parkingName"));
				btParking.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(ParkingLotDetailActivity.this,ParkingLotDetailActivity.class);
						Bundle extras=new Bundle();
						extras.putSerializable("parkingLot",plot);
			            //extras.putBundle("UserInfo",userInfo);
						intent.putExtras(extras);
						startActivity(intent);
					}
				});
				break;
			default:
				break;
			}
		}
	};
}
