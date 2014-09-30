package com.test.testdrawthree;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomView extends FrameLayout {
	/** 打印log信息时的标识 */
	private final static String TAG = CustomView.class.getSimpleName();

	/**放置左侧的刻度列*/
	private LinearLayout rl_view_left;
	/**放置右侧的表报列*/
	private LinearLayout ll_view_right;
	/**报表左右滚动的控件*/
	HorizontalScrollView hsv_right_view;
	/**自定义view*/
	private ChartView4CustomView v1;
	/**当前容器*/
	private Context context;
	// ===============view数据===============
	/**x轴数据*/
	private List<String> columnXList = new ArrayList<String>();
	/**y轴数据*/
	private List<Float> columnYList = new ArrayList<Float>();

	public CustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.chart_view, this);
		this.context = context;
		rl_view_left = (LinearLayout) findViewById(R.id.rl_view_left);
		ll_view_right = (LinearLayout) findViewById(R.id.ll_view_right);
		hsv_right_view = (HorizontalScrollView) findViewById(R.id.hsv_right_view);
	}

	/**
	 * 设置view数据
	 * @param columnXList x轴数据
	 * @param columnYList y轴数据
	 */
	public void setData(List<String> columnXList, List<Float> columnYList) {
		this.columnXList = columnXList;
		this.columnYList = columnYList;
		v1 = new ChartView4CustomView(context, columnXList, columnYList, getHeight());
		initRightChart();// 初始化右侧报表图
		initLeftText();// 初始化左侧的刻度列
	}

	/**初始化右侧的报表*/
	private void initRightChart() {
		ll_view_right.addView(v1);// 将自定义view加到relativeLayout中
		hsv_right_view.setOnTouchListener(judgeTouchWhichListener());
	}

	/**初始化左侧的文字刻度列*/
	private void initLeftText() {
		List<String> yAxisLineValueList = v1.getyAxisLineValueList();
		// 处理左边的控件
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) rl_view_left
				.getLayoutParams();
		param.setMargins(changeDp(5), (int) v1.getBeginY(), changeDp(5), 0);
		rl_view_left.setLayoutParams(param);
		float heightLeft = rl_view_left.getHeight();
		heightLeft = heightLeft - v1.getBeginY() - v1.getMarginBottom();// 总高-上高-下高
		float oneHeight = heightLeft / 6;
		for (int i = yAxisLineValueList.size() - 1; i >= 0; i--) {
			TextView tv = new TextView(context);
			/*tv.setBackgroundColor(0xffff00ff);*/
			tv.setText(yAxisLineValueList.get(i));
			tv.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					(int) oneHeight));
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			// tv.setLayoutParams(new LayoutParams(120, (int) oneHeight));
			// tv.setHeight((int) oneHeight);
			tv.setTextColor(0xffc3c3c3);
			rl_view_left.addView(tv);
		}
	}

	/** dp转化pix像素--工具方法 */
	private int changeDp(int dp) {
		int pix = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics()));
		return pix;
	}

	/**判断点击的是哪一列的事件*/
	private OnTouchListener judgeTouchWhichListener() {
		return new OnTouchListener() {
			private int firstX = 0;// 起始点偏移位置
			boolean runOneFlag = true;
			private int lastX = 0;// 终止点偏移位置
			private int touchEventId = -9983761;
			/**偏移量*/
			int offset = 0;
			/**点击的位置 x*/
			private float pressX;
			/**点击的位置 y*/
			private float pressY;

			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					View scroller = (View) msg.obj;
					// Log.v(TAG, "lastX==" + lastX);
					if (msg.what == touchEventId) {
						if (runOneFlag) {
							runOneFlag = false;
							firstX = scroller.getScrollX();
							Log.v(TAG, "firstX==" + firstX);
						}
						if (lastX == scroller.getScrollX()) {
							// 停止了，此处你的操作业务
							v1.judgePointIn(pressX + offset, pressY);// 调用自定义view中的处理
							offset = lastX - firstX;
							Log.v(TAG, "offset==" + offset);
						} else {
							handler.sendMessageDelayed(
									handler.obtainMessage(touchEventId, scroller), 1);
							lastX = scroller.getScrollX();
						}
					}
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eventAction = event.getAction();
				switch (eventAction) {
				case MotionEvent.ACTION_UP:
					handler.sendMessageDelayed(handler.obtainMessage(touchEventId, v), 5);
					break;
				case MotionEvent.ACTION_DOWN:
					pressX = event.getX();
					pressY = event.getY();
					// Log.v(TAG, "点击：(" + pressX + "," + pressY + ")");
					break;
				default:
					break;
				}
				return false;
			}

		};
	}
}
