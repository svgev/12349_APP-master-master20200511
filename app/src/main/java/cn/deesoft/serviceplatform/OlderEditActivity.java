package cn.deesoft.serviceplatform;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.DialogUtil;
import Util.MyConnection;
import Util.UrlData;

import static Util.GetVillageList.getVillageList;

public class OlderEditActivity extends AppCompatActivity {
    private String currentArea;

    private String olderName;
    private String olderAge;
    private String sex;
    private String olderIdentityId;
    private String olderMobile;
    private String olderTown;
    private String olderVillage;
    private String olderAddress;
    private String contactNumber;
    private String olderContactName;
    private String contactRelationship;
//    private String olderLongiitude;
//    private String olderLatitude;
    private String diseaseHistory;
    private String ID;
    private String isDisability;
    private String isPoor;
    private String isLonely;
    private String isOldAge;
    private String isEnable;
    private String isEmpty;
    private String isLiving;
    //默认老人状态
    private int disability=0;//非残疾
    private int poor=0;//非贫困
    private int lonely=0;
    private int oldAge=0;
    private int empty=0;
    private int living=1;//在世
    private int enable=1;//享受服务

    private ArrayAdapter<String> spLivingAdapter;
    private ArrayAdapter<String> spEnableAdapter;
    private List<String> spLivingList = new ArrayList<String>();
    private List<String> spEnableList = new ArrayList<String>();
    private String[] villageList;
    public Date date;
    private Boolean infoChecked;

    private Spinner spLiving;
    private Spinner spEnable;
    private EditText edtOlderName;
    private TextView txtSex;
    private EditText edtIdentityId;
    private EditText edtOlderMobile;
    private TextView txtTown;
    private TextView txtVillage;
    private EditText edtAddr;
    private EditText edtContactName;
    private TextView txtContactRelationship;
    private EditText edtContactNumber;
    private EditText edtDiseaseHistory;

    private CheckBox cbIsOldAge;
    private CheckBox cbIsDisability;
    private CheckBox cbIsPoor;
    private CheckBox cbIsLonely;
    private CheckBox cbIsEmpty;

    private TextView checkId;
    private TextView txtPhoneNumberChecked;
    private ImageView imgPhoto;
    private Button btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_older_edit);

        Intent intent = getIntent();
        currentArea=intent.getStringExtra("CurrentArea");

        //获取老人信息
        ID=intent.getStringExtra("ID");
        olderName=intent.getStringExtra("Name");
        sex=intent.getStringExtra("Sex");
        olderIdentityId=intent.getStringExtra("IdentityId");
        olderMobile=intent.getStringExtra("PhoneNumber");
        olderTown=intent.getStringExtra("Town");
        olderVillage=intent.getStringExtra("Village");
        olderAddress=intent.getStringExtra("Addr");
        contactNumber=intent.getStringExtra("ContactNumber");
        olderContactName=intent.getStringExtra("Contact");
        contactRelationship=intent.getStringExtra("ContactRelationship");
        diseaseHistory=intent.getStringExtra("DiseaseHistory");

        isDisability=intent.getStringExtra("IsDisability");
        isPoor=intent.getStringExtra("IsPoor");
        isLonely=intent.getStringExtra("IsLonely");
        isOldAge=intent.getStringExtra("IsOldAge");
        isEnable=intent.getStringExtra("IsEnable");
        isEmpty=intent.getStringExtra("IsEmpty");
        isLiving=intent.getStringExtra("IsLiving");

        spEnableList.add("享受服务");
        spEnableList.add("放弃服务");
        spLivingList.add("在世");
        spLivingList.add("过世");

        //控件初始化
        spEnable=findViewById(R.id.spEnable);
        spLiving=findViewById(R.id.spLiving);
        spLivingAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spLivingList);
        spLivingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLiving.setAdapter(spLivingAdapter);
        spEnableAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spEnableList);
        spEnableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEnable.setAdapter(spEnableAdapter);
        edtOlderName=findViewById(R.id.edtName);
        txtSex=findViewById(R.id.txtSex);
        edtIdentityId=findViewById(R.id.edtIdentityId);
        edtOlderMobile=findViewById(R.id.edtPhoneNumber);
        txtTown=findViewById(R.id.edtTown);
        txtVillage=findViewById(R.id.edtVillage);
        edtAddr=findViewById(R.id.edtAddr);
        edtContactName=findViewById(R.id.edtContact);
        edtContactNumber=findViewById(R.id.edtContactNumber);
        edtDiseaseHistory=findViewById(R.id.edtDiseaseHistory);
        txtContactRelationship=findViewById(R.id.txtRelationship);

        cbIsOldAge=findViewById(R.id.cbIsOldAge);
        cbIsOldAge.setClickable(false);
        cbIsDisability=findViewById(R.id.cbIsDisability);
        cbIsPoor=findViewById(R.id.cbIsPoor);
        cbIsLonely=findViewById(R.id.cbIsLonely);
        cbIsEmpty=findViewById(R.id.cbIsEmpty);

        checkId=findViewById(R.id.checkId);
        txtPhoneNumberChecked=findViewById(R.id.phoneNumberChecked);
        btnSubmit=findViewById(R.id.btnSubmit);

        imgPhoto = findViewById(R.id.imgPhoto);
        Glide.with(OlderEditActivity.this).load(R.mipmap.nophoto2).into(imgPhoto);

        //初始化乡镇控件
