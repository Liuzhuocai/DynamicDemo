package com.dynamic.liuzuo.dynamicicon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dynamic.liuzuo.dynamicicon.ui.DynamicCalendar;
import com.dynamic.liuzuo.dynamicicon.ui.DynamicClock;

public class MainActivity extends AppCompatActivity {
    TextView mClock;
    TextView mCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onFinishInflate();
        initView();
    }

    private void initView() {
        new DynamicCalendar().init(this,mCalendar);
        new DynamicClock().init(this,mClock);
    }

    private void onFinishInflate() {
        mClock = (TextView) findViewById(R.id.clock);
        mCalendar = (TextView) findViewById(R.id.calendar);
    }

}
