package com.example.fhuang.myproj0;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class TrackFragment extends Fragment {
    private ListView lvTracks;
    private ArrayList<TrackC> ltATrack; // list of artist tracks
    private int notification_counter = 1;
    private TrackAdapter adapterATrack; // ArrayAdapter for list of artist tracks
    private int mStackLevel;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static TrackFragment newInstance(String param1, String param2) {
        TrackFragment fragment = new TrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        ArtistTracksC.mediaPlayer = null;
        synchronized (ArtistTracksC.progress) {
            ArtistTracksC.progress = 0;
        }
        ArtistTracksC.dgFragPlayer = null;
        // ArtistPhotosC.trackFrag = fragment;
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ArtistPhotosC.trackFrag = this;
    }

    public View vwTrackFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vwTrackFragment = inflater.inflate(R.layout.fragment_track, container, false);

        setupViews();

        // to restore previous state without internet search as rotation occurs
        if (ArtistTracksC.ltATrack == null) {
            ArtistTracksC.ltATrack = new ArrayList<TrackC>();
        }
        ltATrack = ArtistTracksC.ltATrack;

        adapterATrack = new TrackAdapter(getActivity(), ltATrack);
        lvTracks.setAdapter(adapterATrack);

        if (ltATrack.size() == 0) {
            // pull out artist's spotify Id from intent
            String artistId = ArtistTracksC.artistId;

            // to send async requests and load tracks to ArtistTracksC.ltATrack (same as ltATrack)
            populate_artist_tracks(artistId);
        } else {
            adapterATrack.notifyDataSetChanged();
        }

        /*
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        */

        return vwTrackFragment;
    }

    private void setupViews () {
        lvTracks = (ListView) vwTrackFragment.findViewById(R.id.lvTracks);

        lvTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ArtistTracksC.currPos = position;
                ArtistTracksC.mediaPlayer = null;
                synchronized (ArtistTracksC.progress) {
                    ArtistTracksC.progress = 0;
                }
                ArtistTracksC.set_playing(true);
                showMediaPlayerDialog(position);
            }
        });
    }

    public void showMediaPlayerDialog(int pos) {
        mStackLevel++;

        // Two cases : (1) create and show dialog fragment for tablet 2-pane UI
        //             (2) make dialog fragment as normal fullscreen fragment for phone

        View loTablet2PaneUI = ArtistPhotosC.artistActivity.findViewById(R.id.loTablet2PaneUI);
        if (loTablet2PaneUI != null) { // tablet
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentManager fm = getActivity().getSupportFragmentManager();

            DialogFragment playerFragment = MediaPlayerDialogFragment.newInstance(ltATrack.get(pos).name);
            /*
            Fragment prev = fm.findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack("dialog");
            */
            playerFragment.show(fm, "dialog");
        } else { // phone
            Intent i = new Intent(getActivity(), PlayerActivity.class);
            startActivity(i);
        }
    }

    private void proc_notification2 () {
        if (notification_counter > 1) {
            return;
        }
        ++ notification_counter;
        ArtistPhotosC.flag_replay_track__notification = false;

        if (ArtistTracksC.ltATrack == null || ArtistTracksC.ltATrack.size() == 0) return;

        int posTrack = ArtistPhotosC.notification_trackPos;
        /*
        int posTrack = -1;
        for (int k = 0; k < ArtistTracksC.ltATrack.size(); ++k) {
            if (ArtistTracksC.ltATrack.get(k).preview_url.equals(ArtistPhotosC.track_preview_url)) {
                posTrack = k;
                break;
            }
        }
        if (posTrack == -1) return;
        */

        if (ArtistPhotosC.notification_action != null) {
            if (ArtistPhotosC.notification_action.equals("next")) {
                ++posTrack;
                if (posTrack >= ArtistTracksC.ltATrack.size()) posTrack = 0;
            } else if (ArtistPhotosC.notification_action.equals("prev")) {
                --posTrack;
                if (posTrack < 0) posTrack = ArtistTracksC.ltATrack.size() - 1;
            }
        }
        ArtistTracksC.currPos = posTrack;
        ArtistTracksC.mediaPlayer = null;
        synchronized (ArtistTracksC.progress) {
            ArtistTracksC.progress = 0;
        }
        ArtistTracksC.set_playing(true);
        if (ArtistPhotosC.notification_action != null) {
            if (ArtistPhotosC.notification_action.equals("pause")) {
                ArtistTracksC.set_playing(false);
            }
        }
        showMediaPlayerDialog(posTrack);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public Context cnt = null;

    // to populate list of artist tracks
    public void populate_artist_tracks (String artistId) {
        if (isNetworkAvailable() == false) {
            Toast.makeText(getActivity(), "network is disconnected", Toast.LENGTH_SHORT).show();
            return;
        }

        String trackSearch = "https://api.spotify.com/v1/artists/" + artistId +
                "/top-tracks?country=" + ArtistPhotosC.country_code;
        cnt = (Context) getActivity();

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(trackSearch, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int StatusCode, Header[] headers, JSONObject response) {
                // Log.d("DEBUG", response.toString());
                // Toast.makeText(cnt, response.toString(), Toast.LENGTH_LONG).show();
                try {
                    ltATrack.clear();
                    JSONArray arr1 = response.getJSONArray("tracks");
                    if (arr1.length() == 0) {
                        String not_found_msg = "no artist tracks are found";
                        Toast.makeText(cnt, not_found_msg, Toast.LENGTH_LONG).show();
                    } else {
                        for (int k = 0; k < arr1.length() && ltATrack.size() <= 10; ++k) {
                            JSONObject obj2 = arr1.getJSONObject(k);
                            TrackC track = new TrackC(obj2);
                            ltATrack.add(track);
                        }
                        adapterATrack.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Bundle arg = getArguments();
                if (arg != null) {
                    String notiFlag = arg.getString(ARG_PARAM1);
                    if (notification_counter == 1 && notiFlag != null &&
                            notiFlag.equals("replay_track__notification")) { // tablet 2Pane
                        proc_notification2();
                    } else {
                        if (ArtistPhotosC.mTablet2PaneUI == false) { // phone
                            if (ArtistPhotosC.flag_replay_track__notification) {
                                proc_notification2();
                            }
                        }
                    }
                } else {
                    if (ArtistPhotosC.mTablet2PaneUI == false) { // phone
                        if (ArtistPhotosC.flag_replay_track__notification) {
                            proc_notification2();
                        }
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] h, String s, Throwable throwable) {
                String not_found_msg = "no artist tracks are found";
                Toast.makeText(cnt, not_found_msg, Toast.LENGTH_LONG).show();
                // Toast.makeText(cnt, "fail", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