//        String selectedTown = txtTown.getText().toString();
        String selectedTown = olderTown;
        villageList = getVillageList(selectedTown);



        //初始化控件事件
        txtSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OlderEditActivity.this);
                builder.setCancelable(true);
                builder.setNegativeButton("关闭",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setTitle("性别");
                final String[] sexList={"男","女"};
                builder.setItems(sexList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtSex.setText(sexList[which]);
                    }
                });
                builder.show();
            }
        });

        //检查身份证
        edtIdentityId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String changedId=edtIdentityId.getText().toString();
                if (getIdChecked(changedId)) {
                    checkId.setVisibility(View.INVISIBLE);
                    infoChecked=true;
                    Toast.makeText(OlderEditActivity.this, date.toString(), Toast.LENGTH_SHORT).show();

                }else {
                    infoChecked=false;
                    checkId.setVisibility(View.VISIBLE);
                }
            }
        });

        edtOlderMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String changedPhoneNumber=edtOlderMobile.getText().toString();
                if (!changedPhoneNumber.equals("")) {
                    txtPhoneNumberChecked.setVisibility(View.INVISIBLE);
                    infoChecked=true;
                }
                if(changedPhoneNumber.indexOf(" ")> -1){
                    txtPhoneNumberChecked.setVisibility(View.VISIBLE);
                    txtPhoneNumberChecked.setText("手机号不能有空格");
                    infoChecked=false;
                }
                if (changedPhoneNumber.equals("")){
                    txtPhoneNumberChecked.setVisibility(View.INVISIBLE);
                    infoChecked=true;
                }
            }
        });

        txtTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(OlderEditActivity.this);
                builder.setCancelable(true);
                builder.setNegativeButton("关闭",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setTitle("选择乡镇");
                //    指定下拉列表的显示数据
                final String[] townList = {"武康街道", "阜溪街道", "舞阳街道", "乾元镇", "新市镇", "禹越镇", "洛舍镇", "新安镇", "莫干山镇", "下渚湖街道", "雷甸镇", "钟管镇"};
                //    设置一个下拉的列表选择项
                builder.setItems(townList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtTown.setText(townList[which]);
                        if (townList[which] != olderTown) {
                            txtVillage.setText("请选择");
                            villageList = getVillageList(townList[which]);
                        }

                    }
                });
                builder.show();
            }
        });

        txtVillage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(OlderEditActivity.this);
                builder.setCancelable(true);
                builder.setNegativeButton("关闭",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setTitle("选择街道");
                //    指定下拉列表的显示数据

                //    设置一个下拉的列表选择项
                builder.setItems(villageList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtVillage.setText(villageList[which]);

                    }
                });
                builder.show();
            }
        });

        txtContactRelationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OlderEditActivity.this);
                builder.setCancelable(true);
                builder.setNegativeButton("关闭",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setTitle("家属关系");
                final String[] relationshipList={"子女","夫妻","父母","兄弟姐妹","其他"};
                builder.setItems(relationshipList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtContactRelationship.setText(relationshipList[which]);
                    }
                });
                builder.show();
            }
        });

        if(isLiving.equals("false")){
            spLiving.setSelection(1);
            living=0;
        }
        if(isEnable.equals("false")){
            spEnable.setSelection(1);
            enable=0;
        }
        if(isDisability.equals("true")){
            cbIsDisability.setChecked(true);
            disability=1;
        }
        if(isPoor.equals("true")){
            cbIsPoor.setChecked(true);
            poor=1;
        }
        if(isLonely.equals("true")){
            cbIsLonely.setChecked(true);
            lonely=1;
        }
        if(isOldAge.equals("true")){
            cbIsOldAge.setChecked(true);
            oldAge=1;
        }
        if(isEmpty.equals("true")){
            cbIsEmpty.setChecked(true);
            empty=1;
        }
        edtOlderName.setText(olderName);
        txtSex.setText(sex);
        edtOlderMobile.setText(olderMobile);
        edtIdentityId.setText(olderIdentityId);
        txtTown.setText(olderTown);
        txtVillage.setText(olderVillage);
        edtAddr.setText(olderAddress);
        edtContactName.setText(olderContactName);
        txtContactRelationship.setText(contactRelationship);
        edtContactNumber.setText(contactNumber);
        edtDiseaseHistory.setText(diseaseHistory);

        //提交修改事件
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //根据选中的标签重置老人状态
                if(spLiving.getSelectedItemPosition()==1){
                    living=0;
                }else{
                    living=1;
                }
                if(spEnable.getSelectedItemPosition()==1){
                    enable=0;
                }
                else{
                    enable=1;
                }
                if(cbIsDisability.isChecked()){
                    disability=1;
                }
                else{
                    disability=0;
                }
                if(cbIsEmpty.isChecked()){
                    empty=1;
                }
                else{
                    empty=0;
                }
                if(cbIsLonely.isChecked()){
                    lonely=1;
                }
                else{
                    lonely=0;
                }
                if(cbIsOldAge.isChecked()){
                    oldAge=1;
                }
                else{
                    oldAge=0;
                }
                if(cbIsPoor.isChecked()){
                    poor=1;
                }
                else{
                    poor=0;
                }
                olderName=edtOlderName.getText().toString();
                sex=txtSex.getText().toString();
                olderIdentityId=edtIdentityId.getText().toString();
                olderMobile=edtOlderMobile.getText().toString();
                olderTown=txtTown.getText().toString();
                olderVillage=txtVillage.getText().toString();
                olderAddress=edtAddr.getText().toString();
                olderContactName=edtContactName.getText().toString();
                contactRelationship=txtContactRelationship.getText().toString();
                contactNumber=edtContactNumber.getText().toString();
                diseaseHistory=edtDiseaseHistory.getText().toString();
                SubmitInfo();
            }
        });
    }

