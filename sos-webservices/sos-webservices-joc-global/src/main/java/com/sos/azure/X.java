package com.sos.azure;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class X {

    public static void main(String[] args) {

        try {

            SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss",Locale.ENGLISH);
            fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateString = fmt.format(Calendar.getInstance().getTime()) + " GMT";

            
            String ownerAccount = "admin";
            String ownerAccessKey = "Evbe9qo6uMo16Qoe4lWVbyWEMPf/KP0Trhmeb2znUfGqJgPTK+/EqlIb3cW8PhC/hJuVTQeXqrmj5f5NtAeemg==";
            String version = "2019-10-10";

            String container = "yade";
            String signature;
            signature = AzureSignature.createSignature("LIST", container, "blob", "yade", ownerAccessKey, dateString, version,-1);

            System.out.println(signature);

            URI uri = new URI("https://" + ownerAccount + ".blob.core.windows.net/" + container + "?restype=container&comp=list");
            String response = AzureSignature.invokeBlobRequest(uri, ownerAccount, signature, dateString, version, "get", "");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
