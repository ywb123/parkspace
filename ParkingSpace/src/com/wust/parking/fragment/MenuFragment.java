package com.wust.parking.fragment;

import java.util.ArrayList;

import com.wust.parking.adapter.NavDrawerListAdapter;
import com.wust.parking.entity.NavDrawerItem;
import com.wust.parkingspace.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


@SuppressLint("NewApi")
public class MenuFragment extends Fragment implements OnItemClickListener {

	private ListView mDrawerList;
	private String[] mNavMenuTitles;
	private TypedArray mNavMenuIconsTypeArray;
	private ArrayList<NavDrawerItem> mNavDrawerItems;
	private NavDrawerListAdapter mAdapter;
	private SLMenuListOnItemClickListener mCallback;
	private int selected = -1;

	private TextView tvTitle;
	@Override
	public void onAttach(Activity activity) {
		try {
			mCallback = (SLMenuListOnItemClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnResolveTelsCompletedListener");
		}
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_menu, null);
		
		findView(rootView);
		
		return rootView;
	}

	private void findView(View rootView) {
		
		mDrawerList = (ListView) rootView.findViewById(R.id.left_menu);  
		tvTitle=(TextView) rootView.findViewById(R.id.tvTitle);
		mNavMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);  
        // nav drawer icons from resources  
        mNavMenuIconsTypeArray = getResources()  
                    .obtainTypedArray(R.array.nav_drawer_icons);  
              
        mNavDrawerItems = new ArrayList<NavDrawerItem>();  
  
        // adding nav drawer items to array  
        // 我的车  
//        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[0], mNavMenuIconsTypeArray  
//                .getResourceId(0, -1))); 
        SharedPreferences sp=getActivity().getSharedPreferences("LoginUserInfo", getActivity().MODE_PRIVATE);
        tvTitle.setText(sp.getString("licenseNumber", "我的车"));
        // home
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[1], mNavMenuIconsTypeArray  
                .getResourceId(1, -1)));  
        // parking record
       // mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[2], mNavMenuIconsTypeArray  
        //        .getResourceId(2, -1)));  
     // setting 
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[2], mNavMenuIconsTypeArray  
                .getResourceId(2, -1)));  
        
        mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[3], mNavMenuIconsTypeArray  
                .getResourceId(3, -1)));  
        // Recycle the typed array  
        mNavMenuIconsTypeArray.recycle();  
          
        // setting the nav drawer list adapter  
        mAdapter = new NavDrawerListAdapter(getActivity(),  
                        mNavDrawerItems);  
        mDrawerList.setAdapter(mAdapter);  
        mDrawerList.setOnItemClickListener(this);
        
        if(selected!=-1){
        	mDrawerList.setItemChecked(selected, true);  
            mDrawerList.setSelection(selected);  
        }else{
        	mDrawerList.setItemChecked(0, true);  
            mDrawerList.setSelection(0);  
        }
        Log.d("aaa", "findView");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		// update selected item and title, then close the drawer  
        mDrawerList.setItemChecked(position, true);  
        mDrawerList.setSelection(position);  
        
        if(mCallback!=null){
        	mCallback.selectItem(position, mNavMenuTitles[position+1]);
        }
        selected = position;
	        
        
	}

	/**
     * 左侧菜单 点击回调接口
     * 
     *
     */
    public interface SLMenuListOnItemClickListener{
    	
    	public void selectItem(int position,String title);
    }
}