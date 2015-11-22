/*
 *  Copyright : Copyright 2014 Cube26 Software Pvt. Ltd., all rights reserved.
 *  Any unauthorized use of this code is punishable offense by law. If
 *  you are reading this code, and you are not authorized to do so,
 *  please close the code, and delete all the copies of it.
 *
 */
package com.cube26.trendingapps.webservices;

import java.io.Serializable;

public class FetchedDealData implements Serializable {

    private static final long serialVersionUID = 1L;
    private String dealUrl = "";
    private String dealIconUrl = "";
    private String dealOfferUrl = "";
    private String dealDescription = "";
    private ActivationDTO activationDTO;
    
    public ActivationDTO getActivationDTO() {
        return activationDTO;
    }

    public void setActivationDTO(ActivationDTO activationDTO) {
        this.activationDTO = activationDTO;
    }

    public String getDealUrl() {
        return dealUrl;
    }

    public void setDealUrl(String dealUrl) {
        this.dealUrl = dealUrl;
    }

    public String getDealIconUrl() {
        return dealIconUrl;
    }

    public void setDealIconUrl(String dealIconUrl) {
        this.dealIconUrl = dealIconUrl;
    }

    public void setDealOfferUrl(String dealOfferUrl) {
        this.dealOfferUrl = dealOfferUrl;
    }

    public String getDealOfferUrl() {
        return dealOfferUrl;
    }

    public String getDealDescription() {
        return dealDescription;
    }

    public void setDealDescription(String dealDescription) {
        this.dealDescription = dealDescription;
    }
}