package com.newslistview;

import android.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImageLoader {
	private ImageView mImageView;
	private String mUrl;
	// ����Cache,�����������豣���������֣�������󣨼�ֵ�ԣ�
	private LruCache<String, Bitmap> mCache;
	private ListView mListView;
	private Set<NewsAsyncTask> mTask;

	public ImageLoader(ListView listView) {
		mListView = listView;
		mTask = new HashSet<NewsAsyncTask>();
		// ��ȡ��ǰӦ�ÿ�������ڴ�
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// �����ڴ�����
		int cacheSize = maxMemory / 4;
		// ��ʼ��LruCache��ͨ�������ڲ���
		mCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				// ��ÿ�δ��뻺��ʱ���ã���ȡ��ǰ����Bitmap�����С
				return value.getByteCount();
			}

		};

	}

	// ���ӵ�����
	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (getBitmapFromCache(url) == null) {
			mCache.put(url, bitmap);// key��value�ֱ��Ӧ��url, bitmap
		}
	}

	// �ӻ����л�ȡ����
	public Bitmap getBitmapFromCache(String url) {
		// LruCache��������һ��map,������get()��put()�������ײ���ͨ��linkedHashMapʵ�ֵġ��˴���key������
		return mCache.get(url);

	}

	/******************************************************************************************************/
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// ����ȡ��tag�봫������url�Ƚϣ����һ������ʾ��������step3��
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);// ��msg��bitmapȡ������ʾ�ڿؼ���
			}

		}
	};

	// ����һ��ʹ�ö��̼߳���ͼƬ
	public void showImageByThread(ImageView imageView, final String url) {// ����ȥһ���ؼ�����һ��url
		mImageView = imageView;
		// ���ó�Ա�����������ֵ����ֹ��������ʱ����󣨷�����step2��
		mUrl = url;
		// ����һ�����߳�
		new Thread() {
			@Override
			public void run() {
				super.run();
				Bitmap bitmap = getBitmapFromURL(url);
				Message message = Message.obtain();// ���ַ�������ʹ�����С����յ���message�����������
				message.obj = bitmap;// ��obj��Ϊ����������bitmap
				mhandler.sendMessage(message);// ���ʹ���bitmap��message
			}

		}.start();
	}

	// ��url������bitmap
	public Bitmap getBitmapFromURL(String urlString) {
		Bitmap bitmap;
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			is = new BufferedInputStream(connection.getInputStream());// ��InputStream��װ
			bitmap = BitmapFactory.decodeStream(is);// ��������InputStream������bitmap
			Log.v("ang", "�ѽ���ͼƬ");
			connection.disconnect();// �ͷ���Դ1
			// Thread.sleep(1000);//ģ�����粻�õ�״��
			return bitmap;
		} catch (MalformedURLException e) {
			Log.v("ang", "�����쳣1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ang", "�����쳣2");
			e.printStackTrace();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
		} finally {
			try {
				is.close();// �ͷ���Դ2
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	/******************************************************************************************************/
	// ��������ʹ��AsyncTask����ͼƬ
	public void showImageByAsyncTask(ImageView imageView, String url) {
		// �ӻ�����ȡ����ӦͼƬ
		Bitmap bitmap = getBitmapFromCache(url);
		// ���������û������ͼƬ������������أ��������ֱ��ʹ��
		if (bitmap == null) {
			imageView.setImageResource(R.mipmap.sym_def_app_icon);

		} else {
			imageView.setImageBitmap(bitmap);
		}

	}
//�������ش�start��end������ͼƬ
	public void loadImages(int start, int end) {
		for (int i = start; i < end; i++) {
			String url = NewsAdapter.URLS[i];// ֻ��ȡURLS[i]�е���start��ʼ������ͼƬ��url
			// �ӻ�����ȥ��ͼƬ
			Bitmap bitmap = getBitmapFromCache(url);
			// �������û�У�����ȥ���أ����ѻ��棬��ֱ����ʾͼƬ
			if (bitmap == null) {
				NewsAsyncTask task = new NewsAsyncTask(url);
				task.execute(url);
				mTask.add(task);// ��ÿ���½���task�ŵ���ǰ���Set<NewsAsyncTask>���϶���mTask��ͳһ����
			} else {
				// ͨ��tagѰ��imageView
				ImageView imageView = (ImageView) mListView
						.findViewWithTag(url);
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {
		// private ImageView mImageView;
		private String mUrl;

		// ��д���췽�������ݽ�url���ɣ�imageview���Դ�listview��ȡ
		public NewsAsyncTask(String url) {
			mUrl = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			// �������ȡͼƬ
			Bitmap bitmap = getBitmapFromURL(url);
			// ���������غ󣬽����ٻ����ͼƬ�ӵ�������
			if (bitmap != null) {
				addBitmapToCache(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			// ͨ��tagѰ��imageView
			ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			//��ʾ��ͼƬ�����������Ӧ��mTask������ȡ����������
			mTask.remove(this);
		}
	}

	public void cancelAllTask() {
		if (mTask != null) {
			//��mTask��ÿһ��taskȡ������ȡ����
			for (NewsAsyncTask task : mTask) {
				task.cancel(false);
			}
		}
	}
}
