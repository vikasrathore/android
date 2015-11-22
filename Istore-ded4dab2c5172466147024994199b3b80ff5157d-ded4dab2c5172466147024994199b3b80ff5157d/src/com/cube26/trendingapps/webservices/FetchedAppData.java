/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.webservices;

import java.io.Serializable;
import java.util.ArrayList;

public class FetchedAppData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String appName = "";
    private String appUrl = "";
    private String appIconUrl = "";
    private String appImage1 = "";
    private String appImage2 = "";
    private String appImage3 = "";
    private String appType = "";
    private String launchTime = "";
    private String appFullText = "";
    private String appDev = "";
    private Boolean flag_install = false;
    private Float appRating = 0.0f;
    private Boolean flag_premium = false;
    private Boolean flag_download = false;
    private String appMD5 = "";
    private String packageName = "";
    private Boolean onlyWifi = false;
    private String appBannerImage = "";
    //private String appVideoUri = "";
    private long apkSize;
    private ArrayList<ForceClickAttributes> forceClickAttributes;
    private long forceCloseApp = 0;
    private int activationCounter = 1;
    
    public int getActivationCounter() {
        return activationCounter;
    }
    public void setActivationCounter(int activationCounter) {
        this.activationCounter = activationCounter;
    }
    public ArrayList<ForceClickAttributes> getForceClickAttributes() {
        return forceClickAttributes;
    }
    public void setForceClickAttributes(ArrayList<ForceClickAttributes> forceClickAttributes) {
        this.forceClickAttributes = forceClickAttributes;
    }
   
    public long getForceCloseApp() {
        return forceCloseApp;
    }
    public void setForceCloseApp(long forceCloseApp) {
        this.forceCloseApp = forceCloseApp;
    }
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppFullText() {
        return appFullText;
    }

    public void setAppFullText(String appFullText) {
        this.appFullText = appFullText;
    }

    public String getImage1() {
        return appImage1;
    }

    public void setImage1(String appImage1) {
        this.appImage1 = appImage1;
    }

    public String getImage2() {
        return appImage2;
    }

    public void setImage2(String appImage2) {
        this.appImage2 = appImage2;
    }

    public String getImage3() {
        return appImage3;
    }

    public void setImage3(String appImage3) {
        this.appImage3 = appImage3;
    }

    public String getAppDev() {
        return appDev;
    }

    public void setMD5(String appMD5) {
        this.appMD5 = appMD5;
    }

    public String getMD5() {
        return appMD5;
    }

    public void setAppDev(String appDev) {
        this.appDev = appDev;
    }

    public Float getAppRating() {
        return appRating;
    }

    public void setAppRating(Float appRating) {
        this.appRating = appRating;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public void setAppIconUrl(String appIconUrl) {
        this.appIconUrl = appIconUrl;
    }

    public void setFlag_Premium(Boolean flag_premium) {
        this.flag_premium = flag_premium;
    }

    public Boolean getFlag_Premium() {
        return flag_premium;
    }

    public void setFlag_Download(Boolean flag_download) {
        this.flag_download = flag_download;
    }

    public Boolean getFlag_Download() {
        return flag_download;
    }

    public void setOnlyWifi(Boolean onlyWifi) {
        this.onlyWifi = onlyWifi;
    }

    public Boolean getOnlyWifi() {
        return onlyWifi;
    }

    public void setFlag_Install(Boolean flag_install) {
        this.flag_install = flag_install;
    }

    public Boolean getFlag_Install() {
        return flag_install;
    }

    public void setActivationTime(String launchTime) {
        this.launchTime = launchTime;
    }

    public String getActivationTime() {
        return launchTime;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppType() {
        return appType;
    }
    
    public void setBannerImage(String bannerImage){
        appBannerImage = bannerImage;
    }
    
    public String getBannerImage(){
        return appBannerImage;
    }

    public String getAppVideoUri() {
        // Best way to disable videos is returning empty string
        return "";
        //return appVideoUri;
    }

    public void setAppVideoUri(String appVideoUri) {
        //this.appVideoUri = appVideoUri;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }
    
    
}