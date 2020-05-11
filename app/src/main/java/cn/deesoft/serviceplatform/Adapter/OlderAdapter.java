package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import Model.Older;
import cn.deesoft.serviceplatform.R;

public class OlderAdapter extends ArrayAdapter {
    private final int resourceId;
    ArrayList<Older> older_list;
    LayoutInflater inflater;
    public String olderName;
    private Context context;
    Older older;


    public OlderAdapter(Context context, int textViewResourceId, ArrayList<Older> older_list) {
        super(context, textViewResourceId, older_list);
        this.older_list = older_list;
        this.inflater = LayoutInflater.from(context);
        resourceId = textViewResourceId;
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return older_list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return older_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        older = (Older) getItem(position);
        View view;
        ViewHolder viewHolder;
        //性能优化，不由重复加载布局
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.olderImage = (ImageView) view.findViewById(R.id.olderImage);
            viewHolder.olderName = (TextView) view.findViewById(R.id.olderName);//获取该布局内的文本视图
            viewHolder.olderAge = (TextView) view.findViewById(R.id.olderAge);
            viewHolder.olderIdentityId = (TextView) view.findViewById(R.id.olderIdentityID);
            viewHolder.olderId=view.findViewById(R.id.olderId);
            viewHolder.olderVillage=view.findViewById(R.id.olderVillage);
            viewHolder.islving=view.findViewById(R.id.isLiving);//这个可以删除


            view.setTag(viewHolder);


        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.olderImage.setImageResource(older.getImageId());
        viewHolder.olderName.setText(older.getOlderName());
        viewHolder.olderAge.setText(older.getOlderAge());
        viewHolder.olderIdentityId.setText(older.getIdentityId());
        viewHolder.olderId.setText(older.getID());
        viewHolder.olderVillage.setText(older.getVillage());

        return view;


    }

    class ViewHolder {
        ImageView olderImage;
        TextView olderName;
        TextView olderAge;
        TextView olderIdentityId;
        TextView olderId;
        TextView olderVillage;
        TextView islving;
    }
}



