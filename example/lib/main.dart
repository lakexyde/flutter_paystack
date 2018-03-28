import 'package:flutter/material.dart';
import 'package:flutter_paystack/flutter_paystack.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Yay!';

  @override
  initState() {
    super.initState();
    
  }

  void _chargeCard(context) async{
    var cardOptions = {"cardNumber": "4084084084084081", "cvc": "408", "expiryMonth": "08", "expiryYear": "2019", "amountInKobo": 40000, "email": "payu@me.com"};

    FlutterPaystack.chargeCard(cardOptions).
    then((response) {
      print(response);
    })
    .catchError(print);
    
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('Plugin example app'),
          elevation: 1.0,
        ),
        body: new Center(
          child: new Column(
            children: <Widget>[
              new Text("Hello + $_platformVersion"),
              new RaisedButton(
                child: new Text("Charge the card"),
                onPressed: () =>_chargeCard(context),
              )
            ],
          ),
        ),
      ),
    );
  }
}
