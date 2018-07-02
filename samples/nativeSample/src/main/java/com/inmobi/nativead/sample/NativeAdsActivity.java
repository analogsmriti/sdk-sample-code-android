package com.inmobi.nativead.sample;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.inmobi.commons.core.configs.ConfigNetworkRequest;
import com.inmobi.commons.core.utilities.Logger;
import com.inmobi.sdk.InMobiSdk;

import com.inmobi.ads.AdNetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class NativeAdsActivity extends AppCompatActivity {
    public static int loop = 0;
    Thread.UncaughtExceptionHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);

        /*handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("TEST", "" + e.getMessage());
                Log.e("TEST", "" + e.getStackTrace());
                e.printStackTrace();
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(handler);*/

        //Initialize Inmobi SDK before any API call.

        ConfigNetworkRequest.setDefaultConfigUrl("www.google.com");
        AdNetworkRequest.setAdServerUrl("http://rc-studio.inmobi.com/automation/studiokeg/showad.asm?c=inmobi&envelope=false&" + System.currentTimeMillis() + "&");
        //AdNetworkRequest.setGzipEnabled(false);
        //AdNetworkRequest.setAdServerUrl("http://10.14.122.44:8080/mockserver/request?request=placementphprequest&time=12345&" + System.currentTimeMillis() + "&");
        //AdNetworkRequest.setAdServerUrl("https://mock1001.sdk.corp.inmobi.com:8081/mockserver/request?request=placementphprequest&time=12345&");
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        Logger.setLogLevel(Logger.InternalLogLevel.INTERNAL);
        InMobiSdk.init(this, "12345678901234567890123456789012");



        setContentView(R.layout.activity_native_ads);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), tabLayout);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

}