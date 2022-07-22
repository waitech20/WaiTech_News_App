package com.sst.waitech.activities;

import static com.sst.waitech.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sst.waitech.R;
import com.sst.waitech.callbacks.CallbackPageDetail;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.database.sqlite.DbFavorite;
import com.sst.waitech.models.Post;
import com.sst.waitech.rests.RestAdapter;
import com.sst.waitech.utils.AppBarLayoutBehavior;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPageDetail extends AppCompatActivity {

    private Call<CallbackPageDetail> callbackCall = null;
    private View lytMainContent;
    ImageView primaryImage;
    private WebView webView;
    FrameLayout customViewContainer;
    CoordinatorLayout parentView;
    private ShimmerFrameLayout lytShimmer;
    private SwipeRefreshLayout swipeRefreshLayout;
    String htmlText;
    Post post;
    private String singleChoiceSelected;
    DbFavorite dbFavorite;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_page_detail);
        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);
        dbFavorite = new DbFavorite(this);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this, sharedPref);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setRefreshing(false);

        lytMainContent = findViewById(R.id.lyt_main_content);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        parentView = findViewById(R.id.coordinatorLayout);

        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);
        primaryImage = findViewById(R.id.primary_image);

        requestAction();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
            requestAction();
        });

        setupToolbar();

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostData, Constant.DELAY_REFRESH);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createApiPostDetail(sharedPref.getBloggerId(), "pages", post.id).getPageDetail(sharedPref.getAPIKey());
        this.callbackCall.enqueue(new Callback<CallbackPageDetail>() {
            public void onResponse(Call<CallbackPageDetail> call, Response<CallbackPageDetail> response) {
                CallbackPageDetail resp = response.body();
                if (resp == null) {
                    onFailRequest();
                    return;
                }
                displayData(resp);
                swipeProgress(false);
                lytMainContent.setVisibility(View.VISIBLE);
            }

            public void onFailure(Call<CallbackPageDetail> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lytMainContent.setVisibility(View.GONE);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lytFailed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            lytFailed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytMainContent.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
        });
    }

    private void displayData(CallbackPageDetail resp) {

        Document htmlData = Jsoup.parse(resp.content);
        Elements elements = htmlData.select("img");
        if (elements.hasAttr("src")) {
            Glide.with(this)
                    .load(elements.get(0).attr("src").replace(" ", "%20"))
                    .transition(withCrossFade())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(primaryImage);
            primaryImage.setVisibility(View.VISIBLE);
        } else {
            primaryImage.setVisibility(View.GONE);
        }

        if (htmlData.select("img").first() != null) {
            Element element = htmlData.select("img").first();
            if (element.hasAttr("src")) {
                element.remove();
            }
            htmlText = htmlData.toString();
        } else {
            htmlText = htmlData.toString();
        }

        Tools.displayPostDescription(this, webView, htmlText, customViewContainer, sharedPref);

    }


    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, post.title, true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (menuItem.getItemId() == R.id.action_font_size) {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new AlertDialog.Builder(ActivityPageDetail.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        WebSettings webSettings = webView.getSettings();
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                        } else {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
