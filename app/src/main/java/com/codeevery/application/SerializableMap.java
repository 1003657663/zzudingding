package com.codeevery.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by songchao on 15/8/20.
 */
public class SerializableMap implements Serializable {

    private Map<String,String> map;
    public Map<String,String> getMap(){
        return map;
    }
    public void setMap(Map<String,String> map){
        this.map = map;
    }

}
