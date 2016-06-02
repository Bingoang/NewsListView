package com.newslistview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoader {
	private ImageView mImageView;
	private String mUrl;
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//将获取的tag与传进来的url比较，如果一样才显示（防错乱step3）
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);// 将msg中bitmap取出来显示在控件上
			}

		}
	};

	// 方法一：使用多线程加载图片
	public void showImageByThread(ImageView imageView, final String url) {// 传进去一个控件对象、一个url
		mImageView = imageView;
		//运用成员变量缓存加载值，防止网络下载时序错误（防错乱step2）
		mUrl=url;
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
//			Thread.sleep(1000);//模拟网络不好的状况
			return bitmap;
		} catch (MalformedURLException e) {
			Log.v("ang", "解析异常1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ang", "解析异常2");
			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
		} finally {
			try {
				is.close();// 释放资源2
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}
}
