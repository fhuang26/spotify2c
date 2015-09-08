package com.example.fhuang.myproj0;

import org.json.JSONException;
import org.json.JSONObject;

public class TrackC {
    public AlbumC album;
    public String id;
    public String name;
    public String preview_url;
    TrackC(JSONObject obj) {
        try {
            id = obj.getString("id");
            name = obj.getString("name");
            preview_url = obj.getString("preview_url");
            JSONObject albumObj = obj.getJSONObject("album");
            album = new AlbumC( albumObj );
        } catch( JSONException e ) {
            e.printStackTrace();
        }
    }
}
