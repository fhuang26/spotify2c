package com.example.fhuang.myproj0;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumC {
    public String imageUrl;
    public String id;
    public String name;
    AlbumC(JSONObject obj) {
        try {
            id = obj.getString("id");
            name = obj.getString("name");
            String url = null;
            JSONArray imageArr = obj.getJSONArray("images");
            for (int j = 0; j < imageArr.length(); ++j) {
                JSONObject imageObj = imageArr.getJSONObject(j);
                String ht = imageObj.getString("height");
                String wid = imageObj.getString("width");
                int ht2 = Integer.parseInt(ht);
                int wid2 = Integer.parseInt(wid);
                if (Math.abs(ht2 - 200) <= 140 && Math.abs(wid2 - 200) <= 140) {
                    url = imageObj.getString("url");
                    break;
                }
            }
            if (url == null) {
                url = imageArr.getJSONObject(0).getString("url");
            }
            imageUrl = url;
        } catch( JSONException e ) {
            e.printStackTrace();
        }
    }
}
