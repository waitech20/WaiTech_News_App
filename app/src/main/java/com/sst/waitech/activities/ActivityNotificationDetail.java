package com.sst.waitech.activities;

import static com.sst.waitech.utils.Constant.BANNER_POST_DETAIL;
import static com.sst.waitech.utils.Constant.NATIVE_AD_POST_DETAIL;
import static com.sst.waitech.utils.Constant.POST_ORDER;
import static com.sst.waitech.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sst.waitech.BuildConfig;
import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.fragments.adapters.AdapterCategoryList;
import com.sst.waitech.fragments.adapters.AdapterRelated;
import com.sst.waitech.callbacks.CallbackPost;
import com.sst.waitech.callbacks.CallbackPostDetail;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.database.sqlite.DbFavorite;
import com.sst.waitech.models.Post;
import com.sst.waitech.rests.RestAdapter;
import com.sst.waitech.utils.AdsManager;
import com.sst.waitech.utils.AppBarLayoutBehavior;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNotificationDetail extends AppCompatActivity {

    private Call<CallbackPostDetail> callbackCall = null;
    private final ArrayList<Object> feedItems = new ArrayList<>();
    private View lytMainContent;
    private View lytUncategorized;
    RecyclerView recyclerView;
    ImageButton btnFavorite;
    ImageButton btnFontSize;
    ImageButton btnShare;
    ImageButton btnView;
    TextView txtTitle, txtDate, txtAlphabet;
    LinearLayout lytDate;
    ImageView imgDate;
    ImageView primaryImage;
    RelativeLayout lytPrimaryImage;
    private WebView webView;
    FrameLayout customViewContainer;
    CoordinatorLayout parentView;
    private ShimmerFrameLayout lytShimmer;
    private SwipeRefreshLayout swipeRefreshLayout;
    String originalHtmlData;
    DbFavorite dbFavorite;
    private String singleChoiceSelected;
    SharedPref sharedPref;
    String label;
    LinearLayout lytTitle;
    AdsManager adsManager;
    Tools tools;
    String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_notification_detail);
        sharedPref = new SharedPref(this);
        adsManager = new AdsManager(this);
        tools = new Tools(this);
        Tools.setNavigation(this, sharedPref);

        postId = getIntent().getStringExtra("post_id");
        Log.d("push_notification", "post id : " + postId);

        adsManager.loadBannerAd(BANNER_POST_DETAIL);

        dbFavorite = new DbFavorite(this);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setRefreshing(false);

        lytMainContent = findViewById(R.id.lyt_main_content);
        lytUncategorized = findViewById(R.id.view_uncategorized);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        parentView = findViewById(R.id.coordinatorLayout);

        lytTitle = findViewById(R.id.lyt_title);
        if (sharedPref.getIsDarkTheme()) {
            lytTitle.setBackgroundResource(R.color.colorBackgroundDark);
        } else {
            lytTitle.setBackgroundResource(R.color.colorBackgroundLight);
        }

        recyclerView = findViewById(R.id.recycler_view_category);
        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);
        primaryImage = findViewById(R.id.primary_image);
        lytPrimaryImage = findViewById(R.id.lytPrimaryImage);
        txtTitle = findViewById(R.id.txt_title);
        txtAlphabet = findViewById(R.id.txt_alphabet);
        imgDate = findViewById(R.id.ic_date);
        txtDate = findViewById(R.id.txt_date);
        lytDate = findViewById(R.id.lyt_date);

        btnFavorite = findViewById(R.id.btn_favorite);
        btnFontSize = findViewById(R.id.btn_font_size);
        btnShare = findViewById(R.id.btn_share);
        btnView = findViewById(R.id.btn_view);

        requestAction();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeProgress(true);
            new Handler().postDelayed(() -> swipeProgress(false), 1000);
        });

        setupToolbar();

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostData, Constant.DELAY_REFRESH);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createApiPostDetail(sharedPref.getBloggerId(), "posts", postId).getPostDetail(sharedPref.getAPIKey());
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(@NonNull Call<CallbackPostDetail> call, @NonNull Response<CallbackPostDetail> response) {
                CallbackPostDetail resp = response.body();
                if (resp == null) {
                    onFailRequest();
                    Log.d("Retrofit", "response error " + response.errorBody());
                    return;
                }
                displayData(resp);
                swipeProgress(false);
                lytMainContent.setVisibility(View.VISIBLE);
                Log.d("Retrofit", "response success");
            }

            public void onFailure(@NonNull Call<CallbackPostDetail> call, @NonNull Throwable th) {
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
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
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

    private void displayData(CallbackPostDetail post) {

        Document htmlData = Jsoup.parse(post.content);
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
            txtAlphabet.setVisibility(View.GONE);

            primaryImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                intent.putExtra("image", elements.get(0).attr("src"));
                startActivity(intent);
            });

        } else {
            primaryImage.setVisibility(View.GONE);
            txtAlphabet.setVisibility(View.VISIBLE);
            txtAlphabet.setText(post.title.substring(0, 1));
        }

        if (Config.FIRST_POST_IMAGE_AS_MAIN_IMAGE) {
            if (htmlData.select("img").first() != null) {
                Element element = htmlData.select("img").first();
                if (element.hasAttr("src")) {
                    element.remove();
                }
            }
            lytPrimaryImage.setVisibility(View.VISIBLE);
        } else {
            lytPrimaryImage.setVisibility(View.GONE);
        }
        originalHtmlData = htmlData.toString();

        txtTitle.setText(post.title);

        if (Config.DISPLAY_DATE_LIST_POST) {
            imgDate.setVisibility(View.VISIBLE);
            txtDate.setText(Tools.getFormatedDate(post.published));
            lytDate.setVisibility(View.VISIBLE);
        } else {
            lytDate.setVisibility(View.GONE);
        }

        Tools.displayPostDescription(this, webView, originalHtmlData, customViewContainer, sharedPref);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterCategoryList adapter = new AdapterCategoryList(this, post.labels);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, items, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(EXTRA_OBJC, items.get(position));
            startActivity(intent);
        });

        if (post.labels.size() > 0) {
            if (post.labels.get(0).contains("[") && post.labels.get(0).contains("]")) {
                label = post.labels.get(0).replace("[", "").replace("]", "");
            } else {
                label = post.labels.get(0);
            }
            lytUncategorized.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (Config.DISPLAY_RELATED_POSTS) {
                requestRelatedAPI(label);
            } else {
                findViewById(R.id.viewRelatedPosts).setVisibility(View.GONE);
            }
        } else {
            lytUncategorized.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        if (sharedPref.getIsDarkTheme()) {
            lytUncategorized.setBackgroundResource(R.drawable.bg_chips_dark);
        } else {
            lytUncategorized.setBackgroundResource(R.drawable.bg_chips_default);
        }

        addToFavorite(post);
        adsManager.loadNativeAd(NATIVE_AD_POST_DETAIL);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }


    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
        ((TextView) findViewById(R.id.toolbar_title)).setText("");
    }

    public void addToFavorite(CallbackPostDetail post) {
        List<Post> data = dbFavorite.getFavRow(postId);
        if (data.size() == 0) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        } else {
            if (data.get(0).getId().equals(postId)) {
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            }
        }

        btnFavorite.setOnClickListener(v -> {
            List<Post> posts = dbFavorite.getFavRow(postId);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            } else {
                if (posts.get(0).getId().equals(postId)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
                }
            }
        });

        btnFontSize.setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new AlertDialog.Builder(ActivityNotificationDetail.this)
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
        });

        btnShare.setOnClickListener(v -> {
            String title = Html.fromHtml(post.title).toString();
            String url = post.url;
            String content = Html.fromHtml(getResources().getString(R.string.share_text)).toString();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url + "\n\n" + content + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        if (!sharedPref.getDisplayViewOnSiteMenu().equals("true")) {
            btnView.setVisibility(View.GONE);
        }
        btnView.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url))));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
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

    private void requestRelatedAPI(String category) {
        TextView txtRelated = findViewById(R.id.txt_related);
        RelativeLayout lytRelated = findViewById(R.id.lyt_related);
        RecyclerView recyclerViewRelated = findViewById(R.id.recycler_view_related);
        recyclerViewRelated.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerViewRelated.setNestedScrollingEnabled(false);
        AdapterRelated adapterRelated = new AdapterRelated(this, recyclerViewRelated, feedItems);
        recyclerViewRelated.setAdapter(adapterRelated);

        Call<CallbackPost> callbackCallRelated = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getRelatedPosts(category, POST_ORDER, sharedPref.getAPIKey());
        callbackCallRelated.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    txtRelated.setText(getString(R.string.txt_related));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        lytRelated.setVisibility(View.VISIBLE);
                    }, 2000);
                    adapterRelated.insertData(resp.items);
                    adapterRelated.setOnItemClickListener((view, obj, position) -> {
                        Intent intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
                        intent.putExtra("post_id", obj.id);
                        startActivity(intent);
                        sharedPref.savePostId(obj.id);
                    });
                    adapterRelated.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(parentView, obj));
                    if (resp.items.size() == 1) {
                        txtRelated.setText("");
                        lytRelated.setVisibility(View.GONE);
                    }
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
