import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPaystack {
  static const MethodChannel _channel =
      const MethodChannel('flutter_paystack');

  static Future<Map<String, String>> chargeCard(Map<String, dynamic> chargeOptions){
      return _channel.invokeMethod('chargeCard', chargeOptions);
  }

  static Future<Map<String, String>> chargeCardWithAccessCode(Map<String, dynamic> chargeOptions){
      return _channel.invokeMethod('chargeCardWithAccessCode', chargeOptions);
  }
      
    
}
