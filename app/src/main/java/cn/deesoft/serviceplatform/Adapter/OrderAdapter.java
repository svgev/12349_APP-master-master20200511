package cn.deesoft.serviceplatform.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import Model.Order;
import cn.deesoft.serviceplatform.R;

public class OrderAdapter extends ArrayAdapter {
    private final int resourceId;
    ArrayList<Order> order_list;
    LayoutInflater inflater;
    public String orderName;
    private Context context;
    Order order;

    public OrderAdapter(Context context, int textViewResourceId, ArrayList<Order> objects) {
        super(context, textViewResourceId, objects);
        this.order_list=objects;
        resourceId = textViewResourceId;
        this.inflater = LayoutInflater.from(context);
        this.context=context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return order_list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return order_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Order order = (Order) getItem(position); // 获取当前项的older实例
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象
            viewHolder=new ViewHolder();
            viewHolder.orderImage = (ImageView) view.findViewById(R.id.orderImage);//获取该布局内的图片视图
            viewHolder.olderName=(TextView)view.findViewById(R.id.clientName);
            viewHolder.workerName = (TextView) view.findViewById(R.id.workerName);//获取该布局内的文本视图
            viewHolder.state=(TextView)view.findViewById(R.id.state);
            viewHolder.orderDate=(TextView)view.findViewById(R.id.orderDate);
            viewHolder.orderId=(TextView)view.findViewById(R.id.orderId);
            view.setTag(viewHolder);
        }
        else{
            view=convertView;
            viewHolder=(ViewHolder) view.getTag();
        }


        viewHolder.orderImage.setImageResource(order.getImageId());
        viewHolder.workerName.setText(order.getWorkerName());
        viewHolder.olderName.setText(order.getClientName());
        viewHolder.state.setText(order.getState());
        viewHolder.orderDate.setText(order.getOrderDate());
        viewHolder.orderId.setText(order.getOrderId());
        return view;

    }
    class ViewHolder{
        ImageView orderImage;
        TextView olderName;
        TextView workerName;
        TextView state;
        TextView orderDate;
        TextView orderId;
    }
}