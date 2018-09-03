package tech.kandara.quizapp.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import tech.kandara.quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FAQFragment extends Fragment {

    LinearLayout messengerChat;

    public FAQFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_faq, container, false);
        messengerChat=(LinearLayout)view.findViewById(R.id.messengerChat);
        messengerChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                        getActivity().startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/139975439920091")));
                    } catch (Exception e) {
                        getActivity().startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/139975439920091")));
                    }

            }
        });
        return view;
    }

}
