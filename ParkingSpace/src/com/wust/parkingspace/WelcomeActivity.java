package com.wust.parkingspace;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_welcome);
	}


	public void carOwner(View v){
		Intent intent=new Intent(WelcomeActivity.this,LoginActivity.class);
		startActivity(intent);
		finish();
	}
	public void parkingLotOwner(View v){
		Intent intent=new Intent(WelcomeActivity.this,AdminLoginActivity.class);
		startActivity(intent);
		finish();
	}
}
