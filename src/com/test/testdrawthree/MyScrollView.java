package com.test.testdrawthree;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 水平滑动和垂直滑动事件判断和分发处理，为了解决水平滑动和垂直滑动的冲突
 * 2014-9-29
 */
public class MyScrollView extends ScrollView {

	private GestureDetector mGestureDetector;

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, new YScrollDetector());
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
	}

	class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			int degrees = (int) (Math.atan2(Math.abs(distanceY), Math.abs(distanceX)) * 180 / Math.PI);
			Log.v("degree", degrees + "");
			if (degrees >= 70) {// 大于指定角度时本级捕获，否则分发给子控件处理
				return true;
			}
			return false;
		}
	}
}
