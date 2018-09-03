package tech.kandara.quizapp.Library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by abina on 6/29/2017.
 */

public class MySettings {

    Activity activity;
    static String KEY_SETTINGS="PreferenceSettings";
    static String KEY_SOUND_ON="isSoundOn";
    static String KEY_VIBRATION_ON="isVibrationOn";
    static String KEY_PUSH_ON="isPushOn";


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public MySettings(Activity activity) {
        this.activity = activity;
        sharedPreferences=activity.getSharedPreferences(KEY_SETTINGS, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public boolean isSoundOn(){
        return sharedPreferences.getBoolean(KEY_SOUND_ON, true);
    }

    public boolean isVibrationOn(){
        return sharedPreferences.getBoolean(KEY_VIBRATION_ON, false);
    }

    public boolean isPushNotificationOn(){
        return sharedPreferences.getBoolean(KEY_PUSH_ON, true);
    }

    public void setSound(boolean onOff){
        editor.putBoolean(KEY_SOUND_ON, onOff);
        editor.commit();
    }

    public void setVibration(boolean onOff){
        editor.putBoolean(KEY_VIBRATION_ON, onOff);
        editor.commit();
    }

    public void setPush(boolean onOff){
        editor.putBoolean(KEY_PUSH_ON, onOff);
        editor.commit();
    }


}
