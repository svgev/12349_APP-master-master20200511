package cn.deesoft.serviceplatform;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jzxiang.pickerview.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import Util.ServiceUtil;
import Util.UrlData;

import static com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy;


/**
 *
 */
public class LocalService extends Service {
    MyBinder binder;
    MyConn conn;
    //声明mLocationOption对象，定位参数
    public AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    static double latitude;
    static double longitude;
    static double speed;
    static int locationType;
    static  double accuracy;
    private Intent intent;
    static String aoiName;
    static String phoneNumber="";
    static String identityID="";
    private Timer mTimer;
    static String positionTime="10";
    private SharedPreferences sp;
    private ServiceUtil serviceUtil;

    //用于测试线程
    public int timesOfthread;
    public int timesOfCreate;
    public int timeOfLocationChange;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent=new Intent();
        timesOfthread=1;
        timesOfCreate=1;
        timeOfLocationChange=1;
        UrlData.setLocationServiceStarted(true);

        Log.e("onCreate","服务创建次数:"+timesOfCreate);
        timesOfCreate++;
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        phoneNumber=sp.getString("phoneNumber", "");
        identityID=sp.getString("identityId","");
        serviceUtil=new ServiceUtil();
        binder = new MyBinder();
        conn = new MyConn();
        //初始化定位
        init();
        //设置前台服务
        setNotification();


    }

    class MyBinder extends IServiceAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return LocalService.class.getSimpleName();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //show();
//
//        if(!serviceUtil.isOPen(this))
//        {
//            serviceUtil.openGPS(this);
//        }

        if(mTimer!=null){

        }else {
            Log.e("onStartCommand", "服务启动");
            mTimer = new Timer();
            TimerTask positionTask = new TimerTask() {
                @Override
                public void run() {

                    init();
                    mLocationClient.startLocation();

                }
            };
            mTimer.scheduleAtFixedRate(positionTask, 0, 60 * 1000);
        }
        //Toast.makeText(LocalService.this, " 本地服务活了", Toast.LENGTH_SHORT).show();

        this.bindService(new Intent(LocalService.this, RomoteService.class), conn, Context.BIND_IMPORTANT);
        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(new Intent(LocalService.this, RomoteService.class));
        }
        else
        {
            this.startService(new Intent(LocalService.this, RomoteService.class));
        }

        AlarmManager systemService = (AlarmManager) getSystemService(ALARM_SERVICE);
        long anHour = 60 * 1000;//这是一个小数的毫秒数
        long l = SystemClock.elapsedRealtime() + anHour;
        Intent intentBroad = new Intent(this, BootCompletedReceiver.class);
        intentBroad.setClassName(getPackageName(),getPackageName()+".AlarmReceiver");
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, intentBroad, 0);
        systemService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,l,broadcast);
        return START_STICKY;

    }




    class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("box", "绑定上了远程服务");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("box", "远程服务被干掉了");
            //Toast.makeText(LocalService.this, "远程服务挂了", Toast.LENGTH_SHORT).show();
            //开启远程服务
            LocalService.this.startService(new Intent(LocalService.this, RomoteService.class));
            //绑定远程服务
            LocalService.this.bindService(new Intent(LocalService.this, RomoteService.class), conn, Context.BIND_IMPORTANT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UrlData.setLocationServiceStarted(false);
        Log.e("onDestory","服务销毁");
        NotificationManager mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mManager.cancel(1);
        Toast.makeText(this,"service被杀死",Toast.LENGTH_LONG).show();
        //开启远程服务
        LocalService.this.startService(new Intent(LocalService.this, RomoteService.class));
        //绑定远程服务
        LocalService.this.bindService(new Intent(LocalService.this, RomoteService.class), conn, Context.BIND_IMPORTANT);
        if(mLocationClient!=null){
            mLocationClient.stopLocation();
            destroyLocation();
        }

        if(null!=mTimer){
            mTimer.cancel();
        }
    }


