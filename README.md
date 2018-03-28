# flutter_paystack

A PayStack SDK wrapper for flutter apps.

#### Only supports Android for now.

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

- Add  ```flutter_paystack``` as a dependency in your ```pubspec.yaml``` file.

- For Android, add the following tag in your android/app/src/main/AndroidManifest.xml file within the <application></application> tags:
```
<meta-data android:name="co.paystack.android.PublicKey" android:value="INSERT-PUBLIC-KEY-HERE"/>
```

# Usage
## Charging a Card
- Import ```paystack_flutter```
```
import 'package:flutter_paystack/flutter_paystack.dart';
```
- Call the ```chargeCard``` method with Card parameters
```
Map<string, dynamic> cardOptions ={
    "cardNumber": "4084084084084081", "cvc": "408", 
    "expiryMonth": "08", 
    "expiryYear": "2019", 
    "amountInKobo": 2000, 
    "email": "payu@me.com"
};

FlutterPaystack.chargeCard(cardOptions)
    .then((res) => {
        // Do something with the response
        print(res);
    }).catchError(print);
```
- Response object
```
{
    reference: "trx_1k2o600w"
}
```
## Charging a Card with Access Code
- Import ```paystack_flutter```
```
import 'package:flutter_paystack/flutter_paystack.dart';
```
- Call the ```chargeCardWithAccessCode``` method with Card parameters
```
Map<string, dynamic> cardOptions ={
    "cardNumber": "4084084084084081", "cvc": "408", 
    "expiryMonth": "08", 
    "expiryYear": "2019", 
    "accessCode": "2p3j42th639duy4"
};

FlutterPaystack.chargeCard(cardOptions)
    .then((res) => {
        // Do something with the response
        print(res);
    }).catchError(print);
```
- Response object
```
{
    reference: "trx_1k2o600w"
}
```
## Verifying a Charge
Verify a charge by calling Paystack's [REST](https://api.paystack.co/transaction/verify) API with the ```reference``` obtained above. An ```authorization_code``` will be returned once the card has been charged successfully. Learn more about that [here](https://developers.paystack.co/docs/verify-transaction).
### Parameter
- reference - the transaction reference (required)
### Example
```
$ curl https://api.paystack.co/transaction/verify/trx_1k2o600w \
   -H "Authorization: Bearer SECRET_KEY" \
   -H "Content-Type: application/json" \
   -X GET
```
# Credit
This packages uses the [PayStack Android SDK](https://github.com/PaystackHQ/paystack-android)

# TODO
- Improve documentation
- Support iOS
- Improve example

# License
MIT
