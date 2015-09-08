package com.example.fhuang.myproj0;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MediaPlayerDialogFragment extends DialogFragment implements DialogInterface {
    private EditText mEditText;

    public MediaPlayerDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
        ArtistTracksC.dgFragPlayer = this;
        ArtistTracksC.bm_album = null;
    }

    public static MediaPlayerDialogFragment newInstance(String title) {
        MediaPlayerDialogFragment frag = new MediaPlayerDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    private View vPlayerDialogFragment;
    private int pos;
    private ImageView ivLarge;
    private MediaPlayer mediaPlayer;
    private ImageButton btPause;
    private ImageButton btNext;
    private ImageButton btPrev;
    public SeekBar pgBar;
    public TextView tvCurrTime;
    public TextView tvEndTime;
    public Handler mHandler;
    public Thread td = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        vPlayerDialogFragment = inflater.inflate(R.layout.dialog_fragment_player, container, false);
        pgBar = (SeekBar) vPlayerDialogFragment.findViewById(R.id.progressBar);
        pgBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        ArtistTracksC.set_playing(false);
                        synchronized (ArtistTracksC.progress) {
                            ArtistTracksC.mediaPlayer.pause();
                        }
                        // Toast.makeText(getActivity(), "onStartTrackingTouch : " + progress, Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        // Toast.makeText(getActivity(), "onStopTrackingTouch : " + progress, Toast.LENGTH_LONG).show();
                        synchronized (ArtistTracksC.progress) {
                            ArtistTracksC.mediaPlayer.seekTo(progress);
                            ArtistTracksC.progress = ArtistTracksC.mediaPlayer.getCurrentPosition();
                            // Toast.makeText(getActivity(), "onStopTrackingTouch : " + playerCurrPosi, Toast.LENGTH_LONG).show();
                        }
                        playSong();
                    }
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            // Toast.makeText(getActivity(), "onProgressChanged : " + progress, Toast.LENGTH_LONG).show();
                        }
                    }

                });

        tvCurrTime = (TextView) vPlayerDialogFragment.findViewById(R.id.tvCurrTime);
        tvEndTime = (TextView) vPlayerDialogFragment.findViewById(R.id.tvEndTime);
        btPause = (ImageButton) vPlayerDialogFragment.findViewById(R.id.btPause);
        if (ArtistTracksC.get_playing()) {
            btPause.setImageResource(R.mipmap.ic_media_pause);
        } else {
            btPause.setImageResource(R.mipmap.ic_media_play);
        }

        btPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay(v);
            }
        });
        btNext = (ImageButton) vPlayerDialogFragment.findViewById(R.id.btNext);
        btNext.setImageResource(R.mipmap.ic_media_next);
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack(v);
            }
        });
        btPrev = (ImageButton) vPlayerDialogFragment.findViewById(R.id.btPrev);
        btPrev.setImageResource(R.mipmap.ic_media_previous);
        btPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevTrack(v);
            }
        });
        ivLarge = (ImageView) vPlayerDialogFragment.findViewById(R.id.ivLargeImage);
        if (ArtistTracksC.ltATrack.size() > 0) {
            pos = ArtistTracksC.currPos;
        } else {
            pos = -1;
        }

        // String tn = "track " + ArtistTracksC.currPos;
        // Toast.makeText(getActivity(), tn, Toast.LENGTH_LONG).show();

        setup_mediaPlayer();

        if (ArtistTracksC.get_playing()) {
            send_intent_service("play");
            playSong();
        }
        // btPause.forceLayout();

        return vPlayerDialogFragment;
    }

    public void setup_mediaPlayer () {
        if (0 <= pos && pos < ArtistTracksC.ltATrack.size()) {
            View loTablet2PaneUI = ArtistPhotosC.artistActivity.findViewById(R.id.loTablet2PaneUI);
            String title = ArtistTracksC.artist_name;
            if (loTablet2PaneUI != null) { // For tablet, this acts as a dialog fragment.
                getDialog().setTitle(title);
            } else {
                getActivity().setTitle(title);
            }

            TextView tvAlbum = (TextView) vPlayerDialogFragment.findViewById(R.id.tvAlbum);
            tvAlbum.setText(ArtistTracksC.ltATrack.get(pos).album.name);

            TextView tvTrack = (TextView) vPlayerDialogFragment.findViewById(R.id.tvTrack);
            tvTrack.setText(ArtistTracksC.ltATrack.get(pos).name);

            String imageUrl = ArtistTracksC.ltATrack.get(pos).album.imageUrl;

            // load image by url into image view using picasso
            Picasso.with(getActivity()).load(imageUrl).into(ivLarge);

            String url = ArtistTracksC.ltATrack.get(pos).preview_url;

            if (ArtistTracksC.mediaPlayer == null) {
                ArtistTracksC.mediaPlayer = getMediaPlayer(getActivity());

                ArtistTracksC.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (runnable != null) runnable.terminate();
                        ArtistTracksC.set_playing(false);
                        synchronized (ArtistTracksC.progress) {
                            ArtistTracksC.progress = ArtistTracksC.duration + 1;
                            pgBar.setProgress(ArtistTracksC.duration);
                            update_curr_time(ArtistTracksC.duration, ArtistTracksC.duration);
                        }
                    }
                });

                ArtistTracksC.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    ArtistTracksC.mediaPlayer.setDataSource(url);
                    ArtistTracksC.mediaPlayer.prepare(); // might take long! (for buffering, web communication, etc)

                    synchronized (ArtistTracksC.progress) {
                        ArtistTracksC.duration = ArtistTracksC.mediaPlayer.getDuration();
                        // ArtistTracksC.mediaPlayer.start();
                        ArtistTracksC.mediaPlayer.seekTo(0);
                    }
                    int timeDurationS = ArtistTracksC.duration / 1000;
                    String endTimeStr = "              0:" + timeDurationS;
                    if (timeDurationS < 10) {
                        endTimeStr = "              0:0" + timeDurationS;
                    }
                    tvEndTime.setText(endTimeStr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            ArtistTracksC.mediaPlayer = null;
            ArtistTracksC.set_playing(false);
        }

        mediaPlayer = ArtistTracksC.mediaPlayer;
        td = null;
    }

    public MediaPlayer getMediaPlayer(Context context){

        MediaPlayer mediaplayer = new MediaPlayer();

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer;
        }

        try {
            Class<?> cMediaTimeProvider = Class.forName( "android.media.MediaTimeProvider" );
            Class<?> cSubtitleController = Class.forName( "android.media.SubtitleController" );
            Class<?> iSubtitleControllerAnchor = Class.forName( "android.media.SubtitleController$Anchor" );
            Class<?> iSubtitleControllerListener = Class.forName( "android.media.SubtitleController$Listener" );

            Constructor constructor = cSubtitleController.getConstructor(new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});

            Object subtitleInstance = constructor.newInstance(context, null, null);

            Field f = cSubtitleController.getDeclaredField("mHandler");

            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            }
            catch (IllegalAccessException e) {return mediaplayer;}
            finally {
                f.setAccessible(false);
            }

            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor", cSubtitleController, iSubtitleControllerAnchor);

            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
            //Log.e("", "subtitle is setted :p");
        } catch (Exception e) {}

        return mediaplayer;
    }

    public void update_curr_time (int curr_progress, int duration) {
        int timePlayedSec = curr_progress / 1000;
        String currTimeStr = "              0:" + timePlayedSec;
        if (timePlayedSec < 10) {
            currTimeStr = "              0:0" + timePlayedSec;
        }
        tvCurrTime.setText(currTimeStr);

        int timeDurationS = duration / 1000;
        String endTimeStr = "             0:" + timeDurationS;
        if (timeDurationS < 10) {
            endTimeStr = "             0:0" + timeDurationS;
        }
        tvEndTime.setText(endTimeStr);
    }

    public Integer running = null;

    public class MyTimer implements Runnable {
        MyTimer() {
            running = new Integer(1);
        }

        public void terminate() {
            synchronized(running) {
                running = 0;
            }
        }


        @Override
        public void run() {
            int n = 0;
            int r = -1;
            synchronized(running) { r = running.intValue(); }
            int curr_progress = -1;
            synchronized (ArtistTracksC.progress) {
                curr_progress = ArtistTracksC.progress.intValue();
            }
            while (r == 1 && curr_progress < ArtistTracksC.duration) {
                try {
                    Thread.sleep(100, 0);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized(running) { r = running.intValue(); }
                if (r == 0) return;

                if (ArtistTracksC.get_playing()) {
                    synchronized (ArtistTracksC.progress) {
                        if (curr_progress < 1400) {
                            curr_progress += 100;
                            if (n < 2 && curr_progress > 600) {
                                ArtistTracksC.mediaPlayer.seekTo(curr_progress);
                                ++n;
                            }
                        } else {
                            curr_progress = ArtistTracksC.mediaPlayer.getCurrentPosition();
                        }
                        ArtistTracksC.progress = curr_progress;
                        if (curr_progress >= ArtistTracksC.duration) return;
                    }
                } else {
                    return;
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int r2 = -1;
                        synchronized(running) { r2 = running.intValue(); }
                        if (r2 == 0) return;

                        if (ArtistTracksC.get_playing()) {
                            int curr_progress2 = -1;
                            int duration = -1;
                            synchronized (ArtistTracksC.progress) {
                                if (ArtistTracksC.progress >= ArtistTracksC.duration) return;
                                curr_progress2 = ArtistTracksC.mediaPlayer.getCurrentPosition();
                                duration = ArtistTracksC.duration;
                                        // ArtistTracksC.mediaPlayer.getDuration();
                                ArtistTracksC.progress = curr_progress2;
                                pgBar.setProgress(curr_progress2);
                                // ArtistTracksC.duration = duration;
                            }
                            update_curr_time(curr_progress2, duration);
                        } else {
                            return;
                        }
                    }
                });

                synchronized(running) { r = running.intValue(); }
            }
        }
    }
    public MyTimer runnable = null;

    public void playSong() {
        if (mediaPlayer == null) return;

        ArtistTracksC.set_playing(true);

        btPause.setImageResource(R.mipmap.ic_media_pause);

        int curr_progress = -1;
        int duration = -1;
        synchronized (ArtistTracksC.progress) {
            duration = ArtistTracksC.mediaPlayer.getDuration();
            ArtistTracksC.duration = duration;
            pgBar.setMax(duration);
            curr_progress = ArtistTracksC.mediaPlayer.getCurrentPosition();
            ArtistTracksC.progress = curr_progress;
            pgBar.setProgress(curr_progress);
            if (ArtistTracksC.mediaPlayer.isPlaying() == false) {
                ArtistTracksC.mediaPlayer.start();
            }
        }
        update_curr_time(curr_progress, duration);

        /*
        SubtitleController sc = new SubtitleController(context, null, null);
        sc.mHandler = new Handler();
        mediaplayer.setSubtitleAnchor(sc, null);
        */
        mHandler = new Handler();
        runnable = new MyTimer();
        td = new Thread( runnable );
        td.start();
    }

    private Target loadtarget;
    public void loadBitmap(String url) {
        loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // do something with the Bitmap
                handleLoadedBitmap(bitmap);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }
        };

        Picasso.with(this.getActivity()).load(url).into(loadtarget);
    }

    public void handleLoadedBitmap(Bitmap bm) {
        ArtistTracksC.bm_album = bm;
    }
    public void send_intent_service (String msg) {
        int pos = ArtistTracksC.currPos;
        String album_image_url = ArtistTracksC.ltATrack.get(pos).album.imageUrl;
        loadBitmap(album_image_url);
        Intent intent = new Intent(getActivity(), SpotifyService.class);
        intent.putExtra(SpotifyService.SPOTIFY_SERVICE_KEY, msg);
        getActivity().startService(intent);
    }

    public void nextTrack (View v) {
        if (ArtistTracksC.ltATrack.size() <= 0) return;

        if (ArtistTracksC.mediaPlayer != null) {
            if (runnable != null) runnable.terminate();
            ArtistTracksC.set_playing(false);
            synchronized (ArtistTracksC.progress) {
                ArtistTracksC.mediaPlayer.stop();
                ArtistTracksC.mediaPlayer.release();
                ArtistTracksC.progress = ArtistTracksC.duration + 1;
            }
            try {
                if (td != null) {
                    td.join(300);
                }
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            td = null;
            ArtistTracksC.mediaPlayer = null;
        }

        ++pos;
        if (pos >= ArtistTracksC.ltATrack.size()) {
            pos = 0;
        }
        ArtistTracksC.currPos = pos;

        send_intent_service("next");

        setup_mediaPlayer();
        // ArtistTracksC.set_playing(true);
        playSong();
    }

    public void prevTrack (View v) {
        if (ArtistTracksC.ltATrack.size() <= 0) return;

        if (ArtistTracksC.mediaPlayer != null) {
            if (runnable != null) runnable.terminate();
            ArtistTracksC.set_playing(false);
            synchronized (ArtistTracksC.progress) {
                ArtistTracksC.mediaPlayer.stop();
                ArtistTracksC.mediaPlayer.release();
                ArtistTracksC.progress = ArtistTracksC.duration + 1;
            }
            try {
                if (td != null) {
                    td.join(300);
                }
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            td = null;
        }
        ArtistTracksC.mediaPlayer = null;

        --pos;
        if (pos < 0) {
            pos = ArtistTracksC.ltATrack.size() - 1;
        }
        ArtistTracksC.currPos = pos;

        send_intent_service("prev");

        setup_mediaPlayer();
        // ArtistTracksC.set_playing(true);
        playSong();
    }

    public void pausePlay(View v) {
        if (mediaPlayer == null) return;

        if (ArtistTracksC.get_playing()) {
            // mediaPlayer.pause();
            if (runnable != null) runnable.terminate();
            ArtistTracksC.set_playing(false);
            synchronized (ArtistTracksC.progress) {
                ArtistTracksC.mediaPlayer.pause();
            }
            btPause.setImageResource(R.mipmap.ic_media_play);

            try {
                if (td != null) {
                    td.join(300);
                }
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            td = null;

            send_intent_service("pause");
        } else {
            send_intent_service("play");
            playSong();
        }

    }

    public void resume_now_playing () {
        if (ArtistTracksC.mediaPlayer == null) {
            setup_mediaPlayer();
        } else {
            synchronized (ArtistTracksC.progress) {
                ArtistTracksC.mediaPlayer.seekTo(ArtistTracksC.progress);
            }
        }

        playSong();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        synchronized (ArtistTracksC.progress) {
            if (ArtistTracksC.mediaPlayer != null) {
                ArtistTracksC.mediaPlayer.pause();
                if (ArtistTracksC.progress >= ArtistTracksC.duration) {
                    ArtistTracksC.progress = 0;
                }
                // ArtistTracksC.mediaPlayer.release();
            }
        }
        if (runnable != null) runnable.terminate();
        try {
            if (td != null) {
                td.join(300);
            }
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }

        td = null;
        // ArtistTracksC.mediaPlayer = null;
        // ArtistTracksC.set_progress(0);
        super.onCancel(dialog);
    }

    @Override
    public void cancel() {
        dismiss();
    }
}
