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
    private UsbDevice deviceFound = null;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface = null;
    private UsbEndpoint endpointRead = null;
    private UsbEndpoint endpointWrite =null;

    private int targetVendorID= 1972;
    private int targetProductID = 144;
    int lTIMEOUT = 2000;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }else if(action.equals("add")) {
            this.add(args, callbackContext);
            return true;
        }else if(action.equals("findUsbDevices")) {
            this.findUsbDevices(callbackContext);
            return true;
        }else if(action.equals("setUsbDevice")) {
            this.setUsbDevice(args, callbackContext);
            return true;
        }else if(action.equals("openUsbConnection")) {
            this.openUsbConnection(callbackContext);
            return true;
        }else if(action.equals("closeUsbConnection")) {
            this.closeUsbConnection(callbackContext);
            return true;
        }else if(action.equals("sendCommand")) {
            String command = args.getString(0);
            this.sendCommand(command,callbackContext);
            return true;
        }else if(action.equals("getUsbDevice")) {
            this.getUsbDevice(callbackContext);
            return true;
        }
        return false;
    }

    private void getUsbDevice(CallbackContext callbackContext) {     
            callbackContext.success("PID AND VID IS"+targetProductID + "-----"+targetVendorID);
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
           if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

     private void findUsbDevices(CallbackContext callbackContext) {

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

    
    private void setUsbDevice(JSONArray args, CallbackContext callbackContext) {
            if(args != null){
             try{
                   int pid =  Integer.parseInt(args.getJSONObject(0).getString("pid"));
                   int vid =  Integer.parseInt(args.getJSONObject(0).getString("vid"));
                   targetVendorID = vid;
                   targetProductID = pid;
                   callbackContext.success("success");
             } catch(Exception ex) {
                callbackContext.error("Something went wrong" + ex);
             }

        } else {
            callbackContext.error("Please don't pass null value. ");
        }
    }

    private void openUsbConnection(CallbackContext callbackContext) {
            mUsbManager = (UsbManager) cordova.getActivity().getSystemService(UsbManager.class);
            connectedDevices = mUsbManager.getDeviceList();
            if (connectedDevices.isEmpty()) {
                callbackContext.success("No Devices Currently Connected");
            }else{
            Iterator<UsbDevice> deviceIterator = connectedDevices.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                if(device.getVendorId()==targetVendorID){
                if(device.getProductId()==targetProductID){
                        deviceFound = device;
                   }
               }           
           }
           if(deviceFound != null){
                getPermission(deviceFound); 
                try{
                    connection = mUsbManager.openDevice(device);
                    callbackContext.success("Connection Established Successfully");   
                }catch (Exception e) {
                    callbackContext.error("Failed to Established the Connection");
                } 
           }else{
                    callbackContext.error("No Device Found of PID AND VID Tyep"+ targetProductID + "----"+ targetVendorID );
           }      
         }
    }

    private void sendCommand(String command, CallbackContext callbackContext) {   
        if(deviceFound==null){
            callbackContext.error("Please Open the USB Connection First");
        }else {
           getPermission(deviceFound);
           if(connection!=null){
                usbInterface = device.getInterface(0x01);
                connection.claimInterface(usbInterface, true);
                byte[] data = new byte[8];
                connection.controlTransfer(0x21, 0x22, 0x03, 0x01, data, 0, lTIMEOUT);
                byte[] buffer = {0x00, (byte) 0xC2, 0x01, 0x00, 0x00, 0x00, 0x08};
                connection.controlTransfer(0x21, 0x20, 0x00, 0x01, buffer, buffer.length, lTIMEOUT);
                endpointRead  = usbInterface.getEndpoint(0x00);
                endpointWrite = usbInterface.getEndpoint(0x01);
                String command = "ISN?\r\n";
                byte[] buf = command.getBytes(StandardCharsets.UTF_8);
                int dataLength = buf.length;
                int res = connection.bulkTransfer(endpointWrite,buf, dataLength, lTIMEOUT);
                callbackContext.success("Bulk Transfer write, data_len:" + dataLength + "==" + res);
                byte[] sn_data = new byte[64];
                try {
                   connection.bulkTransfer(endpointRead, sn_data, sn_data.length, lTIMEOUT);
                }
                catch (Exception e) {
                callbackContext.error("Bulk Transfer Failed");
                }
           }else{
               callbackContext.error("Please Open the USB Connection First");
           }
        }
    }

    private void closeUsbConnection(CallbackContext callbackContext) {
         if(connection==null){
             callbackContext.error("Failed to Close the USB Connection");
         }else{
             connection.releaseInterface(usbInterface);
             connection.close();
             deviceFound = null;
             usbInterface = null;
             endpointRead = null;
             endpointWrite =null;
         }
    }

    private void getPermission(UsbDevice device) {
         if (!mUsbManager.hasPermission(device)) {
             mUsbManager.requestPermission(device, PendingIntent.getActivity(cordova.getActivity(), 0, new Intent("android.permission.MANAGE_USB"), 0)) ;
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
