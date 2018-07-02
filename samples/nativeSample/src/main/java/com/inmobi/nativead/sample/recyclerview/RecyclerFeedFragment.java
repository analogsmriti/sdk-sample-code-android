package com.inmobi.nativead.sample.recyclerview;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.*;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.nativead.PlacementId;
import com.inmobi.nativead.sample.R;
import com.inmobi.nativead.utility.FeedData;
import com.inmobi.nativead.utility.FeedData.FeedItem;
import com.inmobi.nativead.utility.SwipeRefreshLayoutWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates the use of InMobiNativeStrand to place ads in a RecyclerView.
 * <p/>
 * Note: Swipe to refresh ads.
 */
public class RecyclerFeedFragment extends Fragment {

    private static final String TAG = RecyclerFeedFragment.class.getSimpleName();

    //All the InMobiNativeStrand instances created for this list feed will be held here
    private List<InMobiNative> mStrands = new ArrayList<>();

    private static final int NUM_FEED_ITEMS = 20;
    //Position in feed where the Ads needs to be placed once loaded.

    private int[] mAdPositions = new int[]{8};

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mFeedAdapter;
    private ArrayList<FeedItem> mFeedItems;

    private Map<Integer, FeedItem> mFeedMap = new HashMap<>();

    public static String getTitle() {
        return "RecyclerView Placement";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recycler_feed, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        final SwipeRefreshLayout swipeRefreshLayout = SwipeRefreshLayoutWrapper.getInstance(getActivity(),
                new SwipeRefreshLayoutWrapper.Listener() {
                    @Override
                    public boolean canChildScrollUp() {
                        return mRecyclerView.getVisibility() == View.VISIBLE && canViewScrollUp(mRecyclerView);
                    }

                    @Override
                    public void onRefresh() {
                        refreshAds();
                    }
                });
        swipeRefreshLayout.addView(view,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return swipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFeedItems = FeedData.generateFeedItems(NUM_FEED_ITEMS);
        mFeedAdapter = new FeedsAdapter(mFeedItems, getActivity());
        mRecyclerView.setAdapter(mFeedAdapter);
        mFeedAdapter.notifyDataSetChanged();
        createStrands();
        loadAds();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mFeedAdapter.notifyDataSetChanged();
    }

    private void createStrands() {
        for (int position : mAdPositions) {
            final InMobiNative nativeStrand = new InMobiNative(getContext(),
                    PlacementId.YOUR_PLACEMENT_ID_HERE, new StrandAdListener(position));
            mStrands.add(nativeStrand);
        }
    }

    @Override
    public void onDestroyView() {
        clearAds();
        super.onDestroyView();
    }

    private void loadAds() {
        for (final InMobiNative strand : mStrands) {
            strand.load();
            //strand.load();
            /*ScheduledExecutorService scheduler
                    = Executors.newSingleThreadScheduledExecutor();

            Runnable task = new Runnable() {
                public void run() {
                    strand.load();
                }
            };

            int delay = 2;
            scheduler.schedule(task, delay, TimeUnit.SECONDS);
            scheduler.shutdown();*/
        }
    }

    private void refreshAds() {
        clearAds();
        createStrands();
        loadAds();
    }

    private void clearAds() {
        Iterator<FeedItem> feedItemIterator = mFeedItems.iterator();
        while (feedItemIterator.hasNext()) {
            final FeedItem feedItem = feedItemIterator.next();
            if (feedItem instanceof AdFeedItem) {
                feedItemIterator.remove();
            }
        }
        mFeedAdapter.notifyDataSetChanged();
        for (InMobiNative strand : mStrands) {
            strand.destroy();
        }
        mStrands.clear();
        mFeedMap.clear();
    }

    private boolean canViewScrollUp(RecyclerView recyclerView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(recyclerView, -1);
        } else {
            return recyclerView.getChildCount() > 0 &&
                    (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() > 0
                            || recyclerView.getChildAt(0).getTop() < recyclerView.getPaddingTop());
        }
    }

    private final class StrandAdListener implements InMobiNative.NativeAdListener {

        private int mPosition;

        public StrandAdListener(int position) {
            mPosition = position;
        }

        @Override
        public void onAdLoadSucceeded(@NonNull InMobiNative inMobiNativeStrand) {
            /*JSONObject jsonobject = inMobiNativeStrand.getAdMetaInfo();
            Log.d(TAG, String.valueOf(jsonobject));
            if (jsonobject.has("bidValue")) {
                double value=jsonobject.optDouble("bidValue");
                Log.d(TAG, String.valueOf(value));
            }*/
            Log.d(TAG, "Strand loaded at position " + mPosition);
            if (!mFeedItems.isEmpty()) {
                FeedData.FeedItem oldFeedItem = mFeedMap.get(mPosition);
                if (oldFeedItem != null) {
                    mFeedMap.remove(mPosition);
                    mFeedItems.remove(oldFeedItem);
                }
                AdFeedItem adFeedItem = new AdFeedItem(inMobiNativeStrand);
                mFeedMap.put(mPosition, adFeedItem);
                mFeedItems.add(mPosition, adFeedItem);
                mFeedAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull InMobiNative inMobiNativeStrand, @NonNull final InMobiAdRequestStatus inMobiAdRequestStatus) {
            Log.d(TAG, "Ad Load failed  for" + mPosition + "(" + inMobiAdRequestStatus.getMessage() + ")");
            if (!mFeedItems.isEmpty()) {
                FeedData.FeedItem oldFeedItem = mFeedMap.get(mPosition);
                if (oldFeedItem != null) {
                    mFeedMap.remove(mPosition);
                    mFeedItems.remove(oldFeedItem);
                    mFeedAdapter.notifyItemRemoved(mPosition);
                    Log.d(TAG, "Ad removed for" + mPosition);
                }
            }
        }

        @Override
        public void onAdFullScreenDismissed(InMobiNative inMobiNative) {
        }

        @Override
        public void onAdFullScreenWillDisplay(InMobiNative inMobiNative) {
        }

        @Override
        public void onAdFullScreenDisplayed(InMobiNative inMobiNative) {
        }

        @Override
        public void onUserWillLeaveApplication(InMobiNative inMobiNative) {
        }

        @Override
        public void onAdImpressed(@NonNull InMobiNative inMobiNativeStrand) {
            Log.d("Impressed", "Impression recorded for strand at position:" + mPosition);
        }

        @Override
        public void onAdClicked(@NonNull InMobiNative inMobiNativeStrand) {
            Log.d(TAG, "Click recorded for ad at position:" + mPosition);
        }

        @Override
        public void onMediaPlaybackComplete(@NonNull InMobiNative inMobiNative) {
        }

        @Override
        public void onAdStatusChanged(@NonNull InMobiNative inMobiNative) {
        }

        @Override
        public void onUserSkippedMedia(@NonNull InMobiNative inMobiNative) {

        }
    }
}