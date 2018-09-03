package tech.kandara.quizapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.kyleduo.switchbutton.SwitchButton;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import tech.kandara.quizapp.EntryScreen;
import tech.kandara.quizapp.Library.utils.MySettings;
import tech.kandara.quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    SwitchButton sound, vibration, push;

    Button logOutBtn;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sound = view.findViewById(R.id.sound);
        vibration = view.findViewById(R.id.vibration);
        push = view.findViewById(R.id.push);
        logOutBtn = view.findViewById(R.id.logOutBtn);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), EntryScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();

            }
        });

        final MySettings settings = new MySettings(getActivity());
        if (settings.isSoundOn()) {
            sound.setChecked(true);
        } else {
            sound.setChecked(false);
        }
        if (settings.isVibrationOn()) {
            vibration.setChecked(true);
        } else {
            vibration.setChecked(false);
        }
        if (settings.isPushNotificationOn()) {
            push.setChecked(true);
        } else {
            push.setChecked(false);
        }

        sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setSound(isChecked);
            }
        });

        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVibration(isChecked);
            }
        });

        push.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setPush(isChecked);
            }
        });
        return view;
    }

}
