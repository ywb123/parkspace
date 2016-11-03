package com.wust.parking.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wust.parkingspace.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SettingFragment extends Fragment {
	private TextView tvInfo;
	public SettingFragment(){}
	PackageInfo pInfo;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        tvInfo=(TextView) rootView.findViewById(R.id.tvInfo);
        try {
			pInfo=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日HH时");
        tvInfo.setText("\n版本号："+pInfo.versionCode+
        		"\n版本名："+pInfo.versionName+
        		"\n首次安装时间："+format.format(new Date(pInfo.firstInstallTime))+
        		"\n最近更新时间："+format.format(new Date(pInfo.lastUpdateTime))+
        		"\n包名："+pInfo.packageName); 
        return rootView;
    }
}