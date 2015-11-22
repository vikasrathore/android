package com.cube26.notificationactivationlib;

import java.util.ArrayList;

public class DataTransferObject {
    
    ArrayList<NotificationDTO> notificationDTOs;
    ArrayList<ActivationDTO> activationDTOs;
    
    public ArrayList<NotificationDTO> getNotificationDTOs() {
        return notificationDTOs;
    }
    public void setNotificationDTOs(ArrayList<NotificationDTO> notificationDTOs) {
        this.notificationDTOs = notificationDTOs;
    }
    public ArrayList<ActivationDTO> getActivationDTOs() {
        return activationDTOs;
    }
    public void setActivationDTOs(ArrayList<ActivationDTO> activationDTOs) {
        this.activationDTOs = activationDTOs;
    }
}
