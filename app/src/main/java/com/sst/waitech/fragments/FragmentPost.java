package com.sst.waitech.fragments;

import static com.sst.waitech.utils.Constant.POST_ORDER;
import static com.sst.waitech.utils.Tools.EXTRA_OBJC;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.activities.ActivityPostDetail;
import com.sst.waitech.activities.MainActivity;
import com.sst.waitech.fragments.adapters.AdapterRecent;
import com.sst.waitech.callbacks.CallbackPost;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.models.Post;
import com.sst.waitech.rests.RestAdapter;
import com.sst.waitech.utils.AdsManager;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPost extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterRecent adapterRecent;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    private ArrayList<Object> feedItems = new ArrayList<>();
    SharedPref sharedPref;
    AdsManager adsManager;
    Tools tools;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post, container, false);

        if (getActivity() != null) {
            sharedPref = new SharedPref(getActivity());
            adsManager = new AdsManager(getActivity());
            tools = new Tools(getActivity());
        }

        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        if (Config.DISPLAY_POST_LIST_DIVIDER) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        }
        //recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterRecent = new AdapterRecent(getActivity(), recyclerView, feedItems);
        recyclerView.setAdapter(adapterRecent);

        adapterRecent.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            ((MainActivity) getActivity()).showInterstitialAd();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        adapterRecent.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getPostToken() != null) {
                requestAction();
            } else {
                adapterRecent.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterRecent.resetListData();
            sharedPref.resetPostToken();
            requestAction();
        });

        requestAction();

        return rootView;
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getPostToken() == null) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getPosts(POST_ORDER, sharedPref.getAPIKey(), sharedPref.getPostToken());
        this.callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updatePostToken(token);
                        Log.d("PAGE_TOKEN", token);
                    } else {
                        sharedPref.resetPostToken();
                        Log.d("PAGE_TOKEN", "Last page there is no token");
                    }
                    adapterRecent.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(Objects.requireNonNull(getActivity()).findViewById(R.id.tab_coordinator_layout), obj));
                } else {
                    onFailRequest();
                }
            }

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Post> items) {
        adapterRecent.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytFailed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoItem.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

}