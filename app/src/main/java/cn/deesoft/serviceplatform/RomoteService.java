package cn.deesoft.serviceplatform;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class RomoteService extends Service {
    MyConn conn;
    MyBinder binder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        conn = new MyConn();
        binder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        show();
        Log.i("box", "远程服务活了");
       // Toast.makeText(this, " 远程服务活了", Toast.LENGTH_SHORT).show();
        this.bindService(new Intent(this, LocalService.class), conn, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    class MyBinder extends IServiceAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return RomoteService.class.getSimpleName();
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("box", "绑定本地服务成功");
             //Toast.makeText(RomoteService.this, "绑定本地服务成功", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("box", "本地服务被干掉了");
           //Toast.makeText(RomoteService.this, "本地服务挂了", Toast.LENGTH_SHORT).show();

                RomoteService.this.startService(new Intent(RomoteService.this, LocalService.class));

            //绑定本地服务
            RomoteService.this.bindService(new Intent(RomoteService.this, LocalService.class), conn, Context.BIND_IMPORTANT);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
            //开启本地服务
            RomoteService.this.startService(new Intent(RomoteService.this, LocalService.class));
        //绑定本地服务
        RomoteService.this.bindService(new Intent(RomoteService.this, LocalService.class), conn, Context.BIND_IMPORTANT);

    }

    private void show() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
               // Toast.makeText(RomoteService.this, "远程服务正常", Toast.LENGTH_SHORT).show();
                Log.i("box", "远程服务正常");
                super.handleMessage(msg);
            }
        };

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 10000, 10000);
    }
}
