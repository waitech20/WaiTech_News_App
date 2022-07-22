package com.sst.waitech.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sst.waitech.BuildConfig;
import com.sst.waitech.R;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.utils.Tools;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ActivitySettings extends AppCompatActivity {

    private static final String TAG = "ActivitySettings";
    SwitchMaterial switchTheme;
    RelativeLayout btnSwitchTheme;
    SharedPref sharedPref;
    LinearLayout parentView;
    private String singleChoiceSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_settings);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this, sharedPref);
        initView();
        setupToolbar();
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.txt_title_settings), true);
    }

    private void initView() {
        parentView = findViewById(R.id.parent_view);

        switchTheme = findViewById(R.id.switch_theme);
        switchTheme.setChecked(sharedPref.getIsDarkTheme());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.e("INFO", "" + isChecked);
            sharedPref.setIsDarkTheme(isChecked);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnSwitchTheme = findViewById(R.id.btn_switch_theme);
        btnSwitchTheme.setOnClickListener(v -> {
            if (switchTheme.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                switchTheme.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                switchTheme.setChecked(true);
            }
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 200);
        });

        findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        findViewById(R.id.btn_text_size).setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new AlertDialog.Builder(ActivitySettings.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                        } else {
                            sharedPref.updateFontSize(2);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        findViewById(R.id.btn_publisher_info).setOnClickListener(v -> Tools.openWebPage(this,
                getString(R.string.title_setting_publisher), sharedPref.getPublisherInfoUrl()
        ));

        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> Tools.openWebPage(this,
                getString(R.string.title_setting_privacy), sharedPref.getPrivacyPolicyUrl()
        ));

        findViewById(R.id.btn_terms_and_conditions).setOnClickListener(v -> Tools.openWebPage(this,
                getString(R.string.title_setting_terms), sharedPref.getTermsConditionsUrl()
        ));

        findViewById(R.id.btn_rate).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))));

        findViewById(R.id.btn_more).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl()))));

        findViewById(R.id.btn_about).setOnClickListener(v -> {
            LayoutInflater layoutInflater = LayoutInflater.from(ActivitySettings.this);
            View view = layoutInflater.inflate(R.layout.dialog_about, null);
            TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
            txtAppVersion.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");
            final AlertDialog.Builder alert = new AlertDialog.Builder(ActivitySettings.this);
            alert.setView(view);
            alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
            alert.show();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
