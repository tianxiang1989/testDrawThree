package com.test.testdrawthree;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

/**
 * 折线图view [这个被封装在CustomView中了，请勿单独调用]
 * @author liuxiuquan 2014-9-19
 * 
 * 注：这个view如果数据源如果改变了，不能靠postInvalidate();
 * 只能通过requestLayout()刷新，因为宽度改变了，postInvalidate()不会调用onMeasure方法
 */
public class ChartView4CustomView extends View {
	/** 打印log信息时的标识 */
	private final String TAG = ChartView4CustomView.class.getSimpleName();

	// ===============各种常量===============
	/**枚举：标识传来的x数据中最大值和最小值的正负的
	 * BOTH_POSITIVE:最大值最小值均为正数
	 * BOTH_NEGATIVE:最大值最小值均为负数
	 * POSITIVE_NEGATIVE:最大值为正数，最小值为负数
	 * */
	public enum POSITIVE_FLAG {
		BOTH_POSITIVE, BOTH_NEGATIVE, POSITIVE_NEGATIVE
	}

	/**标识传来的x数据中最大值和最小值的正负*/
	POSITIVE_FLAG positive_Flag;
	/**y坐标几等分*/
	private final int Y_HOW_MANY = 6;
	/**x的左边距起始位置*/
	private float MARGIN_LEFT = changeDp(2);
	/**y的上边距起始位置*/
	private float MARGIN_TOP = changeDp(10);
	// ===============view状态变量===============
	/**底边距*/
	private int marginBottom;
	/**当前容器*/
	Context context;
	/**view的宽度*/
	float viewWidth;
	/**view的高度*/
	float viewHeight;
	/**y=0的位置 [柱状图中用，暂时没有完成柱状图的绘制]*/
	private float zeroYAxis = 0;
	/**y值最大的宽度*/
	float yMaxWidth;
	/**当前容器*/
	// ===============各种标识变量===============
	/**记录点击的是哪一列的文字，相应点击事件*/
	int drawUpTextIndex = -1;
	// ===============view数据===============
	/** y轴的数据 */
	private List<Float> yValueList = new ArrayList<Float>();
	/** y轴坐标 [左上角的点] */
	private List<Float> yAxisList = new ArrayList<Float>();

	/** x轴的数据 */
	private List<String> xValueList = new ArrayList<String>();
	/** x轴坐标 [左上角的点] */
	private List<Float> xAxisList = new ArrayList<Float>();

	/** y轴坐标 分割线的y坐标值 */
	private List<Float> yAxisLineList = new ArrayList<Float>();
	/** y轴坐标 分割线的y显示值 */
	private List<String> yAxisLineValueList = new ArrayList<String>();
	/**标识x轴有几个值*/
	private int size;

	// ===============各种画笔===============
	/**背景的画笔*/
	private Paint backGroundPaint;
	/**轴的画笔*/
	private Paint axisPaint;
	/**画x轴数值的textPaint画笔*/
	TextPaint textXYPaint;
	/**阴影的画笔*/
	private Paint shadowPaint;
	/**折线/柱状图文字的画笔*/
	private TextPaint textChartPaint;
	/**连接左右两点的线*/
	private Paint joinLinePaint;

