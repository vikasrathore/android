/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.webservices;

import java.io.Serializable;

public class FetchedWidgetUpdateData implements Serializable {

    private static final long serialVersionUID = 1L;
    private String appVersion = "";
    private String updateURL = "";
    private String md5 = "";

    public String getVersion() {
        return appVersion;
    }

    public void setVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public void setUpdateURL(String updateURL) {
        this.updateURL = updateURL;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}