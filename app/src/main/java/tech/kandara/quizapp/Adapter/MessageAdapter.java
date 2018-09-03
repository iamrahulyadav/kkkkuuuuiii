package tech.kandara.quizapp.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tech.kandara.quizapp.GameActivity;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;

/**
 * Created by abina on 7/20/2017.
 */

public class MessageAdapter extends BaseAdapter{

    ArrayList<ParseObject> list;
    Activity activity;

    public MessageAdapter(ArrayList<ParseObject> list, Activity activity) {
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        convertView=activity.getLayoutInflater().inflate(R.layout.item_message, parent, false);

        final ParseObject parseObject=list.get(position);

        TextView tvTitle=(TextView)convertView.findViewById(R.id.tvTitle);
        TextView tvMessage=(TextView)convertView.findViewById(R.id.tvMessage);

        tvTitle.setText(parseObject.getString(PC.KEY_MESSAGE_TITLE));
        tvMessage.setText(parseObject.getString(PC.KEY_MESSAGE_MESSAGE));

        final RelativeLayout relativeLayout=(RelativeLayout)convertView.findViewById(R.id.rlll);
        if(!parseObject.getBoolean(PC.KEY_MESSAGE_SEEN)){
            relativeLayout.setBackgroundColor(activity.getResources().getColor(R.color.cardColorUnseen));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showYesNoDialog(parseObject.getString(PC.KEY_MESSAGE_MESSAGE), "OK", "OK", new GameActivity.OnYesClicked() {
                    @Override
                    public void onDone() {
                        relativeLayout.setBackgroundColor(activity.getResources().getColor(R.color.cardColorseen));
                        parseObject.put(PC.KEY_MESSAGE_SEEN, true);
                        parseObject.saveInBackground();
                    }
                }, new GameActivity.OnNoClicked() {
                    @Override
                    public void onDone() {

                    }
                });
            }
        });

        return convertView;
    }
    public void showYesNoDialog(String title, String yes, String no, final GameActivity.OnYesClicked onYesClicked, final GameActivity.OnNoClicked onNoClicked) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        onYesClicked.onDone();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        if (onNoClicked != null) {
                            onNoClicked.onDone();
                        }
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(title).setPositiveButton(yes, dialogClickListener).show();
    }
}
