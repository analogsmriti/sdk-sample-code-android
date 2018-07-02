package com.inmobi.banner.sample;


import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.banner.utility.BannerFetcher;
import com.inmobi.banner.utility.Constants;
import com.inmobi.banner.utility.DataFetcher;
import com.inmobi.banner.utility.NewsSnippet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.facebook.drawee.backends.pipeline.Fresco;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.inmobi.banner.utility.Constants.BANNER_HEIGHT;
import static com.inmobi.banner.utility.Constants.BANNER_WIDTH;


public class BannerPrefetchActivity extends AppCompatActivity {

    private static final String TAG = BannerPrefetchActivity.class.getSimpleName();

    private InMobiBanner mBannerAd;
    private ListView mNewsListView;
    private BannerApplication bannerApplication;
    private BannerFetcher bannerFetcher;


    @NonNull
    private final Handler mHandler = new Handler();
    private List<NewsSnippet> mItemList = new ArrayList<>();
    private NewsFeedAdapter mAdapter;
    private AtomicInteger forcedRetry = new AtomicInteger(0);

    public interface OnHeadlineSelectedListener {
        void onArticleSelected(int position);
    }

    private OnHeadlineSelectedListener mCallback = new OnHeadlineSelectedListener() {
        @Override
        public void onArticleSelected(int position) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        bannerApplication = ((BannerApplication) this.getApplication());

        bannerFetcher = new BannerFetcher() {
            @Override
            public void onFetchSuccess() {
                setupBannerAd();
            }

            @Override
            public void onFetchFailure() {
                /*if (forcedRetry.getAndIncrement() < 2) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bannerApplication.fetchBanner(bannerFetcher);
                        }
                    }, 2000);
                }*/
            }
        };
        setContentView(R.layout.activity_banner_ads);
        setupListView();
        getHeadlines();
    }

    private void setupBannerAd() {
        mBannerAd = bannerApplication.getBanner();
        if (null == mBannerAd) {
            Log.d("SOS", "mBannerAd is null.. Fetching again..");
            //bannerApplication.fetchBanner(bannerFetcher);
            return;
        }
        RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.ad_container);
        mBannerAd.setAnimationType(InMobiBanner.AnimationType.ROTATE_HORIZONTAL_AXIS);
        mBannerAd.setRefreshInterval(60);
        mBannerAd.setListener(new InMobiBanner.BannerAdListener() {
            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                Log.d(TAG, "onAdLoadSucceeded");
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner,
                                       InMobiAdRequestStatus inMobiAdRequestStatus) {
                Log.d(TAG, "Banner ad failed to load with error: " +
                        inMobiAdRequestStatus.getMessage());
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                Log.d(TAG, "onAdDisplayed");
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                Log.d(TAG, "onAdDismissed");
            }

            @Override
            public void onAdInteraction(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                Log.d(TAG, "onAdInteraction");
            }

            @Override
            public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                Log.d(TAG, "onUserLeftApplication");
            }

            @Override
            public void onAdRewardActionCompleted(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                Log.d(TAG, "onAdRewardActionCompleted");
            }
        });
        setBannerLayoutParams();
        adContainer.addView(mBannerAd);
        //Providing activity context to show ad
        mBannerAd.load(this);
    }


    private void setBannerLayoutParams() {
        int width = toPixelUnits(BANNER_WIDTH);
        int height = toPixelUnits(BANNER_HEIGHT);
        RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(width, height);
        bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mBannerAd.setLayoutParams(bannerLayoutParams);
    }


    private int toPixelUnits(int dipUnit) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dipUnit * density);
    }

    private void setupListView() {
        mNewsListView = (ListView) findViewById(R.id.lvNewsContainer);
        mAdapter = new NewsFeedAdapter(this, mItemList);
        mNewsListView.setAdapter(mAdapter);
        mNewsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int position, final long id) {
                AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(BannerPrefetchActivity.this);
                confirmationDialog.setTitle("Delete Item?");
                confirmationDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NewsSnippet newsSnippet = mItemList.get(position);
                        mItemList.remove(newsSnippet);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                confirmationDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                confirmationDialog.show();
                return true;
            }
        });

        mNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mCallback.onArticleSelected(position);
                mNewsListView.setItemChecked(position, true);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getHeadlines() {
        new DataFetcher().getFeed(Constants.FEED_URL, new DataFetcher.OnFetchCompletedListener() {
            @Override
            public void onFetchCompleted(@Nullable final String data, @Nullable final String message) {
                if (null != data) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadHeadlines(data);
                        }
                    });
                }
            }
        });
    }


    private void loadHeadlines(String data) {
        try {
            JSONArray feed = new JSONObject(data).
                    getJSONArray(Constants.FeedJsonKeys.FEED_LIST);
            for (int i = 0; i < feed.length(); i++) {
                JSONObject item = feed.getJSONObject(i);
                Log.v(TAG, item.toString());
                NewsSnippet feedEntry = new NewsSnippet();
                try {
                    feedEntry.title = item.getString(Constants.FeedJsonKeys.CONTENT_TITLE);
                    try {
                        JSONObject enclosureObject = item.getJSONObject(Constants.FeedJsonKeys.CONTENT_ENCLOSURE);
                        if (!enclosureObject.isNull(Constants.FeedJsonKeys.CONTENT_LINK)) {
                            feedEntry.imageUrl = item.getJSONObject(Constants.FeedJsonKeys.CONTENT_ENCLOSURE).
                                    getString(Constants.FeedJsonKeys.CONTENT_LINK);
                        } else {
                            feedEntry.imageUrl = Constants.FALLBACK_IMAGE_URL;
                        }
                    } catch (JSONException e) {
                        feedEntry.imageUrl = Constants.FALLBACK_IMAGE_URL;
                    }
                    feedEntry.landingUrl = item.getString(Constants.FeedJsonKeys.CONTENT_LINK);
                    feedEntry.content = item.getString(Constants.FeedJsonKeys.FEED_CONTENT);
                    feedEntry.isSponsored = false;
                    mItemList.add(feedEntry);
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            }
            setupBannerAd();
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.d(TAG, "JSONException for loadHeadlines", e);
        }
    }
}