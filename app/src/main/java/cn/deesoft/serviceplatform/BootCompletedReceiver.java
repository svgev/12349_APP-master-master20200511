package cn.deesoft.serviceplatform;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class BootCompletedReceiver extends BroadcastReceiver {
    private Message message;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i("application", "Screen went OFF");
           // Toast.makeText(context, "screen OFF", Toast.LENGTH_LONG).show();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i("application", "Screen went ON");
           // Toast.makeText(context, "screen ON", Toast.LENGTH_LONG).show();

        }

        else if (intent.getAction().equals("sleep")) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //android8.0以上通过startForegroundService启动service
                    context.startForegroundService(new Intent(context, LocalService.class));
                }else{
                    context.startService(new Intent(context, LocalService.class)); }
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //example:启动程序
            Intent start = new Intent(context, MainActivity.class);
            start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
            context.startActivity(start);
        }
        else if(intent.getAction().equals("location"))
        {
            message.getMsg(intent.getStringExtra("msg"));
        }
    }
    interface Message {
        void getMsg(String str);
     }

     public void setMessage(Message message)
     {
        this.message = message;
    }


}
