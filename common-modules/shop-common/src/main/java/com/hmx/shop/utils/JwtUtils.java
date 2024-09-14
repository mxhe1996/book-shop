package com.hmx.shop.utils;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.shop.vo.LoginUser;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {



    public static String generateToken(LoginUser user)  {
        Map<String,String> header = new HashMap<>();
        header.put("typ","JWT");
        header.put("alg","RSA");

        Map<String,Object> payload = new HashMap<>();
        payload.put("id",user.getId());
        payload.put("userName",user.getUserName());
        payload.put("email",user.getEmail());
        payload.put("roles",user.getRoles().toArray(new String[0]));
        payload.put("iat", LocalDateTime.now(ZoneId.systemDefault()));

        String header64 = new String(Base64Utils.encode(JSONObject.toJSONString(header).getBytes()));
        String payload64 = new String(Base64Utils.encode(JSONObject.toJSONString(payload).getBytes()));
        try{
            return encodeWithPublicKey(header64+"."+payload64);
        }catch (Exception e){
            return "error:"+e.getMessage();
        }

    }


    public static boolean verifyToken(String jwt) throws Exception {
        String[] splitJWT = jwt.split("\\.");
        String header = new String(Base64Utils.decode(splitJWT[0].getBytes()));
        String payload = new String(Base64Utils.decode(splitJWT[1].getBytes()));
        String sign = decodeWithPrivateKey(splitJWT[2]);
        String[] signOri = sign.split("\\.");
        String headerSign = new String(Base64Utils.decode(signOri[0].getBytes()));
        String payloadSign = new String(Base64Utils.decode(signOri[1].getBytes()));
//        System.out.printf("解码后的header:%s, signature解密后的header:%s",header,headerSign);
//        System.out.printf("解码后的payload:%s, signature解密后的payload:%s",payload,payloadSign);
        return header.equals(headerSign) && payload.equals(payloadSign);
    }

    /**
     *  make sure jwt is valid
     * @param jwt validated json web token
     * @return
     * @throws Exception
     */
    public static String parseTokenPayload(String jwt) throws Exception{
        String[] splitJWT = jwt.split("\\.");
        return new String(Base64Utils.decode(splitJWT[1].getBytes()));
    }



    private static String encodeWithPublicKey(String text) throws Exception {
        PublicKey publicKey = ConstantUtils.generatePublickey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] textBytes = text.getBytes();
        byte[] bytes = {};
        for(int i=0;i*117<textBytes.length;i++){
            byte[] subBytes = cipher.doFinal(textBytes, i * 117, Math.min(117, textBytes.length-i*117));
            bytes = ArrayUtils.addAll(bytes,subBytes);
        }
        return text+"."+Base64.getEncoder().encodeToString(bytes);
    }


    private static String decodeWithPrivateKey(String text) throws Exception{
        String[] infoArray = text.split("\\.");
        String encodedText = infoArray[infoArray.length-1];
        PrivateKey privateKey = ConstantUtils.generatePrivateKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encodedTextBytes = Base64.getDecoder().decode(encodedText); // 生成jwt时用了Base64编码，本处需要解码
        byte[] bytes = {};
        int inputLength = encodedTextBytes.length;
        int MAX_DECRYPT_BLOCK = 128;
        for(int i=0;i*MAX_DECRYPT_BLOCK<inputLength;i++){
            byte[] subBytes = cipher.doFinal(encodedTextBytes, i * MAX_DECRYPT_BLOCK, Math.min(MAX_DECRYPT_BLOCK, inputLength-i*MAX_DECRYPT_BLOCK));
            bytes = ArrayUtils.addAll(bytes,subBytes);
        }
        return new String(bytes);

    }


}
