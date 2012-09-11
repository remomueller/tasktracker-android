package com.github.remomueller.tasktracker.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

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
import java.util.Calendar;
import java.util.Date;

import android.widget.Toast;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;

public class StickiesFragment extends SherlockFragment {
    private static final String TAG = "TaskTrackerAndroid";

    private int position = -1;
    Project current_project;
    ListView list;

    public ArrayList<Sticky> stickies = new ArrayList<Sticky>();
    StickyAdapter stickyAdapter;

    public static StickiesFragment newInstance(int location, Project project) {
        StickiesFragment fragment = new StickiesFragment();
        fragment.position = location;
        fragment.current_project = project;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        new GetStickies().execute(Integer.toString(position));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stickies_index, container, false);
        list=(ListView)view.findViewById(R.id.stickies_list);

        stickyAdapter = new StickyAdapter(getActivity(), stickies);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setAdapter(stickyAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sticky sticky = stickies.get(position);

                String text = stickies.get(position).id + ": " + stickies.get(position).description;


                Intent intent = new Intent(getActivity(), StickiesShow.class);

                intent.putExtra(Sticky.STICKY_POSITION, Integer.toString(position));
                intent.putExtra(Sticky.STICKY_ID, Integer.toString(sticky.id));
                intent.putExtra(Sticky.STICKY_DESCRIPTION, sticky.description);
                intent.putExtra(Sticky.STICKY_GROUP_DESCRIPTION, sticky.group_description);
                intent.putExtra(Sticky.STICKY_DUE_DATE, sticky.due_date);
                intent.putExtra(Sticky.STICKY_COMPLETED, Boolean.toString(sticky.completed));

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

                intent.putExtra(Tag.TAG_ID, tag_id);
                intent.putExtra(Tag.TAG_NAME, tag_name);
                intent.putExtra(Tag.TAG_COLOR, tag_color);

                startActivity(intent);
            }

        });

    }


    private class GetStickies extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... tab_page_nums) {
            try {
                return getStickiesFromDB(Integer.parseInt(tab_page_nums[0]));
            } catch (IOException e) {
                return "Unable to Connect: Make sure you have an active network connection.";
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
            } catch (JsonParseException e) {
                stickies_array = new Sticky[0];
                result = "Login Failed: Please make sure your email and password are correct.";
            }

            for(int i = 0; i < stickies_array.length; i++){
                stickies.add(stickies_array[i]);
            }

            if(stickyAdapter != null){
                stickyAdapter.notifyDataSetChanged();
            }
       }
    }

    private String getStickiesFromDB(int location) throws IOException {
      InputStream is = null;
      int len = 1000;

      try {
        Date today = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String due_date_today = formatter.format(today);
        String due_date_tomorrow = formatter.format(tomorrow);

        if(getActivity() == null){
            return "";
        }
        User current_user = new User(getActivity().getApplicationContext());
        // String email = current_user.getEmail(getActivity().getApplicationContext());
        // String password = current_user.getPassword(getActivity().getApplicationContext());
        // String site_url = current_user.getSiteURL(getActivity().getApplicationContext());

        String params = "";

        if(location == 0) { // Completed
            params = "status[]=completed&order=stickies.due_date+DESC&due_date_end_date="+due_date_today;
        }else if(location == 2) { // Upcoming
            params = "status[]=planned&order=stickies.due_date+ASC&due_date_start_date="+due_date_tomorrow;
        }else{ // Past Due
            params = "status[]=planned&order=stickies.due_date+DESC&due_date_end_date="+due_date_today;
        }

        // TODO: Allow user to set preference from 2. View Control
        // http://developer.android.com/design/patterns/actionbar.html
        if(true)
            params = params + "&owner_id=me";

        // Filter by project if project is selected
        if(current_project != null && current_project.id > 0)
            params = params + "&project_id=" + current_project.id;

        URL url = new URL(current_user.site_url + "/stickies.json?" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET"); /* Can be POST */
        conn.setDoInput(true);
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        // conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("WWW-Authenticate", "Basic realm='Application'");


        String decoded = current_user.email+":"+current_user.password;
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
