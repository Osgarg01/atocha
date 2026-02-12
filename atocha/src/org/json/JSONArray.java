package org.json;

public class JSONArray {
protected Object[] myArrayList;
public JSONArray() {
    myArrayList = new Object[0];
}
public JSONArray(Object[] array) {
    myArrayList = array;
}
public void put(Object value){
    Object[] newArray=new Object[myArrayList.length+1]; 
    for(Object o:myArrayList){
        newArray[newArray.length-1]=o;
    }
    newArray[newArray.length-1]=value;  
    myArrayList=newArray;   
}
public Object get(int index){
    if(index<0||index>=myArrayList.length)
        throw new IndexOutOfBoundsException( "Index: " + index + ", Size: " + myArrayList.length);
    return myArrayList[index];
}
public int length(){
    return myArrayList.length;
}

public String toString(){
    StringBuilder sb=new StringBuilder();
    sb.append("[");
    for(int i=0;i<myArrayList.length;i++){
        if(i>0)
            sb.append(",");
        sb.append(myArrayList[i]);
    }
    sb.append("]");
    return sb.toString();
}

public int getInt(int index) {
    Object o = get(index);
    if (o.equals(o.toString())) {
        return ((Number) o).intValue();
    }
    throw new JSONException("JSONArray[" + index + "] is not a number.");   
}

}
