package com.hisanweb.flutter_plugin_print;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hisanweb.flutter_plugin_print.msPrintSdk.UsbDriver;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import static com.hisanweb.flutter_plugin_print.msPrintSdk.UtilsTools.hexToByteArr;

/** FlutterPluginPrintPlugin */
public class FlutterPluginPrintPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private Context context;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_plugin_print");
    channel.setMethodCallHandler(this);

    context = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "connectPrint":
        connectPrint(result);
        break;
      case "sendPrint":
        sendPrint(result);
        break;
      case "openPagerBox":
        openPagerBox(result);
        break;
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  /**
   * 链接打印机
   * @param result
   */
  private void connectPrint(Result result) {
    MsPrint msPrint = new MsPrint(context);
    result.success(msPrint.connectUsbPrint());
  }

  /**
   * 打印
   */
  private void sendPrint(Result result) {
    result.success(true);
  }

  /**
   * 打开纸盒
   */
  private void openPagerBox(Result result) {
    byte[] bytes = hexToByteArr("1378");
    UsbDriver mUsbDriver = MsPrintManager.mUsbDriver;
    if (mUsbDriver!=null) {
      mUsbDriver.write(bytes);
      result.success(true);
    }
    result.error("-1","open error",null);
  }
}
