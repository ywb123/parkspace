package com.wust.parking.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wust.parking.domin.ParkingRecord;
import com.wust.parking.util.HttpRequestUtil;
import com.wust.parkingspace.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ParkingRecordFragment extends Fragment {
	
	protected static final int SHOW = 0;
	final String[] days = new String[]{"一周","十天","二十天"};
    final int[] daysNum = new int[]{7,10,20};
    ArrayList<ParkingRecord> parkingRecords = null;
    LinearLayout draw_layout;
    Button btBrokeLineRecord;
    Button btHistRecord;
    Spinner spinner;
    
    String[] xLabelTexts;
    double[] x;
    double[] y;
	
	public ParkingRecordFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_parkingrecord, container, false);
        
        parkingRecords = new ArrayList<ParkingRecord>();
        spinner = (Spinner)rootView.findViewById(R.id.sp_days);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,days);
        spinner.setAdapter(adapter);
        //得到资源
        draw_layout = (LinearLayout)rootView.findViewById(R.id.draw_layout);
        btHistRecord=(Button) rootView.findViewById(R.id.btHistRecord);
        btBrokeLineRecord=(Button) rootView.findViewById(R.id.btBrokeLineRecord);
        
        
        return rootView;
    }
	
            
   
    
    
    public Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch (msg.what) {
			case SHOW:
				//设置绘图参数
                long[] durations = new long[parkingRecords.size()];
                for(int i = 0;i<parkingRecords.size();i++){
                    durations[i] = parkingRecords.get(i).getDuration();
                }
				break;

			default:
				break;
			}
		}
    	
    	
    };

    /**
     * function:设置停车时长到坐标点
     */
    public void SetXYParkingDuration() {
        xLabelTexts = new String[parkingRecords.size()];
        x = new double[parkingRecords.size()];
        y = new double[parkingRecords.size()];
        for (int i = 0; i < parkingRecords.size(); i++) {
            if(i%2==0)
                xLabelTexts[i] = new Date(parkingRecords.get(i).getDate()).toLocaleString().substring(5,9);
            else
                xLabelTexts[i] = "";
            x[i] = i;
            y[i] = parkingRecords.get(i).getDuration() / (double) 1000;
        }
    }

    /**
     * function:获取停车次数/每天
     * return:天数与停车次数映射
     */
    public HashMap<String,Integer> GetParkingCount()
    {
        HashMap<String,Integer> dayCount = new HashMap<String, Integer>();
        //统计不同天数的停车次数
        for(int i = 0;i<parkingRecords.size();i++){
            long date = parkingRecords.get(i).getDate();
            String day = new Date(date).toLocaleString().substring(0,10);
            if(!dayCount.containsKey(day)){
                dayCount.put(day,1);
            }
            else{
                dayCount.put(day,dayCount.get(day)+1);
            }
        }
        return dayCount;
    }

    /**
     * function:设置每日停车次数到坐标点
     * @param dayCount
     */
    public void SetXYDayCount(HashMap<String,Integer> dayCount)
    {
        x = new double[dayCount.size()];
        y = new double[dayCount.size()];
        Object[] keys = dayCount.keySet().toArray();
        for(int i = 0;i<keys.length;i++){
            xLabelTexts[i] = keys[i].toString();
            x[i] = i;
            y[i] = dayCount.get(keys[i]);
        }
    }

    /**
     * 画柱状图
     */
    public GraphicalView DrawHist(double[] x, double[] y,String[] xLabelTexts)
    {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxesColor(Color.BLACK); //设置坐标轴黑色
        renderer.setLabelsColor(Color.BLACK);
        //设置网格
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GREEN);
        //设置表格标题
        renderer.setChartTitle("停车记录");
        renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
        renderer.setMarginsColor(0x008888);
        //设置坐标轴
        renderer.setXAxisMin(-0.5);//设置X轴的开始值
        renderer.setYAxisMin(0);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        if(xLabelTexts.length!=0){
            renderer.setXLabels(0);
            for(int i = 0;i<xLabelTexts.length;i++){
                renderer.addXTextLabel(i,xLabelTexts[i]);
            }
        }
        renderer.setBarSpacing(0.02);//设置间距
        renderer.setXTitle("停车日期");
        renderer.setYTitle("停车次数");

        renderer.setApplyBackgroundColor(true);
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setGradientStart(20,Color.BLUE);
        r.setGradientStop(100,Color.CYAN);
        r.setColor(Color.CYAN);
        renderer.addSeriesRenderer(r);
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries("停车次数");
        for(int i=0;i<x.length;i++){
            series.add(x[i], y[i]);
        }
        dataset.addSeries(series);
        GraphicalView chart = ChartFactory.getBarChartView(getActivity(), dataset, renderer, BarChart.Type.DEFAULT);
        return chart;
    }

    public GraphicalView DrawChartLine(double[] x,double[] y,String[] xLabelTexts)
    {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxesColor(Color.BLACK); //设置坐标轴黑色
        renderer.setLabelsColor(Color.BLACK);
        //设置网格
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GREEN);
        //设置表格标题
        renderer.setChartTitle("停车记录");
        renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
        renderer.setMarginsColor(0x008888);
        //设置坐标轴
        renderer.setXAxisMin(0);//设置X轴的开始值
        renderer.setYAxisMin(0);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        if(xLabelTexts.length!=0){
            renderer.setXLabels(0);
            for(int i = 0;i<xLabelTexts.length;i++){
                renderer.addXTextLabel(i,xLabelTexts[i]);
            }
        }

        renderer.setBarSpacing(0.02);//设置间距
        renderer.setXTitle("停车/次");
        renderer.setYTitle("时长/s");

        renderer.setApplyBackgroundColor(true);
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setPointStyle(PointStyle.TRIANGLE);//设置点的形状【三角形】
        r.setFillPoints(true);
        r.setLineWidth(2);
        r.setColor(Color.RED);
        renderer.addSeriesRenderer(r);
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries("停车时间");
        for(int i=0;i<x.length;i++){
            series.add(x[i], y[i]);
        }
        dataset.addSeries(series);
        GraphicalView chart = ChartFactory.getLineChartView(getActivity(), dataset, renderer);
        return chart;
    }



   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onActivityCreated(savedInstanceState);
    	btBrokeLineRecord.setOnClickListener(l );
    	btHistRecord.setOnClickListener(l);
    	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
            	Toast.makeText(getActivity().getApplicationContext(),Integer.toString(position),Toast.LENGTH_LONG).show();
                new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
		                //获取对应的天数的记录
		                //获取用户信息
		                SharedPreferences sp = getActivity().getSharedPreferences("LoginUserInfo", getActivity().MODE_PRIVATE);
		                int uid = sp.getInt("id",0);
						ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		                params.add(new BasicNameValuePair("action","queryByUserIdOfDays"));
		                params.add(new BasicNameValuePair("userId",Integer.toString(uid)));
		                params.add(new BasicNameValuePair("days",Integer.toString(daysNum[position])));
		                String jsonStr = HttpRequestUtil.postRequest(params,"ParkingRecordServlet");
		                try {
		                    parkingRecords = new ArrayList<ParkingRecord>();
		                    //封装每一条记录
		                    JSONArray jsonArray = new JSONArray(jsonStr);
		                    for(int i = 0;i<jsonArray.length();i++){
		                        JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
		                        Log.v("Json"+i,jsonArray.get(i).toString());
		                        parkingRecords.add(new ParkingRecord(
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
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    
    
    public OnClickListener l=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btBrokeLineRecord:
				 //绘制折线图【停车时间、时长】
				SetXYParkingDuration();
		        draw_layout.removeAllViews();
		        draw_layout.addView(DrawChartLine(x, y,xLabelTexts), new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
		                RelativeLayout.LayoutParams.MATCH_PARENT));
				break;
			case R.id.btHistRecord:
				//绘制柱状图【停车次数】
				draw_layout.removeAllViews();
		        SetXYDayCount(GetParkingCount());
		        draw_layout.addView(DrawHist(x,y,xLabelTexts),new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
		                RelativeLayout.LayoutParams.MATCH_PARENT));
				break;
			default:
				break;
			}
		}
	};
}