	// ---------------各种方法BEGIN---------------
	// ---------------复写父类的方法---------------
	/**
	 * 构造方法
	 * @param context 当前容器
	 * @param xValueList x轴的数据
	 * @param yValueList y轴的数据
	 * @param height 给定的组件高度[单位dp]
	 */
	public ChartView4CustomView(Context context, List<String> xValueList, List<Float> yValueList,
			int height) {
		super(context);
		this.context = context;
		this.size = xValueList.size();
		this.xValueList = xValueList;
		this.yValueList = yValueList;
		initPaint();// 初始化画笔
		if (calXNameMaxLeng() > 4) {// TODO_Q 这里只考虑了一行两行的情况 [因为实际x名称数据最多8位]
			marginBottom = (int) ((getFontHeight(textChartPaint) + changeDp(2)) * 2);
		} else {
			marginBottom = (int) ((getFontHeight(textChartPaint) + changeDp(2)));
		}
//		viewHeight = changeDp(height);
		viewHeight=height;
		calYAxisList();// 计算y值的数据
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		yMaxWidth = calYMaxWidth();// 获取y值最大的宽度
		if (size >= 0) {
			viewWidth = (int) (yMaxWidth * (size - 1)) + changeDp(15) + MARGIN_LEFT;
		}
		setMeasuredDimension((int) viewWidth, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initData();// 初始化数据
		drawOutsideBackground(canvas);// 画背景
		drawYRelativeAxis(canvas);// 画水平的分割线
		drawXAxisValue(canvas);// 画x轴上显示的数据
		drawJoinLine(canvas);// 画左右点的连接线
		drawChart(canvas);// 画报表图
		drawShadowPath(canvas);// 画折线图的阴影
		// drawPoint(canvas);// 画数值点
	}

	// ---------------初始化数据的方法---------------
	/** 初始化画笔 */
	public void initPaint() {
		// 背景的画笔
		backGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backGroundPaint.setAntiAlias(true);
		backGroundPaint.setColor(Color.WHITE);

		// 轴的画笔
		axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		axisPaint.setColor(0xffc6c6c6);
		axisPaint.setTextSize(changeDp(12));

		// 画x,y值数值的画笔
		textXYPaint = new TextPaint();
		textXYPaint.setTextSize(changeDp(12));
		textXYPaint.setColor(0xffc3c3c3);

		// 背景参考柱状图的画笔
		shadowPaint = new Paint();
		// shadowPaint.setColor(0xfff0f7fd);// 淡蓝色的参考柱状图背景色
		shadowPaint.setAntiAlias(true);
		shadowPaint.setStyle(Paint.Style.FILL);

		// 报表文字的画笔
		textChartPaint = new TextPaint();
		textChartPaint.setTextSize(changeDp(12));
		textChartPaint.setColor(0xff1ea8ff);

		// 连接左右两点的线
		joinLinePaint = new Paint();
		joinLinePaint.setAntiAlias(true);
		joinLinePaint.setColor(0xff91e8ee);
		joinLinePaint.setStrokeWidth(2.8f);// 线宽
	}

	/**对外的初始化数据的方法*/
	public void initData() {
		// 1 计算x的数值
		calXAxisList();
		// 2 计算y分割线的数据
		calYRelativeAxis();
	}

	// ---------------计算数值的方法---------------
	// =====这些是x的=====
	/**x轴名称的最大长度*/
	private int calXNameMaxLeng() {
		int maxLength = 0;
		for (String x : xValueList) {
			int l = x.length();
			if (l > maxLength) {
				maxLength = l;
			}
		}
		return maxLength;
	}

	/**计算x相关的数据*/
	private void calXAxisList() {
		float startX = MARGIN_LEFT;// 起始位置
		xAxisList.clear();
		for (int i = 0; i < xValueList.size(); i++) {
			xAxisList.add(startX);
			startX = startX + yMaxWidth;
		}
	}

	// =====这些是y的=====
	/**计算y相关的数据*/
	private void calYAxisList() {
		// 变量列表
		/**y值的最小值*/
		float yMinValue = getYMinValue();
		/**y值的最大值*/
		float yMaxValue = getYMaxValue();
		/**精度比例*/
		float degree;
		/**画的y最大值的位置*/
		float yDrawMaxValue;
		/**画的y最小值的位置*/
		float yDrawMinValue;
		/**传来数组中y的值*/
		float yValue;
		/**传来数组中y的坐标*/
		float yAlis;
		/**y坐标的间距*/
		float perSpaceAxisValue = (viewHeight - (marginBottom) - MARGIN_TOP) / Y_HOW_MANY;
		// 方法列表
		judgPositive(yMinValue, yMaxValue);// 判断最大最小值的正负
		switch (positive_Flag) {
		case BOTH_POSITIVE:// 最大最小值均为正值
			// 思路：从x的最小值计算各个点
			if ((yMaxValue - yMinValue) / 2f > yMinValue) {
				yDrawMinValue = yMinValue / 1.3f;
			} else {
				yDrawMinValue = 0;
			}
			degree = (yMaxValue - yDrawMinValue)
					/ (viewHeight - (getFontHeight(axisPaint) + marginBottom) - MARGIN_TOP
							- getFontHeight(axisPaint) - changeDp(4));

			// 计算y各个值的坐标
			yAxisList.clear();
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = viewHeight - (getFontHeight(axisPaint) + marginBottom)
						- (yValue - yDrawMinValue) / degree;
				yAxisList.add(yAlis);
			}

			// 计算y等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMinValue));
				yDrawMinValue = yDrawMinValue + perSpaceAxisValue * degree;
			}

			break;
		case BOTH_NEGATIVE:// 最大值最小值均为负值
			// 思路:从y的最大值计算各个点
			if ((yMinValue - yMaxValue) / 2f < yMaxValue) {
				yDrawMaxValue = 0;
			} else {
				yDrawMaxValue = yMaxValue - yMaxValue * 0.01f;
			}
			degree = (yDrawMaxValue - yMinValue)
					/ (viewHeight - (getFontHeight(axisPaint) + marginBottom) - MARGIN_TOP
							- getFontHeight(axisPaint) - changeDp(4));// 总高-下高-上高-表报上方文字的高度
			// 计算y各个值的坐标
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = MARGIN_TOP + (yDrawMaxValue - yValue) / degree;
				yAxisList.add(yAlis);
			}

