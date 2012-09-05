package com.github.remomueller.tasktracker.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;
import com.github.remomueller.tasktracker.android.util.UserFunctionsGSON;


public class StickiesIndex extends SherlockActivity {
    TextView selection;

    private static final String TAG = "TaskTrackerAndroid";

    public ArrayList<Sticky> stickies = new ArrayList<Sticky>();

    public final static String STICKY_POSITION = "com.github.remomueller.tasktracker.android.stickies.STICKY_POSITION";
    public final static String STICKY_ID = "com.github.remomueller.tasktracker.android.stickies.STICKY_ID";
    public final static String STICKY_DESCRIPTION = "com.github.remomueller.tasktracker.android.stickies.STICKY_DESCRIPTION";
    public final static String STICKY_GROUP_DESCRIPTION = "com.github.remomueller.tasktracker.android.stickies.STICKY_GROUP_DESCRIPTION";
    public final static String STICKY_DUE_DATE = "com.github.remomueller.tasktracker.android.stickies.STICKY_DUE_DATE";
    public final static String STICKY_COMPLETED = "com.github.remomueller.tasktracker.android.stickies.STICKY_COMPLETED";

    public final static String TAG_ID = "com.github.remomueller.tasktracker.android.stickies.TAG_ID";
    public final static String TAG_NAME = "com.github.remomueller.tasktracker.android.stickies.TAG_NAME";
    public final static String TAG_COLOR = "com.github.remomueller.tasktracker.android.stickies.TAG_COLOR";


    UserFunctionsGSON userFunctionsGSON;
    Button btnLogout;


    ArrayAdapter<String> mAdapter;
    StickyAdapter stickyAdapter;
    ListView list;
    Context context;

    ActionBar actionBar;

    public ArrayList<Sticky> getStickies() {
        return stickies;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userFunctionsGSON = new UserFunctionsGSON();

        Intent intent = getIntent();

        new DownloadJSONSTickies().execute(MainActivity.HOST_URL);

        setContentView(R.layout.stickies_index);

        list=(ListView)findViewById(R.id.stickies_list);

        stickyAdapter = new StickyAdapter(this, stickies);
        list.setAdapter(stickyAdapter);

        selection=(TextView)findViewById(R.id.selection);
        context = this;

        actionBar = getSupportActionBar();

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sticky sticky = stickies.get(position);

                String text = stickies.get(position).id + ": " + stickies.get(position).description;


                Intent intent = new Intent(context, StickiesShow.class);

                intent.putExtra(STICKY_POSITION, Integer.toString(position));
                intent.putExtra(STICKY_ID, Integer.toString(sticky.id));
                intent.putExtra(STICKY_DESCRIPTION, sticky.description);
                intent.putExtra(STICKY_GROUP_DESCRIPTION, sticky.group_description);
                intent.putExtra(STICKY_DUE_DATE, sticky.due_date);
                intent.putExtra(STICKY_COMPLETED, Boolean.toString(sticky.completed));

                String tag_id = "0";
                String tag_name = "";
                String tag_color = "#80FFFFFF";

                for(int i = 0; i < sticky.tags.length; i++){
                    if(i == 0){
                        tag_id = Integer.toString(sticky.tags[i].id);
                        tag_name = sticky.tags[i].name;
                        tag_color = sticky.tags[i].color;
                    }
                }

                intent.putExtra(TAG_ID, tag_id);
                intent.putExtra(TAG_NAME, tag_name);
                intent.putExtra(TAG_COLOR, tag_color);

                startActivity(intent);
            }

        });

        // user already logged in show dashboard
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                userFunctionsGSON.logoutUser(getApplicationContext());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                // Closing dashboard screen
                finish();
            }
        });
    }

    private class DownloadJSONSTickies extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return getStickiesJSON();
            } catch (IOException e) {
                return "Login Failed: Please make sure your email and password are correct.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String json) {
            String result = "";

            Gson gson = new Gson();

            Sticky[] stickies_array;

            try {
                stickies_array = gson.fromJson(json, Sticky[].class);
                if(stickies_array == null){
                    stickies_array = new Sticky[0];
                }
                result = stickies_array.length + " Stick" + (stickies_array.length == 1 ? "y" : "ies");
                selection.setText(result);
            } catch (JsonParseException e) {
                stickies_array = new Sticky[0];
                result = "Login Failed: Please make sure your email and password are correct.";
                selection.setText(result);
            }

            for(int i = 0; i < stickies_array.length; i++){
                stickies.add(stickies_array[i]);
            }

            stickyAdapter.notifyDataSetChanged();
       }
    }

    private String getStickiesJSON() throws IOException {
      InputStream is = null;
      int len = 1000;

      try {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String s = formatter.format(date);
        String due_date_end_date = s;

        String email = userFunctionsGSON.getEmail(getApplicationContext());
        String password = userFunctionsGSON.getPassword(getApplicationContext());
        String site_url = userFunctionsGSON.getSiteURL(getApplicationContext());

        URL url = new URL(site_url + "/stickies.json?status[]=planned&status[]=completed&owner_id=me&order=stickies.due_date+DESC&due_date_end_date="+due_date_end_date);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET"); /* Can be POST */
        conn.setDoInput(true);
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");


        String decoded = email+":"+password;
        String encoded = Base64.encodeBytes( decoded.getBytes() );

        conn.setRequestProperty("Authorization", "Basic "+encoded);

        // Starts the query
        conn.connect();

        int response = conn.getResponseCode();
        // Log.d(TAG, "The response is: " + response);
        is = conn.getInputStream();

        // Convert the InputStream into a string
        String contentAsString = "";
        if(is != null){
           contentAsString = readIt(is, len);
        }

        if(contentAsString == null){
            contentAsString = "";
        }

        return contentAsString;

      } finally {
        if (is != null) {
            is.close();
        }
      }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        String encoding = "UTF-8";
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, encoding);
        return new String(writer.toString());
    }

}
