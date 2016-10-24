package com.interswitch.techquest.payment.gateway.sample;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interswitch.techquest.secure.utils.InterswitchAuth;
import com.interswitch.techquest.secure.utils.TransactionSecurity;

public class App {

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

    public static void main(String... args) throws Exception {
        try {

            System.out.println("To leave a field empty, please press enter");
            Scanner scanner = new Scanner(System.in);
            String quitFlag = "";
            String CLIENT_ACCESS_TOKEN = InterswitchAuth.getAccessToken(CLIENT_ID, CLIENT_SECRET, PASSPORT_RESOURCE_URL);

            while (quitFlag != null && !quitFlag.equalsIgnoreCase("q")) {
                System.out.println("");
                System.out.println("===================================");
                System.out.println("1.Validations 2.Purchases 3.Status");
                String menuItem = "1";
                scanner = new Scanner(System.in);
                menuItem = scanner.nextLine();

                System.out.println("");
                System.out.println("===================================");
                System.out.println("Enter your PAN: ");
                String pan = scanner.nextLine();

                System.out.println("Enter PAN Expiry Date (Format YYMM e.g. 5004 for Apr, 2050): ");
                String expiryDate = scanner.nextLine();

                System.out.println("Enter CVV. Press enter to ignore: ");
                String cvv = scanner.nextLine();

                System.out.println("Enter PIN. Press enter to ignore: ");
                String pin = scanner.nextLine();

                String certFilePath = "C:\\Users\\abiola.adebanjo\\Documents\\isw-api-jam\\paymentgateway.crt";
                String authData = TransactionSecurity.generateAuthData("1", pan, pin, expiryDate, cvv, certFilePath);

                if ("1".equals(menuItem)) {

                    HashMap<String, String> validateResponse = doValidation(VALIDATION_RESOURCE_URL, CLIENT_ACCESS_TOKEN, authData);

                    int httpResponseCode = Integer.parseInt(validateResponse.get(HTTP_CODE));
                    switch (httpResponseCode) {
                        case 200:
                            //
                            break;
                        case 202:
                            //
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> responseBody = new HashMap<String, Object>();
                            responseBody = mapper.readValue(validateResponse.get(RESPONSE_BODY), new TypeReference<Map<String, String>>() {
                            });
                            if (responseBody != null && responseBody.containsKey("responseCode")) {
                                String responseCode = responseBody.get("responseCode").toString();
                                if (responseCode.equalsIgnoreCase("T0")) {
                                    System.out.println("Enter your OTP e.g. 958274");
                                    String otp = scanner.nextLine();

                                    String transactionRef = responseBody.get("transactionRef").toString();
                                    doValidationAuthOTP(VALIDATION_AUTH_OTP_RESOURCE_URL, CLIENT_ACCESS_TOKEN, otp, transactionRef);
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    System.out.println();
                    System.out.println("===================================");
                    System.out.println("Press any key to contiue, Q to quit");
                    quitFlag = scanner.nextLine();
                } else if ("2".equals(menuItem)) {
                    System.out.println("Enter amount in major denomination e.g. 100.00");
                    String amount = scanner.nextLine();

                    HashMap<String, String> purchaseResponse = doPurchase(PURCHASE_RESOURCE_URL, CLIENT_ACCESS_TOKEN, authData, amount);
                    int httpResponseCode = Integer.parseInt(purchaseResponse.get(HTTP_CODE));
                    switch (httpResponseCode) {
                        case 200:
                            //
                            break;
                        case 202:
                            //
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> responseBody = new HashMap<String, Object>();
                            responseBody = mapper.readValue(purchaseResponse.get(RESPONSE_BODY), new TypeReference<Map<String, String>>() {
                            });
                            if (responseBody != null && responseBody.containsKey("responseCode")) {
                                String responseCode = responseBody.get("responseCode").toString();
                                if (responseCode.equalsIgnoreCase("T0")) {
                                    System.out.println("Enter your OTP e.g. 958274");
                                    String otp = scanner.nextLine();

                                    String paymentId = responseBody.get("paymentId").toString();
                                    doPurchaseAuthOTP(PURCHASE_AUTH_OTP_RESOURCE_URL, CLIENT_ACCESS_TOKEN, otp, paymentId);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else if ("3".equals(menuItem)) {
                    System.out.println("Amount: ");
                    String amount = scanner.nextLine();
                    System.out.println("TransactionRef : ");
                    String transactionRef = scanner.nextLine();

                    doTransactionQuery(PURCHASE_RESOURCE_URL, CLIENT_ACCESS_TOKEN, amount, transactionRef);
                }
            }
            scanner.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Thread.sleep(50000);
        }
    }

    public static String generateRef() {
        UUID nonce = UUID.randomUUID();
        return nonce.toString().replaceAll("-", "");
    }

    public static HashMap<String, String> doREST(String resourceUrl, String httpMethod, String clientAccessToken, String request) throws Exception {
        HashMap<String, String> response = new HashMap<String, String>();
        HashMap<String, String> securityHeaders = InterswitchAuth.generateInterswitchAuth(httpMethod, resourceUrl, CLIENT_ID, CLIENT_SECRET, null, SIGNATURE_METHOD);

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

    public static HashMap<String, String> doValidation(String resourceUrl, String clientAccessToken, String authData) throws Exception {
        String httpMethod = "POST";
        String transactionRef = "ISW|API|JAM|" + generateRef(); // unique id to identify each request

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("authData", authData);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(resourceUrl, httpMethod, clientAccessToken, request);
    }

    public static HashMap<String, String> doValidationAuthOTP(String resourceUrl, String clientAccessToken, String otp, String transactionRef) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(resourceUrl, httpMethod, clientAccessToken, request);
    }

    public static HashMap<String, String> doPurchase(String resourceUrl, String clientAccessToken, String authData, String amount) throws Exception {
        String httpMethod = "POST";
        String transactionRef = "ISW|API|JAM|" + generateRef();
        String customerId = "api-jam@interswitchgroup.com";
        String currency = "NGN"; // Currency in 3 letter ISO alphabetic code

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("customerId", customerId);
        jSONObject.put("amount", amount);
        jSONObject.put("currency", currency);
        jSONObject.put("authData", authData);
        jSONObject.put("transactionRef", transactionRef);

        String request = jSONObject.toString();

        return doREST(resourceUrl, httpMethod, clientAccessToken, request);
    }

    public static HashMap<String, String> doPurchaseAuthOTP(String resourceUrl, String clientAccessToken, String otp, String paymentId) throws Exception {
        String httpMethod = "POST";

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("otp", otp);
        jSONObject.put("paymentId", paymentId);

        String request = jSONObject.toString();

        return doREST(resourceUrl, httpMethod, clientAccessToken, request);
    }

    public static HashMap<String, String> doTransactionQuery(String resourceUrl, String clientAccessToken, String amount, String transactionRef) throws Exception {
        String httpMethod = "GET";

        return doREST(resourceUrl, httpMethod, clientAccessToken, null);
    }
}
