package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import Model.KeyValueInfo;
import cn.deesoft.serviceplatform.R;

public class GridviewAdapter extends BaseAdapter {
    private ArrayList<KeyValueInfo> list;
    private static HashMap<Integer,Boolean> isSelected;
    private Context context;
    private String[] content=null;
    private LayoutInflater inflater = null;
    public GridviewAdapter(ArrayList<KeyValueInfo> list, Context context) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        initDate();
    }
    public GridviewAdapter(ArrayList<KeyValueInfo> list,String[] content, Context context) {
        this.context = context;
        this.content=content;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        initDate();
    }
    private void initDate(){
        if(content!=null) {
            int tag = 0;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < content.length; j++) {
                    if (list.get(i).getValue().equals(content[j])) {
                        getIsSelected().put(i, true);
                        tag = 1;
                    }
                }
                if (tag == 0) {
                    getIsSelected().put(i, false);
                } else {
                    tag = 0;
                }
            }
        }
        else
        {
            for (int i = 0; i < list.size(); i++) {
                getIsSelected().put(i, false);
            }
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.service_gridviewlist, null);
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.ID = (TextView) convertView.findViewById(R.id.item_ID);
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ID.setText(list.get(position).getKey());
        holder.name.setText(list.get(position).getValue());
        holder.cb.setChecked(getIsSelected().get(position));

        return convertView;
    }
    public static class ViewHolder{
        public  CheckBox cb;
        public TextView name;
        public TextView ID;
    }
    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }
    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
        GridviewAdapter.isSelected = isSelected;
    }
}
