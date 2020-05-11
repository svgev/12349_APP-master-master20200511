package Model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;

public class ResultInfoList<T> implements Serializable {
    public String Msg;
    public boolean Success;
    public  T[] Data;

}
