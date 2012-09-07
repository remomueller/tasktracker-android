package com.github.remomueller.tasktracker.android;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;

import android.graphics.Paint;

public class StickiesShow extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stickies_show);

        // Get the message from the intent
        Intent intent = getIntent();

        int position = Integer.parseInt( intent.getStringExtra(Sticky.STICKY_POSITION) );

        // Hopefully won't be needed in future and can access from database
        Sticky sticky = new Sticky();
        sticky.id = Integer.parseInt( intent.getStringExtra(Sticky.STICKY_ID) );
        sticky.description = intent.getStringExtra(Sticky.STICKY_DESCRIPTION);
        sticky.group_description = intent.getStringExtra(Sticky.STICKY_GROUP_DESCRIPTION);
        sticky.due_date = intent.getStringExtra(Sticky.STICKY_DUE_DATE);
        sticky.completed = Boolean.parseBoolean(intent.getStringExtra(Sticky.STICKY_COMPLETED));

        // Hopefully won't be needed in future and can access from database
        Tag tag = new Tag();
        tag.id = Integer.parseInt( intent.getStringExtra(Tag.TAG_ID) );
        tag.name = intent.getStringExtra(Tag.TAG_NAME);
        tag.color = intent.getStringExtra(Tag.TAG_COLOR);



        // Create the text view
        TextView sticky_id = (TextView) findViewById(R.id.sticky_id);
        TextView description = (TextView) findViewById(R.id.description);
        TextView due_date = (TextView) findViewById(R.id.due_date);

        sticky_id.setText(Integer.toString(sticky.id));

        if(sticky.completed){
            sticky_id.setPaintFlags(sticky_id.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        description.setText(sticky.full_description());

        due_date.setText(sticky.short_due_date());

        TextView single_tag = (TextView) findViewById(R.id.single_tag);
        single_tag.setText(tag.name);
        if(tag.id != 0){
            single_tag.setBackgroundColor(Color.parseColor(tag.color));
        }


    }
}
