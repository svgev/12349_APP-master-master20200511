package cn.deesoft.serviceplatform;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import Util.ServiceUtil;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if(!ServiceUtil.isServiceRunning(context,"cn.deesoft.serviceplatform.LocalService"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android8.0以上通过startForegroundService启动service
                context.startForegroundService(new Intent(context, LocalService.class));
            } else {
                context.startService(new Intent(context, LocalService.class));
            }
        }
    }
}
