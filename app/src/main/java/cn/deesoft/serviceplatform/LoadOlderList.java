package cn.deesoft.serviceplatform;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class LoadOlderList extends ListView implements AbsListView.OnScrollListener {
    private int lastVisibleItem;//最后一个可见的item
    private int totalItemCount;//总的item
    private boolean isLoading = false;//是否正在加载数据
    public ILoadListener iLoadListener;//回调接口，用来加载数据
    public View footer;
    public Boolean canLoad;


    public LoadOlderList(Context context) {
        super(context);
        initView(context);
    }
    public LoadOlderList(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    public LoadOlderList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    private void initView(Context context){
        LayoutInflater inflater= LayoutInflater.from(context);
        footer = inflater.inflate(R.layout.footer_layout, null);
        footer.findViewById(R.id.load_layout).setVisibility(View.INVISIBLE);
        canLoad=true;
        //注意，这句代码的意思是给自定义的ListView加上底布局
        this.addFooterView(footer);
        this.setOnScrollListener(this);//千万别忘记设定监听器
    }

    //加载数据完成后，需要执行的操作
    public void loadComplete(){
        isLoading = false;
        footer.findViewById(R.id.load_layout).setVisibility(View.INVISIBLE);
    }

    public interface ILoadListener{
        void onLoad();
    }

    public void setInterface(ILoadListener iLoadListener){
        this.iLoadListener = iLoadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (totalItemCount == lastVisibleItem && scrollState == SCROLL_STATE_IDLE) {
            if (!isLoading) {
                isLoading = true;
                footer.findViewById(R.id.load_layout).setVisibility(
                        View.VISIBLE);
                if(iLoadListener!=null&&canLoad==true) {
                    // 加载更多
                    iLoadListener.onLoad();
                }
            }
        }
    }
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount, int totalItemCount){
        this.lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

}

