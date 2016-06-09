package com.newslistview;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter implements OnScrollListener {

	private List<NewsBean> mList;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private int mStart, mEnd;
	public static String[] URLS;// ���������ڱ��浱ǰ��ȡ��������ͼƬ�ĵ�ַ
	private boolean mFirstIn;// Ԥ�������ر�־λ

	// �������Ĺ��캯���𵽳�ʼ�������ã������Ǹ���ĳ�ʼ��
	public NewsAdapter(Context context, List<NewsBean> data, ListView listView) {
		mList = data;// ������ӳ�����
		mInflater = LayoutInflater.from(context);// ��ʼ��LayoutInflater����
		mImageLoader = new ImageLoader(listView);// ����ÿ�ζ�newһ��LruCache����ֻ֤��һ��LruCache
		URLS = new String[data.size()];
		for (int i = 0; i < data.size(); i++) {
			URLS[i] = data.get(i).iconUrl;// ������ͼƬ�ĵ�ַ�ŵ���̬����URLS[i]��
		}
		mFirstIn = true;// Ĭ���ǵ�һ������
		// ������ע������ӿڼ����¼�
		listView.setOnScrollListener(this);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		// ��mInflaterת����convertVeiw
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item, null);// �ڶ����������������������ﲻ��Ҫָ��������
			viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.title);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.content);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String url = mList.get(position).iconUrl;//�˾�������
		//����������Ѿ����������û���ͼƬ
		Bitmap bitmap = mImageLoader.getBitmapFromCache(url);

		if (bitmap != null) {

		  viewHolder.ivIcon.setImageBitmap(bitmap);

		} else {
		// ����Ĭ��ͼ��
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		}
		
		
		// ����tag,������ͼƬ�ؼ�ivIcon���Ӧ��url�����˰󶨣�������step1��
//		String url = mList.get(position).iconUrl;
		viewHolder.ivIcon.setTag(url);

		// //����һ�����÷���showImageByThread()������item��Ӧ��ͼƬ�ؼ�����Ӧurl����ȥ
		// new ImageLoader().showImageByThread(viewHolder.ivIcon,url);

		// �����������÷���showImageByAsyncTask()������item��Ӧ��ͼƬ�ؼ�����Ӧurl����ȥ
		// new ImageLoader().showImageByAsyncTask(viewHolder.ivIcon,
		// url);//����д���У���new�ܶ��LruCache
		mImageLoader.showImageByAsyncTask(viewHolder.ivIcon, url);// Ӧ����ôд����֤���ظ�new���LruCache

		viewHolder.tvTitle.setText(mList.get(position).title);
		viewHolder.tvContent.setText(mList.get(position).content);
		// ��󷵻�convertVeiw
		return convertView;
	}

	class ViewHolder {
		public TextView tvTitle, tvContent;
		public ImageView ivIcon;
	}

	// ����״̬�仯ʱ���ø÷���
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// ������ֹͣʱ�����ؿɼ������״̬��ֹͣ���м�������
		if (scrollState == SCROLL_STATE_IDLE) {

			mImageLoader.loadImages(mStart, mEnd);
		} else {
			mImageLoader.cancelAllTask();
		}
	}

	// �κ�ʱ���ȥ���ã�������������һ���ɼ�Ԫ��λ�ã��ɼ�Ԫ�س���(����)��
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItem) {
		mStart = firstVisibleItem;
		mEnd = firstVisibleItem + visibleItemCount;
		// �״�����Ԥ����
		if (mFirstIn && visibleItemCount > 0) {
			mImageLoader.loadImages(mStart, mEnd);
			mFirstIn = false;
		}
	}

}
