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
	public static String[] URLS;// 此数组用于保存当前获取到的所有图片的地址
	private boolean mFirstIn;// 预启动加载标志位

	// 适配器的构造函数起到初始化的作用，里面是各项的初始化
	public NewsAdapter(Context context, List<NewsBean> data, ListView listView) {
		mList = data;// 将数据映射过来
		mInflater = LayoutInflater.from(context);// 初始化LayoutInflater对象
		mImageLoader = new ImageLoader(listView);// 不用每次都new一个LruCache，保证只有一个LruCache
		URLS = new String[data.size()];
		for (int i = 0; i < data.size(); i++) {
			URLS[i] = data.get(i).iconUrl;// 把所有图片的地址放到静态数组URLS[i]中
		}
		mFirstIn = true;// 默认是第一次启动
		// 别忘了注册滚动接口监听事件
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
		// 将mInflater转化成convertVeiw
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item, null);// 第二个参数：父容器，在这里不需要指定父容器
			viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.title);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.content);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String url = mList.get(position).iconUrl;//此句上移了
		//如果缓存中已经存在则设置缓存图片
		Bitmap bitmap = mImageLoader.getBitmapFromCache(url);

		if (bitmap != null) {

		  viewHolder.ivIcon.setImageBitmap(bitmap);

		} else {
		// 设置默认图标
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		}
		
		
		// 设置tag,即将各图片控件ivIcon与对应的url进行了绑定（防错乱step1）
//		String url = mList.get(position).iconUrl;
		viewHolder.ivIcon.setTag(url);

		// //方法一：调用方法showImageByThread()，并将item对应的图片控件、对应url传进去
		// new ImageLoader().showImageByThread(viewHolder.ivIcon,url);

		// 方法二：调用方法showImageByAsyncTask()，并将item对应的图片控件、对应url传进去
		// new ImageLoader().showImageByAsyncTask(viewHolder.ivIcon,
		// url);//这样写不行：会new很多个LruCache
		mImageLoader.showImageByAsyncTask(viewHolder.ivIcon, url);// 应该这么写，保证不重复new多个LruCache

		viewHolder.tvTitle.setText(mList.get(position).title);
		viewHolder.tvContent.setText(mList.get(position).content);
		// 最后返回convertVeiw
		return convertView;
	}

	class ViewHolder {
		public TextView tvTitle, tvContent;
		public ImageView ivIcon;
	}

	// 滑动状态变化时调用该方法
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 当滚动停止时，加载可见项；其他状态则停止所有加载任务
		if (scrollState == SCROLL_STATE_IDLE) {

			mImageLoader.loadImages(mStart, mEnd);
		} else {
			mImageLoader.cancelAllTask();
		}
	}

	// 任何时候会去调用，三个参数：第一个可见元素位置，可见元素长度(个数)，
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItem) {
		mStart = firstVisibleItem;
		mEnd = firstVisibleItem + visibleItemCount;
		// 首次启动预加载
		if (mFirstIn && visibleItemCount > 0) {
			mImageLoader.loadImages(mStart, mEnd);
			mFirstIn = false;
		}
	}

}
