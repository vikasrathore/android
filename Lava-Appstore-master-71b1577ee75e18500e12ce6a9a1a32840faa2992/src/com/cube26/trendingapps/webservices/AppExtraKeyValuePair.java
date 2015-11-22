package com.cube26.trendingapps.webservices;

import java.io.Serializable;

public class AppExtraKeyValuePair implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private String keyString;
    private String valueString;
   
    public String getKeyString() {
        return keyString;
    }
    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }
    public String getValueString() {
        return valueString;
    }
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
}
