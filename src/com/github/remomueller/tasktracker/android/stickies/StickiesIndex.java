package com.github.remomueller.tasktracker.android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.PageIndicator;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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

import android.widget.Toast;

// From libs directory
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import com.github.remomueller.tasktracker.android.util.Base64;
import com.github.remomueller.tasktracker.android.util.UserFunctionsGSON;


public class StickiesIndex extends SherlockFragmentActivity {
    private static final String TAG = "TaskTrackerAndroid";

    private static final String[] CONTENT = new String[] { "Recently Completed", "Past Due", "Upcoming" };


    TestFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;


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

    StickyAdapter stickyAdapter;
    ListView list;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.projects:
                intent = new Intent(getApplicationContext(), ProjectsIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.stickies:
                intent = new Intent(getApplicationContext(), StickiesIndex.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.logout:
                userFunctionsGSON.logoutUser(getApplicationContext());
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();

        userFunctionsGSON = new UserFunctionsGSON();

        setContentView(R.layout.simple_tabs);

        mAdapter = new TestFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
        mIndicator = indicator;
        mIndicator.setCurrentItem(1);

    }

    class TestFragmentAdapter extends FragmentPagerAdapter {
        private int mCount = CONTENT.length;

        public TestFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TestFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position];
        }
    }

}
