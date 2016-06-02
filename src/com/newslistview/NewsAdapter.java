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
		mList = data;// ������ӳ�����
		mInflater = LayoutInflater.from(context);// ��ʼ��LayoutInflater����
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
		// ��mInflaterת����convertVeiw
		if (convertVeiw == null) {
			viewHolder = new ViewHolder();
			convertVeiw = mInflater.inflate(R.layout.item, null);// �ڶ����������������������ﲻ��Ҫָ��������
			viewHolder.ivIcon = (ImageView) convertVeiw.findViewById(R.id.icon);
			viewHolder.tvTitle = (TextView) convertVeiw
					.findViewById(R.id.title);
			viewHolder.tvContent = (TextView) convertVeiw
					.findViewById(R.id.content);
			convertVeiw.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertVeiw.getTag();
		}
		//����Ĭ��ͼ��
		viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);
		viewHolder.tvTitle.setText(mList.get(position).title);
		viewHolder.tvContent.setText(mList.get(position).content);
		//��󷵻�convertVeiw
		return convertVeiw;
	}

	class ViewHolder {
		public TextView tvTitle, tvContent;
		public ImageView ivIcon;
	}
}
