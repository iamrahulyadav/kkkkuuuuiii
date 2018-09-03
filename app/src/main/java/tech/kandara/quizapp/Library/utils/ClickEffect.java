package tech.kandara.quizapp.Library.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ClickEffect {


    public static void Opacity(final View view) {
        view.setOnTouchListener(new OnTouchListener() {

            @SuppressLint({"NewApi", "ClickableViewAccessibility"})
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (v == view) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(.5f);
                    } else {
                        v.setAlpha(1f);
                    }
                }
                return false;
            }
        });
    }

}
