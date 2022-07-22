package com.sst.waitech.utils;

import static com.sst.waitech.utils.Constant.PAGER_NUMBER_DEFAULT;
import static com.sst.waitech.utils.Constant.PAGER_NUMBER_NO_PAGE;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.sst.waitech.BuildConfig;
import com.sst.waitech.Config;
import com.sst.waitech.R;
import com.sst.waitech.activities.ActivityImageDetail;
import com.sst.waitech.activities.ActivityNotificationDetail;
import com.sst.waitech.activities.ActivityWebView;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.database.sqlite.DbFavorite;
import com.sst.waitech.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.solodroid.ads.sdk.ui.BannerAdView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("deprecation")
public class Tools {

    private Activity activity;
    MenuItem prevMenuItem;
    SharedPref sharedPref;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    DbFavorite dbFavorite;
    private BottomSheetDialog mBottomSheetDialog;

    public Tools(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
    }

    public static void getTheme(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            activity.setTheme(R.style.AppDarkTheme);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        SharedPref sharedPref = new SharedPref(activity);
        activity.setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
            activity.getSupportActionBar().setHomeButtonEnabled(backButton);
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public void setupViewPager(AppCompatActivity activity, ViewPager viewPager, BottomNavigationView navigation, Toolbar toolbar, SharedPref sharedPref) {
        viewPager.setVisibility(View.VISIBLE);
        if (sharedPref.getDisplayPageMenu().equals("true")) {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterDefault(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_page) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_page));
                    } else if (currentItem == 3) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterNoPage(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_PAGE);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void setupViewPagerRTL(AppCompatActivity activity, RtlViewPager viewPager, BottomNavigationView navigation, Toolbar toolbar, SharedPref sharedPref) {
        viewPager.setVisibility(View.VISIBLE);
        if (sharedPref.getDisplayPageMenu().equals("true")) {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterDefault(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_page) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_page));
                    } else if (currentItem == 3) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterNoPage(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_PAGE);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void showBottomSheetDialog(View parentView, Post post) {
        View view = activity.getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgViewSite = view.findViewById(R.id.img_view);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhite));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhite));
            imgViewSite.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhite));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgViewSite.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnView = view.findViewById(R.id.btn_view);
        if (!sharedPref.getDisplayViewOnSiteMenu().equals("true")) {
            btnView.setVisibility(View.GONE);
        }

        btnFavorite.setOnClickListener(v -> {
            List<Post> posts = dbFavorite.getFavRow(post.id);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(parentView, activity.getString(R.string.msg_favorite_added), Snackbar.LENGTH_SHORT).show();
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);

            } else {
                if (posts.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(parentView, activity.getString(R.string.msg_favorite_removed), Snackbar.LENGTH_SHORT).show();
                    imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            shareArticle(activity, post);
            mBottomSheetDialog.dismiss();
        });

        btnView.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url)));
            mBottomSheetDialog.dismiss();
        });

        if (this.sharedPref.getIsDarkTheme()) {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
        } else {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        dbFavorite = new DbFavorite(activity);
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            txtFavorite.setText(activity.getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
        } else {
            if (posts.get(0).id.equals(post.id)) {
                txtFavorite.setText(activity.getString(R.string.favorite_remove));
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            }
        }

    }

    public static void displayPostDescription(Activity activity, WebView webView, String htmlData, FrameLayout viewContainer, SharedPref sharedPref) {
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);

        if (!Config.ENABLE_TEXT_SELECTION) {
            webView.setOnLongClickListener(v -> true);
            webView.setLongClickable(false);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        WebSettings webSettings = webView.getSettings();
        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String bgParagraph;
        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        if (sharedPref.getIsDarkTheme()) {
            bgParagraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bgParagraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String fontStyleDefault = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom_font.ttf\")}body {font-family: MyFont;font-size: medium; text-align: left;}</style>";

        String textDefault = "<html><head>"
                + fontStyleDefault
                + "<style>img{max-width:100%;height:auto;border-radius:8px;margin-top:8px;margin-bottom:8px;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + Tools.parseHtml(htmlData)
                + "</body></html>";

        String textRtl = "<html dir='rtl'><head>"
                + fontStyleDefault
                + "<style>img{max-width:100%;height:auto;border-radius:8px;margin-top:8px;margin-bottom:8px;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + Tools.parseHtml(htmlData)
                + "</body></html>";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent;
                if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif")) {
                    intent = new Intent(activity, ActivityImageDetail.class);
                    intent.putExtra("image", url);
                } else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                }
                activity.startActivity(intent);
                return true;
            }
        });

        BannerAdView bannerAdView = activity.findViewById(R.id.bannerAdView);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                webView.setVisibility(View.INVISIBLE);
                bannerAdView.setVisibility(View.GONE);
                viewContainer.setVisibility(View.VISIBLE);
                viewContainer.addView(view);
                Tools.darkNavigation(activity);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                webView.setVisibility(View.VISIBLE);
                bannerAdView.setVisibility(View.VISIBLE);
                viewContainer.setVisibility(View.GONE);
                Tools.lightNavigation(activity);
            }
        });

        if (Config.ENABLE_RTL_MODE) {
            webView.loadDataWithBaseURL(null, textRtl, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, textDefault, mimeType, encoding, null);
        }
    }

    public static void shareArticle(Activity activity, Post post) {
        String title = Html.fromHtml(post.title).toString();
        String url = post.url;
        String content = Html.fromHtml(activity.getResources().getString(R.string.share_text)).toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url + "\n\n" + content + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void openAssetActivity(Context context, String title, String fileName) {
        Intent intent = new Intent(context, ActivityWebView.class);
        intent.putExtra("title", title);
        intent.putExtra("file_name", fileName);
        context.startActivity(intent);
    }

    public static String parseHtml(String htmlData) {
        if (htmlData != null && !htmlData.trim().equals("")) {
            return htmlData.replace("", "");
        } else {
            return "";
        }
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {

        String uniqueId = getIntent.getStringExtra("unique_id");
        String postId = getIntent.getStringExtra("post_id");
        String title = getIntent.getStringExtra("title");
        String link = getIntent.getStringExtra("link");

        if (getIntent.hasExtra("unique_id")) {

            if (postId != null && !postId.equals("")) {
                if (!postId.equals("0")) {
                    Intent intent = new Intent(context, ActivityNotificationDetail.class);
                    intent.putExtra("post_id", postId);
                    context.startActivity(intent);
                }
            }

            if (link != null && !link.equals("")) {
                Intent intent = new Intent(context, ActivityWebView.class);
                intent.putExtra("title", title);
                intent.putExtra("url", link);
                context.startActivity(intent);
            }

        }

    }

    public static void setNavigation(Activity activity, SharedPref sharedPref) {
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(activity);
        } else {
            Tools.lightNavigation(activity);
        }
        setLayoutDirection(activity);
    }

    public static void setLayoutDirection(Activity activity) {
        if (Config.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.black));
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.black));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.gnt_white));
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public static void transparentStatusBarNavigation(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return milis;
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static CharSequence getTimeAgo(String dateStr) {
        if (dateStr != null && !dateStr.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(dateStr).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFormatedDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            oldFormat.setTimeZone(TimeZone.getTimeZone("CET"));
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
            try {
                String newStr = newFormat.format(oldFormat.parse(dateStr));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void openWebPage(Activity context, String title, String url) {
        Intent intent = new Intent(context, ActivityWebView.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void downloadImage(Activity activity, String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            //Toast.makeText(activity, "Image download started.", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }

}
