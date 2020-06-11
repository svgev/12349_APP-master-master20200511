package Util;

import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class MyConnection {
    //用于发送请求
    public static String setMyConnection(String url) {
//        Message msg = new Message();
        String response="";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse execute = httpClient.execute(httpGet);
            if (execute.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = execute.getEntity();
                response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
//                msg.what = 1;
//                msg.obj = response;
//                handler.sendMessage(msg);
            } else {
//                msg.what = 3;
//                handler.sendMessage(msg);
                response="远程获取失败";
            }
        } catch (Exception ex) {
//            DialogUtil.closeDialog(mWeiboDialog);
//            msg.what = 3;
//            handler.sendMessage(msg);
//            ex.printStackTrace();
            response="未连接到网络";
        }
        return response;
    }
}
