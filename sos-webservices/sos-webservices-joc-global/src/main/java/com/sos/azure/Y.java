package com.sos.azure;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Y {

    public void GetFileSAS(){

        String accountName = "yade";
        String key = "Evbe9qo6uMo16Qoe4lWVbyWEMPf/KP0Trhmeb2znUfGqJgPTK+/EqlIb3cW8PhC/hJuVTQeXqrmj5f5NtAeemg==";
                 
        String resourceUrl = "https://"+accountName+".file.core.windows.net/fileShare/fileName";

        String start = "startTime";
        String expiry = "expiry";
        String azureApiVersion = "2021-12-02";

        String stringToSign = accountName + "\n" +
                    "r\n" +
                    "f\n" +
                    "o\n" +
                    start + "\n" +
                    expiry + "\n" +
                    "\n" +
                    "https\n" +
                    azureApiVersion+"\n";

        String signature = AzureSignature.getHMAC256(key, stringToSign);

        try{

            String sasToken = "sv=" + azureApiVersion +
                "&ss=f" +
                "&srt=o" +
                "&sp=r" +
                "&se=" +URLEncoder.encode(expiry, "UTF-8") +
                "&st=" + URLEncoder.encode(start, "UTF-8") +
                "&spr=https" +
                "&sig=" + URLEncoder.encode(signature, "UTF-8");

        System.out.println(resourceUrl+"?"+sasToken);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

  
    public static void main(String[] args) {
        Y y = new Y();
        y.GetFileSAS();
        
        // TODO Auto-generated method stub

    }

}
