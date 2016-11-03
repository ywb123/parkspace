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
        SimpleDateFormat format=new SimpleDateFormat("yyyy��MM��dd��HHʱ");
        tvInfo.setText("\n�汾�ţ�"+pInfo.versionCode+
        		"\n�汾����"+pInfo.versionName+
        		"\n�״ΰ�װʱ�䣺"+format.format(new Date(pInfo.firstInstallTime))+
        		"\n�������ʱ�䣺"+format.format(new Date(pInfo.lastUpdateTime))+
        		"\n������"+pInfo.packageName); 
        return rootView;
    }
}