//    private void show() {
//        final Handler handler1 = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//             //  Toast.makeText(LocalService.this, "本地服务正常", Toast.LENGTH_SHORT).show();
//                super.handleMessage(msg);
//            }
//        };
//
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                message.what = 1;
//                handler1.sendMessage(message);
//            }
//        };
//        timer.schedule(task, 10000, 10000);
//    }


    private void init() {
        AMapLocationClient.setApiKey("f46f89c09eec35b6e020e491a91d162a");
        //初始化定位

        Log.e("init()","初始化定位");
        if(mLocationClient!=null){}else{
        mLocationClient = new AMapLocationClient(this);
        initLocationOption();
        mLocationClient.setLocationOption(mLocationOption);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);}
    }
    /**
     * 初始化定位参数
     */
    private void initLocationOption() {

        Log.e("initLocationOption()","初始化定位参数");
        if (null == mLocationOption) {
            mLocationOption = new AMapLocationClientOption();
        }
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //定位精度:高精度模式
        mLocationOption.setLocationMode(Hight_Accuracy);
        //设置定位缓存策略
        mLocationOption.setLocationCacheEnable(true);
        //gps定位优先
        mLocationOption.setGpsFirst(true);
        //设置定位间隔
        //mLocationOption.setInterval(3000);
        mLocationOption.setHttpTimeOut(60000);
        mLocationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
        mLocationOption.setWifiScan(true);//主动刷新设备wifi模块，获取到最新的wifi列表
        mLocationOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mLocationOption.setOnceLocationLatest(true);//true表示获取最近3s内精度最高的一次定位结果；false表示使用默认的连续定位策略。
        //AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        if(null != mLocationClient){
            mLocationClient.setLocationOption(mLocationOption);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }


    }
    private void destroyLocation(){
        if (null != mLocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            Log.e("destroyLocation()","销毁定位");
            mLocationClient.onDestroy();
//            mLocationClient = null;
            mLocationClient = null;
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @android.annotation.TargetApi(Build.VERSION_CODES.N)
        @Override
        public void onLocationChanged(AMapLocation aMapLocation){
            //Toast.makeText(LocalService.this, "数据变化", Toast.LENGTH_SHORT).show();
            Log.e("onLocationChanged()","定位数据变化"+timeOfLocationChange);
            timeOfLocationChange++;
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    LocalService.longitude = aMapLocation.getLongitude();
                    LocalService.latitude = aMapLocation.getLatitude();
                    LocalService.speed = aMapLocation.getSpeed();
                    LocalService.locationType=aMapLocation.getLocationType();
                    LocalService.accuracy=aMapLocation.getAccuracy();
                    LocalService.aoiName=aMapLocation.getAoiName();
                    intent.setAction("location");
                    intent.putExtra("msg",LocalService.latitude+","+LocalService.longitude);
                    sendBroadcast(intent);
                    mLocationClient.stopLocation();
                   // Toast.makeText(LocalService.this,"有数据变化",Toast.LENGTH_LONG).show();
                    Log.e("mapError","定位数据有变化");
                    //获取定位时间
//                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date date = new Date(aMapLocation.getTime());
//                    df.format(date);
                    new Thread() {
                                @Override
                                public void run() {
                                    Log.e("Thread","发送定位线程创建个数："+timesOfthread);
                            timesOfthread++;
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String content = "\n发送时间:" + df.format(new Date()).toString() + "\n经度：" + longitude;
                            content += "       纬度：" + latitude+"\n 定位方式:"+locationType+"    定位精度:"+accuracy;
                            String url = (UrlData.getUrlYy()+"/api/Default/SaveInfo?phoneNumber=" + phoneNumber +"&identityID="+identityID+
                                    "&latitude=" + latitude + "&longitude=" + longitude + "&speed=" + speed +
                                    "&positionType="+locationType+"&aoiName="+aoiName+"&accuracy="+accuracy);
                            try {
                                org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();

                                HttpGet httpGet = new HttpGet(url);
                                HttpResponse execute = httpClient.execute(httpGet);
                            } catch (Exception e) {
                                Looper.prepare();
                                Toast.makeText(LocalService.this, "未开启流量或者wifi，请打开其中一个", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                e.printStackTrace();
                            }

                        }
                    }.start();
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Toast.makeText(LocalService.this, "定位失败", Toast.LENGTH_SHORT).show();
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
                mLocationClient.stopLocation();
                destroyLocation();
            }
        }
    };


    /**
     * 设置前台服务
     */
    private void setNotification()
    {
        Log.e("setNotification","设置前台服务");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel("1", "前台服务", NotificationManager.IMPORTANCE_HIGH);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
                Intent intentForeService = new Intent(this, MenuActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentForeService, 0);
                Notification notification = new NotificationCompat.Builder(this, "1")
                        .setContentTitle("12349")
                        .setContentText("定在后台定位")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(pendingIntent)
                        .build();
                startForeground(1, notification);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            Intent intentForeService = new Intent(this, MenuActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentForeService, 0);
            Notification notification = new NotificationCompat.Builder(this, "4.0的前台服务")
                    .setContentTitle("12349")
                    .setContentText("定在后台定位")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        }
    }
}


