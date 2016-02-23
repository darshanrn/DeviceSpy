package com.dash.mxdevicespy;

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;

import android.net.wifi.WifiManager;

public class BrowserUpnpService extends AndroidUpnpServiceImpl {

	 @Override
	    protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
	        return new AndroidUpnpServiceConfiguration(wifiManager) {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 7000;
            }
            
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[]{
                    new UDAServiceType("RemoteUIServer")
                };
            }

        };
    }
}