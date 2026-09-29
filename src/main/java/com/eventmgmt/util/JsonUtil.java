package com.eventmgmt.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for JSON serialization, deserialization, and HTTP JSON response formatting.
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromReader(BufferedReader reader, Class<T> classOfT) {
        return GSON.fromJson(reader, classOfT);
    }

    /**
     * Writes a standard JSON success response.
     */
    public static void sendSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        if (message != null) {
            map.put("message", message);
        }
        if (data != null) {
            map.put("data", data);
        }

        PrintWriter out = response.getWriter();
        out.write(GSON.toJson(map));
        out.flush();
    }

    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendSuccess(response, null, data);
    }

    /**
     * Writes a standard JSON error response.
     */
    public static void sendError(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(statusCode);

        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", errorMessage);

        PrintWriter out = response.getWriter();
        out.write(GSON.toJson(map));
        out.flush();
    }
}
