package com.example.fhuang.myproj0;

import java.util.*;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;


public class SpotifyActivity extends AppCompatActivity {

    private View loSearch;
    private LinearLayout loTablet2PaneUI;
    public boolean mTablet2Pane;
    public FrameLayout loTracksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArtistPhotosC.artistActivity = this;
        setContentView(R.layout.activity_spotify);
        ArtistPhotosC.trackFrag = null;

        loSearch = (View) findViewById(R.id.loSearchContainer);
        if (loSearch != null) { // phone
            loTablet2PaneUI = null;
            mTablet2Pane = false;
            loTracksContainer = null;
        } else {
            loTablet2PaneUI = (LinearLayout) findViewById(R.id.loTablet2PaneUI);
            mTablet2Pane = true;
            loTracksContainer = (FrameLayout) findViewById(R.id.loTracksContainer2Pane);
        }

        ArtistPhotosC.mTablet2PaneUI = mTablet2Pane;

        Intent i = getIntent();
        if (i != null) {
            String track_preview_url = i.getStringExtra("ArtistTracksC_track_preview_url");
            if (track_preview_url != null) {
                ArtistPhotosC.flag_replay_track__notification = true;
                String ArtistTracksC_artistId = i.getStringExtra("ArtistTracksC_artistId");
                String ArtistPhotosC_name_to_search = i.getStringExtra("ArtistPhotosC_name_to_search");

                ArtistPhotosC.track_preview_url = track_preview_url;
                ArtistPhotosC.ArtistTracksC_artistId = ArtistTracksC_artistId;
                ArtistPhotosC.ArtistPhotosC_name_to_search = ArtistPhotosC_name_to_search;
                ArtistPhotosC.notification_action = i.getStringExtra("notification_action");
                String s = i.getStringExtra("notification_trackPos");
                if (s != null) {
                    ArtistPhotosC.notification_trackPos = Integer.parseInt(s);
                }
                ArtistPhotosC.name_to_search = ArtistPhotosC.ArtistPhotosC_name_to_search;
                ArtistPhotosC.artistFragment.svNameToSearch.setQuery(ArtistPhotosC.name_to_search, false);
                ArtistPhotosC.artistFragment.search_artist(ArtistPhotosC.name_to_search, true);
                i.removeExtra("ArtistTracksC_track_preview_url");
            }
        }
    }

    public void populate_TrackFragment (boolean flag_replay_track__notification) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = null;
        if (flag_replay_track__notification == false) {
            frag = TrackFragment.newInstance("foo", "bar");
        } else {
            frag = TrackFragment.newInstance("replay_track__notification", "bar");
        }

        int containerID = R.id.loTracksContainer2Pane;

        FragmentTransaction ft = fm.beginTransaction();

        // ft.addToBackStack(null);
        ft.replace(containerID, frag);
        ft.commit();
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mTablet2Pane) imm.hideSoftInputFromWindow(loTablet2PaneUI.getWindowToken(), 0);
        else imm.hideSoftInputFromWindow(loSearch.getWindowToken(), 0);
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
        getMenuInflater().inflate(R.menu.menu_spotify, menu);
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

    public void showCountryCodeDialog(AppCompatActivity activity) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("country_code");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment dialog = CountryCodeDialogFragment.newInstance("Country Code");
        dialog.show(ft, "country_code");
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
        if (id == R.id.miGoBackToMainMenu) {
            // to ensure <- in top action bar leads to the same behavior as system Back button
            if (ArtistTracksC.mediaPlayer != null) {
                ArtistTracksC.mediaPlayer.stop();
                ArtistTracksC.mediaPlayer.release();
                ArtistTracksC.mediaPlayer = null;
            }
            super.onBackPressed();
        } else if (id == R.id.miSpotifyNowPlay) {
            if (ArtistPhotosC.trackFrag != null) {
                if (ArtistTracksC.dgFragPlayer != null) {
                    if (ArtistTracksC.mediaPlayer == null) {
                        synchronized (ArtistTracksC.progress) {
                            ArtistTracksC.progress = 0;
                        }
                        ArtistTracksC.set_playing(true);
                        ArtistPhotosC.trackFrag.showMediaPlayerDialog(0);
                    } else {
                        ArtistPhotosC.trackFrag.showMediaPlayerDialog(ArtistTracksC.currPos);
                    }
                    ArtistTracksC.dgFragPlayer.resume_now_playing();
                }
            }
        } else if (id == R.id.miSpotifyShareTrack) {
            if (ArtistPhotosC.trackFrag != null && ArtistTracksC.dgFragPlayer != null) {
                int pos = ArtistTracksC.currPos;
                if (0 <= pos && pos < ArtistTracksC.ltATrack.size()) {
                    String playingTrack = ArtistTracksC.ltATrack.get(pos).preview_url;
                    share_track_facebook(playingTrack);
                }
            }
        } else if (ArtistPhotosC.mTablet2PaneUI == false) {
            if (id == R.id.miSpotifyNotificationVisibility) {
                raiseNotificationVisibilityAlertDialog();
            } else if (id == R.id.miCountryCode) {
                showCountryCodeDialog(this);
            }
        } else if (ArtistPhotosC.mTablet2PaneUI) {
            if (id == R.id.miSpotifyNotificationVisibility2Pane) {
                raiseNotificationVisibilityAlertDialog();
            } else if (id == R.id.miCountryCode2Pane) {
                showCountryCodeDialog(this);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (ArtistTracksC.mediaPlayer != null) {
            ArtistTracksC.mediaPlayer.stop();
            ArtistTracksC.mediaPlayer.release();
            ArtistTracksC.mediaPlayer = null;
        }

        super.onBackPressed();
    }
}
