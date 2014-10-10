package com.test.testdrawthree;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 自定义的折线图和柱状图
 * @author liuxiuquan
 * 2014-9-19
 */
public class MainActivity extends Activity {
	/** 打印log信息时的标识 */
	private final static String TAG = MainActivity.class.getSimpleName();

	/**等效于MainActivity.this*/
	private Context context;
	/**自定义view*/
	private CustomView cv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = MainActivity.this;
		setContentView(R.layout.activity_main);
		cv = (CustomView) findViewById(R.id.myview);
		// 模拟网络请求和延时
		DrawThread th = new DrawThread();
		th.start();
			
		//改变数据源
		Button btn_change_view = (Button) findViewById(R.id.btn_change_view);
		btn_change_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 1 测试数据 最大最小均为正值
				/*String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"296\",\"231\",\"157\",\"87\",\"33\""
						+ "],\"x\":[\"保定\",\"石家庄\",\"唐山\",\"邯郸五六七八九\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				/*// 2 测试数据 最大为正值 最小为负值
				String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"125\",\"-81\",\"-157\",\"-287\",\"-610\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				// 3 测试数据 最大值和 最小均为负值
				String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"-4\",\"-81\",\"-165\",\"-257\",\"-290\",\"-327\",\"-368\",\"-454\",\"-512\",\"-666\",\"-773\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";

				String column = getOnedata(strJson, "column");// 柱状图数据
				// String column = getOnedata(strJson, "zhexian");//折线图数据

				JSONObject columnValues;
				try {
					columnValues = new JSONObject(column);
					JSONArray columnXArray = columnValues.getJSONArray("x");
					JSONArray columnYArray = columnValues.getJSONArray("y");
					List<String> columnXList = new ArrayList<String>();// x轴数据
					List<Float> columnYList = new ArrayList<Float>();// y轴数据
					for (int i = 0; i < columnXArray.length(); i++) {
						columnYList.add(Float.parseFloat(columnYArray.getString(i)));
						columnXList.add(columnXArray.getString(i));
					}
					cv.setData(columnXList, columnYList);// 组件数据赋值
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// 模拟请求网络的延时thread
	public class DrawThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(1500);
				MainActivity.this.runOnUiThread(updateThread);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 增加自定义chart的thread
		 */
		Runnable updateThread = new Runnable() {
			@Override
			public void run() {
				// Toast.makeText(context, "viewWidth==", Toast.LENGTH_SHORT).show();

				// 数据源分析：
				// {"measurename":"日收入", //上方的标题
				// "value":"3946", //上方标题的值
				// "ringrate":"--", //环比
				// "unit":"万元", //单位
				// "column":{"y":["689","568","465","457","368","299","296","231","227","182","165"],//柱状图y数据
				// "x":["保定","石家庄","唐山","邯郸","沧州","邢台","廊坊","张家口","承德","秦皇岛","衡水"]},//柱状图x数据
				// "regionname":"河北省", //地域
				// "cycle":"日", //标识是日数据还是月数据,需要根据这个处理date,再赋值给折线图
				// "date":"2014年6月19日 ",//日期
				// "measuerid":"DAY_INCOME",//指标名称，未用到，和kpiId相同
				// "state":"语音、短信、彩信、梦网、GPRS话单日收入" //口径
				// ,"samerate":"-13.50%", //同比
				// "source":"市场运营简报", //来源
				// "zhexian":{"y":["4040","3721","3457","4238","3912","3946"],//折线图y数据
				// "x":["6月14日 ","6月15日 ","6月16日 ","6月17日 ","6月18日 ","6月19日 "]}} //折线图x数据

				// 1 测试数据 最大最小均为正值
				/*String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"296\",\"231\",\"157\",\"87\",\"33\""
						+ "],\"x\":[\"保定\",\"石家庄\",\"唐山\",\"邯郸五六七八九\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				// 2 测试数据 最大为正值 最小为负值
				String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"125\",\"-81\",\"-157\",\"-287\",\"-610\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";

				// 3 测试数据 最大值和 最小均为负值
				/*String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"-4\",\"-81\",\"-165\",\"-257\",\"-290\",\"-327\",\"-368\",\"-454\",\"-512\",\"-666\",\"-773\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				// String measurename = getOnedata(strJson, "measurename");
				// String value = getOnedata(strJson, "value");
				// String ringrate = getOnedata(strJson, "ringrate");
				// String unit = getOnedata(strJson, "unit");
				// String regionname = getOnedata(strJson, "regionname");
				// String cycle = getOnedata(strJson, "cycle");
				// String date = getOnedata(strJson, "date");
				// String state = getOnedata(strJson, "state");
				// String samerate = getOnedata(strJson, "samerate");
				// String source = getOnedata(strJson, "source");
				// String chartTitleName = date + regionname + "地域分布(单位:" + unit + ")";

				String column = getOnedata(strJson, "column");// 柱状图数据
				// String column = getOnedata(strJson, "zhexian");//折线图数据

				JSONObject columnValues;
				try {
					columnValues = new JSONObject(column);
					JSONArray columnXArray = columnValues.getJSONArray("x");
					JSONArray columnYArray = columnValues.getJSONArray("y");
					List<String> columnXList = new ArrayList<String>();// x轴数据
					List<Float> columnYList = new ArrayList<Float>();// y轴数据
					for (int i = 0; i < columnXArray.length(); i++) {
						columnYList.add(Float.parseFloat(columnYArray.getString(i)));
						columnXList.add(columnXArray.getString(i));
					}
					cv.setData(columnXList, columnYList);// 组件数据赋值
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * 根据key获取json中的value
	 * @param json
	 * @param which
	 * @return
	 * @throws JSONException
	 */
	private String getOnedata(String json, String which) {
		String result = "";
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json);
			result = jsonObject.getString(which).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}
