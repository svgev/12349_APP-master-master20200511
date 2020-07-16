package Util;

import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import Model.Helper;
import Model.ResultInfoList;
import cn.deesoft.serviceplatform.R;

public class MyConnection {
    //用于发送请求
//    public static String setMyConnection(String url) {
////        Message msg = new Message();
//        String response = "";
//        try {
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//            HttpResponse execute = httpClient.execute(httpGet);
//            if (execute.getStatusLine().getStatusCode() == 200) {
//                HttpEntity entity = execute.getEntity();
//                response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
////                msg.what = 1;
////                msg.obj = response;
////                handler.sendMessage(msg);
//            } else {
////                msg.what = 3;
////                handler.sendMessage(msg);
//                response = "远程获取失败";
//            }
//        } catch (Exception ex) {
////            DialogUtil.closeDialog(mWeiboDialog);
////            msg.what = 3;
////            handler.sendMessage(msg);
////            ex.printStackTrace();
//            response = "未连接到网络";
//        }
//        return response;
//    }


    public static String getMD5(String str) throws Exception {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
//        String md5Str = new BigInteger(1, digest).toString(16);
        String md5Str = byte2Hex(digest);
        return md5Str;

    }


    public static String setMyHttpClient(String url) {

        Message msg = new Message();
        String response = "";
        if (TokenData.tokenValue == null || TokenData.tokenValue.equals("")) {
            getToken();
            Log.e("Token未初始化", "需要重新请求token");
           response=MyConnection.setMyHttpClient(url);
        } else {
            try {
                String token = TokenData.getTokenValue();
                HttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader("auth", token);
                Log.e("当前auth", token);
                HttpResponse execute = httpClient.execute(httpGet);
                Header[] headers = execute.getAllHeaders();
                if (execute.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = execute.getEntity();
                    response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
                    Log.e("返回", response);
                } else {
                    if (execute.getStatusLine().getStatusCode() == 400) response = "请求错误";
                    if (execute.getStatusLine().getStatusCode() == 401) response = "未授权";
                    if (execute.getStatusLine().getStatusCode() == 403) response = "禁止访问";
                    if (execute.getStatusLine().getStatusCode() == 404) response = "文件未找到";
                    else response = "未知错误";
                }
                for (int i = 0; i < headers.length; i++) {
                    //如果token过期Headers里会有名为"token-expire"的Header 如果没有就排除token过期的可能
                    String name = headers[i].getName();
                    String value = headers[i].getValue();

                    Log.e("Header", name + ":" + value);
                    if (name.equals("token-expire") && value.equals("true")) {
                        response = "验证过期";
                        getToken();
                        Log.e("Token过期", "需要重新请求token");
                        response=MyConnection.setMyHttpClient(url);
//                    Log.e("返回","验证过期");
//                    getToken();//重新获取新的token
//                    Log.e("重新获取","已经重更获取同token");
////                    //重新发送请求
////                    HttpClient httpClient2 =  new DefaultHttpClient();
////                    httpClient2.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,1000);
////                    HttpGet httpGet2 = new HttpGet(url);
////                    httpGet2.addHeader("auth",TokenData.getTokenValue());
////                    HttpResponse execute2 = httpClient.execute(httpGet);
////                    if (execute2.getStatusLine().getStatusCode() == 200) {
////                        HttpEntity entity = execute.getEntity();
////                        response = EntityUtils.toString(entity);//将entity当中的数据转换为字符串
////                        Log.e("返回",response);
////                    }
////
////                    else {
////                        if (execute.getStatusLine().getStatusCode() == 400)response="请求错误";
////                        if (execute.getStatusLine().getStatusCode() == 401)response="未授权";
////                        if (execute.getStatusLine().getStatusCode() == 403)response="禁止访问";
////                        if (execute.getStatusLine().getStatusCode() == 404)response="文件未找到";
////                        else response="未知错误";
////                    }
////                    break;
                    }
                }
            } catch (Exception ex) {
                response = "未连接到网络";
                Log.e("发送请求失败", "MyConnection.setMyHttpClient");
            }
        }
        return response;

    }


    //从服务器获取token
    public static void getToken() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String timeNow = "";
        final String[] result = {""};
//        if (month >= 10) timeNow = year + "/" + month + "/" + day;
//        else timeNow = year + "/0" + month + "/" + day;
        if(month>=10) timeNow=year + "/" + month;
        else timeNow=year + "/0" + month;
        if(day>=10) timeNow=timeNow+"/" + day;
        else timeNow=timeNow+"/0" + day;
        final String txtMyToken = timeNow + "-Deesoft";
        Log.e("当前时间：", txtMyToken);

        try {
            //发送获取token的请求格式：yyyy/mm/dd+"-deesoft"  用md5加密
            String url = UrlData.getUrlYy()+"/api/AndroidApi/GetToken?secret=" + MyConnection.getMD5(txtMyToken);
            Log.e("发送获取token请求", url);
            Message msg = new Message();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse execute = httpClient.execute(httpGet);
            String result1 = EntityUtils.toString(execute.getEntity());
            JSONObject jsonObject = new JSONObject();
            JSONObject object = jsonObject.parseObject(result1);
            String a = object.getString("token");
            TokenData.setTokenValue(a);
            Log.e("已刷新token", "cxcxcxc");
        } catch (Exception e) {
            e.printStackTrace();
            TokenData.setTokenValue("");
            Log.e("Token获取失败", "在getToken中获取验证失败");

        }
    }

        private static String byte2Hex ( byte[] bytes){
            char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            int j = bytes.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : bytes) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        }
    }
