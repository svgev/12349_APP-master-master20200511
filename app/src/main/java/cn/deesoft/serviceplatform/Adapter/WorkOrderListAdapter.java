package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import Util.UrlData;
import cn.deesoft.serviceplatform.R;

public class WorkOrderListAdapter extends BaseAdapter {
	private Context context;
	private ListView listview;
	private List<Map<String, Object>> list;
	public WorkOrderListAdapter(Context context, List<Map<String, Object>> list) {
		this.list = list;
		this.context=context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (listview == null) {
			listview = (ListView) parent;
		}

		final ViewHolder holder;
		if (convertView == null) {
			convertView  = LayoutInflater.from(context).inflate(R.layout.workorder_listitem, null);
			holder = new ViewHolder();
			
			holder.imgPhoto =  convertView.findViewById(R.id.imgPhoto);
			holder.txtOlderName =  convertView.findViewById(R.id.txtOlderName);
			holder.txtWealInterval=convertView.findViewById(R.id.txtWealInterval);
			holder.txtServiceName = convertView.findViewById(R.id.txtServiceName);
			holder.txtServiceTime =  convertView.findViewById(R.id.txtServiceTime);
			holder.txtPrice =  convertView.findViewById(R.id.txtPrice);
			holder.txtStartTime =  convertView.findViewById(R.id.txtStartTime);
			holder.txtBillingStatus=convertView.findViewById(R.id.txtBillingStatus);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(list.get(position).get("imgPhoto").toString().equals("null"))
		{
			holder.imgPhoto.setImageResource(R.mipmap.nophoto2);
		}
		else {
			Picasso.get().load(UrlData.getUrlYy() + list.get(position).get("imgPhoto").toString()).into(holder.imgPhoto, new Callback() {
				@Override
				public void onSuccess() {

				}

				@Override
				public void onError(Exception e) {
					holder.imgPhoto.setImageResource(R.mipmap.nophoto);
				}
			});
		}
		holder.txtOlderName.setText(list.get(position).get("txtOlderName").toString());
		holder.txtServiceName.setText(list.get(position).get("txtServiceName").toString());
		holder.txtServiceTime.setText(list.get(position).get("txtServiceTime").toString()+"分钟");
		holder.txtPrice.setText(list.get(position).get("txtPrice").toString());
		holder.txtStartTime.setText(list.get(position).get("txtStartTime").toString());
		holder.txtWealInterval.setText(list.get(position).get("txtWealInterval").toString()+"分钟");
		if(list.get(position).get("txtBillingStatus").toString().equals("异常")||list.get(position).get("txtBillingStatus").toString().equals("待结"))
		{
			holder.txtBillingStatus.setTextColor(Color.parseColor("#8E050D"));
		}
		else
		{
			holder.txtBillingStatus.setTextColor(Color.parseColor("#B5B5B5"));
		}
        holder.txtBillingStatus.setText(list.get(position).get("txtBillingStatus").toString());
		
		return convertView;
	}
	class ViewHolder{
		TextView txtOlderName,txtServiceName,txtServiceTime,txtPrice,txtStartTime,txtWealInterval,txtBillingStatus;
		ImageView imgPhoto;
	}
}