			// 计算y等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMaxValue));
				yDrawMaxValue = yDrawMaxValue - perSpaceAxisValue * degree;
			}
			Collections.reverse(yAxisLineValueList);// list翻转

			break;
		case POSITIVE_NEGATIVE:// 最大值为正值，最小值为负值
			// 思路：求出最上边的线，然后计算出各个点
			degree = (yMaxValue - yMinValue)
					/ (viewHeight - (marginBottom) - MARGIN_TOP - (getFontHeight(axisPaint) + changeDp(3)) * 2f);// (最大值-最小值)/(总高-上高-下高-文字高度*2)
			yDrawMaxValue = yMaxValue + (getFontHeight(axisPaint) + changeDp(3)) * degree;// 最上边的点
			// 传过来的数组中x最大值的坐标
			float yMaxAxis = MARGIN_TOP;
			zeroYAxis = yDrawMaxValue / degree + yMaxAxis;// 计算x=0的点的坐标

			// 计算x各个值的坐标
			for (int j = 0; j < yValueList.size(); j++) {
				yValue = yValueList.get(j);
				yAlis = zeroYAxis - (yValue) / degree;
				yAxisList.add(yAlis);
			}
			// 计算x等分点参考线显示的值
			yAxisLineValueList.clear();
			for (int i = 0; i <= Y_HOW_MANY; i++) {
				yAxisLineValueList.add(formatNum(yDrawMaxValue));
				yDrawMaxValue = yDrawMaxValue - perSpaceAxisValue * degree;
			}
			Collections.reverse(yAxisLineValueList);// list翻转
			break;
		}
	}

	/**获得y轴的最小值 [服务器的数据时排序过传来的，其实yValueList的最后一个值就是最小值]*/
	private float getYMinValue() {
		float localYMinValue = yValueList.size() == 0 ? 0 : yValueList.get(yValueList.size() - 1);
		// Log.v(TAG, "localXMinValue==" + localXMinValue);
		return localYMinValue;
	}

	/**获得y轴的最大值 [服务器的数据时排序过传来的，其实yValueList的第一个值就是最大值]*/
	private float getYMaxValue() {
		float localYMaxValue = yValueList.size() == 0 ? 0 : yValueList.get(0);
		// Log.v(TAG, "localXMaxValue==" + localXMaxValue);
		return localYMaxValue;
	}

	/**判断最大最小值的正负*/
	private void judgPositive(float xMinValue, float xMaxValue) {
		if (xMinValue >= 0) {// 最大最小值均为正值
			positive_Flag = POSITIVE_FLAG.BOTH_POSITIVE;
		} else if (xMaxValue < 0) {// 最大值最小值均为负值
			positive_Flag = POSITIVE_FLAG.BOTH_NEGATIVE;
		} else if (xMaxValue >= 0 && xMinValue < 0) {// 最大值为正值，最小值为负值
			positive_Flag = POSITIVE_FLAG.POSITIVE_NEGATIVE;
		}
	}

	/**获取y值最大的宽度*/
	private float calYMaxWidth() {
		float maxW = changeDp(30);// 保存最大宽度 从30dp开始
		for (float y : yValueList) {
			float valueWidth = textXYPaint.measureText(y + "");
			if (valueWidth > maxW) {
				maxW = valueWidth;
			}
		}
		for (String x : xValueList) {
			float textWidth = textXYPaint.measureText(x);
			if (textWidth > maxW) {
				maxW = textWidth;
			}
		}
		maxW = maxW + changeDp(10);// 文字两边留间距
		return maxW;
	}

	/**计算y分割线的数据*/
	private void calYRelativeAxis() {
		float degree2 = (viewHeight - MARGIN_TOP - (marginBottom)) / Y_HOW_MANY;
		yAxisLineList.clear();
		float localBeginY = MARGIN_TOP;
		for (int i = 0; i < Y_HOW_MANY; i++) {
			yAxisLineList.add(localBeginY);
			localBeginY = localBeginY + degree2;
		}
		yAxisLineList.add(viewHeight - (marginBottom));// 最下边的线 (因为float的除法会有误差)
	}

	// ---------------画图的方法---------------
	/**画最外面的背景*/
	private void drawOutsideBackground(Canvas canvas) {
		float rectLeft = 0;
		float rectTop = 0;
		float rectRight = this.viewWidth;
		float rectBottom = this.viewHeight;
		RectF backGroundRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
		canvas.drawRoundRect(backGroundRect, 0, 0, backGroundPaint);
	}

	/**画竖着的参考线*/
	private void drawXTest(Canvas canvas) {
		for (float w : xAxisList) {
			canvas.drawLine(w, MARGIN_TOP, w, viewHeight - (marginBottom), axisPaint);
		}
	}

	/**画水平的分割线*/
	private void drawYRelativeAxis(Canvas canvas) {
		for (int i = 0; i < yAxisLineList.size(); i++) {
			canvas.drawLine(MARGIN_LEFT, yAxisLineList.get(i), viewWidth - changeDp(15),
					yAxisLineList.get(i), axisPaint);
		}
	}

	/**画x轴上显示的值*/
	private void drawXAxisValue(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			// 文字
			String str = xValueList.get(i) + "";
			StaticLayout layout = null;
			int lastLenth = (int) textXYPaint.measureText(str);
			if (i == 0) {// 第一列文字右对齐
				if (str.length() > 4) {
					lastLenth = (int) (lastLenth / 1.5);
				}
				layout = new StaticLayout(str, textXYPaint, lastLenth, Alignment.ALIGN_NORMAL,
						1.0F, 0.0F, true);
			} else if (i < xAxisList.size() - 1) {// 中间文字居中
				layout = new StaticLayout(str, textXYPaint, (int) textXYPaint.measureText(str),
						Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
			} else if (i == xAxisList.size() - 1) {// 最后一列文字左对齐
				if (str.length() > 4) {
					lastLenth = (int) (lastLenth / 1.5);
				}
				layout = new StaticLayout(str, textXYPaint, lastLenth, Alignment.ALIGN_CENTER,
						1.0F, 0.0F, true);
			}
			int cur = canvas.save(); // 保存当前状态
			if (i == 0) {
				canvas.translate(xAxisList.get(i), viewHeight - (marginBottom) * 1f);// 画笔的位置
			} else if (i < xAxisList.size() - 1) {
				canvas.translate(xAxisList.get(i) - textXYPaint.measureText(str) / 2f, viewHeight
						- (marginBottom) * 1f);// 画笔的位置
			} else if (i == xAxisList.size() - 1) {
				canvas.translate(xAxisList.get(xAxisList.size() - 1) - textXYPaint.measureText(str)
						/ 2f, viewHeight - (marginBottom) * 1f);// 画笔的位置
			}
			layout.draw(canvas);
			canvas.restoreToCount(cur);
		}
	}

	/**画数值点*/
	private void drawPoint(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			RectF rf3 = new RectF(xAxisList.get(i) - changeDp(2), yAxisList.get(i) - changeDp(2),
					xAxisList.get(i) + changeDp(2), yAxisList.get(i) + changeDp(2));
			canvas.drawOval(rf3, axisPaint);
		}
	}

	/**画左右点的连接线*/
	private void drawJoinLine(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			if (i > 0) {
				canvas.drawLine(xAxisList.get(i - 1), yAxisList.get(i - 1), xAxisList.get(i),
						yAxisList.get(i), joinLinePaint);
			}
		}
	}

	/**画报表图形*/
	private void drawChart(Canvas canvas) {
		for (int i = 0; i < xAxisList.size(); i++) {
			switch (positive_Flag) {
			case BOTH_POSITIVE:
			case POSITIVE_NEGATIVE:// 全正和正负值，文字均在表报图形上方
				if (drawUpTextIndex == i) {
					if (i == 0) {
						canvas.drawText(yValueList.get(i) + "", xAxisList.get(i), yAxisList.get(i)
								- changeDp(4), axisPaint);
					} else if (i != xAxisList.size() - 1) {
						canvas.drawText(yValueList.get(i) + "",
								xAxisList.get(i) - axisPaint.measureText(yValueList.get(i) + "")
										/ 2f, yAxisList.get(i) - changeDp(4), axisPaint);
					} else if (i == xAxisList.size() - 1) {
						canvas.drawText(
								yValueList.get(xAxisList.size() - 1) + "",
								xAxisList.get(xAxisList.size() - 1)
										- axisPaint.measureText(yValueList.get(xAxisList.size() - 1)
												+ ""), yAxisList.get(xAxisList.size() - 1)
										- changeDp(4), axisPaint);
					}
				}
				break;
			case BOTH_NEGATIVE:// 全负的情况下文字在报表图形的下方
				if (drawUpTextIndex == i) {
					if (i == 0) {
						canvas.drawText(yValueList.get(i) + "", xAxisList.get(i), yAxisList.get(i)
								+ changeDp(0) + getFontHeight(axisPaint), axisPaint);
					} else if (i != xAxisList.size() - 1) {
						canvas.drawText(yValueList.get(i) + "",
								xAxisList.get(i) - axisPaint.measureText(yValueList.get(i) + "")
										/ 2f, yAxisList.get(i) + changeDp(0)
										+ getFontHeight(axisPaint), axisPaint);
					} else if (i == xAxisList.size() - 1) {
						canvas.drawText(
								yValueList.get(xAxisList.size() - 1) + "",
								xAxisList.get(xAxisList.size() - 1)
										- axisPaint.measureText(yValueList.get(xAxisList.size() - 1)
												+ ""), yAxisList.get(xAxisList.size() - 1)
										+ changeDp(0) + getFontHeight(axisPaint), axisPaint);
					}
				}
				break;
			default:
				break;
			}
		}
	}

	/**画折线图的阴影*/
	private void drawShadowPath(Canvas canvas) {
		// 渐变色
		/*新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。连接这2个点就拉出一条渐变线了，玩过PS的都懂。
		 * 然后那个数组是渐变的颜色。下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。最后是模式，这里设置的是循环渐变*/
		Shader mShader = new LinearGradient(xAxisList.get(0), yAxisList.get(0), xAxisList.get(0),
				viewHeight - (marginBottom) * 1f, 0xdfecfafd, 0xbff8fefe, Shader.TileMode.REPEAT);
		shadowPaint.setShader(mShader);

		Path shadowPath;// 阴影的路径
		shadowPath = new Path();
		shadowPath.moveTo(xAxisList.get(0), viewHeight - (marginBottom) * 1f);
		for (int i = 0; i < yValueList.size(); i++) {
			shadowPath.lineTo(xAxisList.get(i), yAxisList.get(i));
		}
		shadowPath.lineTo(xAxisList.get(yValueList.size() - 1), viewHeight - (marginBottom) * 1f);
		shadowPath.close();
		canvas.drawPath(shadowPath, shadowPaint);
	}

	// ---------------事件处理的方法---------------
	/**
	 * 点击事件的响应
	 * @param x 点击位置的x坐标
	 * @param y 点击位置的y坐标
	 */
	public void judgePointIn(float x, float y) {
		// 位置判断
		for (int i = 0; i < xAxisList.size(); i++) {
			if (xAxisList.get(i) - yMaxWidth / 3f < x && x < xAxisList.get(i) + yMaxWidth / 3f
					&& y < viewHeight - (marginBottom) + getFontHeight(axisPaint) + changeDp(20)
					&& y > viewHeight - (marginBottom) - changeDp(20)) {// 判断点击的点是下方的文字
				drawUpTextIndex = i;
				break;
			}
		}
		postInvalidate();
	}

	// ---------------工具方法---------------
	/**
	 * dp转化pix像素--工具方法 
	 * @param dp
	 * @return
	 */
	private int changeDp(int dp) {
		int pix = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics()));
		return pix;
	}

	/**
	 * 根据paint计算文字高度
	 * 
	 * @param paint 画笔
	 * @return 计算得出的文字高度
	 */
	public float getFontHeight(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		float fFontHeight = (float) Math.ceil(fm.descent - fm.ascent);
		return fFontHeight;
	}

	/**
	 * 数字格式化[工具方法]
	 * @param ff 需要处理的数据
	 * @return
	 */
	private String formatNum(float ff) {
		DecimalFormat df;
		String res = "";
		int positiveOrNegativeFlag = 1;// 标识是正数还是负数
		if (ff < 0) {// 先转正数做判断
			positiveOrNegativeFlag = -1;
			ff = -ff;
		}
		if (ff < 1) {
			df = new DecimalFormat("###.000");
			res = "0" + df.format(ff) + "";
		} else if (ff > 100) {
			res = (int) ff + "";
		} else {
			df = new DecimalFormat("###.00");
			res = df.format(ff) + "";
		}
		if (positiveOrNegativeFlag == -1) {
			res = "-" + res;
		}
		return res;
	}

	// ===============对外的set和get方法===============
	public List<String> getyAxisLineValueList() {
		return yAxisLineValueList;
	}

	public float getBeginY() {
		return MARGIN_TOP;
	}

	public int getMarginBottom() {
		return marginBottom;
	}
	// ===============END===============
}
