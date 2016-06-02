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
			//����ȡ��tag�봫������url�Ƚϣ����һ������ʾ��������step3��
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);// ��msg��bitmapȡ������ʾ�ڿؼ���
			}

		}
	};

	// ����һ��ʹ�ö��̼߳���ͼƬ
	public void showImageByThread(ImageView imageView, final String url) {// ����ȥһ���ؼ�����һ��url
		mImageView = imageView;
		//���ó�Ա�����������ֵ����ֹ��������ʱ����󣨷�����step2��
		mUrl=url;
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
//			Thread.sleep(1000);//ģ�����粻�õ�״��
			return bitmap;
		} catch (MalformedURLException e) {
			Log.v("ang", "�����쳣1");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ang", "�����쳣2");
			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
		} finally {
			try {
				is.close();// �ͷ���Դ2
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}
}
