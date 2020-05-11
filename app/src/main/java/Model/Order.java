
package Model;
public class Order {
    private String orderId;
    private int imageId;
    private String workerName;
    private String olderName;
    private String state;
    private String orderDate1;
    private String orderDate;

    public Order(String orderId,String workerName,String olderName,int imageId,String state,String orderDate){
        this.orderId=orderId;
        this.imageId=imageId;
        this.olderName=olderName;
        this.workerName=workerName;
        this.state=state;
        this.orderDate1=orderDate;
        this.orderDate=this.orderDate1.replace("T"," ");
    }

    public Order(String workerName,String olderName,int imageId,String state,String orderDate){
        this.imageId=imageId;
        this.olderName=olderName;
        this.workerName=workerName;
        this.state=state;
        this.orderDate1=orderDate;
        this.orderDate=this.orderDate1.replace("T"," ");
    }

    public int getImageId(){
        return imageId;
    }
    public String getWorkerName(){
        return workerName;
    }
    public String getClientName(){
        return olderName;
    }

    public String getState(){
        return state;
    }
    public String getOrderDate(){
        return orderDate;
    }
    public String getOrderId(){
        return orderId;
    }
}
