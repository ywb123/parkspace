package com.wust.parkingspace;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wust.parking.domin.ParkingRecord;
import com.wust.parking.domin.Score;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parking.view.ColumnarView;
import com.wust.parking.view.DiagramView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;

public class ParkingRecordDataActivity extends Activity {

	protected static final int SHOW = 0;
	private RelativeLayout rlColumnar;
	private RelativeLayout rlDiagram;
	
	private int day=7;
	
	private List<Score> scores=new ArrayList<Score>();
	private List<Score> dayCounts=new ArrayList<Score>();
	private List<ParkingRecord> records=new ArrayList<ParkingRecord>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_record_data);
		rlColumnar=(RelativeLayout) findViewById(R.id.rlColumnar);
		rlDiagram=(RelativeLayout) findViewById(R.id.rlDiagram);
		
		getRecord();
	}

	/*****
	 * ��ȡ��¼ֵ
	 * *******/
	public void getRecord(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
                //��ȡ��Ӧ�������ļ�¼
                //��ȡ�û���Ϣ
                SharedPreferences sp = getSharedPreferences("LoginUserInfo", MODE_PRIVATE);
                int uid = sp.getInt("id",0);
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("action","queryByUserIdOfDays"));
                params.add(new BasicNameValuePair("userId",Integer.toString(uid)));
                params.add(new BasicNameValuePair("days",day+""));
                String jsonStr = HttpRequestUtil.postRequest(params,"ParkingRecordServlet");
                try {
                	records = new ArrayList<ParkingRecord>();
                    //��װÿһ����¼
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    for(int i = 0;i<jsonArray.length();i++){
                        JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Log.v("Json"+i,jsonArray.get(i).toString());
                        records.add(new ParkingRecord(
                                jsonObject.getInt("id"),
                                jsonObject.getInt("userId"),
                                jsonObject.getInt("parkingLotId"),
                                jsonObject.getLong("date"),
                                jsonObject.getLong("duration"),
                                jsonObject.getDouble("grade"),
                                jsonObject.getString("evaluation"),
                                jsonObject.getBoolean("isCharged")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(SHOW);
                
			}
		}).start();
	}
	public Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW:
				showColumnar();
				showDiagram();
				break;

			default:
				break;
			}
		}
		
	};
	/******
	 * ��ʾ����ͼ
	 * ********/
	public void showDiagram(){
		HashMap<String,Integer> dateCount=GetParkingCount();
		Object[] keys = dateCount.keySet().toArray();
		
		dayCounts=new ArrayList<Score>();
		for(int i=keys.length-1;i>=0;i--){
			Score s=new Score();
			s.date=keys[i].toString();
			s.score=dateCount.get(keys[i]);
			s.total=20;
			dayCounts.add(s);
		}
		rlDiagram.addView(new DiagramView(ParkingRecordDataActivity.this, dayCounts));
	}
	/******
	 * ��ʾ����ͼ
	 * *******/
	public void showColumnar(){
		scores=new ArrayList<Score>();
		for(int i=records.size()-1;i>=0;i--){
			Score s=new Score();
			s.date=new Date(records.get(i).getDate()).toLocaleString().substring(0, 10);
			s.score=(int) (records.get(i).getDuration()/1000);
			s.total=24*60*60;
			scores.add(s);
		}
		rlColumnar.addView(new ColumnarView(this, scores));
	}
	 /**
     * function:��ȡͣ������/ÿ��
     * return:������ͣ������ӳ��
     */
    public HashMap<String,Integer> GetParkingCount()
    {
        HashMap<String,Integer> dayCount = new HashMap<String, Integer>();
        
        //ͳ�Ʋ�ͬ������ͣ������
        for(int i = 0;i<records.size();i++){
            long date = records.get(i).getDate();
            String strday = new Date(date).toLocaleString().substring(0,10);
            if(!dayCount.containsKey(strday)){
                dayCount.put(strday,1);
            }
            else{
                dayCount.put(strday,dayCount.get(strday)+1);
            }
        }
        //���ͣ������Ϊ�������
        //ע�⣺����hashMap�Ĵ洢������ģ��ʸ���lingkedHashmap����������˳��һ����
        LinkedHashMap<String,Integer> map = new LinkedHashMap<String, Integer>();
        Date d=new Date();
        long now=d.getTime();
        for(int j=0;j<day;j++){
        	long before_days = now - (long) j * 1000 * 60 * 60 * 24;
        	String str=new Date(before_days).toLocaleString().substring(0, 10);
        	
        	if(dayCount.containsKey(str)){
        		map.put(str, dayCount.get(str));
        	}else{
        		map.put(str, 0);
        	}
        	
        }
        
        Log.i("map", map.toString());
        return map;
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parking_record_data, menu);
		return true;
	}

}
