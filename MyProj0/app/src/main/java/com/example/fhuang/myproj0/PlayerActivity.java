package com.example.fhuang.myproj0;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArtistTracksC.playerActivity = this;
        setContentView(R.layout.activity_player);
        // to make dialog fragment as normal fullscreen fragment for phone
        if (savedInstanceState == null) {
            DialogFragment playerFragment = MediaPlayerDialogFragment.newInstance(
                    ArtistTracksC.ltATrack.get(ArtistTracksC.currPos).name);

            View loTablet2PaneUI = ArtistPhotosC.artistActivity.findViewById(R.id.loTablet2PaneUI);
            if (loTablet2PaneUI == null) { // phone
                // The device is smaller, so show the fragment fullscreen
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                // specify a transition animation
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                // To make it fullscreen, use the 'content' root view as the container
                // for the fragment, which is always the root view for the activity
                // ft.add(android.R.id.content, playerFragment).addToBackStack(null).commit();
                ft.add(R.id.loPlayerContainer, playerFragment).commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        synchronized (ArtistTracksC.progress) {
            if (ArtistTracksC.mediaPlayer != null) {
                ArtistTracksC.mediaPlayer.pause();
                if (ArtistTracksC.progress >= ArtistTracksC.duration) {
                    ArtistTracksC.progress = 0;
                }
            }
        }

        if (ArtistTracksC.dgFragPlayer != null) {
            synchronized (ArtistTracksC.dgFragPlayer.running) {
                ArtistTracksC.dgFragPlayer.running = 0;
            }

            try {
                if (ArtistTracksC.dgFragPlayer.td != null) {
                    ArtistTracksC.dgFragPlayer.td.join(300);
                }
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            ArtistTracksC.dgFragPlayer.td = null;
        }

        super.onBackPressed();
    }
}
