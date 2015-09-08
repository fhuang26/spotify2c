package com.example.fhuang.myproj0;

import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

public class ArtistPhotosC {
    public static ArrayList<ArtistPhoto> ltAPhoto = null; // list of artist photos
    // This will help retain list of artists with photos. When rotation (configuration change)
    // occurs on SpotifyActivity, or coming back from TracksActivity (popular track display),
    // artist list ( ltAPhoto ) in SpotifyActivity can be retrieved from here. It won't be
    // needed to send async requests on internet to fetch artist list again. So this helps
    // improve performance when rotation occurs during SpotifyActivity or it goes back from
    // next activity.
    public static SpotifyActivity artistActivity = null;
    public static TracksActivity trackActi = null;
    public static TrackFragment trackFrag = null;
    public static String name_to_search = null; // keyword of artist name to search
    public static boolean mTablet2PaneUI = false; // false: phone,  true: tablet

    public static int notification_visibility = NotificationCompat.VISIBILITY_PUBLIC;
    // By default VISIBILITY_PUBLIC, notifications will be displayed on lock screen.
    // If a user picks VISIBILITY_PRIVATE by menu "notification visibility",
    // notifications will not appear on lock screen, and they are in drawer.

    public static String country_code = "US";

    public static boolean flag_replay_track__notification = false;
    public static String track_preview_url = null;
    public static String ArtistTracksC_artistId = null;
    public static String ArtistPhotosC_name_to_search = null;
    public static String notification_action = null;
    public static int notification_trackPos = 0;
    public static ArtistFragment artistFragment = null;
}
