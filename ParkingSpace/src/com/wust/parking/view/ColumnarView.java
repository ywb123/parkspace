package com.wust.parking.view;

import java.util.List;

import com.wust.parking.domin.Score;
import com.wust.parkingspace.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

@SuppressLint("ViewConstructor")
public class ColumnarView extends View{

	private List<Score> score;
	private float tb;
	private float interval_left_right;
	private Paint paint_date, paint_rectf_gray, paint_rectf_blue,paint_dottedline;

	private int fineLineColor = 0x5faaaaaa; // 灰色
	private int blueLineColor = 0xff00ffff; // 蓝色
	private int orangeLineColor = 0xffd56f2b; // 橙色

	public ColumnarView(Context context, List<Score> score) {
		super(context);
		init(score);
	}

	public void init(List<Score> score) {
		if (null == score || score.size() == 0)
			return;
		this.score = score;
		Resources res = getResources();
		tb = res.getDimension(R.dimen.historyscore_tb);
		interval_left_right = tb * 5.0f;
		
		paint_date = new Paint();
		paint_date.setStrokeWidth(tb * 0.1f);
		paint_date.setTextSize(tb * 1.2f);
		paint_date.setColor(fineLineColor);
		paint_date.setTextAlign(Align.CENTER);

		paint_dottedline = new Paint();
		paint_dottedline.setStyle(Paint.Style.STROKE);
		paint_dottedline.setColor(orangeLineColor);
		
		paint_rectf_gray = new Paint();
		paint_rectf_gray.setStrokeWidth(tb * 0.1f);
		paint_rectf_gray.setColor(fineLineColor);
		paint_rectf_gray.setStyle(Style.FILL);
		paint_rectf_gray.setAntiAlias(true);

		paint_rectf_blue = new Paint();
		paint_rectf_blue.setStrokeWidth(tb * 0.1f);
		paint_rectf_blue.setColor(blueLineColor);
		paint_rectf_blue.setStyle(Style.FILL);
		paint_rectf_blue.setAntiAlias(true);

		setLayoutParams(new LayoutParams(
				(int) (this.score.size() * interval_left_right),
				LayoutParams.MATCH_PARENT));
	}

	protected void onDraw(Canvas c) {
		if (null == score || score.size() == 0)
			return;
		drawDate(c);
		drawRectf(c);
	}

	/**
	 * 绘制矩形
	 * 
	 * @param c
	 */
	public void drawRectf(Canvas c) {
		
		float base = (getHeight() - tb * 3.0f)
				/ (score.get(0).total);
		for (int i = 0; i < score.size(); i++) {

			RectF f = new RectF();
			f.set(tb * 0.2f + interval_left_right * i,
					0, tb * 3.2f + interval_left_right
							* i, getHeight() - tb * 2.0f);
			c.drawRoundRect(f, tb * 0.3f, tb * 0.3f, paint_rectf_gray);

			float height = base*score.get(i).score;
			RectF f1 = new RectF();
			f1.set(tb * 0.2f + interval_left_right * i, getHeight()
					- (height + tb * 2.0f), tb * 3.2f + interval_left_right * i,
					getHeight() - tb * 2.0f);
			c.drawRoundRect(f1, tb * 0.3f, tb * 0.3f, paint_rectf_blue);
			
			int hour=score.get(i).score/(60*60);
			int minute=(score.get(i).score%(60*60))/60;
			paint_rectf_blue.setTextSize(tb*1.0f);
			c.drawText(String.format("%02d", hour)+":"+String.format("%02d", minute), tb * 0.5f + interval_left_right * i, getHeight()
					- (height + tb * 2.1f), paint_rectf_blue);
		}
		
		
		
	}

	/**
	 * 绘制日期
	 * 
	 * @param c
	 */
	public void drawDate(Canvas c) {
		for (int i = 0; i < score.size(); i++) {
			String date = score.get(i).date;
			String date_1 = date
					.substring(date.indexOf("年") + 1, date.length());
			c.drawText(date_1, tb * 2.0f + interval_left_right * i,
					getHeight(), paint_date);

		}
	}
}
