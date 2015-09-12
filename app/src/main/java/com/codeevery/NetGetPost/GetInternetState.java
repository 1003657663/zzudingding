package com.codeevery.NetGetPost;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//使用此类中的方法需要先传入ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
public class GetInternetState {
    private ConnectivityManager cm;
	public GetInternetState(ConnectivityManager cm){
        this.cm = cm;
    }
    public boolean isNetConnected(){
        if (cm != null) {  
            NetworkInfo[] infos = cm.getAllNetworkInfo();  
            if (infos != null) {  
                for (NetworkInfo ni : infos) {  
                    if (ni.isConnected()) {  
                        return true;  
                    }  
                }  
            }  
        }  
        return false;  
    }  
   

    public boolean isWifiConnected() {
        if (cm != null) {  
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
            if (networkInfo != null 
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
                return true;  
            }  
        }  
        return false;  
    }  
   

    public boolean is3gConnected() {
        if (cm != null) {  
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
            if (networkInfo != null 
                    && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {  
                return true;  
            }  
        }  
        return false;  
    }  
   

    /*public boolean isGpsEnabled() {
        //LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> accessibleProviders = lm.getProviders(true);  
        for (String name : accessibleProviders) {  
            if ("gps".equals(name)) {  
                return true;  
            }  
        }  
        return false;  
    }*/
}  