package com.sst.waitech.activities;

import static com.sst.waitech.utils.Constant.BANNER_CATEGORY_DETAIL;
import static com.sst.waitech.utils.Constant.INTERSTITIAL_POST_LIST;
import static com.sst.waitech.utils.Constant.POST_ORDER;
import static com.sst.waitech.utils.Tools.EXTRA_OBJC;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.fragments.adapters.AdapterRecent;
import com.sst.waitech.callbacks.CallbackPost;
import com.sst.waitech.database.prefs.AdsPref;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.models.Post;
import com.sst.waitech.rests.RestAdapter;
import com.sst.waitech.utils.AdsManager;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetail extends AppCompatActivity {

    private static final String TAG = "ActivityCategoryDetail";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterRecent adapterRecent;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    private ArrayList<Object> feedItems = new ArrayList<>();
    SharedPref sharedPref;
    String category;
    CoordinatorLayout lytParent;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_category_detail);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        tools = new Tools(this);

        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(BANNER_CATEGORY_DETAIL);
        adsManager.loadInterstitialAd(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());

        Tools.setNavigation(this, sharedPref);
        sharedPref.resetCategoryDetailToken();

        category = getIntent().getStringExtra(EXTRA_OBJC);

        lytParent = findViewById(R.id.coordinatorLayout);
        recyclerView = findViewById(R.id.recycler_view);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        if (Config.DISPLAY_POST_LIST_DIVIDER) {
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        }
        //recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterRecent = new AdapterRecent(this, recyclerView, feedItems);
        recyclerView.setAdapter(adapterRecent);

        adapterRecent.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            adsManager.showInterstitialAd();
        });

        adapterRecent.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getCategoryDetailToken() != null) {
                requestAction();
            } else {
                adapterRecent.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterRecent.resetListData();
            sharedPref.resetCategoryDetailToken();
            requestAction();
        });

        requestAction();
        setupToolbar();

    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getCategoryDetailToken() == null) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getCategoryDetail(category, POST_ORDER, sharedPref.getAPIKey(), sharedPref.getCategoryDetailToken());
        this.callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(Call<CallbackPost> call, Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updateCategoryDetailToken(token);
                        Log.d("PAGE_TOKEN", token);
                    } else {
                        sharedPref.resetCategoryDetailToken();
                        Log.d("PAGE_TOKEN", "Last page there is no token");
                    }
                    adapterRecent.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(lytParent, obj));
                } else {
                    onFailRequest();
                }
            }

            public void onFailure(Call<CallbackPost> call, Throwable th) {
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
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
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

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, category, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (menuItem.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
