package com.hisanweb.flutter_plugin_print;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hisanweb.flutter_plugin_print.msPrintSdk.UsbDriver;

import java.util.HashMap;
import java.util.Iterator;

public class MsPrintManager {

    private final String TAG = "MsPrintManager";
    
    private static final int VENDOR_ID = 1035;

    private static final int PRODUCT_ID = 8211;

    private static final String ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION";

    public static UsbDriver mUsbDriver;

    // 找到的USB设备，即当前打印机
    private UsbDevice mUsbDevice = null;

    // 代表USB设备的一个接口
    private UsbInterface mInterface;
    private UsbDeviceConnection mDeviceConnection;

    // 代表一个接口的某个节点的类:写数据节点
    private UsbEndpoint usbEpOut;

    // 代表一个接口的某个节点的类:读数据节点
    private UsbEndpoint usbEpIn;
    
    private Context context;

    public MsPrintManager(Context appContext) {
        context = appContext;
        mUsbDriver = new UsbDriver((UsbManager) context.getSystemService(Context.USB_SERVICE), context);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent);
        // Broadcast listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
    }
    
    // 链接打印机
    public int connectPrint() {
        int result = 0;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device != null) {
                if (device.getVendorId()==VENDOR_ID && device.getProductId()==PRODUCT_ID) {
                    mUsbDevice = device;
                }
            }
        }
        // 获取设备接口
        if (mUsbDevice != null) {
            for (int i = 0; i < mUsbDevice.getInterfaceCount();) {
                // 一般来说一个设备都是一个接口，你可以通过getInterfaceCount()查看接口的个数
                UsbInterface usbInterface = mUsbDevice.getInterface(i);
                mInterface = usbInterface;
                break;
            }
        }
        // 用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
        if (mInterface.getEndpoint(1) != null) {
            usbEpOut = mInterface.getEndpoint(1);
        }
        if (mInterface.getEndpoint(0) != null) {
            usbEpIn = mInterface.getEndpoint(0);
        }
        // 判断权限
        if (mInterface != null) {
            if (usbManager.hasPermission(mUsbDevice)) {
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                mDeviceConnection = usbManager.openDevice(mUsbDevice);
                if (mDeviceConnection != null) {
                    result = -1;
                    if (!mDeviceConnection.claimInterface(mInterface, true)) {
                        mDeviceConnection.close();
                        result = 0;
                    }
                }
            } else {
                result = 403;
                // 没有权限
            }
        } else {
            result = 404;
            // 没有设备
        }
        return result;
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if ((device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID)
                        || (device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID)) {
                    mUsbDriver.closeUsbDevice(device);
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if ((device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID)
                            || (device.getProductId() == PRODUCT_ID && device.getVendorId() == VENDOR_ID)) {
                        //赋权限以后的操作
                    }
                } else {
//                    Toast.makeText(MainActivity.this, "permission denied for device",
                    Log.i(TAG,"permission denied for device");
                }
            }
        }
    };
}
