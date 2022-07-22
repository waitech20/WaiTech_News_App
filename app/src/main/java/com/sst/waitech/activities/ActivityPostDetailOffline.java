package com.sst.waitech.activities;

import static com.sst.waitech.utils.Tools.EXTRA_OBJC;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.fragments.adapters.AdapterCategoryList;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.models.Post;
import com.sst.waitech.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ActivityPostDetailOffline extends AppCompatActivity {

    Post post;
    WebView webView;
    FrameLayout customViewContainer;
    String htmlText;
    SharedPref sharedPref;
    TextView txtDate;
    LinearLayout lytDate;
    ImageView imgDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_offline);
        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this, sharedPref);
        displayData();
        setupToolbar();

    }

    public void displayData() {

        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);

        ImageView primaryImage = findViewById(R.id.primary_image);
        Document docImage = Jsoup.parse(post.content);
        Elements elementImage = docImage.select("img");
        if (elementImage.hasAttr("src")) {
            Glide.with(this)
                    .load(elementImage.get(0).attr("src").replace(" ", "%20"))
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

        ((TextView) findViewById(R.id.txt_title)).setText(post.title);

        imgDate = findViewById(R.id.ic_date);
        txtDate = findViewById(R.id.txt_date);
        lytDate = findViewById(R.id.lyt_date);

        if (Config.DISPLAY_DATE_LIST_POST) {
            imgDate.setVisibility(View.VISIBLE);
            txtDate.setText(Tools.getFormatedDate(post.published));
            lytDate.setVisibility(View.VISIBLE);
        } else {
            lytDate.setVisibility(View.GONE);
        }

        Document image = Jsoup.parse(post.content);
        Element element = image.select("img").first();
        if (element.hasAttr("src")) {
            element.remove();
        }

        htmlText = image.toString();
        Tools.displayPostDescription(this, webView, htmlText, customViewContainer, sharedPref);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterCategoryList adapter = new AdapterCategoryList(this, post.labels);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
