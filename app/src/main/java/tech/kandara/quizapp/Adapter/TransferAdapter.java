package tech.kandara.quizapp.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;

/**
 * Created by Abinash on 6/14/2017.
 */

public class TransferAdapter extends BaseAdapter {

    ArrayList<ParseObject> transferArrays;
    Activity activity;

    public TransferAdapter(ArrayList<ParseObject> transferArrays, Activity activity) {
        this.transferArrays = transferArrays;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return transferArrays.size();
    }

    @Override
    public Object getItem(int position) {
        return transferArrays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = activity.getLayoutInflater().inflate(R.layout.item_transfers, parent, false);
        TextView tvAmount = (TextView) convertView.findViewById(R.id.tvItemAmount);
        TextView tvNumber = (TextView) convertView.findViewById(R.id.tvItemNumber);
        TextView tvStatus = (TextView) convertView.findViewById(R.id.tvItemStatus);

        ParseObject transfer = transferArrays.get(position);
        try {
            if (transfer.getBoolean(PC.KEY_TRANSFER_IS_REJECTED)) {
                tvStatus.setText("Rejected");
                tvStatus.setTextColor(activity.getResources().getColor(R.color.colorRed));


            } else if (transfer.getBoolean(PC.KEY_TRANSFER_IS_PROCESSED)) {
                tvStatus.setText("Processed");
                tvStatus.setTextColor(activity.getResources().getColor(R.color.colorGreen));
            } else {

                int PROCESSING_HOUR = 13;
                Calendar current = Calendar.getInstance();

                Calendar calendar = Calendar.getInstance();

//Set the time for the notification to occur.
                calendar.set(Calendar.YEAR, 2017);

                int dayOfMonth = current.get(Calendar.DAY_OF_MONTH);
                int month = current.get(Calendar.MONTH);
                int houroFDay = current.get(Calendar.HOUR_OF_DAY);
                if (dayOfMonth == getTotalDay(month)) {
                    calendar.set(Calendar.MONTH, (current.getTime().getMonth() + 1));
                }
                if (houroFDay > PROCESSING_HOUR) {
                    calendar.set(Calendar.DAY_OF_MONTH, (dayOfMonth + 1));
                }
                calendar.set(Calendar.HOUR_OF_DAY, PROCESSING_HOUR);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                Log.e("Current", current.getTime().toString());
                Log.e("Later", calendar.getTime().toString());

                int hours = (int) hoursBetween(current, calendar);
                tvStatus.setText("in " + Math.abs(hours) + " hours");
                tvStatus.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            }
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        tvNumber.setText(transfer.getString(PC.KEY_TRANSFER_PHONE_NUMBER));
        tvAmount.setText(transfer.getString(PC.KEY_TRANSFER_AMOUNT));
        return convertView;
    }

    public int getTotalDay(int month) {
        switch (month) {
            case 1:
                return 31;
            case 2:
                return 28;
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;

            default:
                return 30;
        }
    }

    public static long hoursBetween(Calendar startDate, Calendar endDate) {
        long diff = startDate.getTimeInMillis() - endDate.getTimeInMillis();
        long diffHours = diff / (60 * 60 * 1000);
        return diffHours;
    }

}
