package tech.kandara.quizapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by Abinash on 6/8/2017.
 */

public class QuizApp extends Application {

    @Override
    public void onCreate() {
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(getResources().getString(R.string.applicationId))
                .clientKey(getResources().getString(R.string.clientKey))
                .server(getResources().getString(R.string.server))
        .build()
        );



        super.onCreate();
    }
}
