package cn.deesoft.serviceplatform;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import Model.Order;
import Model.ResultInfoList;
import Util.DialogUtil;
import Util.UrlData;
import cn.deesoft.serviceplatform.Adapter.OrderAdapter;
import cn.deesoft.serviceplatform.R;

public class OrderListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{


    private ImageView deleteText;
    private EditText searchText;
    private TextView searchButton;
    public String searchContent;

    private LoadOrderList listView;
    OrderAdapter orderAdapter;
    private ArrayList<Order> orderList = new ArrayList<>();

    private int pageNum;
    private String town;
    private String key;
    private String billingstatus;

    private Spinner mySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> spinnerList = new ArrayList<String>();

    private Dialog mWeiboDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        key = "";
        Intent intent = getIntent();
        town = intent.getStringExtra("Area");
        billingstatus=null;
        orderAdapter = new OrderAdapter(OrderListActivity.this, R.layout.order_listitem, orderList);
        listView = findViewById(R.id.order_list_view);
        listView.setAdapter(orderAdapter);
        listView.setOnItemClickListener(this);
        deleteText = (ImageView) findViewById(R.id.bt_delete_text);
        searchText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_button_1);

        initOrders();

        spinnerList.add("全部");
        spinnerList.add("待结");
        spinnerList.add("搁置");
        spinnerList.add("已结");
        spinnerList.add("异常");


        mySpinner=findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(spinnerAdapter);


        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                billingstatus=spinnerAdapter.getItem(arg2);
                pageNum=1;
                orderList.clear();
                initOrders();

            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                billingstatus=null;
            }
        });



        pageNum = 1;
        myOnclick();
        initClick();


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(town+" - 工单");
        }
    }

    private Handler firstPageHandler = new Handler() {
        ResultInfoList<Object> list = new ResultInfoList<Object>();

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        DialogUtil.closeDialog(mWeiboDialog);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(msg.obj.toString());
                        list = mapper.readValue(node.toString(), new TypeReference<ResultInfoList<LinkedHashMap>>() {
                        });
                        for (LinkedHashMap map : (LinkedHashMap[]) list.Data) {
                            String orderId=map.get("ID").toString();
                            String helperName = map.get("HelperName").toString();
                            String olderName = map.get("OldPeopleName").toString();
                            String billState = map.get("BillingStatus").toString();
                            String Date = map.get("AddDate").toString();
                            Order order = new Order(orderId,helperName, olderName, R.mipmap.nophoto2, billState, Date);
                            orderList.add(order);
                        }
                    } catch (Exception ex) {
                    }
                    orderAdapter.notifyDataSetChanged();
                    listView.loadComplete();
                    break;

//          通知适配器数据已经改变

            }


        }


    };

    private void initOrders() {
        ResultInfoList<Object> list = new ResultInfoList<Object>();
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();

                String url = UrlData.getUrl() + "/api/Default/GetWorkOrderList?pageNum=" + pageNum + "&status="+billingstatus+"&town="+town+"&key="+key;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse execute = httpClient.execute(httpGet);
                    if (execute.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = execute.getEntity();
                        String response = EntityUtils.toString(entity);   //将entity当中的数据转换为字符串
                        msg.what = 1;
                        msg.obj = response;
                        firstPageHandler.sendMessage(msg);
                    } else {
                        msg.what = 2;
                        firstPageHandler.sendMessage(msg);
                    }
                } catch (Exception ex) {
                    DialogUtil.closeDialog(mWeiboDialog);
                }
            }
        }.start();
        firstPageHandler.sendEmptyMessageDelayed(1, 100);
    }

    private void myOnclick() {
//      加载数据
        listView.setInterface(new LoadOrderList.ILoadOrderListener() {
            @Override
            public void onLoad() {
                initOrders();
                pageNum = pageNum + 1;
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //搜索框事件
    private void initClick() {
        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
                key="";
                orderList.clear();
                pageNum=1;
                initOrders();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContent=searchText.getText().toString();
                listView.footer.setVisibility(View.GONE);
                key=searchContent;
                orderList.clear();
                pageNum=1;
                initOrders();

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //通过view获取其内部的组件，进而进行操作
        try {
            Intent intent = new Intent();
                intent.putExtra("ID", (String) ((TextView) view.findViewById(R.id.orderId)).getText());
                intent.setClass(OrderListActivity.this, OrderDetailActivity.class);
                OrderListActivity.this.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "请稍等", Toast.LENGTH_LONG).show();
        }
    }
}