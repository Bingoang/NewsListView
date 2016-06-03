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
	 * ʵ��������첽����
	 * ��������������URL;���Ȳ��ø���;����һ��NewsBean����,ÿ��NewsBean����һ������,���List����Adapter,���ձ���ʾ
	 */
	class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

		@Override
		protected List<NewsBean> doInBackground(String... params) {
			return getJsonData(params[0]);
		}

		//������ݺ󣬽����ݴ���newsAdapter������listView����������
		@Override
		protected void onPostExecute(List<NewsBean> newsBeans) {
			super.onPostExecute(newsBeans);
			NewsAdapter newsAdapter=new NewsAdapter(MainActivity.this,newsBeans,listView);
			listView.setAdapter(newsAdapter);
		}

		/*
		 * ͨ��is���ؽ�����ҳ�����ص�����
		 */
		private String readStream(InputStream is) {// ����һ��InputStream�����ֽ���
			InputStreamReader isr;
			String result = "";
			try {
				String line = "";
				isr = new InputStreamReader(is, "utf-8");// �ֽ���ת�����ַ���
				BufferedReader br = new BufferedReader(isr);// �����������ַ���
				while ((line = br.readLine()) != null) {
					result += line;// ÿһ�еĽ������ȡ����
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;
		}

		// ��URL��JSON��ʽ����ת�������Ƿ�װ��NewsBean����newsBeanList���أ����newsBeanList���ڴ���adapter
		private List<NewsBean> getJsonData(String url) {
			List<NewsBean> newsBeanList = new ArrayList<NewsBean>();

			try {
				/*
				 * �ʾ书����url.openConnnetion().getInputStream()��ͬ�����Ը���URL
				 * ֱ��������ȡ�������ݣ��򵥴ֱ�������ֵ����ΪInputStream
				 */
				String jsonString = readStream(new URL(url).openStream());
				Log.v("ang", jsonString);
				JSONObject jsonObject;
				NewsBean newsBean;// ����NewsBean�����װ����
				jsonObject = new JSONObject(jsonString);// ��json��ʽ����ת����jsonObject
				JSONArray jsonArray = jsonObject.getJSONArray("data");// ��jsonObjectȡ����Ϊ��data��������
				// ��������dataÿһ��obj��������ȡ����
				for (int i = 0; i < jsonArray.length(); i++) {
					jsonObject = jsonArray.getJSONObject(i);
					newsBean = new NewsBean();
					newsBean.iconUrl = jsonObject.getString("picSmall");
					newsBean.title = jsonObject.getString("name");
					newsBean.content = jsonObject.getString("description");
					// ��������������newsBeanList
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
