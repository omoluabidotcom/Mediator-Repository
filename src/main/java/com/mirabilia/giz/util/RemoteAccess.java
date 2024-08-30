package com.mirabilia.giz.util;

import com.mirabilia.giz.MiddlewareMappingAdapter;
import org.apache.commons.codec.binary.Base64;
import org.openhim.mediator.engine.MediatorConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteAccess {

    private final MediatorConfig config;

    public RemoteAccess(MediatorConfig config) {
        this.config = config;
    }

    // retireve your data using the retrieveRemoteData(url) method here and forward to a custom class where you perform your cleaning and manipulation
    public String retrieveRemoteData(String pg_url) {

        HttpURLConnection urlConnection = null;

        String name = (String) config.getDynamicConfig().get("originator_user");
        String password = (String) config.getDynamicConfig().get("originator_passwd");
        StringBuilder sb = new StringBuilder();

        String authString = name + ":" + password;
        if (name.isEmpty()) {
            return "";
        }

        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        try {
            System.out.println(pg_url + " urlllllllllllllllllll");
            URL url = new URL(pg_url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            int HttpResult = urlConnection.getResponseCode();

            if (HttpResult == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                if (sb.toString().indexOf("success") >= 1) {
                    return sb.toString();
                }
                if (sb.toString().indexOf("warning") >= 1) {
                    return sb.toString();
                }
                if ((sb.toString().indexOf("warning") >= 1) || (sb.toString().indexOf("success") >= 1)) {
                    return sb.toString();
                }

            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), StandardCharsets.UTF_8));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println(sb.toString());
                return sb.toString();
            }

        } catch (IOException ex) {
            Logger.getLogger(MiddlewareMappingAdapter.class.getName()).log(Level.SEVERE, "error occured in resolver", ex);
        }

        return sb.toString();
    }
}
