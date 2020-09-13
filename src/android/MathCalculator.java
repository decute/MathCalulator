package cordova.plugin.mathcalculator;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class echoes a string called from JavaScript.
 */
public class MathCalculator extends CordovaPlugin {

    
    HashMap<String, UsbDevice> connectedDevices = null;
     /* USB system service */
    private UsbManager mUsbManager;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if(action.equals("add")) {
            this.add(args, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
            mUsbManager = (UsbManager) cordova.getActivity().getSystemService(UsbManager.class);
            connectedDevices = mUsbManager.getDeviceList();

            if (connectedDevices.isEmpty()) {
                callbackContext.success("No Devices Currently Connected");
            }else{
            Iterator<UsbDevice> deviceIterator = connectedDevices.values().iterator();
            JSONObject deviceObj = null;
            JSONArray devicesArray = new JSONArray();
            while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();  
                    try{
                    deviceObj = new JSONObject();    
                    deviceObj.put("DeviceID", device.getDeviceId());
                    deviceObj.put("DeviceName", device.getDeviceName());
                    deviceObj.put("DeviceClass", device.getDeviceClass());
                    deviceObj.put("VendorID", device.getVendorId());
                    deviceObj.put("ProductID", device.getProductId());
                    deviceObj.put("InterfaceCount", device.getInterfaceCount());    
                    devicesArray.put(deviceObj); 
                    } catch (JSONException e) {
                     // TODO Auto-generated catch block                        
                       callbackContext.error("Failed to bind Device Information");
                    } 
              }
                callbackContext.success(devicesArray);
            }
    }

    private void add(JSONArray args, CallbackContext callbackContext) {
        if(args != null){
             try{
                   int p1 =  Integer.parseInt(args.getJSONObject(0).getString("param1"));
                   int p2 =  Integer.parseInt(args.getJSONObject(0).getString("param2"));
                   callbackContext.success(""+(p1+p2));

             } catch(Exception ex) {
                callbackContext.error("Something went wrong" + ex);
             }

        } else {
            callbackContext.error("Please don't pass null value. ");
        }
    }
}
