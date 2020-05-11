package Model;

public class Helper {
    private String name;
    private String identityid;
    private String helperId;
    private String mobile;
    private String address;
    private String town;
    private String village;
    private int imageId;

    public Helper(String name,String identityId,String helperId, String mobile,String address,String town,String village,int imageId){
        this.name=name;
        this.identityid=identityId;
        this.helperId=helperId;
        this.mobile=mobile;
        this.address=address;
        this.town=town;
        this.village=village;
        this.imageId=imageId;
    }

    public Helper(String name,String identityId, String mobile,String address,String town,String village,int imageId){
        this.name=name;
        this.identityid=identityId;
        this.mobile=mobile;
        this.address=address;
        this.town=town;
        this.village=village;
        this.imageId=imageId;
    }
    public Helper(String name, String mobile,String address,String town,String village,int imageId){
        this.name=name;
        this.mobile=mobile;
        this.address=address;
        this.town=town;
        this.village=village;
        this.imageId=imageId;
    }
    public Helper(String name,String helperId,int imageId,String town,String village,String mobile){
        this.name=name;
        this.helperId=helperId;
        this.mobile=null;
        this.address=null;
        this.town=town;
        this.village=village;
        this.mobile=mobile;
        this.imageId=imageId;
    }
    public Helper(String name,int imageId,String town){
        this.name=name;
        this.mobile=null;
        this.address=null;
        this.town=town;
        this.village=null;
        this.imageId=imageId;
    }
    public void setHelperName(String name) { this.name = name; }
    public void setHelperId(String helperId) { this.name =helperId; }
    public void setHelperMobile(String mobile) { this.mobile = mobile; }
    public void setHelperAddress(String address){this.address=address;}
    public void setHelperTown(String town) { this.town = town; }
    public void setHelperVillage(String village) { this.village = village; }
    public void setImage(int imageId) {
        this.imageId = imageId;
    }


    public String getHelperName() { return name; }
    public String getHelperId() { return helperId; }
    public String getHelperMobile() { return mobile;}
    public String getHelperAddress(){ return address;}
    public String getHelperTown() {
        return town;
    }
    public String getHelperVillage() { return village;}
    public int getImageId() {
        return imageId;
    }
}
