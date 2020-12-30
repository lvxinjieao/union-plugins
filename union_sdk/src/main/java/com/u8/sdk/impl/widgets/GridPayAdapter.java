package com.u8.sdk.impl.widgets;

import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.u8.sdk.impl.activities.PayActivity;
import com.u8.sdk.impl.common.PayPlatformType;
import com.u8.sdk.utils.ResourceHelper;


public class GridPayAdapter extends BaseAdapter{

	private List<GridPayTypeData> dataList;
	
	private LayoutInflater inflater;
	
	private Handler handler;
	
	public GridPayAdapter(Activity context, List<GridPayTypeData> data, Handler handler){
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
		if(convertView == null){
			convertView = this.inflater.inflate(ResourceHelper.getIdentifier(inflater.getContext(), "R.layout.x_pay_grid_item"), parent, false);
			holder = new ViewHolder();
			holder.payTypeImage = (ImageView)ResourceHelper.getViewByParent(convertView, "R.id.x_pay_img");
			holder.payCheckedImage = (ImageView)ResourceHelper.getViewByParent(convertView, "R.id.x_pay_slt");
			holder.payTypeName = (TextView)ResourceHelper.getViewByParent(convertView, "R.id.x_pay_c_name");
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		GridPayTypeData item = this.dataList.get(position);
		holder.payTypeName.setText(item.name);
		holder.payTypeImage.setImageResource(item.imgID);
		if(item.isChecked){
			holder.payCheckedImage.setVisibility(View.VISIBLE);
		}else{
			holder.payCheckedImage.setVisibility(View.GONE);
		}
		
		convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(handler != null){
					Message msg = new Message();
					msg.what = PayActivity.TAG_PAY_TYPE_SELECTED;
					msg.obj = position;
					handler.sendMessage(msg);
				}
				
			}
		});
		
		return convertView;
	}
	
	public class ViewHolder{
		public ImageView payTypeImage;
		public ImageView payCheckedImage;
		public TextView payTypeName;
	}
	
	public static class GridPayTypeData{
		public PayPlatformType typeID;
		public int imgID;
		public String name;
		public boolean isChecked;
		public boolean canChecked;
		
		public GridPayTypeData(){
			this.isChecked = false;
			this.canChecked = true;
		}
	}

}


