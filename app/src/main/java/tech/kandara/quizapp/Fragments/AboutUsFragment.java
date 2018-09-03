package tech.kandara.quizapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.ParseUser;

import tech.kandara.quizapp.Library.TitlEffect.TiltEffectAttacher;
import tech.kandara.quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsFragment extends Fragment {

    Button contactUsBtn;

    public AboutUsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_about_us, container, false);
        contactUsBtn=(Button)view.findViewById(R.id.contactUsBtn);
        TiltEffectAttacher.attach(contactUsBtn);
        contactUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"kandara.tech2015@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Hello");
                i.putExtra(Intent.EXTRA_TEXT   , "From "+ ParseUser.getCurrentUser().getUsername()+"\n");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

}
