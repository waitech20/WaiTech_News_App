package com.sst.waitech.fragments;

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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sst.waitech.R;
import com.sst.waitech.activities.ActivityPageDetail;
import com.sst.waitech.fragments.adapters.AdapterPage;
import com.sst.waitech.callbacks.CallbackPage;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.models.Post;
import com.sst.waitech.rests.RestAdapter;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPage extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterPage adapterPage;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPage> callbackCall = null;
    private ArrayList<Object> feedItems = new ArrayList<>();
    SharedPref sharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_page, container, false);

        if (getActivity() != null) {
            sharedPref = new SharedPref(getActivity());
        }

        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        //recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterPage = new AdapterPage(getActivity(), recyclerView, feedItems);
        recyclerView.setAdapter(adapterPage);

        adapterPage.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityPageDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
        });

        adapterPage.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getPageToken() != null) {
                requestAction();
            } else {
                adapterPage.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterPage.resetListData();
            sharedPref.resetPageToken();
            requestAction();
        });

        requestAction();

        return rootView;
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getPageToken() == null) {
            swipeProgress(true);
        } else {
            adapterPage.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getPages(sharedPref.getAPIKey(), sharedPref.getPageToken());
        this.callbackCall.enqueue(new Callback<CallbackPage>() {
            public void onResponse(Call<CallbackPage> call, Response<CallbackPage> response) {
                CallbackPage resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updatePageToken(token);
                        Log.d("PAGE_TOKEN", token);
                    } else {
                        sharedPref.resetPageToken();
                        Log.d("PAGE_TOKEN", "Last page there is no token");
                    }
                } else {
                    onFailRequest();
                }
            }

            public void onFailure(Call<CallbackPage> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Post> items) {
        adapterPage.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        adapterPage.setLoaded();
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
