package com.example.fhuang.myproj0;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class TracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArtistPhotosC.trackActi = this;
        // requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_tracks);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(ArtistTracksC.artist_name);
    }

    private void populate_TrackFragment (boolean flag_replay_track__notification) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = null;
        if (flag_replay_track__notification == false) {
            frag = TrackFragment.newInstance("foo", "bar");
        } else {
            frag = TrackFragment.newInstance("replay_track__notification", "bar");
        }

        int containerID = R.id.loTrackContainer;

        String tag = "tracks";
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ft.replace(containerID, frag, tag);
        ft.commit();
    }

    // This requires API 16 or later.
    /*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Intent getParentActivityIntent() {
        // add the clear top flag - which checks if the parent (main)
        // activity is already running and avoids recreating it
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracks, menu);
        return true;
    }

    public void share_track_facebook (String urlToShare) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        // intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has no effect!
        intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        // See if official Facebook app is found
        boolean facebookAppFound = false;
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        if (facebookAppFound) {
            startActivity(intent);
            return;
        }

        // As fallback, launch sharer.php in a browser
        String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        startActivity(intent);
    }

    private void raiseNotificationVisibilityAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Visibility")
                .setMessage("Do you want notifications to show on lock screen ?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //do stuff onclick of YES
                        set_notification_visibility( NotificationCompat.VISIBILITY_PUBLIC );
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //do stuff onclick of CANCEL
                        set_notification_visibility( NotificationCompat.VISIBILITY_PRIVATE );
                    }
                }).show();
    }
    public void set_notification_visibility (int value) {
        ArtistPhotosC.notification_visibility = value;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //     return true;
        // }
        if (id == R.id.miGoBackToArtistSearch) {
            // to ensure <- in top action bar leads to the same behavior as system Back button
            ArtistPhotosC.trackFrag = null;
            if (ArtistTracksC.mediaPlayer != null) {
                ArtistTracksC.mediaPlayer.stop();
                ArtistTracksC.mediaPlayer.release();
                ArtistTracksC.mediaPlayer = null;
            }
            ArtistTracksC.dgFragPlayer = null;
            super.onBackPressed();
        } else if (id == R.id.miTrackNowPlay) {
            if (ArtistPhotosC.trackFrag != null && ArtistTracksC.dgFragPlayer != null) {
                if (ArtistTracksC.mediaPlayer == null) {
                    synchronized (ArtistTracksC.progress) {
                        ArtistTracksC.progress = 0;
                    }
                    ArtistTracksC.set_playing(true);
                    ArtistPhotosC.trackFrag.showMediaPlayerDialog(0);
                } else {
                    synchronized (ArtistTracksC.progress) {
                        if (ArtistTracksC.progress >= ArtistTracksC.duration) {
                            ArtistTracksC.mediaPlayer = null;
                        }
                    }
                    ArtistPhotosC.trackFrag.showMediaPlayerDialog(ArtistTracksC.currPos);
                }

                ArtistTracksC.dgFragPlayer.resume_now_playing();
            }
        } else if (id == R.id.miTrackShareTrack) {
            if (ArtistPhotosC.trackFrag != null && ArtistTracksC.dgFragPlayer != null) {
                int pos = ArtistTracksC.currPos;
                if (0 <= pos && pos < ArtistTracksC.ltATrack.size()){
                    String playingTrack = ArtistTracksC.ltATrack.get(pos).preview_url;
                    share_track_facebook(playingTrack);
                }
            }
        } else if (id == R.id.miTrackNotificationVisibility) {
            raiseNotificationVisibilityAlertDialog();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ArtistPhotosC.trackFrag = null;
        if (ArtistTracksC.mediaPlayer != null) {
            ArtistTracksC.mediaPlayer.stop();
            ArtistTracksC.mediaPlayer.release();
            ArtistTracksC.mediaPlayer = null;
        }
        ArtistTracksC.dgFragPlayer = null;
        super.onBackPressed();
    }
}
