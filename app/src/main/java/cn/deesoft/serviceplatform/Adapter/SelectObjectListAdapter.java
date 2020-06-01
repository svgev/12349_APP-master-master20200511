package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import Util.HttpUtil;
import Util.UrlData;
import cn.deesoft.serviceplatform.R;
import cn.deesoft.serviceplatform.ServiceObjectActivity;
import cn.deesoft.serviceplatform.StartServiceActivity;


public class SelectObjectListAdapter extends BaseAdapter {
    private Context context;
    private ListView listview;
    private List<Map<String, Object>> list;
    public SelectObjectListAdapter(Context context, List<Map<String, Object>> list) {
        this.list = list;
        this.context=context;
    }
@Override
    public int getCount() { return list.size(); }

@Override
public Object getItem(int position) {
    return list.get(position);
}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (listview == null) {
            listview = (ListView) parent;
        }
        final SelectObjectListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView  = LayoutInflater.from(context).inflate(R.layout.selectobject_listitem, null);
            holder = new SelectObjectListAdapter.ViewHolder();

            holder.btnService=convertView.findViewById(R.id.btnService);
            holder.userIcon =  convertView.findViewById(R.id.userIcon);
            holder.txtName =  convertView.findViewById(R.id.txtName);
            holder.txtAge=convertView.findViewById(R.id.txtAge);
            holder.txtRemainTime=convertView.findViewById(R.id.txtRemainTime);
            convertView.setTag(holder);
        }else {
            holder = (SelectObjectListAdapter.ViewHolder) convertView.getTag();
        }

        holder.btnService.setTag(position);

        if(list.get(position).get("photo")!=null) {
            Picasso.get().load(UrlData.getUrlYy() + list.get(position).get("photo").toString()).into(holder.userIcon, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(Exception e) {
                    holder.userIcon.setImageResource(R.mipmap.nophoto2);
                }
            });
        }
        else
        {
            if(Boolean.valueOf(list.get(position).get("IsLiving").toString())){
                holder.userIcon.setImageResource(R.mipmap.nophoto);}else{
                holder.userIcon.setImageResource(R.mipmap.nophoto_black);
            }
        }
        holder.txtName.setText(list.get(position).get("txtName").toString());
        holder.txtAge.setText(list.get(position).get("txtAge").toString()+"岁");
        holder.txtRemainTime.setText(list.get(position).get("RemainTime").toString());
        //holder.txtRemainTime.setText(list.get(position).get("txtRemainTime").toString());
        holder.btnService.setOnClickListener(new  View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
                if (HttpUtil.isFastClick()) {
                    //onclik事件中无法获取getView方法中的position变量，只能通过获取按钮的tag得到索引
                    int position=(Integer)v.getTag();
                    Bundle bundle = new Bundle();
                    bundle.putString("OlderID", list.get(position).get("ID").toString());
                    bundle.putString("OlderName", list.get(position).get("txtName").toString());
                    bundle.putString("Age",list.get(position).get("txtAge").toString());
                    bundle.putBoolean("IsLiving",Boolean.valueOf(list.get(position).get("IsLiving").toString()));
                    bundle.putString("RemainTime",list.get(position).get("RemainTime").toString());
                    if(list.get(position).get("photo")!=null) {
                        bundle.putString("Photo",list.get(position).get("photo").toString());
                    }
                    else
                    {
                        bundle.putString("Photo","null");
                    }
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(SelectObjectListAdapter.this.context, StartServiceActivity.class);
                    SelectObjectListAdapter.this.context.startActivity(intent);
                }
            }
        });
        return convertView;
    }
    class ViewHolder{
        TextView txtName,txtAge,txtRemainTime;
        Button btnService;
        ImageView userIcon;
    }
}
