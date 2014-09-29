package com.ucla_ieee.app.util;

import android.text.TextUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class to handle json actions
 */
public class JsonServerUtil {

    public String getStringFromServerResponse(HttpEntity entity) {
        try {
            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                StringBuilder builder = new StringBuilder();

                String st;
                while((st = reader.readLine()) != null) {
                    builder.append(st).append("\n");
                }

                instream.close();
                return builder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public JsonArray getJsonArrayFromString(String string) {
        if (TextUtils.isEmpty(string) || string == null) {
            return null;
        }
        try {
            JsonParser parser = new JsonParser();
            return (JsonArray) parser.parse(string);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public JsonObject getJsonObjectFromString(String string) {
        if (TextUtils.isEmpty(string) || string == null) {
            return null;
        }
        try {
            JsonParser parser = new JsonParser();
            return (JsonObject) parser.parse(string);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
