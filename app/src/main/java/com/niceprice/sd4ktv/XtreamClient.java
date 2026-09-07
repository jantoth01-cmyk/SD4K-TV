package com.niceprice.sd4ktv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class XtreamClient {
    private final String server;
    private final String username;
    private final String password;

    public XtreamClient(String server, String username, String password) {
        String s = server.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        this.server = s;
        this.username = username.trim();
        this.password = password.trim();
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String api(String action) {
        String base = server + "/player_api.php?username=" + enc(username)
                + "&password=" + enc(password);
        return action == null ? base : base + "&action=" + enc(action);
    }

    private String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "SD4K-TV/0.1");
        c.setRequestProperty("Accept", "application/json");
        int code = c.getResponseCode();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code);
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) b.append(line);
            return b.toString();
        } finally {
            c.disconnect();
        }
    }

    public void validate() throws Exception {
        JSONObject root = new JSONObject(get(api(null)));
        JSONObject ui = root.optJSONObject("user_info");
        if (ui == null) throw new Exception("Hibás Xtream válasz");
        String auth = ui.optString("auth", "0");
        String status = ui.optString("status", "");
        if (!"1".equals(auth) || ("Disabled".equalsIgnoreCase(status) || "Expired".equalsIgnoreCase(status))) {
            throw new Exception("A belépés sikertelen vagy az előfizetés nem aktív");
        }
    }

    public List<Channel> getLiveStreams() throws Exception {
        JSONArray a = new JSONArray(get(api("get_live_streams")));
        List<Channel> out = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            int id = o.optInt("stream_id", -1);
            String name = o.optString("name", "Csatorna " + id);
            String cat = o.optString("category_id", "");
            if (id > 0) out.add(new Channel(id, name, cat));
        }
        return out;
    }

    public String streamUrl(int streamId) {
        return server + "/live/" + enc(username) + "/" + enc(password) + "/" + streamId + ".ts";
    }
}