//    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) {
//            switch (which) {
//                case android.app.AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            String url = UrlData.getUrl()+"/api/AndroidApi/EditOlderInfo?id=17287&identityId=330521194505160024&trueName=%E6%B5%8B%E8%AF%95%E4%B8%80&sex=%E5%A5%B3&mobile=17826833512&town=%E6%AD%A6%E5%BA%B7%E8%A1%97%E9%81%93&village=%E6%98%A5%E6%99%96%E7%A4%BE%E5%8C%BA&addr=%E7%A4%BE%E5%8C%BA&contactName=%E5%BC%A0&contactNumber=&contactContactRelationship=&diseaseHistory=&isliving=1&isDisability=0&isPoor=1&isEmpty=0&isOldAge=0&isEnable=1&isLonely=0";
//                            Message msg = new Message();
//                            String response;
//                            try{
//                                response= MyConnection.setMyHttpClient(url);
//                                if (response!=null) {
//                                    if(response.equals("请求错误")||response.equals("未授权")||response.equals("禁止访问")||response.equals("文件未找到")||response.equals("未知错误")||response.equals("未连接到网络")) {
//                                        msg.what = 2;
//                                        msg.obj = response;//返回错误原因
//                                    }
//                                    if(response.equals("验证过期")){
//                                        //执行token过期的操作
//                                        msg.what=4;
//                                        msg.obj=response;
//                                        Log.e("验证失败",response);
//                                    }
//                                    else {
//                                        msg.what = 1;
//                                        msg.obj = response;//返回正常数据
//                                    }
//                                }else {
//                                    msg.what = 3;
//                                }
//                            }
//                            catch (Exception e) {
//                                msg.what = 3;
//                            }
//                            handler.sendMessage(msg);
//                        }
//                    }.start();
////                        Intent intent=new Intent();
////                        Bundle bundle=new Bundle();
////                        bundle.putString("Longitude",longitude);
////                        bundle.putString("Latitude",latitude);
////                        intent.putExtras(bundle);
////                        setResult(1,intent);
////                        finish();
//                    break;
//                case android.app.AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    Handler handler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            Model.ResultInfo<LinkedHashMap> list=new Model.ResultInfo<LinkedHashMap>();
            if(msg.what==1)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node=mapper.readTree(msg.obj.toString());
                    list= mapper.readValue(node.toString(),list.getClass());
                    if(list.Success==true)
                    {
                        //返回老人信息页面
                        Toast.makeText(OlderEditActivity.this, list.Msg, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(OlderEditActivity.this, OlderDetailActivity.class);
                        intent.putExtra("TrueName",olderName);
                        intent.putExtra("ID",ID);
                        intent.putExtra("CurrentArea",currentArea);
                        OlderEditActivity.this.startActivity(intent);
                        OlderEditActivity.this.finish();
                    }
                    else {
                        Toast.makeText(OlderEditActivity.this, "验证错误", Toast.LENGTH_LONG).show();
                    }

                }
                catch(Exception ex)
                {
                    Toast.makeText(OlderEditActivity.this,"出现未知异常，请联系管理员!",Toast.LENGTH_LONG).show();
                }
            }
            if(msg.what==2)
            {
                Toast.makeText(OlderEditActivity.this,msg.obj.toString(),Toast.LENGTH_LONG).show();
            }
            if(msg.what==3)
            {
                Toast.makeText(OlderEditActivity.this,"未连接到网络",Toast.LENGTH_LONG).show();
            }
            if(msg.what==4)
            {
                Toast.makeText(OlderEditActivity.this,"验证过期",Toast.LENGTH_LONG).show();
            }
        }
    };

    //显示确认提交对话框
