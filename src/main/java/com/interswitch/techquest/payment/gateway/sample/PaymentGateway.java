/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interswitch.techquest.payment.gateway.sample;

import com.interswitch.techquest.secure.utils.InterswitchAuth;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author Abiola.Adebanjo
 */
public class PaymentGateway {

    final static String CLIENT_ID = "IKIA67A8FBB81191FC4F1226098245E9541711B3E959";
    final static String CLIENT_SECRET = "FQ+X6B28Y/HJZdsDa1SsbKI23W+pIOLcyxBhGgb8Q9U=";

    public static final String PASSPORT_RESOURCE_URL = "http://172.26.40.117:6060/passport/oauth/token";

    public static final String PURCHASE_RESOURCE_URL = "http://172.26.40.131:19081/api/v2/purchases";
    public static final String PURCHASE_AUTH_OTP_RESOURCE_URL = "http://172.26.40.131:19081/api/v2/purchases/otps/auths";
    public static final String VALIDATION_RESOURCE_URL = "http://172.26.40.131:19081/api/v2/purchases/validations";
    public static final String VALIDATION_AUTH_OTP_RESOURCE_URL = "http://172.26.40.131:19081/api/v2/purchases/validations/otps/auths";

    public static final String SIGNATURE_METHOD = "SHA-256";
    public static final String HTTP_CODE = "HTTP_CODE";
    public static final String RESPONSE_BODY = "RESPONSE_BODY";

    public static String generateRef() {
        UUID nonce = UUID.randomUUID();
        String transRef = "ISW|API|JAM|" + nonce.toString().replaceAll("-", "");
        return transRef;
    }

    public static HashMap<String, String> doREST(String resourceUrl, String httpMethod, String request) throws Exception {
        HashMap<String, String> response = new HashMap<String, String>();
        HashMap<String, String> securityHeaders = InterswitchAuth.generateInterswitchAuth(httpMethod, resourceUrl, CLIENT_ID, CLIENT_SECRET, null, SIGNATURE_METHOD);
        String clientAccessToken = InterswitchAuth.getAccessToken(CLIENT_ID, CLIENT_SECRET, PASSPORT_RESOURCE_URL);

        URL obj = new URL(resourceUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(httpMethod);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + clientAccessToken);
        con.setRequestProperty("Timestamp", securityHeaders.get("TIMESTAMP"));
        con.setRequestProperty("Nonce", securityHeaders.get("NONCE"));
        con.setRequestProperty("SignatureMethod", SIGNATURE_METHOD);
        con.setRequestProperty("Signature", securityHeaders.get("SIGNATURE"));

        if (request != null) {
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(request);
            wr.flush();
            wr.close();
        }

        int responseCode = con.getResponseCode();
        System.out.println("\nSending " + httpMethod + " request to URL : " + resourceUrl);
        System.out.println("Post parameters : " + request);
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (Exception ex) {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuffer responseBuffer = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            responseBuffer.append(inputLine);
        }
        in.close();
        JSONObject jSONObjectx = new JSONObject(responseBuffer.toString());
        System.out.println(jSONObjectx.toString(2));

        response.put(HTTP_CODE, String.valueOf(responseCode));
        response.put(RESPONSE_BODY, responseBuffer.toString());

        return response;
    }

    public static HashMap<String, String> doValidation(String authData) throws Exception {
        String httpMethod = "POST";
        String transactionRef = generateRef(); // unique id to identify each request

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("authData", authData);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(VALIDATION_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doValidationAuthOTP(String otp, String transactionRef) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(VALIDATION_AUTH_OTP_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doPurchase(String authData, String amount) throws Exception {
        String httpMethod = "POST";
        String transactionRef = generateRef();
        String customerId = "api-jam@interswitchgroup.com";
        String currency = "NGN"; // Currency in 3 letter ISO alphabetic code

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("customerId", customerId);
        jSONObject.put("amount", amount);
        jSONObject.put("currency", currency);
        jSONObject.put("authData", authData);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(PURCHASE_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doPurchaseAuthOTP(String otp, String paymentId) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("paymentId", paymentId);

        String request = jSONObject.toString();

        return doREST(PURCHASE_AUTH_OTP_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doTransactionQuery(String amount, String transactionRef) throws Exception {
        String httpMethod = "GET";
        String transactionQueryUrl = PURCHASE_RESOURCE_URL + "?amount=" + amount + "&ransactionRef=" + transactionRef;
        
        return doREST(transactionQueryUrl, httpMethod, null);
    }
}
