/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interswitch.techquest.payment.gateway.sample;

/**
 *
 * @author Abiola.Adebanjo
 */
public class Constants {

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
}
