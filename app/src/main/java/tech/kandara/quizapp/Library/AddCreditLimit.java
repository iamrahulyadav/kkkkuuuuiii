package tech.kandara.quizapp.Library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by abina on 7/24/2017.
 */

public class AddCreditLimit {
    Activity activity;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String KEY_PREF_NAME="CreditLimitPref";
    String KEY_PREF_TOTAL="CreditLimitTotal";
    String KEY_PREF_TOTAL_REACHED="";
    public AddCreditLimit(Activity activity){
        this.activity=activity;
        sharedPreferences=activity.getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void addCredit(long now){
        int total=sharedPreferences.getInt(KEY_PREF_TOTAL, 0);
        if(total==20){
            editor.putLong(KEY_PREF_TOTAL_REACHED, now);
            editor.putInt(KEY_PREF_TOTAL, 0);
        }else{
            editor.putInt(KEY_PREF_TOTAL, total+2);
        }
        editor.commit();


    }

    public boolean isLimitReached(long now){
        long lastTime=sharedPreferences.getLong(KEY_PREF_TOTAL_REACHED, 0);
        if(lastTime==0){
            return false;
        }else{
            long diff = now - lastTime;
            long diffMinutes = diff / (60 * 1000) % 60;
            if(diffMinutes<5){
                return true;
            }else{
                return false;
            }
        }
    }
}
