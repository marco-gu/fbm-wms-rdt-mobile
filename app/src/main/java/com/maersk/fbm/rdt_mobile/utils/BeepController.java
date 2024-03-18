package com.maersk.fbm.rdt_mobile.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.maersk.fbm.rdt_mobile.R;


public class BeepController {

    private final Context mContext;

    public BeepController(Context context){
        mContext = context;
    }

    /**
     * Calls playSound based on if the scan was good/bad
     * @param isGood - which beep needs to be played
     */
    public void beep(boolean isGood){
        playSound(isGood ? R.raw.beep_yes : R.raw.beep_no);
    }

    /**
     * Plays mp3 file
     * @param resId specifies which mp3 file to play
     */
    private void playSound(int resId){
        if(resId == 0) return;
        final MediaPlayer mp = MediaPlayer.create(mContext, resId);
        if(mp != null) {
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    }
}
