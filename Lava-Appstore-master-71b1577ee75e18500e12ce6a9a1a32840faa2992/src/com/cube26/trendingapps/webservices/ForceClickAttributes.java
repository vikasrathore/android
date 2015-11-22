/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.webservices;

import java.io.Serializable;

public class ForceClickAttributes implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    float xPos;
    float yPos;
    long timeToWait;
    public float getxPos() {
        return xPos;
    }
    public void setxPos(float xPos) {
        this.xPos = xPos;
    }
    public float getyPos() {
        return yPos;
    }
    public void setyPos(float yPos) {
        this.yPos = yPos;
    }
    public long getTimeToWait() {
        return timeToWait;
    }
    public void setTimeToWait(long timeToWait) {
        this.timeToWait = timeToWait;
    }
}
