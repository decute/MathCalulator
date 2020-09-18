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

/**
 * This class echoes a string called from JavaScript.
 */
public class MathCalculator extends CordovaPlugin {

    
    HashMap<String, UsbDevice> connectedDevices;
     /* USB system service */
    private UsbManager mUsbManager;
    private UsbDevice deviceFound;
    private UsbDeviceConnection connection;

    private UsbInterface usbInterface;
    private UsbEndpoint endpointRead;
    private UsbEndpoint endpointWrite;

    private UsbInterface usbAscanInterface;
    private UsbEndpoint endPointAscanRead;
    private UsbEndpoint endPointAscanWrite;

    private int targetVendorID= 1972;
    private int targetProductID = 144;
    int lTIMEOUT = 5000;

     // Bluetooth state notification
    CallbackContext stateCallback, stateTimerCallback;
    BroadcastReceiver stateReceiver;

    private static final String TAG = "USBPlugin";
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

    private Timer timer;
  
    private static final String LOG_TAG = "MathCalculator";
    public int value = 0;


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
        }else if(action.equals("sendCommandAndWaitResponse")) {
            String command = args.getString(0);
            this.sendCommandAndWaitResponse(command,callbackContext);
            return true;
        }else if(action.equals("getUsbDevice")) {
            this.getUsbDevice(callbackContext);
            return true;
        }else if (action.equals("isUsbDeviceConnected")) {
            if (this.stateCallback != null) {
                callbackContext.error("State callback already registered.");
            } else {
                this.stateCallback = callbackContext;
                addStateListener();
            }
        }else if(action.equals("openAscanUsbConnection")) {
            this.openAscanUsbConnection(callbackContext);
            return true;
        }else if(action.equals("getAscan")) {
            this.getAscan(callbackContext);
            return true;
        }else if (action.equals("testFunction")) {
                testFunction(callbackContext);
        }else if(action.equals("testThreadFunction")) {
            this.testThreadFunction(callbackContext);
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
                    connection = mUsbManager.openDevice(deviceFound);
                    usbInterface = deviceFound.getInterface(0x01);
                    connection.claimInterface(usbInterface, true);
                    endpointRead  = usbInterface.getEndpoint(0x00);
                    endpointWrite = usbInterface.getEndpoint(0x01);
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

    private void openAscanUsbConnection(CallbackContext callbackContext) {
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
                    usbAscanInterface = deviceFound.getInterface(0x02);
                    connection.claimInterface(usbAscanInterface, true);
                    endPointAscanRead  = usbAscanInterface.getEndpoint(0x00);
                    endPointAscanWrite = usbAscanInterface.getEndpoint(0x01);
                    callbackContext.success("Connection Established Successfully");
                }catch (Exception e) {
                    callbackContext.error("Failed to Established the Connection");
                }
            }else{
                callbackContext.error("No Device Found of PID AND VID Tyep"+ targetProductID + "----"+ targetVendorID );
            }
        }
    }

    private void sendCommandAndWaitResponse(String args, CallbackContext callbackContext) {
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
                    usbInterface = deviceFound.getInterface(0x01);
                    connection.claimInterface(usbInterface, true);
                    endpointRead  = usbInterface.getEndpoint(0x00);
                    endpointWrite = usbInterface.getEndpoint(0x01);
                    byte[] buffer = {0, -61, 1, 0, 0, 0, 8};
                    connection.controlTransfer(33, 32, 0, 0, buffer, buffer.length, lTIMEOUT);
                    connection.controlTransfer(33, 34, 1, 0, null, 0, lTIMEOUT);
                    connection.controlTransfer(33, 34, 3, 0, null, 0, lTIMEOUT);
                    String command = (args==null) ? "ISN?\r\n" : args;
                    byte[] buf = command.getBytes(StandardCharsets.UTF_8);
                    int dataLength = buf.length;
                    int res = connection.bulkTransfer(endpointWrite,buf, dataLength, lTIMEOUT);
                    byte[] sn_data = new byte[64];
                    try {
                    connection.bulkTransfer(endpointRead, sn_data, sn_data.length, lTIMEOUT);
                    }
                    catch (Exception e) {
                    callbackContext.error("Bulk Transfer READ Failed" + e);
                    }
                    String result = new String(sn_data, StandardCharsets.UTF_8);
                    callbackContext.success("DATA is, Data_Length:" + dataLength + "==" + result);   
                }catch (Exception e) {
                    callbackContext.error("Failed to Established the Connection" + e);
                } 
           }else{
                    callbackContext.error("No Device Found of PID AND VID Tyep"+ targetProductID + "----"+ targetVendorID );
           }      
         }
    }

    private void sendCommand(String args, CallbackContext callbackContext) {  
        if(deviceFound==null){
            callbackContext.error("Device Referenced not Found ... Please open the connection First" + deviceFound);
        }else {
           getPermission(deviceFound);
           if(connection!=null){
                String command = (args==null) ? "ISN?\r\n" : args;
                byte[] buf = command.getBytes(StandardCharsets.UTF_8);
                int dataLength = buf.length;
                int res = connection.bulkTransfer(endpointWrite,buf, dataLength, lTIMEOUT);
                byte[] sn_data = new byte[64];
                int r = connection.bulkTransfer(endpointRead, sn_data, sn_data.length, lTIMEOUT);
                if (r >= 0) {
                     callbackContext.success("DATA for command : " + command + "Data_Length : " + dataLength + "Response :" + new String(sn_data, StandardCharsets.UTF_8));
                }else{
                    callbackContext.error("Bulk Transfer read Failed"+ r + "write: "+ res);
                }
           }else{
               callbackContext.error("Please Open the USB Connection First"+ connection);
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

    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[(bytes.length/2) * 7];
        for (int j = 0; j < (bytes.length/2); j+=2) {
            int v1 = bytes[j] & 0xFF;
            int v2 = bytes[j+1] & 0xFF;

            hexChars[j*7 + 0] = '(';
            hexChars[j*7 + 1] = HEX_ARRAY[v1 >>> 4];
            hexChars[j*7 + 2] = HEX_ARRAY[v1 & 0x0F];
            hexChars[j*7 + 3] = ',';
            hexChars[j*7 + 4] = HEX_ARRAY[v2 >>> 4];
            hexChars[j*7 + 5] = HEX_ARRAY[v2 & 0x0F];
            hexChars[j*7 + 6] = ')';
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    private void getAscan(CallbackContext callbackContext){
        if(deviceFound==null){
            callbackContext.success("Device Referenced not Found ... Please open the connection First" + deviceFound);
        }else {
            getPermission(deviceFound);
            if(connection!=null){
                int dataSize = endPointAscanRead.getMaxPacketSize();
                byte[] data = new byte[dataSize];

                //do the first read
                int rval = connection.controlTransfer(0xC1, 0xFD, 0x00, 0x00, null, 0, lTIMEOUT);
                rval =  connection.bulkTransfer(endPointAscanRead, data, dataSize, lTIMEOUT);
                int ascanSize = (data[3] << 24) | (data[2] << 16) | (data[1] << 8) | data[0];
                ascanSize *= 4;
                byte[] ascanData = new byte[ascanSize];
                for(int i=0; i<(dataSize-4); i++) {
                    ascanData[i] = data[4+i];
                }
                while (ascanData.length < ascanSize) {
                    rval = connection.controlTransfer(0xC1, 0xFE, 0x00, 0x00, null, 0, lTIMEOUT); assert(rval>=0);
                    rval = connection.bulkTransfer(endPointAscanRead, data, dataSize, lTIMEOUT); assert(rval==dataSize);

                    int x = ascanData.length;
                    if ((x+dataSize)>ascanSize) {
                        dataSize -= ((x+dataSize) - ascanSize);
                    }

                    for(int i=0; i<dataSize; i++) {
                        ascanData[x+i] = data[i];
                    }
                }
                 callbackContext.success(bytesToHex(ascanData));
            }else{
                callbackContext.error("Please Open the USB Connection First");
            }
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
            IntentFilter intentFilterDetach = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            webView.getContext().registerReceiver(this.stateReceiver, intentFilterAttach);
            webView.getContext().registerReceiver(this.stateReceiver, intentFilterDetach);
        } catch (Exception e) {
            //LOG.e(TAG, "Error registering state receiver: " + e.getMessage(), e);
        }
    }

    private void testThreadFunction(CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				 callbackContext.success(""+10);
			}
		});
	}

     public void testFunction(final CallbackContext callbackContext) {
        // final MathCalculator that = this;
        // cordova.getThreadPool().execute(new Runnable() {   
        //    public void run() {
        //           that.timer = new Timer(LOG_TAG, true);
        //           TimerTask timerTask = new TimerTask() {
        //                 public void run() {
        //                     double db = 0;                                
        //                     db = 20.0 * value + 90;
        //                     ++value;              
        //                     PluginResult result = new PluginResult(PluginResult.Status.OK, (float) db);
        //                     result.setKeepCallback(true);
        //                     callbackContext.sendPluginResult(result);
        //                 }
        //             };
        //          that.timer.scheduleAtFixedRate(timerTask, 0, 1000);
        //       }
        //     }); 
         double db = 10;     
         PluginResult result = new PluginResult(PluginResult.Status.OK, (float) db);
         callbackContext.sendPluginResult(result);        
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
