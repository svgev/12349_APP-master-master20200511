package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import Model.Helper;
import cn.deesoft.serviceplatform.R;

public class HelperAdapter extends ArrayAdapter {
    private final int resourceId;
    ArrayList<Helper> helper_list;
    LayoutInflater inflater;
    private Context context;
    public Helper helper;


    public HelperAdapter(Context context, int textViewResourceId, ArrayList<Helper> helper_list) {
        super(context, textViewResourceId, helper_list);
        this.helper_list =helper_list;
        this.inflater = LayoutInflater.from(context);
        resourceId = textViewResourceId;
        this.context=context;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return helper_list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return helper_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        helper = (Helper) getItem(position);
        View view;
        HelperAdapter.ViewHolder viewHolder;
        //性能优化，不由重复加载布局
        if(convertView==null){
            view=LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new HelperAdapter.ViewHolder();
            viewHolder.helperImage=(ImageView) view.findViewById(R.id.helperImage);
            viewHolder.helperName = (TextView) view.findViewById(R.id.helperName);//获取该布局内的文本视图
            viewHolder.helperTown=(TextView) view.findViewById(R.id.helperTown);
            viewHolder.helperId=view.findViewById(R.id.helperId);
            viewHolder.helperId.setVisibility(View.INVISIBLE);
            viewHolder.helperMobile=view.findViewById(R.id.helperMobile);

            view.setTag(viewHolder);


        }else{
            view=convertView;
            viewHolder=(HelperAdapter.ViewHolder) view.getTag();
        }
        viewHolder.helperImage.setImageResource(helper.getImageId());
        viewHolder.helperName.setText(helper.getHelperName());
        viewHolder.helperTown.setText(helper.getHelperTown()+" "+helper.getHelperVillage());
        viewHolder.helperId.setText("编号："+helper.getHelperId());
        viewHolder.helperMobile.setText("手机："+helper.getHelperMobile());
        return view;
    }

    class ViewHolder{
        ImageView helperImage;
        TextView helperName;
        TextView helperTown;
        TextView helperId;
        TextView helperVillage;
        TextView helperMobile;
    }

}
