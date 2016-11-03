package com.wust.parkingspace;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.wust.parking.util.HttpRequestUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EvaluateActivity extends Activity {
    protected static final int SUCCESS = 0;
	protected static final int ERROR = 1;
	//Bundle userInfo;
    int rid;    //停车记录id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
        //userInfo = getIntent().getExtras().getBundle("UserInfo");
        rid = getIntent().getExtras().getInt("rid");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.evaluate, menu);
        return true;
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

    public void onEvaluate(View view)
    {
    	//评分
        final RatingBar ratingBar = (RatingBar)this.findViewById(R.id.ratingBar);
        //文本评价
        final EditText etEvaluation = (EditText)this.findViewById(R.id.etEvaluation);
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("action","evaluate"));
                params.add(new BasicNameValuePair("rid",Integer.toString(rid)));
                params.add(new BasicNameValuePair("grade",Float.toString(ratingBar.getRating())));
                params.add(new BasicNameValuePair("evaluation",etEvaluation.getText().toString()));
                String jsonStr=HttpRequestUtil.postRequest(params,"ParkingRecordServlet");
                JSONObject json = null;
                String state= null;
                try {
                    json = new JSONObject(jsonStr);
                    state = json.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                if (state.equals("success")) {
                    msg.what = SUCCESS;
                    handler.sendMessage(msg);
                } else {
                    msg.what = ERROR;
                    handler.sendMessage(msg);
                }
            }
        }).start();
       
        
    }
    
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESS:
                    Toast.makeText(getApplicationContext(), "谢谢您的评价！", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    Toast.makeText(getApplicationContext(), "评价失败", Toast.LENGTH_LONG).show();
                    break;
                default:
                	break;
            }
        }
    };
}
