package com.hisanweb.flutter_plugin_print;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

public class MsPrint {

    private static final String TAG = "MsPrint";

    private static final int VENDOR_ID = 1035;

    private static final int PRODUCT_ID = 8211;

    private Context context;

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
        this.context = context;
    }

    /**
     * 链接usb打印机，0代表成功，1代表打开设备失败，2代表链接设备失败，3代表没有权限，4代表没有对应usb设备
     * @return
     */
    public int connectUsbPrint() {
        // 获取USB设备
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        //获取到设备列表
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.e(TAG, "vid=" + device.getVendorId() + "---pid=" + device.getProductId());
            if (VENDOR_ID == device.getVendorId() && PRODUCT_ID == device.getProductId()) {//找到指定设备
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
}
