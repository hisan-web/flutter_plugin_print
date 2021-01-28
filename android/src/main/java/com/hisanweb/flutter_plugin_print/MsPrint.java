package com.hisanweb.flutter_plugin_print;

import android.annotation.SuppressLint;
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

public class MsPrint {

    private static final String TAG = "MsPrint";

    private static final String ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION";

    private Context mContext;

    public static UsbDriver mUsbDriver;

    //USB管理器:负责管理USB设备的类
    private UsbManager manager;

    //找到的USB设备
    private UsbDevice mUsbDevice;

    //代表USB设备的一个接口
    private UsbInterface mInterface;
    private UsbDeviceConnection mDeviceConnection;

    //代表一个接口的某个节点的类:写数据节点
    private UsbEndpoint usbEpOut;

    //代表一个接口的某个节点的类:读数据节点
    private UsbEndpoint usbEpIn;


    public MsPrint(Context context) {
        this.mContext = context;

        // Broadcast listen for new devices
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        mContext.registerReceiver(mUsbReceiver, filter);
    }

    /**
     * 链接usb打印机，0代表成功，1代表打开设备失败，2代表链接设备失败，3代表没有权限，4代表没有对应usb设备
     * @return
     */
    public int connectUsbPrint() {
        // 获取USB设备
        manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        //
        mUsbDriver = new UsbDriver(manager, mContext);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent);

        //获取到设备列表
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.e(TAG, "vid=" + device.getVendorId() + "---pid=" + device.getProductId());
            if (1305 == device.getVendorId() && 8211 == device.getProductId() || 1305 == device.getVendorId() && 8213 == device.getProductId()) {//找到指定设备
                mUsbDevice = device;
            }
        }
        if (mUsbDevice == null) {
            return 4;
        }
        //获取设备接口
        for (int i = 0; i < mUsbDevice.getInterfaceCount(); ) {
            // 一般来说一个设备都是一个接口，你可以通过getInterfaceCount()查看接口的个数
            // 这个接口上有两个端点，分别对应OUT 和 IN
            UsbInterface usbInterface = mUsbDevice.getInterface(i);
            mInterface = usbInterface;
            break;
        }

        if (mInterface != null) {
            //用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
            if (mInterface.getEndpoint(1) != null) {
                usbEpOut = mInterface.getEndpoint(1);
            }
            if (mInterface.getEndpoint(0) != null) {
                usbEpIn = mInterface.getEndpoint(0);
            }

            // 判断是否有权限
            if (manager.hasPermission(mUsbDevice)) {
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                mDeviceConnection = manager.openDevice(mUsbDevice);
                if (mDeviceConnection == null) {
                    return 1;
                }
                if (mDeviceConnection.claimInterface(mInterface, true)) {
                    // 找到设备接口
                    return 0;
                } else {
                    mDeviceConnection.close();
                    return 2;
                }
            } else {
                // 没有权限
                return 3;
            }
        } else {
            // 没有找到设备接口！
            return 4;
        }
    }

//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        @SuppressLint("NewAPI")
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    mContext.unregisterReceiver(mUsbReceiver);
//                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && mUsbDevice.equals(device)) {
//                        //TODO 授权成功，操作USB设备
//                    }else{
//                        //用户点击拒绝了
//                    }
//                }
//            }
//        }
//    };
}
