package com.sst.waitech.activities;

import static com.sst.waitech.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sst.waitech.R;
import com.sst.waitech.fragments.adapters.AdapterCategoryList;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.database.sqlite.DbFavorite;
import com.sst.waitech.models.Post;
import com.sst.waitech.utils.Constant;
import com.sst.waitech.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityFavoriteDetail extends AppCompatActivity {

    Post post;
    WebView webView;
    FrameLayout customViewContainer;
    String htmlText;
    ArrayList<String> arrayListLabels;
    SharedPref sharedPref;
    DbFavorite dbFavorite;
    private String singleChoiceSelected;
    CoordinatorLayout parentView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_offline);
        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this, sharedPref);
        dbFavorite = new DbFavorite(this);
        parentView = findViewById(R.id.coordinatorLayout);
        displayData();
        setupToolbar();
    }

    public void displayData() {

        ImageView primaryImage = findViewById(R.id.primary_image);
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
            primaryImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                intent.putExtra("image", elements.get(0).attr("src"));
                startActivity(intent);
            });
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

        ((TextView) findViewById(R.id.txt_title)).setText(post.title);
        ((TextView) findViewById(R.id.txt_date)).setText(Tools.getTimeAgo(post.published));

        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);
        Tools.displayPostDescription(this, webView, htmlText, customViewContainer, sharedPref);

        String labels = String.valueOf(post.labels).replace("[[", "").replace("]]", "").replace(", ", ",");
        arrayListLabels = new ArrayList(Arrays.asList((labels.split(","))));
        RecyclerView recyclerView = findViewById(R.id.recycler_view_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterCategoryList adapter = new AdapterCategoryList(this, arrayListLabels);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, items, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(EXTRA_OBJC, items.get(position));
            startActivity(intent);
        });

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
        this.menu = menu;
        addToFavorite();
        if (!sharedPref.getDisplayViewOnSiteMenu().equals("true")) {
            MenuItem viewOnSiteItem = menu.findItem(R.id.action_launch);
            viewOnSiteItem.setVisible(false);
        }
        return true;
    }

    public void addToFavorite() {
        List<Post> data = dbFavorite.getFavRow(post.id);
        if (data.size() == 0) {
            menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite_outline));
        } else {
            if (data.get(0).getId().equals(post.id)) {
                menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_favorite:
                List<Post> posts = dbFavorite.getFavRow(post.id);
                if (posts.size() == 0) {
                    dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                    Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
                    menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite));

                } else {
                    if (posts.get(0).getId().equals(post.id)) {
                        dbFavorite.RemoveFav(new Post(post.id));
                        Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite_outline));
                    }
                }
                break;

            case R.id.action_font_size:
                String[] items = getResources().getStringArray(R.array.dialog_font_size);
                singleChoiceSelected = items[sharedPref.getFontSize()];
                int itemSelected = sharedPref.getFontSize();
                new AlertDialog.Builder(ActivityFavoriteDetail.this)
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
                break;

            case R.id.action_share:
                Tools.shareArticle(this, post);
                break;

            case R.id.action_launch:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url)));
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
