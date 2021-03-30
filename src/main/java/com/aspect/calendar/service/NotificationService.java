package com.aspect.calendar.service;

import com.aspect.calendar.entity.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    private final AppConfig config;
    private final Logger logger;

    @Autowired
    public NotificationService(AppConfig config) {
        this.config = config;
        this.logger = LoggerFactory.getLogger(NotificationService.class);
    }

    public void sendMessage(String message){
        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", message);
        parameters.put("DIALOG_ID", "chat" + config.bitrixNotificationChatId);
        parameters.put("SYSTEM", "Y");

        try{
            String url_str = config.bitrixRESTUrl + getParamsString(parameters);
            URL url = new URL(url_str);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            connection.setSSLSocketFactory(socketFactory);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            if(connection.getResponseCode() != 200){
                String response = getJsonResponse(connection.getErrorStream());
                BitrixJsonError error = new ObjectMapper().readValue(response, BitrixJsonError.class);
                if(error != null) throw new IOException(error.error_description);
            }

        } catch (IOException ex){
            logger.error("Failed to send bitrix message",  ex);
        }
    }

    private static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        result.append(("?"));

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        return result.toString();
    }

    private String getJsonResponse(InputStream errorStream) throws IOException{
        StringBuilder res = new StringBuilder();
        InputStreamReader in = new InputStreamReader(errorStream);
        BufferedReader br = new BufferedReader(in);
        String output;
        while ((output = br.readLine()) != null) {
            res.append(output);
        }
        return res.toString();
    }

    private static class BitrixJsonError{
        private String error;
        private String error_description;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getError_description() {
            return error_description;
        }

        public void setError_description(String error_description) {
            this.error_description = error_description;
        }
    }
}
