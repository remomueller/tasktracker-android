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

// import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;


public class StickiesIndex extends SherlockFragmentActivity {
    private static final String TAG = "TaskTrackerAndroid";

    private static final String[] CONTENT = new String[] { "Completed", "Past Due", "Upcoming" };


    StickiesFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;


    public ArrayList<Sticky> stickies = new ArrayList<Sticky>();


    User current_user;
    Project current_project;

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
            case android.R.id.home:
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
                current_user.logoutUser(getApplicationContext());
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

        current_user = new User();

        setContentView(R.layout.simple_tabs);

        mAdapter = new StickiesFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
        mIndicator = indicator;
        mIndicator.setCurrentItem(1);

        Intent intent = getIntent();
        current_project = new Project();

        if(intent.getStringExtra(Project.PROJECT_ID) != null)
        {
            current_project.id = Integer.parseInt( intent.getStringExtra(Project.PROJECT_ID) );
            current_project.name = intent.getStringExtra(Project.PROJECT_NAME);
            // current_project.color = intent.getStringExtra(Project.PROJECT_COLOR);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(current_project.name);
            // actionBar.setTextColor(Color.parseColor(current_project.color));
        }

    }

    class StickiesFragmentAdapter extends FragmentPagerAdapter {
        private int mCount = CONTENT.length;

        public StickiesFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return StickiesFragment.newInstance(position, current_project);
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
