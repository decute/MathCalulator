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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.apache.cordova.LOG;
import java.util.*;
import android.content.BroadcastReceiver;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.cordova.PluginResult;
import java.util.Arrays;

/**
 * This class echoes a string called from JavaScript.
 */
public class MathCalculator extends CordovaPlugin {

    
    HashMap<String, UsbDevice> connectedDevices;
     /* USB system service */
    private UsbManager mUsbManager;
    private UsbDevice deviceFound;
    private UsbDeviceConnection connection;

    private UsbInterface usbSerialInterface;
    private UsbEndpoint endpointSerialRead;
    private UsbEndpoint endpointSerialWrite;

    private UsbInterface usbBulkInterface;
    private UsbEndpoint endPointBulkAscanRead;
    private UsbEndpoint endPointBulkAscanWrite;

    private int targetVendorID= 1972;
    private int targetProductID = 144;
    int lTIMEOUT = 5000;

     // Bluetooth state notification
    CallbackContext stateCallback;
    BroadcastReceiver stateReceiver;

    private static final String TAG = "USBPlugin";
    private Timer timer;
    private static final String LOG_TAG = "MathCalculator";
    private JSONArray mAscanData = null;
    private int mAscanData_size = 0;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("findUsbDevices")) {
            this.findUsbDevices(callbackContext);
            return true;
        }else if (action.equals("isUsbDeviceConnected")) {
            if (this.stateCallback != null) {
                callbackContext.error("State callback already registered.");
            } else {
                this.stateCallback = callbackContext;
                addStateListener();
            }
            return true;
        }else if(action.equals("setUsbDevice")) {
            this.setUsbDevice(args, callbackContext);
            return true;
        }else if(action.equals("getUsbDevice")) {
            this.getUsbDevice(callbackContext);
            return true;
        }else if(action.equals("openUsbConnection")) {
            this.openUsbConnection(callbackContext);
            return true;
        }else if(action.equals("closeUsbConnection")) {
            this.closeUsbConnection(callbackContext);
            return true;
        }else if(action.equals("sendCommand")) {
            this.sendCommand(args,callbackContext);
            return true;
        }else if(action.equals("getAscan")) {
            this.getAscan(callbackContext);
            return true;
        }else if (action.equals("getContiniousAscan")) {
            this.getContiniousAscan(callbackContext);
            return true;
        }
        return false;
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

    private void addStateListener() {
         if (this.stateReceiver == null) {
            this.stateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onUsbStateChange(intent);
                }
            };
        }
        try {
            IntentFilter intentFilterAttach = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            IntentFilter intentFilterDetach = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            webView.getContext().registerReceiver(this.stateReceiver, intentFilterAttach);
            webView.getContext().registerReceiver(this.stateReceiver, intentFilterDetach);
        } catch (Exception e) {
            //LOG.e(TAG, "Error registering state receiver: " + e.getMessage(), e);
        }
    }

    private void onUsbStateChange(Intent intent) {
        final String action = intent.getAction();
         if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) 
         {
              sendUsbState("Attach");
         }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
         {
              sendUsbState("Detach");
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

    private void getUsbDevice(CallbackContext callbackContext) {     
        callbackContext.success("PID AND VID IS"+targetProductID + "-----"+targetVendorID);
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
                    connection = mUsbManager.openDevice(deviceFound);
                    usbSerialInterface = deviceFound.getInterface(0x01);
                    usbBulkInterface = deviceFound.getInterface(0x02);
                    connection.claimInterface(usbSerialInterface, true);
                    connection.claimInterface(usbBulkInterface, true);
                    endpointSerialRead  = usbSerialInterface.getEndpoint(0x00);
                    endpointSerialWrite = usbSerialInterface.getEndpoint(0x01);
                    endPointBulkAscanRead  = usbBulkInterface.getEndpoint(0x00);
                    endPointBulkAscanWrite = usbBulkInterface.getEndpoint(0x01);
                    byte[] buffer = {0, -61, 1, 0, 0, 0, 8};
                    connection.controlTransfer(33, 32, 0, 0, buffer, buffer.length, lTIMEOUT);
                    connection.controlTransfer(33, 34, 1, 0, null, 0, lTIMEOUT);
                    connection.controlTransfer(33, 34, 3, 0, null, 0, lTIMEOUT);
                    callbackContext.success("Connection Established Successfully");   
                }catch (Exception e) {
                    callbackContext.error("Failed to Established the Connection"+e);
                } 
           }else{
                    callbackContext.error("No Device Found of PID AND VID Tyep"+ targetProductID + "----"+ targetVendorID );
           }      
         }
    }

    private void closeUsbConnection(CallbackContext callbackContext) {
         if(connection==null){
             callbackContext.error("Failed to Close the USB Connection");
         }else{
             connection.releaseInterface(usbSerialInterface);
             connection.releaseInterface(usbBulkInterface);
             connection.close();
             deviceFound = null;
             usbSerialInterface = null;
             endpointSerialRead = null;
             endpointSerialWrite =null;
             usbBulkInterface = null;
             endPointBulkAscanRead = null;
             endPointBulkAscanWrite =null;
         }
    }

    private void sendCommand(JSONArray args, CallbackContext callbackContext) {  
        if(deviceFound==null){
            callbackContext.error("Device Referenced not Found ... Please open the connection First" + deviceFound);
        }else {
           getPermission(deviceFound);
           if(connection!=null){
                 try{
                    String command = "VER?\r\n";
                    if(args != null) {
                        command = args.getJSONObject(0).getString("command");
                        command = command + "\r\n";
                    } else {
                        callbackContext.error("Command Send null");
                    }                
                    byte[] buf = command.getBytes();
                    connection.bulkTransfer(endpointSerialWrite, buf, buf.length, lTIMEOUT);
                    int dataLen = endpointSerialRead.getMaxPacketSize();
                    byte[] data = new byte[dataLen];
                    int r =  connection.bulkTransfer(endpointSerialRead, data, dataLen, lTIMEOUT);
                    if (r >= 0) {
                        callbackContext.success("Buffer"+ Arrays.toString(buf) + "Data_Length : " + dataLen + "Response :" + new String(data, StandardCharsets.UTF_8));
                    }else{
                        callbackContext.error("Bulk Transfer read Failed"+ r);
                    }
                } catch(Exception ex) {
                    callbackContext.error("Something went wrong" + ex);
                }        
           }else{
               callbackContext.error("Please Open the USB Connection First"+ connection);
           }
        }
    }

    private void getAscan(CallbackContext callbackContext){
        if(deviceFound==null){
            callbackContext.success("Device Referenced not Found ... Please open the connection First" + deviceFound);
        }else {
            getPermission(deviceFound);
            if(connection!=null){
                mAscanData = new JSONArray();
                        mAscanData_size = -1;
                        int dataSize = endPointBulkAscanRead.getMaxPacketSize();
                        byte[] data = new byte[dataSize];

                        int rval = connection.controlTransfer(0xC1, 0xFD, 0x00, 0x00, null, 0, lTIMEOUT);
                        rval =  connection.bulkTransfer(endPointBulkAscanRead, data, dataSize, lTIMEOUT);

                        int ascanSize = (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
                        ascanSize *= 4;

                        int cur_ascanSize = dataSize-4;

                        bytesToJSON(data);
                        mAscanData.remove(0);
                        mAscanData_size--;

                        while (cur_ascanSize < ascanSize) {
                                rval = connection.controlTransfer(0xC1, 0xFE, 0x00, 0x00, null, 0, lTIMEOUT); assert(rval>=0);
                                rval = connection.bulkTransfer(endPointBulkAscanRead, data, dataSize, lTIMEOUT); assert(rval==dataSize);
                                bytesToJSON(data);
                                int overFlow = (cur_ascanSize+dataSize) - ascanSize;
                                if (overFlow>0) {
                                    dataSize -= overFlow;
                                    for(int k=0; k<(overFlow/4); k++) {
                                        mAscanData.remove(mAscanData.length()-1);
                                        mAscanData_size--;
                                    }
                                }

                                cur_ascanSize += dataSize;
                            }

                  callbackContext.success(mAscanData);
            }else{
                callbackContext.error("Please Open the USB Connection First");
            }
        }
    }


   public void getContiniousAscan(final CallbackContext callbackContext) {
        //final MathCalculator that = this;
        cordova.getThreadPool().execute(new Runnable() {   
           public void run() {
               if(deviceFound==null){
                     callbackContext.error("Device Referenced not Found ... Please open the connection First" + deviceFound);
               }else {
                //   that.timer = new Timer(LOG_TAG, true);
                //   TimerTask timerTask = new TimerTask() {
                //     public void run() {

                     getPermission(deviceFound);
                    if (connection != null) {
                        mAscanData = new JSONArray();
                        mAscanData_size = -1;
                        int dataSize = endPointBulkAscanRead.getMaxPacketSize();
                        byte[] data = new byte[dataSize];
                        int rval = connection.controlTransfer(0xC1, 0xFD, 0x00, 0x00, null, 0, lTIMEOUT);
                        rval =  connection.bulkTransfer(endPointBulkAscanRead, data, dataSize, lTIMEOUT);
                        int ascanSize = (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
                        ascanSize *= 4;
                        int cur_ascanSize = dataSize-4;
                        bytesToJSON(data);
                        mAscanData.remove(0);
                        mAscanData_size--;
                        while (cur_ascanSize < ascanSize) {
                                rval = connection.controlTransfer(0xC1, 0xFE, 0x00, 0x00, null, 0, lTIMEOUT); assert(rval>=0);
                                rval = connection.bulkTransfer(endPointBulkAscanRead, data, dataSize, lTIMEOUT); assert(rval==dataSize);
                                bytesToJSON(data);
                                int overFlow = (cur_ascanSize+dataSize) - ascanSize;
                                if (overFlow>0) {
                                    dataSize -= overFlow;
                                    for(int k=0; k<(overFlow/4); k++) {
                                        mAscanData.remove(mAscanData.length()-1);
                                        mAscanData_size--;
                                    }
                                }
                                cur_ascanSize += dataSize;
                            }
                            PluginResult result = new PluginResult(PluginResult.Status.OK, mAscanData);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                            } else {
                                callbackContext.error("Please Open the USB Connection First");
                            }    

                //         }
                //     };

                //   that.timer.scheduleAtFixedRate(timerTask, 0, 200);
                  
               }  
              }
            }); 
         PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT); 
         pluginResult.setKeepCallback(true);  
    }


    private void getPermission(UsbDevice device) {
         if (!mUsbManager.hasPermission(device)) {
             mUsbManager.requestPermission(device, PendingIntent.getActivity(cordova.getActivity(), 0, new Intent("android.permission.MANAGE_USB"), 0)) ;
           }
    }

   private void bytesToJSON(byte[] bytes) {
        JSONObject jsonObj = null;
        int i = 0;
        while (i < bytes.length) {
            int minVal = (bytes[i+0] & 0xFF) + ((bytes[i+1] & 0xFF) << 8);
            int maxVal = (bytes[i+2] & 0xFF) + ((bytes[i+3] & 0xFF) << 8);

            try {
                jsonObj = new JSONObject();
                jsonObj.put("x", mAscanData_size);
                jsonObj.put("y", maxVal);
                mAscanData.put(jsonObj);
            } catch (JSONException e) {
            }
            i+=4;
            mAscanData_size++;
        }
    }

    private void sendUsbState(String result){
        if (this.stateCallback != null) {
            this.stateCallback.success(result);
        }
    }

    public void onDestroy() {
        removeStateListener();
    }

    private void removeStateListener() {
        if (this.stateReceiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.stateReceiver);
            } catch (Exception e) {
                //LOG.e(TAG, "Error unregistering state receiver: " + e.getMessage(), e);
            }
        }
        this.stateCallback = null;
        this.stateReceiver = null;
    }
}
