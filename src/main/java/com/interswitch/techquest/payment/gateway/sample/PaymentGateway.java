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

    public static String generateRef() {
        UUID nonce = UUID.randomUUID();
        String transRef = "ISW|API|JAM|" + nonce.toString().replaceAll("-", "");
        return transRef;
    }

    public static HashMap<String, String> doREST(String clientAccessToken, String resourceUrl, String httpMethod, String request) throws Exception {
        HashMap<String, String> response = new HashMap<String, String>();
        HashMap<String, String> securityHeaders = InterswitchAuth.generateInterswitchAuth(httpMethod, resourceUrl, Constants.CLIENT_ID, Constants.CLIENT_SECRET, null, Constants.SIGNATURE_METHOD);

        URL obj = new URL(resourceUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(httpMethod);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + clientAccessToken);
        con.setRequestProperty("Timestamp", securityHeaders.get("TIMESTAMP"));
        con.setRequestProperty("Nonce", securityHeaders.get("NONCE"));
        con.setRequestProperty("SignatureMethod", Constants.SIGNATURE_METHOD);
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
//            ex.printStackTrace();
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
//            throw ex;
        }

        String inputLine;
        StringBuffer responseBuffer = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            responseBuffer.append(inputLine);
        }
        in.close();
        JSONObject jSONObjectx = new JSONObject(responseBuffer.toString());
        System.out.println(jSONObjectx.toString(2));

        response.put(Constants.HTTP_CODE, String.valueOf(responseCode));
        response.put(Constants.RESPONSE_BODY, responseBuffer.toString());

        return response;
    }

    public static HashMap<String, String> doValidation(String clientAccessToken, String authData) throws Exception {
        String httpMethod = "POST";
        String transactionRef = generateRef(); // unique id to identify each request

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("authData", authData);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(clientAccessToken, Constants.VALIDATION_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doValidationAuthOTP(String clientAccessToken, String otp, String transactionRef) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(clientAccessToken, Constants.VALIDATION_AUTH_OTP_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doPurchase(String clientAccessToken, String authData, String amount) throws Exception {
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

        return doREST(clientAccessToken, Constants.PURCHASE_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doPurchaseAuthOTP(String clientAccessToken, String otp, String paymentId) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("paymentId", paymentId);

        String request = jSONObject.toString();

        return doREST(clientAccessToken, Constants.PURCHASE_AUTH_OTP_RESOURCE_URL, httpMethod, request);
    }

    public static HashMap<String, String> doTransactionQuery(String clientAccessToken, String amount, String transactionRef) throws Exception {
        String httpMethod = "GET";
        String transactionQueryUrl = Constants.PURCHASE_RESOURCE_URL + "?amount=" + amount + "&transactionRef=" + transactionRef;

        return doREST(clientAccessToken, transactionQueryUrl, httpMethod, null);
    }
}
