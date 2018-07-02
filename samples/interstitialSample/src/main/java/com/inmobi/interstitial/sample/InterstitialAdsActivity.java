package com.inmobi.interstitial.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.stetho.Stetho;
import com.inmobi.ads.AdNetworkRequest;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.commons.core.configs.ConfigNetworkRequest;
import com.inmobi.commons.core.utilities.Logger;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InterstitialAdsActivity extends Activity {

    private InMobiInterstitial mInterstitialAd;
    //private InMobiInterstitial mInterstitialAd1;
    private Button mLoadAdButton;
    private Button mShowAdButton;
    private Button mPrefetch;
    private InterstitialApplication interstitialApplication;
    private InterstitialFetcher interstitialFetcher;
    private final String TAG = InterstitialAdsActivity.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private AtomicInteger forcedRetry = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fun();
        Stetho.initializeWithDefaults(this);
        ConfigNetworkRequest.setDefaultConfigUrl("www.google.com");
        JSONObject obj = new JSONObject();

        InMobiSdk.init(this, "12345678901234567890123456789012");
        //InMobiSdk.init(this, "1234567890qwerty0987654321qwerty12345");
        AdNetworkRequest.setAdServerUrl("https://mock1001.sdk.corp.inmobi.com:8081/mockserver/request?request=placementphprequest&time=12345&");

        //AdNetworkRequest.setAdServerUrl("http://rc-studio.inmobi.com/automation/studiokeg/showad.asm?c=inmobi&envelope=false&" + System.currentTimeMillis() + "&");
        //AdNetworkRequest.setAdServerUrl("http://10.14.122.44:8080/mockserver/request?request=placementphprequest&time=12345&" + System.currentTimeMillis() + "&");
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
        Logger.setLogLevel(Logger.InternalLogLevel.INTERNAL);
        setContentView(R.layout.activity_interstitial_ads);
        interstitialApplication = (InterstitialApplication) this.getApplication();

        /*interstitialFetcher = new InterstitialFetcher() {
            @Override
            public void onFetchSuccess() {
                setupInterstitial();
                setupInterstitial1();

            }

            @Override
            public void onFetchFailure() {
                if (forcedRetry.getAndIncrement() < 2) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           // interstitialApplication.fetchInterstitial(interstitialFetcher);
                        }
                    }, 5000);
                } else {
                    adjustButtonVisibility();
                }
            }
        };*/

        mPrefetch = (Button) findViewById(R.id.button_prefetch);
        mLoadAdButton = (Button) findViewById(R.id.button_load_ad);
        mShowAdButton = (Button) findViewById(R.id.button_show_ad);

        mPrefetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadAdButton.setVisibility(View.GONE);
                mShowAdButton.setVisibility(View.GONE);
                forcedRetry.set(0);
                prefetchInterstitial();
            }
        });
        mLoadAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrefetch.setVisibility(View.GONE);
                if (null == mInterstitialAd) {
                    setupInterstitial();
                } else {
                    mInterstitialAd.load();

                    JSONObject jsonobject = mInterstitialAd.getAdMetaInfo();
                    Log.d(TAG, String.valueOf(jsonobject));
                    if (jsonobject.has("bidValue")) {
                        double value=jsonobject.optDouble("bidValue");
                        Log.d(TAG, String.valueOf(value));
                    }

                }
            }
        });

        mShowAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show();
            }
        });
        //setupInterstitial();
    }

    @Override
    public void onResume() {
        super.onResume();
        adjustButtonVisibility();
    }

    private void adjustButtonVisibility() {
        mPrefetch.setVisibility(View.VISIBLE);
        mLoadAdButton.setVisibility(View.VISIBLE);
        mShowAdButton.setVisibility(View.GONE);
    }

    private void setupInterstitial() {

        mInterstitialAd = new InMobiInterstitial(InterstitialAdsActivity.this, PlacementId.YOUR_PLACEMENT_ID,
                new InMobiInterstitial.InterstitialAdListener2() {
                    @Override
                    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        Log.d(TAG, "Unable to load interstitial ad (error message: " +
                                inMobiAdRequestStatus.getMessage());
                    }

                    @Override
                    public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdReceived");
                    }

                    @Override
                    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdLoadSuccessful");
                        /*JSONObject jsonobject = inMobiInterstitial.getAdMetaInfo();
                        Log.d(TAG, String.valueOf(jsonobject));
                        if (jsonobject.has("bidValue")) {
                            double value=jsonobject.optDouble("bidValue");
                            Log.d(TAG, String.valueOf(value));
                        }*/

                        //mInterstitialAd1.load();
                        if (inMobiInterstitial.isReady()) {
                            if (mShowAdButton != null) {
                                mShowAdButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "onAdLoadSuccessful inMobiInterstitial not ready");
                        }
                    }

                    @Override
                    public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        Log.d(TAG, "onAdRewardActionCompleted " + map.size());
                    }

                    @Override
                    public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDisplayFailed " + "FAILED");
                    }

                    @Override
                    public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdWillDisplay " + inMobiInterstitial);
                    }

                    @Override
                    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDisplayed " + inMobiInterstitial);
                    }

                    @Override
                    public void onAdInteraction(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        Log.d(TAG, "onAdInteraction " + inMobiInterstitial);
                    }

                    @Override
                    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDismissed " + inMobiInterstitial);
                    }

                    @Override
                    public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onUserWillLeaveApplication " + inMobiInterstitial);
                    }
                });
        mInterstitialAd.load();
        System.gc();

    }

    /*private void setupInterstitial1() {
        mInterstitialAd1 = new InMobiInterstitial(InterstitialAdsActivity.this, PlacementId.YOUR_PLACEMENT_ID,
                new InMobiInterstitial.InterstitialAdListener2() {
                    @Override
                    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        Log.d(TAG, "Unable to load interstitial ad (error message: " +
                                inMobiAdRequestStatus.getMessage());
                    }

                    @Override
                    public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdReceived");
                    }

                    @Override
                    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdLoadSuccessful1");

                    }

                    @Override
                    public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        Log.d(TAG, "onAdRewardActionCompleted1 " + map.size());
                    }

                    @Override
                    public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDisplayFailed1 " + "FAILED");
                    }

                    @Override
                    public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdWillDisplay1 " + inMobiInterstitial);
                    }

                    @Override
                    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDisplayed 1" + inMobiInterstitial);
                    }

                    @Override
                    public void onAdInteraction(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        Log.d(TAG, "onAdInteraction 1" + inMobiInterstitial);
                    }

                    @Override
                    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onAdDismissed 1" + inMobiInterstitial);
                    }

                    @Override
                    public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                        Log.d(TAG, "onUserWillLeaveApplication 1" + inMobiInterstitial);
                    }
                });
    }*/

    private void prefetchInterstitial() {
//        mInterstitialAd = interstitialApplication.getInterstitial();
//        if (null == mInterstitialAd) {
//            interstitialApplication.fetchInterstitial(interstitialFetcher);
//            return;
//        }
//
//        mInterstitialAd.setInterstitialAdListener(new InMobiInterstitial.InterstitialAdListener2() {
//            @Override
//            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
//                Log.d(TAG, "Unable to load interstitial ad (error message: " +
//                        inMobiAdRequestStatus.getMessage());
//            }
//
//            @Override
//            public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdReceived");
//            }
//
//            @Override
//            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdLoadSuccessful");
//                if (inMobiInterstitial.isReady()) {
//                    if (mShowAdButton != null) {
//                        mShowAdButton.setVisibility(View.VISIBLE);
//                        mShowAdWithAnimation.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    Log.d(TAG, "onAdLoadSuccessful inMobiInterstitial not ready");
//                }
//            }
//
//            @Override
//            public void onAdRewardActionCompleted(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
//                Log.d(TAG, "onAdRewardActionCompleted " + map.size());
//            }
//
//            @Override
//            public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdDisplayFailed " + "FAILED");
//            }
//
//            @Override
//            public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdWillDisplay " + inMobiInterstitial);
//            }
//
//            @Override
//            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdDisplayed " + inMobiInterstitial);
//            }
//
//            @Override
//            public void onAdInteraction(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
//                Log.d(TAG, "onAdInteraction " + inMobiInterstitial);
//            }
//
//            @Override
//            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onAdDismissed " + inMobiInterstitial);
//            }
//
//            @Override
//            public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
//                Log.d(TAG, "onUserWillLeaveApplication " + inMobiInterstitial);
//            }
//        });
//        mInterstitialAd.load();
    }
    /*@IntDef(flag=true,value={DisplayOptions.a, DisplayOptions.b, DisplayOptions.c})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayOptions {
        int a = 2;
        int b = 5;
        int c = 6;
    }


    private void foo(@DisplayOptions int i){
        System.out.println("I is "+i);
    }

    private void fun(){
        foo(DisplayOptions.b&DisplayOptions.c);
    }*/

    @StringDef({DisplayOptions.a, DisplayOptions.b})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayOptions {
        String a = "x";
        String b = "X";

    }


    private void foo(Map<String,Object> map){
        @DisplayOptions  String value=(String)map.get("obj");
        boolean ans=(value.equals(DisplayOptions.a))?true:false;
        //boolean ans1=(j==DisplayOptions.b)?true:false;
        System.out.println("I is "+ans);
    }

    private void fun(){
        setupInterstitial();
        Map<String,Object> mp=new HashMap<>();
        @DisplayOptions String a=DisplayOptions.a;
        mp.put("obj",a);
        foo(mp);
    }

}
