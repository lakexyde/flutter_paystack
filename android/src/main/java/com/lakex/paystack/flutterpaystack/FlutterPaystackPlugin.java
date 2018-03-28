package com.lakex.paystack.flutterpaystack;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Patterns;

import java.util.HashMap;
import java.util.Map;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import co.paystack.android.Transaction;

/**
 * FlutterPaystackPlugin
 */
public class FlutterPaystackPlugin implements MethodCallHandler {
  private final Registrar registrar;
  private final MethodChannel channel;
  private Result result;
  protected Card card;
	private Charge charge;
  private Transaction transaction;
  public static final String TAG = "FlutterPaystackPlugin";
  private Map chargeOptions;
  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_paystack");
    channel.setMethodCallHandler(new FlutterPaystackPlugin(registrar, channel));
  }

  private FlutterPaystackPlugin(Registrar registrar, MethodChannel channel){
    this.registrar = registrar;
    this.channel = channel;
    PaystackSdk.initialize(registrar.context());
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("chargeCard")){
      if (!(call.arguments instanceof Map)) {
        throw new IllegalArgumentException("Plugin not passing a map as parameter: " + call.arguments);
      }
      Map chargeParams = (Map<String, Object>) call.arguments;
      chargeCard(chargeParams, result);
    } else if(call.method.equals("chargeCardWithAccessCode")){
      if (!(call.arguments instanceof Map)) {
        throw new IllegalArgumentException("Plugin not passing a map as parameter: " + call.arguments);
      }
      Map chargeParams = (Map<String, Object>) call.arguments;
      chargeCardWithAccessCode(chargeParams, result);

    } else {
      result.notImplemented();
    }
  }

  protected void handleError(String errorCode, String errorMsg){
    if(this.result != null){
      Map err = new HashMap<String, String>();
      err.put("error", errorMsg);
      this.result.error(errorCode, errorMsg, err);
      this.result = null;
    }
  }

  protected void handleSuccess(Map data){
    if(this.result != null){
      this.result.success(data);
      this.result = null;
    }
  }

  private void chargeCardWithAccessCode(Map chargeOptions, Result result){
    this.chargeOptions = null;
    this.result = result;

    this.chargeOptions = chargeOptions;

    validateAccessCodeTransaction();
    if (card != null && card.isValid()) {
      try {
        createTransaction();
      } catch(Exception error) {
        handleError("E_CHARGE_ERROR", error.getMessage());
      }
    }
  }

  private void chargeCard(Map chargeOptions, Result result){
    this.chargeOptions = null;
    this.result = result;

    this.chargeOptions = chargeOptions;

    validateFullTransaction();
    if (card != null && card.isValid()) {
      try {
        createTransaction();
      } catch(Exception error) {
        handleError("E_CHARGE_ERROR", error.getMessage());
      }
    }
  }

  protected void validateCard(String cardNumber, String expiryMonth, String expiryYear, String cvc){

    if (isEmpty(cardNumber)){
      handleError("E_INVALID_NUMBER","Empty card number.");
      return;
    }

    //build card object with ONLY the number, update the other fields later
    card = new Card.Builder(cardNumber, 0, 0, "").build();

    if (!card.validNumber()){
      handleError("E_INVALID_NUMBER","Invalid card number.");
      return;
    }

    //validate cvc
    if (isEmpty(cvc)){
      handleError("E_INVALID_CVC", "Empty cvc code.");
      return;
    }

    //update the cvc field of the card
    card.setCvc(cvc);

    //check that it's valid
    if (!card.validCVC()) {
      handleError("E_INVALID_CVC", "Invalid cvc code.");
      return;
    }

    int month = -1;
    try{
      month = Integer.parseInt(expiryMonth);
    } catch(Exception ignored){
    }

    //validate expiry month;
    if (month < 1) {
      handleError("E_INVALID_MONTH", "Invalid expiration month.");
      return;
    }

    //update the expiryMonth field of the card
    card.setExpiryMonth(month);

    int year = -1;
    try{
      year = Integer.parseInt(expiryYear);
    } catch(Exception ignored){
    }

    //validate expiry year;
    if (year < 1) {
      handleError("E_INVALID_YEAR", "Invalid expiration year.");
      return;
    }

    //update the expiryYear field of the card
    card.setExpiryYear(year);

    //validate expiry
    if (!card.validExpiryDate()) {
      handleError("E_INVALID_DATE", "Invalid expiration date.");
    }

  }

  private void validateAccessCodeTransaction() {
    String cardNumber = (String)chargeOptions.get("cardNumber");
    String expiryMonth = (String)chargeOptions.get("expiryMonth");
    String expiryYear = (String)chargeOptions.get("expiryYear");
    String cvc = (String)chargeOptions.get("cvc");

    validateCard(cardNumber, expiryMonth, expiryYear, cvc);

    charge = new Charge();
    charge.setCard(card);

    if (hasStringKey("accessCode")) {
      charge.setPlan((String)chargeOptions.get("accessCode"));
    }
  }

  private void validateFullTransaction(){

    String cardNumber = (String)chargeOptions.get("cardNumber");
    String expiryMonth = (String)chargeOptions.get("expiryMonth");
    String expiryYear = (String)chargeOptions.get("expiryYear");
    String cvc = (String)chargeOptions.get("cvc");
    String email = (String)chargeOptions.get("email");
    int amountInKobo = (int)chargeOptions.get("amountInKobo");

    validateCard(cardNumber, expiryMonth, expiryYear, cvc);

    charge = new Charge();
    charge.setCard(card);

    if (isEmpty(email)) {
      handleError("E_INVALID_EMAIL","Email cannot be empty");
      return;
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      handleError("E_INVALID_EMAIL","Invalid email.");
      return;
    }

    charge.setEmail(email);

    if (amountInKobo < 1) {
      handleError("E_INVALID_AMOUNT","Invalid amount");
      return;
    }

    charge.setAmount(amountInKobo);

    if (hasStringKey("currency")) {
      charge.setCurrency((String)chargeOptions.get("currency"));
    }

    if (hasStringKey("plan")) {
      charge.setPlan((String)chargeOptions.get("plan"));
    }

    if (hasStringKey("subAccount")) {
      charge.setSubaccount((String)chargeOptions.get("subAccount"));

      if (hasStringKey("bearer") && (String)chargeOptions.get("bearer") == "subaccount") {
        charge.setBearer(Charge.Bearer.subaccount);
      }

      if (hasStringKey("bearer") && (String)chargeOptions.get("bearer")== "account") {
        charge.setBearer(Charge.Bearer.account);
      }

      // if (transactionCharge > 0) {
      //   charge.setTransactionCharge(transactionCharge);
      // }
    }

    if (hasStringKey("reference")) {
      charge.setReference((String)chargeOptions.get("reference"));
    }
  }

  private void createTransaction() {
    transaction = null;
    PaystackSdk.chargeCard(registrar.activity(), charge, new Paystack.TransactionCallback() {
      @Override
      public void onSuccess(Transaction transaction) {
        // This is called only after transaction is successful
        FlutterPaystackPlugin.this.transaction = transaction;
        Map res = new HashMap<String,Object>();
        res.put("reference", transaction.getReference());
        handleSuccess(res);
      }

      @Override
      public void beforeValidate(Transaction transaction) {
        // This is called only before requesting OTP
        // Save reference so you may send to server if
        // error occurs with OTP
        FlutterPaystackPlugin.this.transaction = transaction;
      }

      @Override
      public void onError(Throwable error, Transaction transaction) {
        if (FlutterPaystackPlugin.this.transaction == null) {
          handleError("E_NO_TRANSACTION", error.getMessage());
        } else{
          handleError("E_TRANSACTION_ERROR", transaction.getReference() + " concluded with error: " + error.getMessage());
        }
      }
    });
  }

  private boolean isEmpty(String s){
    return s == null || s.length() < 1;
  }

  private boolean hasStringKey(String key){
    return chargeOptions.containsKey(key) && !isEmpty((String)chargeOptions.get(key));
  }
}
