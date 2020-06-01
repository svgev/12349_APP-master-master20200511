package Model;

public class  Older {
    private String olderName;
    private int imageId;
    private int ageNum;
    private String identityId;
    private String ID;
    private Boolean isLiving;
    private String village;
    private int remainTime=240;

    public Older(String name, int imageId,int old,String identityId){
        this.imageId=imageId;
        this.olderName=name;
        this.ageNum=old;
        this.identityId=identityId;
    }

    public Older(String name, int imageId,int old,String identityId,String id,String village){
        this.imageId=imageId;
        this.olderName=name;
        this.ageNum=old;
        this.identityId=identityId;
        this.ID=id;
        this.village=village;
        this.isLiving=true;
    }
//
//    public Older(String name, int imageId,int old,String identityId,String id,Boolean isLiving) {
//        this.imageId=imageId;
//        this.olderName=name;
//        this.ageNum=old;
//        this.identityId=identityId;
//        this.isLiving=true;
//        this.ID=id;
//        this.isLiving=isLiving;
//
//    }



    public String getOlderName(){
        return olderName;
    }
    public String getIdentityId() {
        return identityId;
    }
    public String getOlderAge(){
        String age=String.valueOf(ageNum)+"岁";
        return age;
    }
    public int getImageId() {
        return imageId;
    }
    public String getID(){
        return ID;
    }
    public String getVillage(){
        return village;
    }
    public Boolean getIsLiving(){
        return isLiving;
    }
    public int getRemainTime(){
        return remainTime;
    }


    public void setName(String name) {
        this.olderName = name;
    }
    public void setAge(int age) {
        this.ageNum = age;
    }
    public void setImage(int imageId) {
        this.imageId = imageId;
    }
    public void setIdentityId(String identityId){this.identityId=identityId;}
    public void setID(String id){this.ID=id;}
    public void setIsLiving(Boolean isLiving){this.isLiving=isLiving;}
    public void setRemainTime(int remainTime){
        this.remainTime=remainTime;
    }
}
