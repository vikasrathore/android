package com.cube26.trendingapps.webservices;

import java.io.Serializable;
import java.util.ArrayList;

public class ActivationDTO implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private String packageName;
    private String activationTime;
    private String activityName;
    private ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs;
    
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getActivationTime() {
        return activationTime;
    }
    public void setActivationTime(String activationTime) {
        this.activationTime = activationTime;
    }
    public String getActivityName() {
        return activityName;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public ArrayList<AppExtraKeyValuePair> getAppExtraKeyValuePairs() {
        return appExtraKeyValuePairs;
    }
    public void setAppExtraKeyValuePairs(ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs) {
        this.appExtraKeyValuePairs = appExtraKeyValuePairs;
    }
    
}