//    private void showMyDialog() {
//        // 创建退出对话框
//        android.app.AlertDialog.Builder isExit = new android.app.AlertDialog.Builder(this);
//        // 设置对话框标题
//        //isExit.setTitle("提示");
//        // 设置对话框消息
//        isExit.setMessage("确定要使用当前位置吗？");
//        // 添加选择按钮并注册监听
//        isExit.setPositiveButton("确定", listener);
//        isExit.setNegativeButton("取消", listener);
//        // 显示对话框
//        isExit.show();
//    }
//
    private Boolean getIdChecked(String changedId){
        String id=changedId;
        String Ai = "";
        if (id.length() == 18) {
            Ai = id.substring(0, 17);
            if(isDate(Ai)){
                int year = Integer.parseInt(Ai.substring(6, 10));
                Calendar calendar = Calendar.getInstance();
                int yearNow = calendar.get(Calendar.YEAR);
                if((yearNow-year)>90){
                    cbIsOldAge.setChecked(true);
                    oldAge=1;//因为服务器会重新判断是否高龄 所以oldAge不用传给服务器
                }else{
                    cbIsOldAge.setChecked(false);
                    oldAge=0;
                }
                return true;
            }else{
                return false;
            }
        } else if (id.length() == 15) {
            Ai = id.substring(0, 6) + "19" +id.substring(6, 15);
            if(isDate(Ai)){
                int year = Integer.parseInt(Ai.substring(6, 10));
                Calendar calendar = Calendar.getInstance();
                int yearNow = calendar.get(Calendar.YEAR);
                if((yearNow-year)>90){
                    cbIsOldAge.setChecked(true);
                    oldAge=1;//因为服务器会重新判断是否高龄 所以oldAge不用传给服务器
                }else{
                    cbIsOldAge.setChecked(false);
                    oldAge=0;
                }
                return true;
            }else{
                return false;
            }
        }

        //String strYear = Ai.substring(6, 10);// 年份
        //String strMonth = Ai.substring(10, 12);// 月份
        //String strDay = Ai.substring(12, 14);// 月份
        //if(isDate(strYear + "-" + strMonth + "-" + strDay) == false){
        //return  false;
        //}

        if(id.length() != 15 && id.length() != 18){
            return false;
        }
        else {
            return true;
        }


    }
    private void SubmitInfo(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                String url= UrlData.getUrl()+"/api/AndroidApi/EditOlderInfo?id="+ID+"&identityId="+olderIdentityId+"&trueName="+olderName+"&sex="+sex+"&mobile="+olderMobile+"&town="+olderTown+"&village="+olderVillage+"&addr="+olderAddress+"&contactName="+olderContactName+"&contactNumber="+contactNumber+"&contactContactRelationship="+contactRelationship+"&diseaseHistory="+diseaseHistory+"&isliving="+living+"&isDisability="+disability+"&isPoor="+poor+"&isEmpty="+empty+"&isEnable="+enable+"&isLonely="+lonely;
                Log.e("修改请求", url);
                String response="";
                Message msg = new Message();
                try{
                    response= MyConnection.setMyHttpClient(url);
                    if (response!=null) {
                        if(response.equals("请求错误")||response.equals("未授权")||response.equals("禁止访问")||response.equals("文件未找到")||response.equals("未知错误")||response.equals("未连接到网络")) {
                            msg.what = 2;
                            msg.obj = response;//返回错误原因
                            Log.e("GetHelperInfo",response);
                        }
                        if(response.equals("验证过期")){
                            //执行token过期的操作
                            msg.what=4;
                            msg.obj=response;
                            Log.e("GetHelperInfo",response);
                        }
                        else {
                            msg.what = 1;
                            msg.obj = response;//返回正常数据
                        }
                    }else {
                        msg.what = 3;
                        Log.e("GetHelperInfo","未连接网络");
                    }
                }
                catch (Exception e) {
                    msg.what = 3;
                    Log.e("GetHelperInfo","未连接网络");
                }
                handler.sendMessage(msg);
            }
        }).start();
    }


    private  boolean isDate(String Ai) {

        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        String time=strYear+"-"+strMonth+"-"+strDay;
        int intMonth= Integer.parseInt(strMonth);
        int intDay=Integer.parseInt(strDay);
        int intYear=Integer.parseInt(strYear);
        if(intMonth>12){
            return false;
        }

        if (isNumeric(Ai)==false){
            return false;
        }
        else {
            try{
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                this.date = format.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                if(day!=intDay){
                    return false;
                }
                else{
                    return true;
                }

            }catch (Exception e){
                return false;
            }
        }
    }
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
