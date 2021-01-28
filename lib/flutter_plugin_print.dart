
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPluginPrint {
  static const MethodChannel _channel = const MethodChannel('flutter_plugin_print');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 链接打印机
  static Future<bool> connectPrint() async {
    try {
      await _channel.invokeMethod('connectPrint');
      return true;
    } catch(e) {
      return false;
    }
  }

  /// 打开纸盒
  static Future<bool> openPageBox() async {
    try {
      await _channel.invokeMethod('openPageBox');
      return true;
    } catch(e) {
      return false;
    }
  }
}
