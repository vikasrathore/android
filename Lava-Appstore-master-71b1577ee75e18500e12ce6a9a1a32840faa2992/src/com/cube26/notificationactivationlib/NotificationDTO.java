package com.cube26.notificationactivationlib;

import java.io.Serializable;
import java.util.ArrayList;

public class NotificationDTO implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private String iconUrl;
    private String redirectionUrl;
    private String packageName;
    private String activityName;
    private String time;
    private ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public String getRedirectionUrl() {
        return redirectionUrl;
    }
    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getActivityName() {
        return activityName;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public ArrayList<AppExtraKeyValuePair> getAppExtraKeyValuePairs() {
        return appExtraKeyValuePairs;
    }
    public void setAppExtraKeyValuePairs(ArrayList<AppExtraKeyValuePair> appExtraKeyValuePairs) {
        this.appExtraKeyValuePairs = appExtraKeyValuePairs;
    }
    
}
