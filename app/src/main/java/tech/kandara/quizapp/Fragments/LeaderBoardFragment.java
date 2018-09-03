package tech.kandara.quizapp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tech.kandara.quizapp.Adapter.LeaderBoardAdapter;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;
import tech.kandara.quizapp.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderBoardFragment extends Fragment {


    ListView weekListView;

    public LeaderBoardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leader_board, container, false);

        weekListView = view.findViewById(R.id.listViewWeek);
        //Toast.makeText(getActivity().getApplicationContext(),"dataSnapshot.getKey()",Toast.LENGTH_LONG).show();

        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("total_credit_won").limitToFirst(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<User> userList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    userList.add(user);
                    Collections.sort(userList,new CustomComparator());
                    LeaderBoardAdapter adapter = new LeaderBoardAdapter(userList, getActivity());
                    weekListView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });/*
        ParseQuery<ParseUser> query=ParseUser.getQuery();
        query.addDescendingOrder(PC.KEY_USER_TOTAL_CREDIT_WON);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null){
                    weekTopUsers= (ArrayList<ParseUser>) objects;
                    LeaderBoardAdapter adapter=new LeaderBoardAdapter(weekTopUsers, getActivity());
                    weekListView.setAdapter(adapter);

                }
            }
        });*/
        return view;
    }
    public class CustomComparator implements Comparator<User> {
        @Override
        public int compare(User o1, User o2) {
            return o2.getTotal_credit_won()-o1.getTotal_credit_won();
        }
    }
}

