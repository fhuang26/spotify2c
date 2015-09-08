package com.example.fhuang.myproj0;

import android.graphics.Bitmap;
import android.media.MediaPlayer;

import java.util.ArrayList;

public class ArtistTracksC {
    public static ArrayList<TrackC> ltATrack = null; // list of artist tracks
    // This will help retain list of artists tracks. When rotation (configuration change)
    // occurs on TracksActivity, track list ( ltATrack ) in TracksActivity can be retrieved
    // from here. It won't be needed to send async requests on internet to fetch track list
    // again. So this helps improve performance when rotation occurs during TracksActivity.

    // This will be displayed in subTitle in action bar of TracksActivity.
    public static PlayerActivity playerActivity = null;
    public static MediaPlayerDialogFragment dgFragPlayer = null;
    public static Bitmap bm_album = null;
    public static String artist_name;
    public static String artistId;
    public static int currPos;
    public static int duration; // of a track in ms
    public static MediaPlayer mediaPlayer = null;
    private static boolean playing = true;
    public static synchronized void set_playing(boolean flag) {
        playing = flag;
    }
    public static synchronized boolean get_playing() { return playing; }

    public static Integer progress = new Integer(-1);
}
