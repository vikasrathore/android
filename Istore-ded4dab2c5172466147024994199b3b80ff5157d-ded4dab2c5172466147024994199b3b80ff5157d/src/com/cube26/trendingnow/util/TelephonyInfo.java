package com.cube26.trendingnow.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

public final class TelephonyInfo {

	 private static final String MTK_TELEPHONY_MANAGER_EX_CLASS_NAME = "com.mediatek.telephony.TelephonyManagerEx";

	    private static final String SPRD_OPERATORUTILS_CLASS_NAME = "android.telephony.SprdPhoneSupport";

	    public static final String GEMINI_SIM_NUM_PROP = "persist.gemini.sim_num";

	    /**
	     * SIM ID for GEMINI
	     */
	    public static final int GEMINI_SIM_1 = 0;
	    public static final int GEMINI_SIM_2 = 1;
	    public static final int GEMINI_SIM_3 = 2;
	    public static final int GEMINI_SIM_4 = 3;

	    /**
	     * The number of SIM supported by current configuration.
	     * 
	     * @internal
	     */
	    public static final int GEMINI_SIM_NUM = SystemProperties.getInt(GEMINI_SIM_NUM_PROP, 2);

	    private static final int FIRST_SLOT_ID = GEMINI_SIM_1;

	    public static String getImeis(Context context){
	        if(isMediatek()){
	            List<Integer> imeiSlots = getSimSlots();
	            String imeis = "";
	            if(imeiSlots != null && imeiSlots.size() > 0){
	                for(int slot : imeiSlots){
	                    imeis+= "" + getMTKImeiForSimSlot(context, MTK_TELEPHONY_MANAGER_EX_CLASS_NAME, slot)+"+";
	                }
	                if(imeis.length()>0)
	                {
	                	imeis=imeis.substring(0,imeis.length()-1);
	                }
	            }
	            return imeis.trim();
	        }else if(isSpreadtrum()){
	            int phoneCount = getSPRDPhoneCount(context);
	            if(phoneCount == 0){
	                return getGenericSingleImei(context);
	            }
	            if(phoneCount == 1){
	                return TelephonyManager.getDefault().getDeviceId();
	            }
	            String imeis = "";
	            for(int i = 0; i<phoneCount; i++){
	                imeis+= "" + getSPRDImeiForSimSlot(context, i)+"+";
	            }
	            if(imeis.length()>0)
                {
                	imeis=imeis.substring(0,imeis.length()-1);
                }
	            return imeis.trim();
	        }else{
	            return getGenericSingleImei(context);
	        }
	    }

	    private static ArrayList<Integer> getSimSlots(){
	        ArrayList<Integer> slotIds = new ArrayList<Integer>();
	        for (int i = 0; i < GEMINI_SIM_NUM; i++) {
	            int slotId = FIRST_SLOT_ID + i;
	            slotIds.add(slotId);
	        }
	        return slotIds;
	    }

	    private static String getMTKImeiForSimSlot(Context context, String className, int slotID){
	        try{

	            Class<?> telephonyClass = Class.forName(className);

	            Method[] methods = telephonyClass.getMethods();
	            Object getDefaultObj = null;
	            String imei = null;
	            for (Method m : methods) {
	                if(m.getName().equals("getDefault")){
	                    getDefaultObj =  m.invoke(null, (Object [])null);
	                    break;
	                }
	            }
	            for (Method m : methods) {
	                if(m.getName().equals("getDeviceId")){
	                    Object[] obParameter = new Object[1];
	                    obParameter[0] = slotID;
	                    Object imeiObj =  m.invoke(getDefaultObj, obParameter);
	                    imei = imeiObj.toString();
	                    break;
	                }
	            }
	            return imei;


	        } catch (Exception e) {
	            e.printStackTrace();
	            return getGenericSingleImei(context);
	        }
	    }

	    private static String getSPRDImeiForSimSlot(Context context, int slotID){
	        try{

	            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
	            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

	            String serviceName = "";

	            Method[] methods = telephonyClass.getMethods();
	            for (Method m : methods) {
	                if(m.getName().equals("getServiceName")){
	                    Object[] obParameter = new Object[2];
	                    obParameter[0] = Context.TELEPHONY_SERVICE;
	                    obParameter[1] = slotID;
	                    serviceName =  (String) m.invoke(null, obParameter);
	                    break;
	                }
	            }
	            return ((TelephonyManager)context.getSystemService(serviceName)).getDeviceId();

	        } catch (Exception e) {
	            e.printStackTrace();
	            return getGenericSingleImei(context);
	        }
	    }

	    private static int getSPRDPhoneCount(Context context){
	        try{
	            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
	            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
	            Method[] methods = telephonyClass.getMethods();
	            int count = 0;
	            for (Method m : methods) {
	                if(m.getName().equals("getPhoneCount")){
	                    count =  (Integer) m.invoke(null, (Object [])null);
	                    break;
	                }
	            }
	            return count;
	        }catch(Exception ex){
	            return 0;
	        }

	    }

	    private static String getGenericSingleImei(Context context){
	        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        String id;
	        id = TelephonyMgr.getDeviceId();
	        return id != null ? id : "";       
	    }

	    private static boolean isMediatek(){
	        try {
	            Class.forName(MTK_TELEPHONY_MANAGER_EX_CLASS_NAME);
	            return true;
	        } catch (ClassNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            return false;
	        }
	    }

	    private static boolean isSpreadtrum(){
	        try {
	            Class.forName(SPRD_OPERATORUTILS_CLASS_NAME);
	            return true;
	        } catch (ClassNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            return false;
	        }
	    }
}