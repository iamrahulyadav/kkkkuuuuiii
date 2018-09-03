package tech.kandara.quizapp.Adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tech.kandara.quizapp.Library.CircularImageView;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;
import tech.kandara.quizapp.User;

/**
 * Created by abina on 6/28/2017.
 */

public class LeaderBoardAdapter extends BaseAdapter {
    List<User> list;
    Activity activity;

    public LeaderBoardAdapter(List<User> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    @Override

    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=activity.getLayoutInflater().inflate(R.layout.item_leaderboard, parent, false);

        final User currentUser=list.get(position);
        ImageView profileImg= convertView.findViewById(R.id.profileImg);
        TextView tvName= convertView.findViewById(R.id.tvName);
        TextView tvCredit= convertView.findViewById(R.id.tvCredit);
        TextView tvRanking= convertView.findViewById(R.id.tvRanking);


        tvCredit.setText(currentUser.getTotal_credit_won() + "");

        tvName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        Picasso.with(activity).load(currentUser.getPhotolink()).fit().centerCrop().placeholder(R.drawable.placeholder).into(profileImg);

        tvRanking.setText((position+1)+"");

        return convertView;
    }
}
