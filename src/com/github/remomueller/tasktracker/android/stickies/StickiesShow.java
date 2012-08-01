package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;

public class StickiesShow extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sticky_show);

        // Get the message from the intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(StickiesIndex.STICKY_ID);


        // Create the text view
        TextView description = (TextView) findViewById(R.id.description);
        // textView.setTextSize(40);
        description.setText(message);
    }
}
