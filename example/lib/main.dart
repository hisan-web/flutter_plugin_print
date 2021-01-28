import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_plugin_print/flutter_plugin_print.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  String _printConnectStatus = "No Connect";

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> connectPrint() async {
    bool result = await FlutterPluginPrint.connectPrint();
    if ( result ) {
      if (!mounted) return;
      setState(() {
        _printConnectStatus = "Connect Success";
      });
    }
  }

  Future<void> openPagerBox() async {
    await FlutterPluginPrint.openPageBox();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterPluginPrint.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: new Column(
            children: [
              Text('Running on: $_platformVersion\n, Print Connect: $_printConnectStatus\n'),
              RaisedButton(onPressed: openPagerBox, child: new Text("Open Pager Box"),)
            ],
          ),
        ),
      ),
    );
  }
}
