package com.newslistview;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter {

	private List<NewsBean> mList;
	private LayoutInflater mInflater;


	public NewsAdapter(Context context, List<NewsBean> data) {
		mList = data;// 将数据映射过来
		mInflater = LayoutInflater.from(context);// 初始化LayoutInflater对象
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
	public View getView(int position, View convertVeiw, ViewGroup parent) {
		ViewHolder viewHolder = null;
		// 将mInflater转化成convertVeiw
		if (convertVeiw == null) {
			viewHolder = new ViewHolder();
			convertVeiw = mInflater.inflate(R.layout.item, null);// 第二个参数：父容器，在这里不需要指定父容器
			viewHolder.ivIcon = (ImageView) convertVeiw.findViewById(R.id.icon);
			viewHolder.tvTitle = (TextView) convertVeiw
					.findViewById(R.id.title);
			viewHolder.tvContent = (TextView) convertVeiw
					.findViewById(R.id.content);
			convertVeiw.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertVeiw.getTag();
		}
		//设置默认图标
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		viewHolder.tvTitle.setText(mList.get(position).title);
		viewHolder.tvContent.setText(mList.get(position).content);
		//最后返回convertVeiw
		return convertVeiw;
	}

	class ViewHolder {
		public TextView tvTitle, tvContent;
		public ImageView ivIcon;
	}
}
