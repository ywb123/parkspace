package com.wust.parking.view;

import java.util.List;

import com.wust.parking.domin.Score;
import com.wust.parkingspace.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

@SuppressLint("ViewConstructor")
public class DiagramView extends View{

	private List<Score> scores;
	private float tb;
	private float interval_left_right;
	private float interval_left;
	private Paint paint_date, paint_brokenLine, paint_dottedline,
			paint_brokenline_big, framPanint;

	private Bitmap bitmap_point;
	private Path path;
	private float dotted_text;

	public float getDotted_text() {
		return dotted_text;
	}

	public void setDotted_text(float dotted_text) {
		this.dotted_text = dotted_text;
	}

	private int fineLineColor = 0x5faaaaaa; // 灰色
	private int blueLineColor = 0xff00ffff; // 蓝色
	private int orangeLineColor = 0xffd56f2b; // 橙色

	public DiagramView(Context context, List<Score> scores) {
		super(context);
		init(scores);
	}

	public void init(List<Score> scores) {
		if (null == scores || scores.size() == 0)
			return;
		this.scores = delZero(scores);
		Resources res = getResources();
		tb = res.getDimension(R.dimen.historyscore_tb);
		interval_left_right = tb * 6.0f;
		interval_left = tb * 0.5f;

		paint_date = new Paint();
		paint_date.setStrokeWidth(tb * 0.1f);
		paint_date.setTextSize(tb * 1.2f);
		paint_date.setColor(fineLineColor);

		paint_brokenLine = new Paint();
		paint_brokenLine.setStrokeWidth(tb * 0.1f);
		paint_brokenLine.setColor(blueLineColor);
		paint_brokenLine.setAntiAlias(true);

		paint_dottedline = new Paint();
		paint_dottedline.setStyle(Paint.Style.STROKE);
		paint_dottedline.setColor(fineLineColor);

		paint_brokenline_big = new Paint();
		paint_brokenline_big.setStrokeWidth(tb * 0.4f);
		paint_brokenline_big.setColor(fineLineColor);
		paint_brokenline_big.setAntiAlias(true);

		framPanint = new Paint();
		framPanint.setAntiAlias(true);
		framPanint.setStrokeWidth(2f);

		path = new Path();
		bitmap_point = BitmapFactory.decodeResource(getResources(),
				R.drawable.icon_point_blue);
		setLayoutParams(new LayoutParams(
				(int) (this.scores.size() * interval_left_right),
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * 移除左右为零的数据
	 * 
	 * @return
	 */
	public List<Score> delZero(List<Score> scores) {
		return scores;
		/*List<Score> list = new ArrayList<Score>();
		int sta = 0;
		int end = 0;
		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i).score >= 0) {
				sta = i;
				break;
			}
		}
		for (int i = scores.size() - 1; i >= 0; i--) {
			if (scores.get(i).score >= 0) {
				end = i;
				break;
			}
		}
		for (int i = 0; i < scores.size(); i++) {
			if (i >= sta && i <= end) {
				list.add(scores.get(i));
			}
		}
		
		dotted_text = (scores.get(0).total) / 12.0f * 5.0f;
		return list;*/
	}

	protected void onDraw(Canvas c) {
		if (null == scores || scores.size() == 0)
			return;
		drawStraightLine(c);
		drawBrokenLine(c);
		drawDate(c);
	}

	/**
	 * 绘制竖线
	 * 
	 * @param c
	 */
	public void drawStraightLine(Canvas c) {
		paint_brokenline_big.setColor(fineLineColor);
		int count_line = 0;
		for (int i = 0; i < scores.size(); i++) {
			//if (count_line == 0) {
				c.drawLine(interval_left_right * i, 0, interval_left_right * i,
						getHeight(), paint_date);
			/*}
			if (count_line == 2) {
				c.drawLine(interval_left_right * i, tb * 1.5f,
						interval_left_right * i, getHeight(), paint_date);
			}
			if (count_line == 1 || count_line == 3) {
				Path path = new Path();
				path.moveTo(interval_left_right * i, tb * 1.5f);
				path.lineTo(interval_left_right * i, getHeight());
				PathEffect effects = new DashPathEffect(new float[] { tb * 0.3f,
						tb * 0.3f, tb * 0.3f, tb * 0.3f }, tb * 0.1f);
				paint_dottedline.setPathEffect(effects);
				c.drawPath(path, paint_dottedline);
			}*/
			count_line++;
			if (count_line >= 4) {
				count_line = 0;
			}
		}
		c.drawLine(0, getHeight() - tb * 0.2f, getWidth(), getHeight() - tb
				* 0.2f, paint_brokenline_big);
	}

	/**
	 * 绘制折线
	 * 
	 * @param c
	 */
	public void drawBrokenLine(Canvas c) {
		/*int index = 0;
		float temp_x = 0;
		float temp_y = 0;*/
		float base = (getHeight() )
				/ (scores.get(0).total);

		Shader mShader = new LinearGradient(0, 0, 0, getHeight(), new int[] {
				Color.argb(100, 0, 255, 255), Color.argb(45, 0, 255, 255),
				Color.argb(10, 0, 255, 255) }, null, Shader.TileMode.CLAMP);
		framPanint.setShader(mShader);

		for (int i = 0; i < scores.size()-1 ; i++) {
			float x1 = interval_left_right * i;
			float y1 = getHeight()  - (base * scores.get(i).score);
			float x2 = interval_left_right * (i + 1);
			float y2 = getHeight()  - (base * scores.get(i + 1).score);

			/*if ((int) (base * scores.get(i + 1).score) == 0 && index == 0) {
				index++;
				temp_x = x1;
				temp_y = y1;
			}
			if ((int) (base * scores.get(i + 1).score) != 0 && index != 0) {*/
				//index = 0;
				/*x1 = temp_x;
				y1 = temp_y;*/
			//}
			//if (index == 0) {
			c.drawLine(x1, y1, x2, y2, paint_brokenLine);
			path.lineTo(x1, y1);
			c.drawBitmap(bitmap_point,
					x1 - bitmap_point.getWidth() / 2,
					y1 - bitmap_point.getHeight() / 2, null);
			c.drawText(scores.get(i).score+"次", x1 ,y1-0.2f*tb, paint_date);
			c.drawText(scores.get(i+1).score+"次", x2 ,y2-0.2f*tb, paint_date);
			if (i == scores.size() - 2) {
				path.lineTo(x2, y2);
				path.lineTo(x2, getHeight());
				path.lineTo(0, getHeight());
				path.close();
				c.drawPath(path, framPanint);
				c.drawBitmap(bitmap_point,
						x2 - bitmap_point.getWidth() / 2,
						y2 - bitmap_point.getHeight() / 2, null);
			}
			//}
		}
		
		//c.drawText(scores.get(scores.size()-1).score+"次", x1 ,y1-0.2f*tb, paint_date);
		
		paint_dottedline.setColor(orangeLineColor);
		//20
		Path path0 = new Path();
		path0.moveTo(0, 0 );
		path0.lineTo(getWidth(), 0);
		//15
		Path path1 = new Path();
		path1.moveTo(0, getHeight()/4 );
		path1.lineTo(getWidth(), getHeight() /4);
		//10
		Path path2 = new Path();
		path2.moveTo(0, getHeight()/2 );
		path2.lineTo(getWidth(), getHeight()/2);
		//5
		Path path3 = new Path();
		path3.moveTo(0, 3*getHeight()/4 );
		path3.lineTo(getWidth(), 3*getHeight()/4);
		
		
		
		PathEffect effects = new DashPathEffect(new float[] { tb * 0.3f,
				tb * 0.3f, tb * 0.3f, tb * 0.3f }, tb * 0.1f);
		paint_dottedline.setPathEffect(effects);
		
		
		c.drawPath(path0, paint_dottedline);
		c.drawText(""+(int)scores.get(0).total, 0, 0, paint_date);
		
		c.drawPath(path1, paint_dottedline);
		c.drawText(""+(int)(3*scores.get(0).total/4), 0,getHeight()/4, paint_date);
		
		c.drawPath(path2, paint_dottedline);
		c.drawText(""+(int)scores.get(0).total/2, 0, getHeight()/2, paint_date);
		
		c.drawPath(path3, paint_dottedline);
		c.drawText(""+(int)scores.get(0).total/4, 0, 3*getHeight()/4, paint_date);

	}

	/**
	 * 绘制时间
	 * 
	 * @param c
	 */
	public void drawDate(Canvas c) {
		for(int i=0;i<scores.size();i++){
			String date = scores.get(i).date;
			String date_1 = date.substring(date.indexOf("年") + 1, date.length());
			
			c.drawText(date_1, interval_left_right * i + interval_left,
					tb * 1.0f, paint_date);
		}
		

	}
}
