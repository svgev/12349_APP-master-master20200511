package Util;

public class GetVillageList {
    public static String[] getVillageList(String town) {

        if (town.equals("武康街道")) {
            String[] villageList = {"永兴社区", "春晖社区", "祥和社区", "居仁社区","振兴社区","群安社区","吉祥社区","英溪社区","丰桥社区","五龙社区","新丰社区","对河口村","城西村","千秋村"};
            return villageList;
        }
        if (town.equals("舞阳街道")) {
            String[] villageList = {"舞阳社区", "塔山社区", "上柏社区", "宋村村","塔山村","上柏村","山民村","城山村","双燕村","长春村","龙凤村","下柏村","太平村","灯塔村"};
            return villageList;
        }
        if (town.equals("阜溪街道")) {
            String[] villageList = {"三桥社区", "五四村", "三桥村", "民进村","龙山村","龙胜村","王母山村","狮山村","秋北村","秋山村","郭肇村","兴山村"};
            return villageList;
        }
        if (town.equals("乾元镇")) {
            String[] villageList = {"北郊社区", "东郊社区", "西郊社区", "溪街社区","直街社区","城北村","联合村","金鹅山村","联星村","金火村","明星村","恒星村","卫星村","齐星村"};
            return villageList;
        }
        if (town.equals("新市镇")) {
            String[] villageList = {"仙潭社区", "西安社区", "东升社区", "南昌社区","水北村","梅林村","子思桥村","新塘村","丰年村","韶村村","白彪村","士林村","加元村","城西村","城东村","宋市村","栎林村","东安村","石泉村","厚皋村","谷门村","孟溪村","句城村","蔡界村","舍渭村","勇兴村","王公郎村","乐安村"};
            return villageList;
        }
        if (town.equals("禹越镇")) {
            String[] villageList = {"天皇殿村", "杨家坝村", "木桥头村", "高桥村","西港村","东港村","栖湖村","钱塘村","三林村","夏东村"};
            return villageList;
        }
        if (town.equals("洛舍镇")) {
            String[] villageList = {"三家村", "雁塘村", "东衡村", "洛舍村","洛舍砂村","洛舍社区","张陆湾村"};
            return villageList;
        }
        if (town.equals("新安镇")) {
            String[] villageList = {"勾里村", "城头村", "百富兜村", "孙家桥村","下舍村","舍东村","舍南村","舍西村","舍北村","新桥村","西庙桥村"};
            return villageList;
        }
        if (town.equals("莫干山镇")) {
            String[] villageList = {"劳岭村", "高峰村", "庙前村", "紫岭村","东沈村","瑶坞村","大造坞村","勤劳村","上皋坞村","筏头村","佛堂村","兰树坑村","燎原村","仙潭村","何村村","后坞村","四合村","南路村","筏头集镇居委会","莫干山集镇居委会"};
            return villageList;
        }
        if (town.equals("下渚湖街道")) {
            String[] villageList = {"二都村", "沿河村", "朱家村", "上杨村","下杨村","和睦村","四都村","宝塔山村","双桥村","八字桥村","康介山村","塘泾村","新琪村","新禺居委会","唐家琪村"};
            return villageList;
        }
        if (town.equals("雷甸镇")) {
            String[] villageList = {"新利村", "塘北村", "雷甸村", "下高桥村","东新村","杨墩村","和平村","光辉村","解放村","双溪村","水产村","中兴社区"};
            return villageList;
        }
        if (town.equals("钟管镇")) {
            String[] villageList = {"干村村", "钟管村", "南湖社区", "东千村","下塘村","新联村","塍头村","曲溪村","沈家墩村","戈亭村","东舍墩村","审塘村","三墩村","葛山村","茅山村","干山村","东坝斗村","北代舍村","青墩村","蠡山村"};
            return villageList;
        }else {
            String[] villageList = {"请选择"};
            return villageList;
        }
    }
}
