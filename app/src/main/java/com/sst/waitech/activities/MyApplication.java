package com.sst.waitech.activities;

import static com.sst.waitech.Config.GOOGLE_DRIVE_JSON_FILE_ID;
import static com.sst.waitech.Config.JSON_FILE_HOST_TYPE;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.sst.waitech.Config;
import com.sst.waitech.callbacks.CallbackConfig;
import com.sst.waitech.database.prefs.SharedPref;
import com.sst.waitech.models.Notification;
import com.sst.waitech.rests.RestAdapter;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;
import com.solodroid.ads.sdk.format.AppOpenAd;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    Call<CallbackConfig> callbackCall = null;
    private static MyApplication mInstance;
    FirebaseAnalytics mFirebaseAnalytics;
    SharedPref sharedPref;
    private AppOpenAd appOpenAdManager;
    String message = "";
    String bigPicture = "";
    String title = "";
    String link = "";
    String postId = "";
    String uniqueId = "";
    Notification notification;

    public MyApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> {
        });
        mInstance = this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        sharedPref = new SharedPref(this);
        appOpenAdManager = new AppOpenAd.Builder(this).build();
        initNotification();
    }

    public void initNotification() {
        OneSignal.disablePush(false);
        Log.d(TAG, "OneSignal Notification is enabled");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        requestAPI();

        OneSignal.setNotificationOpenedHandler(
                result -> {
                    title = result.getNotification().getTitle();
                    message = result.getNotification().getBody();
                    bigPicture = result.getNotification().getBigPicture();
                    Log.d(TAG, title + ", " + message + ", " + bigPicture);
                    try {
                        uniqueId = result.getNotification().getAdditionalData().getString("unique_id");
                        postId = result.getNotification().getAdditionalData().getString("post_id");
                        link = result.getNotification().getAdditionalData().getString("link");
                        Log.d(TAG, postId + ", " + uniqueId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("unique_id", uniqueId);
                    intent.putExtra("post_id", postId);
                    intent.putExtra("title", title);
                    intent.putExtra("link", link);
                    startActivity(intent);
                });

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);
    }

    private void requestAPI() {
        if (JSON_FILE_HOST_TYPE == 0) {
            callbackCall = RestAdapter.createApiGoogleDrive().getDriveJsonFileId(GOOGLE_DRIVE_JSON_FILE_ID);
            Log.d(TAG, "Request API from Google Drive");
        } else {
            callbackCall = RestAdapter.createApiJsonUrl().getJsonUrl(Config.JSON_URL);
            Log.d(TAG, "Request API from Json Url");
        }
        callbackCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                if (resp != null) {
                    notification = resp.notification;
                    FirebaseMessaging.getInstance().subscribeToTopic(notification.fcm_notification_topic);
                    OneSignal.setAppId(notification.onesignal_app_id);
                    Log.d(TAG, "FCM Subscribe topic : " + notification.fcm_notification_topic);
                    Log.d(TAG, "OneSignal App ID : " + notification.onesignal_app_id);
                }
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                Log.e(TAG, "initialize failed");
            }
        });
    }

    public AppOpenAd getAppOpenAdManager() {
        return this.appOpenAdManager;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}