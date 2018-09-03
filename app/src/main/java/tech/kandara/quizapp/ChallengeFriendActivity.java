package tech.kandara.quizapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChallengeFriendActivity extends AppCompatActivity {

    ParseObject arena;
    String objectId = "M4lgEBEt4r";

    ParseObject myArena;
    ArrayList<ParseObject> arenaCurrentUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_friend);

        Timer timer = new Timer();
        timer.schedule(new refreshArenaTask(), 0, 1000);

        Timer timer2 = new Timer();
        timer2.schedule(new UpdateMyArena(), 0, 1000);
    }

    private void refreshArena() {
        arenaCurrentUsers=new ArrayList<>();
        getArena(new ArenaCallBack() {
            @Override
            public void onArenaReceived() {

                ArrayList<ParseObject> userArenas=(ArrayList<ParseObject>) arena.get(PC.KEY_ARENA_ACTIVE_USERS);
                for(ParseObject object:userArenas){
                    long seconds = (Calendar.getInstance().getTimeInMillis()-myArena.getDate(PC.KEY_MYARENA_UPDATED).getTime())/1000;
                    if(seconds<=2){
                        arenaCurrentUsers.add(object);
                    }
                }
                Toast.makeText(getApplicationContext(), "Total : "+arenaCurrentUsers.size(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    interface ArenaCallBack {
        void onArenaReceived();
    }

    class refreshArenaTask extends TimerTask {
        public void run() {
            refreshArena();
        }
    }

    class UpdateMyArena extends TimerTask {
        public void run() {
            if(myArena!=null) {
                myArena.put(PC.KEY_MYARENA_UPDATED, Calendar.getInstance().getTime());
                myArena.saveInBackground();
            }
        }
    }

    public void getArena(final ArenaCallBack arenaCallBack) {

        final ParseQuery<ParseObject> myArenaQuery = new ParseQuery<>(PC.KEY_MYARENA_OBJECT);
        myArenaQuery.whereEqualTo(PC.KEY_MYARENA_USER, ParseUser.getCurrentUser());
        myArenaQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() != 0) {
                        myArena = objects.get(0);
                        ParseQuery<ParseObject> query = new ParseQuery<>(PC.KEY_ARENA_OBJECT);
                        query.whereEqualTo("objectId", objectId);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    if (objects.size() != 0) {

                                        arena = objects.get(0);
                                        ArrayList<String> userArenas= (ArrayList<String>) arena.get(PC.KEY_ARENA_ACTIVE_USERS);

                                        if(!userArenas.contains(myArena.getObjectId())){
                                            arena.add(PC.KEY_ARENA_ACTIVE_USERS, myArena);
                                            arena.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e==null){
                                                        arenaCallBack.onArenaReceived();
                                                    }
                                                }
                                            });
                                        }else{
                                            arenaCallBack.onArenaReceived();
                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        final ParseObject myArena = new ParseObject(PC.KEY_MYARENA_OBJECT);
                        myArena.put(PC.KEY_MYARENA_USER, ParseUser.getCurrentUser());
                        myArena.put(PC.KEY_MYARENA_UPDATED, Calendar.getInstance().getTime());
                        myArena.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                ParseQuery<ParseObject> query = new ParseQuery<>(PC.KEY_ARENA_OBJECT);
                                query.whereEqualTo("objectId", objectId);
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        if (e == null) {
                                            if (objects.size() != 0) {
                                                ParseObject ss = objects.get(0);
                                                ss.add(PC.KEY_ARENA_ACTIVE_USERS, myArena.getObjectId());
                                                ss.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if(e==null){
                                                            getArena(arenaCallBack);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });

                            }
                        });
                    }
                }
            }
        });


    }
}
