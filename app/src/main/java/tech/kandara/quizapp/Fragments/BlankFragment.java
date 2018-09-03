package tech.kandara.quizapp.Fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import tech.kandara.quizapp.Adapter.MessageAdapter;
import tech.kandara.quizapp.GameActivity;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {

    ListView messageListView;
    ArrayList<String> messages;
    ArrayList<ParseObject> messageObjects;
    private InterstitialAd mInterstitialAd;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_blank, container, false);
        messageListView =(ListView)view.findViewById(R.id.listViewChat);
        messages=new ArrayList<>();
        messageObjects=new ArrayList<>();
        retrieveList();

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadInterstitialAd();
                super.onAdClosed();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        loadInterstitialAd();

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

        return view;
    }

    public void loadInterstitialAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build());
    }

    public void retrieveList(){
        ParseQuery<ParseObject> query=new ParseQuery<>(PC.KEY_OBJECT_MESSAGE);
        query.addDescendingOrder("createdAt");
        query.whereEqualTo(PC.KEY_MESSAGE_USERID, ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if(objects.size()!=0){

                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                        for(ParseObject parseObject:objects){

                            messages.add(parseObject.getString(PC.KEY_MESSAGE_TITLE));
                            messageObjects.add(parseObject);
                        }
                        MessageAdapter adapter=new MessageAdapter(messageObjects, getActivity());
                        messageListView.setAdapter(adapter);
                    }
                }else{
                    if (e.getCode() == ParseException.CONNECTION_FAILED) {
                        Toast.makeText(getContext(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
