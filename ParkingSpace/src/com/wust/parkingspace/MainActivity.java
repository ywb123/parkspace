package com.wust.parkingspace;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.wust.parking.fragment.MainFragment;
import com.wust.parking.fragment.MenuFragment;
import com.wust.parking.fragment.MenuFragment.SLMenuListOnItemClickListener;
import com.wust.parking.fragment.ParkingRecordFragment;
import com.wust.parking.fragment.SettingFragment;
import com.wust.parking.fragment.StateRecordFragment;
import com.wust.service.FloatListService;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

public class MainActivity extends SlidingActivity implements SLMenuListOnItemClickListener {
	private SlidingMenu mSlidingMenu;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle("Home");
//		setTitle(R.string.sliding_title);
        setContentView(R.layout.frame_content);

        //1、 set the Behind View
        setBehindContentView(R.layout.frame_menu);
        
        // customize the SlidingMenu
        mSlidingMenu = getSlidingMenu();
//      mSlidingMenu.setMenu(R.layout.frame_menu);	//设置左侧菜单的布局文件
//      mSlidingMenu.setSecondaryMenu(R.layout.frame_menu);	设置右侧菜单的布局文件
        
//      mSlidingMenu.setShadowWidth(5);
//      mSlidingMenu.setBehindOffset(100);
        mSlidingMenu.setShadowDrawable(R.drawable.drawer_shadow);//设置阴影图片
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width); //设置阴影图片的宽度
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset); //SlidingMenu划出时主页面显示的剩余宽度
        mSlidingMenu.setFadeDegree(0.35f);
        //设置SlidingMenu 的手势模式
        //TOUCHMODE_FULLSCREEN 全屏模式，在整个content页面中，滑动，可以打开SlidingMenu
        //TOUCHMODE_MARGIN 边缘模式，在content页面中，如果想打开SlidingMenu,你需要在屏幕边缘滑动才可以打开SlidingMenu
        //TOUCHMODE_NONE 不能通过手势打开SlidingMenu
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

        //设置 SlidingMenu 内容
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MenuFragment menuFragment = new MenuFragment();
        fragmentTransaction.replace(R.id.menu, menuFragment);
        fragmentTransaction.replace(R.id.content, new MainFragment());
        fragmentTransaction.commit();
        
        //使用左上方icon可点，这样在onOptionsItemSelected里面才可以监听到R.id.home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_home));
        
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	toggle();
    	getMenuInflater().inflate(R.menu.main, menu);
    	menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				SharedPreferences sp=getSharedPreferences("LoginUserInfo",MODE_PRIVATE);
				Editor e=sp.edit();
				e.clear();
				e.commit();
				System.exit(0);
				return false;
			}
		});
        return true;
    }
    private long firstTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			long secondTime=System.currentTimeMillis();
			if(secondTime-firstTime>1000){
				Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
				//toggle(); //动态判断自动关闭或开启SlidingMenu
				firstTime=secondTime;
			}else{
				System.exit(0);
				
			}
			
			return true;

		default:
			return super.onKeyDown(keyCode, event);
			
		}
    	
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            
            toggle(); //动态判断自动关闭或开启SlidingMenu
//          getSlidingMenu().showMenu();//显示SlidingMenu
//          getSlidingMenu().showContent();//显示内容
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@SuppressLint("NewApi")
	@Override
	public void selectItem(int position, String title) {
		// update the main content by replacing fragments  
	    Fragment fragment = null;  
	    switch (position) {  
	    case 0:  
	        fragment = new MainFragment();  
	        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_home));
	        break;  
	    case 1:  
	        fragment = new StateRecordFragment();  
	        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_parking));
	        break; 
	    case 2:  
	        fragment = new SettingFragment();  
	        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_drawer_setting));
	        break; 
	    case 3:
	    	fragment = new StateRecordFragment();
	    	break;
	    default:  
	        break;  
	    }  
	  
	    if (fragment != null) {  
	        FragmentManager fragmentManager = getFragmentManager();
	        fragmentManager.beginTransaction()  
	                .replace(R.id.content, fragment).commit();  
	        // update selected item and title, then close the drawer  
	        setTitle(title);
	        mSlidingMenu.showContent();
	    } else {  
	        // error in creating fragment  
	        Log.e("MainActivity", "Error in creating fragment");  
	    }  
	}
}
