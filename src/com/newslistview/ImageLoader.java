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
	// 创建Cache,两个参数：需保存对象的名字，保存对象（键值对）
	private LruCache<String, Bitmap> mCache;
	private ListView mListView;
	private Set<NewsAsyncTask> mTask;

	public ImageLoader(ListView listView) {
		mListView = listView;
		mTask = new HashSet<NewsAsyncTask>();
		// 获取当前应用可用最大内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// 设置内存上限
		int cacheSize = maxMemory / 4;
		// 初始化LruCache，通过匿名内部类
		mCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				// 在每次存入缓存时调用，获取当前存入Bitmap对象大小
				return value.getByteCount();
			}

		};

	}

	// 增加到缓存
	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (getBitmapFromCache(url) == null) {
			mCache.put(url, bitmap);// key和value分别对应：url, bitmap
		}
	}

	// 从缓存中获取数据
	public Bitmap getBitmapFromCache(String url) {
		// LruCache本质上是一个map,所以有get()和put()方法，底层是通过linkedHashMap实现的。此处把key传进来
		return mCache.get(url);

	}

	/******************************************************************************************************/
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 将获取的tag与传进来的url比较，如果一样才显示（防错乱step3）
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);// 将msg中bitmap取出来显示在控件上
			}

		}
	};

	// 方法一：使用多线程加载图片
	public void showImageByThread(ImageView imageView, final String url) {// 传进去一个控件对象、一个url
		mImageView = imageView;
		// 运用成员变量缓存加载值，防止网络下载时序错误（防错乱step2）
		mUrl = url;
		// 开启一个子线程
		new Thread() {
			@Override
			public void run() {
				super.run();
				Bitmap bitmap = getBitmapFromURL(url);
				Message message = Message.obtain();// 这种方法可以使用现有、回收到的message，提高利用率
				message.obj = bitmap;// 将obj设为解析出来的bitmap
				mhandler.sendMessage(message);// 发送带有bitmap的message
			}

		}.start();
	}

	// 将url解析成bitmap
	public Bitmap getBitmapFromURL(String urlString) {
		Bitmap bitmap;
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			is = new BufferedInputStream(connection.getInputStream());// 把InputStream包装
			bitmap = BitmapFactory.decodeStream(is);// 将传进的InputStream解析成bitmap
			Log.v("ang", "已解析图片");
			connection.disconnect();// 释放资源1
			// Thread.sleep(1000);//模拟网络不好的状况
			return bitmap;
		} catch (MalformedURLException e) {
			Log.v("ang", "解析异常1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ang", "解析异常2");
			e.printStackTrace();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
		} finally {
			try {
				is.close();// 释放资源2
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	/******************************************************************************************************/
	// 方法二：使用AsyncTask加载图片
	public void showImageByAsyncTask(ImageView imageView, String url) {
		// 从缓存中取出对应图片
		Bitmap bitmap = getBitmapFromCache(url);
		// 如果缓存中没有这张图片，则从网络下载；如果有则直接使用
		if (bitmap == null) {
			imageView.setImageResource(R.mipmap.sym_def_app_icon);

		} else {
			imageView.setImageBitmap(bitmap);
		}

	}
//用来加载从start到end的所有图片
	public void loadImages(int start, int end) {
		for (int i = start; i < end; i++) {
			String url = NewsAdapter.URLS[i];// 只获取URLS[i]中到从start开始的所有图片的url
			// 从缓存中去除图片
			Bitmap bitmap = getBitmapFromCache(url);
			// 如果缓存没有，必须去下载；若已缓存，则直接显示图片
			if (bitmap == null) {
				NewsAsyncTask task = new NewsAsyncTask(url);
				task.execute(url);
				mTask.add(task);// 将每个新建的task放到当前活动的Set<NewsAsyncTask>集合对象mTask中统一管理
			} else {
				// 通过tag寻找imageView
				ImageView imageView = (ImageView) mListView
						.findViewWithTag(url);
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {
		// private ImageView mImageView;
		private String mUrl;

		// 重写构造方法，传递进url即可，imageview可以从listview获取
		public NewsAsyncTask(String url) {
			mUrl = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			// 从网络获取图片
			Bitmap bitmap = getBitmapFromURL(url);
			// 从网络下载后，将不再缓存的图片加到缓存中
			if (bitmap != null) {
				addBitmapToCache(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			// 通过tag寻找imageView
			ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			//显示完图片后，任务结束，应从mTask集合中取消掉该任务
			mTask.remove(this);
		}
	}

	public void cancelAllTask() {
		if (mTask != null) {
			//将mTask中每一个task取出来，取消掉
			for (NewsAsyncTask task : mTask) {
				task.cancel(false);
			}
		}
	}
}
