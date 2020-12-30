package com.u8.sdk.permission.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.u8.sdk.utils.ResourceHelper;

import java.util.List;

public class PermissionGridAdapter extends BaseAdapter {

	private List<PermissionItemData> dataList;

	private LayoutInflater inflater;

	private Handler handler;

	public PermissionGridAdapter(Context context, List<PermissionItemData> data,
								 Handler handler) {
		this.dataList = data;
		this.inflater = LayoutInflater.from(context);

		this.handler = handler;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int index) {
		return dataList.get(index);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = this.inflater.inflate(ResourceHelper.getIdentifier(
					inflater.getContext(), "R.layout.u8_permission_grid_item"), parent,
					false);
			holder = new ViewHolder();
			holder.permissionImg = (ImageView) ResourceHelper.getViewByParent(
					convertView, "R.id.u8_permission_item_img");
			holder.permissionName = (TextView) ResourceHelper.getViewByParent(
					convertView, "R.id.u8_permission_item_name");
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PermissionItemData item = this.dataList.get(position);
		holder.permissionName.setText(item.name);
		holder.permissionImg.setImageResource(ResourceHelper.getIdentifier(inflater.getContext(), item.imgID));


//		convertView.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				if (handler != null) {
//					Message msg = new Message();
//					msg.what = MsgTagID.TAG_PAY_TYPE_SELECTED;
//					msg.obj = position;
//					handler.sendMessage(msg);
//				}
//
//			}
//		});

		return convertView;
	}

	public class ViewHolder {
		public ImageView permissionImg;
		public TextView permissionName;
	}

	public static class PermissionItemData {
		public String imgID;
		public String name;

		public PermissionItemData() {

		}

		public PermissionItemData(String imgID, String name){
			this.imgID = imgID;
			this.name = name;
		}
	}

}
