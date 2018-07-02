package com.inmobi.banner.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.inmobi.commons.core.configs.ConfigNetworkRequest;
import com.inmobi.commons.core.utilities.Logger;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONException;
import org.json.JSONObject;

public class BannerBase extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Stetho.initializeWithDefaults(this);
        //InMobiSdk.init(this, "1234567890qwerty0987654321qwerty12345");
        JSONObject obj = new JSONObject();
        try {
            obj.put("gdpr_consent_available", true);
            //Log.e("obj",obj.toString());
            InMobiSdk.init(this, "12345678901234567890123456789012", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        ConfigNetworkRequest.setDefaultConfigUrl("www.google.com");
        //AdNetworkRequest.setAdServerUrl("http://rc-studio.inmobi.com/automation/studiokeg/showad.asm?c=inmobi&envelope=false&" + System.currentTimeMillis() + "&");
        //AdNetworkRequest.setAdServerUrl("http://10.14.122.44:8080/mockserver/request?request=placementphprequest&time=12345&" + System.currentTimeMillis() + "&");
        Logger.setLogLevel(Logger.InternalLogLevel.INTERNAL);

        setContentView(R.layout.banner_base);

        /*Button xmlIntegration = (Button) findViewById(R.id.xmlSample);
        xmlIntegration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BannerBase.this, BannerXmlActivity.class));
            }
        });*/
        Button normalIntegration = (Button) findViewById(R.id.normalBanner);
        normalIntegration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BannerBase.this, BannerAdsActivity.class));

            }
        });
        /*Button prefetchSample = (Button) findViewById(R.id.prefetchSample);
        prefetchSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BannerBase.this, BannerPrefetchActivity.class));
            }
        });*/
    }
}
