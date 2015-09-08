package com.example.fhuang.myproj0;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class SpotifyService extends IntentService {
    public static final String SPOTIFY_SERVICE_KEY = "SpotifyService";
    public static final int SPOTIFY_NOTIFICATION_ID = 0;

    public SpotifyService() {
        super("SpotifyService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String val = intent.getStringExtra(SPOTIFY_SERVICE_KEY);
        Log.d(SPOTIFY_SERVICE_KEY, val);
        String msg = "";
        if (val.equals("next")) {
            msg = msg + ArtistTracksC.artist_name + " - ";
        } else if (val.equals("prev")) {
            msg = msg + ArtistTracksC.artist_name + " - ";
        } else if (val.equals("play")) {
            msg = msg + ArtistTracksC.artist_name + " - ";
        } else if (val.equals("pause")) {
            msg = msg + ArtistTracksC.artist_name + " - ";
        }
        int pos = ArtistTracksC.currPos;
        msg = msg + ArtistTracksC.ltATrack.get(pos).name;
        String album_image_url = ArtistTracksC.ltATrack.get(pos).album.imageUrl;
        sendNotification(msg, album_image_url, ArtistTracksC.ltATrack.get(pos).preview_url);
    }
    public NotificationCompat.Builder noteB = null;

    private Intent prepare_Intent (String track_preview_url, String action2, Context cnt) {
        Intent i = new Intent(cnt, SpotifyActivity.class);

        i.putExtra("ArtistPhotosC_name_to_search", ArtistPhotosC.name_to_search);
        i.putExtra("ArtistTracksC_artistId", ArtistTracksC.artistId);
        i.putExtra("ArtistTracksC_track_preview_url", track_preview_url);
        i.putExtra("notification_action", action2);
        i.putExtra("notification_trackPos", "" + ArtistTracksC.currPos);
        return i;
        /*
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TracksActivity.class);
        stackBuilder.addNextIntent(i);
        PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        */

    }

    private void sendNotification(String msg, String album_image_url, String track_preview_url) {
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_SERVICE, SPOTIFY_NOTIFICATION_ID);
        // mgr.cancelAll();

        noteB = new NotificationCompat.Builder(this);

        // By default VISIBILITY_PUBLIC, notifications will be displayed on lock screen.
        // If a user picks VISIBILITY_PRIVATE by menu "notification visibility",
        // notifications will not appear on lock screen, and they are in drawer.
        //
        // noteB.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        // noteB.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        noteB.setVisibility(ArtistPhotosC.notification_visibility);

        noteB.setContentTitle("Spotify Streamer");
        noteB.setContentText(msg);
        int icon = R.drawable.ic_launcher;
        noteB.setSmallIcon(icon);

        if (ArtistTracksC.bm_album != null) {
            noteB.setLargeIcon(ArtistTracksC.bm_album );
        }
        noteB.setAutoCancel(true);

        Intent i0 = prepare_Intent(track_preview_url, "prev", this.getBaseContext());
        PendingIntent piPrev = PendingIntent.getActivity(
                this.getBaseContext(),
                0,
                i0,
                Intent.FLAG_ACTIVITY_NEW_TASK
        );
        NotificationCompat.Action a0 = new NotificationCompat.Action(R.mipmap.ic_media_previous, "", piPrev);
        noteB.addAction(a0); // #0

        Intent i1 = prepare_Intent(track_preview_url, "play", this.getBaseContext());
        PendingIntent piPlay = PendingIntent.getActivity(
                this.getBaseContext(),
                1,
                i1,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Action a1 = new NotificationCompat.Action(R.mipmap.ic_media_play, "", piPlay);
        noteB.addAction(a1);     // #1

        Intent i2 = prepare_Intent(track_preview_url, "next", this.getBaseContext());
        PendingIntent piNext = PendingIntent.getActivity(
                this.getBaseContext(),
                2,
                i2,
                PendingIntent.FLAG_UPDATE_CURRENT );
        NotificationCompat.Action a2 = new NotificationCompat.Action(R.mipmap.ic_media_next, "", piNext);
        noteB.addAction(a2);     // #2

        boolean f01 = i0.filterEquals(i1);
        boolean f02 = i0.filterEquals(i2);
        boolean f12 = i1.filterEquals(i2);

             // .addAction(0, "PAUSE", piPause);  // #3
             //   no enough space for additional notification action item
                // Apply the media style template

        Notification note = noteB.build();

        mgr.notify(SPOTIFY_NOTIFICATION_ID, note);
    }

    static public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
