package Util;


public class UrlData {
    //服务器地址 http://12349.51youyou.cn  http://180.153.43.7
    //本地  192.168.1.93
    private static String myIp="http://12349.51youyou.cn";

    private static Boolean locationServiceStarted=false;
    private static String urlData ="http://12349.51youyou.cn";
    private static String localip="http://12349.51youyou.cn";
    public static String getUrl() {
        return localip;
    }
    public static String getUrlYy() { return localip; }
    public static void setUrlData(String a){
        UrlData.urlData=a;
    }

    public static  Boolean getLocationServiceStarted(){ return locationServiceStarted; }
    public static void setLocationServiceStarted(Boolean isStarted){UrlData.locationServiceStarted=isStarted;}
}





