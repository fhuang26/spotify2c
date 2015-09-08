package com.example.fhuang.myproj0;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
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
public class ArtistFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SpotifyApi api;
    public SpotifyService spotify;
    private OnFragmentInteractionListener mListener;

    public SearchView svNameToSearch;
    private GridView gvAPhoto;
    private ArrayList<ArtistPhoto> ltAPhoto; // list of artists with photos
    private ArtistPhotoAdapter adapterAPhoto; // ArrayAdapter for list of artist photos
    public final static String EXTRA_STR = "com.example.fhuang.spotify1.KEY";
    /**
     * The fragment's ListView/GridView.
     */
    // private AbsListView mListView;

    // TODO: Rename and change types of parameters
    public static ArtistFragment newInstance(String param1, String param2) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        ArtistPhotosC.artistFragment = this;
    }

    public View vwArtistFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vwArtistFragment = inflater.inflate(R.layout.fragment_artist, container, false);
        api = new SpotifyApi();
        // api.setAccessToken("");
        spotify = api.getService(); // not used for spotify stage 1 ?

        setupViews();

        // to restore previous state without internet search as rotation occurs
        if (ArtistPhotosC.ltAPhoto == null) {
            ArtistPhotosC.ltAPhoto = new ArrayList<ArtistPhoto>();
        }
        ltAPhoto = ArtistPhotosC.ltAPhoto;

        if (ArtistPhotosC.name_to_search != null) {
            if (ArtistPhotosC.name_to_search.length() > 0) {
                svNameToSearch.setQuery(ArtistPhotosC.name_to_search, false);
            }
        }

        adapterAPhoto = new ArtistPhotoAdapter(getActivity(), ltAPhoto);
        gvAPhoto.setAdapter(adapterAPhoto);

        if (ltAPhoto.size() > 0) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        return vwArtistFragment;
    }



    private void setupViews() {

        svNameToSearch = (SearchView) vwArtistFragment.findViewById(R.id.svNameToSearch);
        svNameToSearch.setIconifiedByDefault(false);
        svNameToSearch.setQueryHint(getResources().getString(R.string.artist_search_hint));
        svNameToSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchKeyword = svNameToSearch.getQuery().toString();
                search_artist(searchKeyword, false);
                svNameToSearch.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        gvAPhoto=(GridView) vwArtistFragment.findViewById(R.id.gvAPhoto);

        gvAPhoto.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ArtistPhoto aPhoto = ltAPhoto.get(position);
                ArtistTracksC.artist_name = aPhoto.name;
                ArtistTracksC.artistId = aPhoto.id;
                ArtistTracksC.ltATrack = null;

                if (ArtistPhotosC.mTablet2PaneUI == false) { // phone
                    Intent i = new Intent(getActivity(), TracksActivity.class);
                    i.putExtra("artistId", aPhoto.id);
                    startActivity(i);
                } else { // tablet
                    ((SpotifyActivity) getActivity()).populate_TrackFragment(false);
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void proc_notification () {
        if (ArtistPhotosC.ltAPhoto == null || ArtistPhotosC.ltAPhoto.size() == 0) {
            ArtistPhotosC.flag_replay_track__notification = false;
            return;
        }
        int posArtist = -1;
        for (int k = 0; k < ArtistPhotosC.ltAPhoto.size(); ++k) {
            if (ArtistPhotosC.ltAPhoto.get(k).id.equals(ArtistPhotosC.ArtistTracksC_artistId)) {
                posArtist = k;
                break;
            }
        }
        if (posArtist == -1) {
            ArtistPhotosC.flag_replay_track__notification = false;
            return;
        }
        ArtistPhoto aPhoto = ArtistPhotosC.ltAPhoto.get(posArtist);
        ArtistTracksC.artist_name = aPhoto.name;
        ArtistTracksC.artistId = aPhoto.id;
        ArtistTracksC.ltATrack = null;
        flag_replay_track__notification = false;
        if (ArtistPhotosC.mTablet2PaneUI == false) { // phone
            Intent i2 = new Intent(this.getActivity(), TracksActivity.class);
            i2.putExtra("artistId", aPhoto.id);
            i2.putExtra("replay_track__notification", "true");
            startActivity(i2);
        } else { // tablet
            ((SpotifyActivity)this.getActivity()).populate_TrackFragment(true);
        }
    }

    public boolean flag_replay_track__notification = false;
    public Context cnt = null;
    public void search_artist(String name_to_search, boolean flag_replay_track__notification_in) {
        if (isNetworkAvailable() == false) {
            Toast.makeText(getActivity(), "network is disconnected", Toast.LENGTH_LONG).show();
            return;
        }

        if (name_to_search == null || name_to_search.length() == 0) {
            Toast.makeText(getActivity(), "please enter artist name keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        flag_replay_track__notification = flag_replay_track__notification_in;
        cnt = (Context) getActivity();

        ArtistPhotosC.name_to_search = name_to_search; // to retain keyword entered by a user
        ((SpotifyActivity)getActivity()).hideSoftKeyboard();

        AsyncHttpClient client = new AsyncHttpClient();
        String searchUrl = "https://api.spotify.com/v1/search?q=" + name_to_search + "&type=artist";
        client.get(searchUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int StatusCode, Header[] headers, JSONObject response) {
                // Log.d("DEBUG", response.toString());
                // Toast.makeText(cnt, response.toString(), Toast.LENGTH_LONG).show();
                try {
                    ltAPhoto.clear();
                    JSONObject obj1 = response.getJSONObject("artists");
                    JSONArray arr2 = obj1.getJSONArray("items");
                    if (arr2.length() == 0) {
                        String not_found_msg = "no artists are found";
                        Toast.makeText(cnt, not_found_msg, Toast.LENGTH_LONG).show();
                    } else {
                        for (int k = 0; k < arr2.length() && ltAPhoto.size() <= 26; ++k) {
                            try {
                                JSONObject obj3 = arr2.getJSONObject(k);
                                String id = obj3.getString("id");
                                String name = obj3.getString("name");
                                String url = null;
                                JSONArray imageArr = obj3.getJSONArray("images");
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

                                ArtistPhoto aPhoto = new ArtistPhoto(id, name, url);
                                ltAPhoto.add(aPhoto);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        adapterAPhoto.notifyDataSetChanged();
                        if (flag_replay_track__notification) {
                            proc_notification();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] h, String s, Throwable throwable) {
                String not_found_msg = "no artists are found";
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
    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }
    */

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    /*
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }
    */

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
