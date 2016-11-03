package com.wust.parking.view;

import com.wust.parking.util.Phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ShowAmountView extends View {

	

	Paint paint;
	private int sum=1;
	private int sur=1;
	public ShowAmountView(Context context,AttributeSet aSet) {
		super(context,aSet);
		// TODO Auto-generated constructor stub
		
		paint=new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		RectF rect=new RectF(10, 10, Phone.width-30, 60);
		canvas.drawRoundRect(rect, 10, 10, paint);
		paint.setARGB(0xF0, 0xFF, 0x90, 0x00);
		RectF rect1=new RectF(10, 10, (float)(10+(Phone.width-40)*(1.0f*(sum-sur))/(1.0*sum)), 60);
		canvas.drawRoundRect(rect1, 10, 10, paint);
		paint.setTextSize(30);
		paint.setARGB(0xFF, 0x99, 0x99, 0x99);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		canvas.drawText("≥µŒª £”‡¡ø£∫"+sur+"/"+sum, 50, 48, paint);
	}
	public int getSum() {
		return sum;
	}

	public void setSum(int sum) {
		this.sum = sum;
	}

	public int getSur() {
		return sur;
	}

	public void setSur(int sur) {
		this.sur = sur;
	}
	
}
