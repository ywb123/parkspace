package com.wust.parkingspace;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.wust.parking.util.HttpRequestUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class PayActivity extends Activity {
	
	/**
     * - Set to PaymentActivity.ENVIRONMENT_PRODUCTION to move real money.
     *
     * - Set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AVmPRxBFOK945Cged4VIu7vSHRSt5O0yVoutl4_1KKAtiTOwaYL99DmBcWzz";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

	private static final String TAG = "TAG";
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
                    // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("ParkingSpace");

	
	
	
    //Bundle userInfo;
    int rid;    //停车记录id
    int pid;    //停车站id
    long time;  //停车时间
    double price;   //停车单价
    double pay; //停车花费
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        //获取传递的数据
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        rid = bundle.getInt("rid");
        pid = bundle.getInt("pid");
        //userInfo = getIntent().getExtras().getBundle("UserInfo");

        time = bundle.getLong("duration");
        price = bundle.getDouble("price");
        pay = bundle.getDouble("pay");

        //显示数据
        TextView tvPrice = (TextView)this.findViewById(R.id.tvPrice);
        Chronometer croTime = (Chronometer)this.findViewById(R.id.croTime);
        TextView tvPay = (TextView)this.findViewById(R.id.tvPay);

        tvPrice.setText(Double.toString(price)+"元/小时");
        croTime.setBase(SystemClock.elapsedRealtime()-time);
        tvPay.setText(Double.toString(pay)+"RMB");
        
        
        Intent intent1 = new Intent(this, PayPalService.class);
        intent1.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent1);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pay, menu);
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

    //付款
    public void onPay(View view)
    {
    	/*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to PAYMENT_INTENT_AUTHORIZE to only authorize payment and
         * capture funds later.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(PayActivity.this, PaymentActivity.class);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
        
        
        Toast.makeText(this,"付款成功，来评价一下吧！",Toast.LENGTH_SHORT);
        //付款成功，将此次停车记录的isCharged更新为true
        setIsChargedTrue();
        
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("评价这次停车？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), EvaluateActivity.class);
                        Bundle bundle = new Bundle();
                        //bundle.putBundle("UserInfo",userInfo);
                        bundle.putInt("rid",rid);
                        bundle.putSerializable("pid",pid);
                        bundle.putSerializable("time",time);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent =new Intent(PayActivity.this,MainActivity.class);
            	startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }
    
    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(pay), "USD", "hipster jeans",
                paymentIntent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        Toast.makeText(
                                getApplicationContext(),
                                "PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(
                                getApplicationContext(),
                                "Future Payment code received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }

    /*****
     * 将服务器端的停车记录ischarged设为true
     * ********/
    private void setIsChargedTrue(){
    	new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 将停车请求发送给服务器端
				ArrayList<NameValuePair> paras = new ArrayList<NameValuePair>();
				paras.add(new BasicNameValuePair("action", "sureCharge"));
				paras.add(new BasicNameValuePair("rid", Integer.toString(rid)));
				String jsonStr = HttpRequestUtil.postRequest(paras,
						"ParkingRecordServlet");
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
				
				if (state.equals("success")) {
					Log.d("charge", "ok");
				} else {
					Log.d("charge", "error");
				}
			}
    		
    	}).start();
    }
    
	private void sendAuthorizationToServer(PayPalAuthorization auth) {
		// TODO Auto-generated method stub
		
	}

}
