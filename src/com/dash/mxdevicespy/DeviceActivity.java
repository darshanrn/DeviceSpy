package com.dash.mxdevicespy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.UUID;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;

import com.dash.mxdevicespy.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class DeviceActivity extends Activity implements PropertyChangeListener{

	private AndroidUpnpService upnpService;

    private UDN udn = new UDN(UUID.randomUUID()); // TODO: Not stable!

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            LocalService<SwitchPower> switchPowerService = getSwitchPowerService();

            // Register the device when this activity binds to the service for the first time
            if (switchPowerService == null) {
                try {
                	LocalDevice binaryLightDevice = createDevice();

                    Toast.makeText(DeviceActivity.this, "Registering the device", Toast.LENGTH_SHORT).show();
                    upnpService.getRegistry().addDevice(binaryLightDevice);

                    switchPowerService = getSwitchPowerService();

                } catch (Exception ex) {
                    Toast.makeText(DeviceActivity.this, "Failed to create device", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Obtain the state of the power switch and update the UI
            //setLightbulb(switchPowerService.getManager().getImplementation().getStatus());

            // Start monitoring the power switch
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .addPropertyChangeListener(DeviceActivity.this);

        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device);

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        
        /**
         * Start the server to open up a socket and listen
         */
        
        int port = 8900;
        try
        {
           Server serv = new Server(port, this);
           Thread thread = new Thread(serv);
           thread.start();
        }catch(IOException e)
        {
           e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop monitoring the power switch
        LocalService<SwitchPower> switchPowerService = getSwitchPowerService();
        if (switchPowerService != null)
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .removePropertyChangeListener(this);

        getApplicationContext().unbindService(serviceConnection);
    }

    @SuppressWarnings("unchecked")
	protected LocalService<SwitchPower> getSwitchPowerService() {
        if (upnpService == null)
            return null;

        LocalDevice binaryLightDevice;
        if ((binaryLightDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<SwitchPower>)
                binaryLightDevice.findService(new UDAServiceType("RemoteUIServer", 1));
    }

    public void propertyChange(PropertyChangeEvent event) {
        // This is regular JavaBean eventing, not UPnP eventing!
        if (event.getPropertyName().equals("status")) {
            Toast.makeText(DeviceActivity.this, "Turning light: " + event.getNewValue(), Toast.LENGTH_SHORT).show();
            setLightbulb((Boolean) event.getNewValue());
        }
    }

    protected void setLightbulb(final boolean on) {
        runOnUiThread(new Runnable() {
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.light_imageview);
                imageView.setImageResource(on ? R.drawable.light_on : R.drawable.light_off);
                // You can NOT externalize this color into /res/values/colors.xml. Go on, try it!
                imageView.setBackgroundColor(on ? Color.parseColor("#9EC942") : Color.WHITE);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
	protected LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException, IOException {

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Android Phone",
                        new ManufacturerDetails("Samsung"),
                        new ModelDetails("Samsung", "Icecream Sandwich", "4.4")
                );

        @SuppressWarnings("rawtypes")
		LocalService service =
                new AnnotationLocalServiceBinder().read(SwitchPower.class);

        service.setManager(
                new DefaultServiceManager<SwitchPower>(service, SwitchPower.class)
        );

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
                service
        );
    }
}
