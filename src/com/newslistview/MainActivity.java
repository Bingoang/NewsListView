package com.newslistview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.widget.ListView;
import android.app.Activity;

public class MainActivity extends Activity {

	private ListView listView;
	private static String URL = "http://www.imooc.com/api/teacher?type=4&num=30";
	private NewsAsyncTask newsAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		newsAsyncTask = new NewsAsyncTask();
		newsAsyncTask.execute(URL);
	}

	private void initView() {
		listView = (ListView) findViewById(R.id.listview);

	}

	/*
	 * 实现网络的异步访问
	 * 三个参数：传入URL;进度不用更新;返回一个NewsBean集合,每个NewsBean代表一行数据,这个List传给Adapter,最终被显示
	 */
	class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

		@Override
		protected List<NewsBean> doInBackground(String... params) {
			return getJsonData(params[0]);
		}

		//获得数据后，将数据传给newsAdapter，并将listView于适配器绑定
		@Override
		protected void onPostExecute(List<NewsBean> newsBeans) {
			super.onPostExecute(newsBeans);
			NewsAdapter newsAdapter=new NewsAdapter(MainActivity.this,newsBeans,listView);
			listView.setAdapter(newsAdapter);
		}

		/*
		 * 通过is返回解析网页所返回的数据
		 */
		private String readStream(InputStream is) {// 传进一个InputStream对象：字节流
			InputStreamReader isr;
			String result = "";
			try {
				String line = "";
				isr = new InputStreamReader(is, "utf-8");// 字节流转换成字符流
				BufferedReader br = new BufferedReader(isr);// 缓存器读出字符流
				while ((line = br.readLine()) != null) {
					result += line;// 每一行的结果都读取出来
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;
		}

		// 将URL中JSON格式数据转化成我们封装的NewsBean对象newsBeanList返回，这个newsBeanList用于传给adapter
		private List<NewsBean> getJsonData(String url) {
			List<NewsBean> newsBeanList = new ArrayList<NewsBean>();

			try {
				/*
				 * 词句功能与url.openConnnetion().getInputStream()相同，可以根据URL
				 * 直接联网获取网络数据，简单粗暴！返回值类型为InputStream
				 */
				String jsonString = readStream(new URL(url).openStream());
				Log.v("ang", jsonString);
				JSONObject jsonObject;
				NewsBean newsBean;// 创建NewsBean对象封装数据
				jsonObject = new JSONObject(jsonString);// 将json格式数据转化成jsonObject
				JSONArray jsonArray = jsonObject.getJSONArray("data");// 从jsonObject取出名为“data”的数组
				// 遍历数组data每一个obj并把它们取出来
				for (int i = 0; i < jsonArray.length(); i++) {
					jsonObject = jsonArray.getJSONObject(i);
					newsBean = new NewsBean();
					newsBean.iconUrl = jsonObject.getString("picSmall");
					newsBean.title = jsonObject.getString("name");
					newsBean.content = jsonObject.getString("description");
					// 将所有数据塞到newsBeanList
					newsBeanList.add(newsBean);
				}

			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return newsBeanList;
		}

	}

}
