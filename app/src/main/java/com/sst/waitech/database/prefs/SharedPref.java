package com.sst.waitech.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("blog_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //blog credentials
    public void saveBlogCredentials(String bloggerId, String apiKey) {
        editor.putString("blogger_id", bloggerId);
        editor.putString("api_key", apiKey);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public Boolean getIsNotificationOn() {
        return sharedPreferences.getBoolean("notification", true);
    }

    public void setIsNotificationOn(Boolean isNotificationOn) {
        editor.putBoolean("notification", isNotificationOn);
        editor.apply();
    }

    public void saveConfig(String moreAppsUrl, String redirectUrl, String privacyPolicyUrl, String publisherInfoUrl, String termsConditionsUrl, String displayPageMenu, String displayViewOnSiteMenu, String customLabelList) {
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("redirect_url", redirectUrl);
        editor.putString("privacy_policy_url", privacyPolicyUrl);
        editor.putString("publisher_info_url", publisherInfoUrl);
        editor.putString("terms_conditions_url", termsConditionsUrl);
        editor.putString("display_page_menu", displayPageMenu);
        editor.putString("display_view_on_site_menu", displayViewOnSiteMenu);
        editor.putString("custom_label_list", customLabelList);
        editor.apply();
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public String getRedirectUrl() {
        return sharedPreferences.getString("redirect_url", "");
    }

    public String getPrivacyPolicyUrl() {
        return sharedPreferences.getString("privacy_policy_url", "");
    }

    public String getPublisherInfoUrl() {
        return sharedPreferences.getString("publisher_info_url", "");
    }

    public String getTermsConditionsUrl() {
        return sharedPreferences.getString("terms_conditions_url", "");
    }

    public String getDisplayPageMenu() {
        return sharedPreferences.getString("display_page_menu", "true");
    }

    public String getDisplayViewOnSiteMenu() {
        return sharedPreferences.getString("display_view_on_site_menu", "true");
    }

    public String getCustomLabelList() {
        return sharedPreferences.getString("custom_label_list", "false");
    }

    public String getBloggerId() {
        return sharedPreferences.getString("blogger_id", "0");
    }

    public String getAPIKey() {
        return sharedPreferences.getString("api_key", "0");
    }

    public String getPostId() {
        return sharedPreferences.getString("post_id", "0");
    }

    public void savePostId(String post_id) {
        editor.putString("post_id", post_id);
        editor.apply();
    }

    public void resetPostId() {
        sharedPreferences.edit().remove("post_id").apply();
    }

    //post
    public String getPostToken() {
        return sharedPreferences.getString("post_token", null);
    }

    public void updatePostToken(String post_token) {
        editor.putString("post_token", post_token);
        editor.apply();
    }

    public void resetPostToken() {
        sharedPreferences.edit().remove("post_token").apply();
    }

    //category detail
    public String getCategoryDetailToken() {
        return sharedPreferences.getString("category_detail_token", null);
    }

    public void updateCategoryDetailToken(String category_detail_token) {
        editor.putString("category_detail_token", category_detail_token);
        editor.apply();
    }

    public void resetCategoryDetailToken() {
        sharedPreferences.edit().remove("category_detail_token").apply();
    }

    //search post
    public String getSearchToken() {
        return sharedPreferences.getString("search_token", null);
    }

    public void updateSearchToken(String search_token) {
        editor.putString("search_token", search_token);
        editor.apply();
    }

    public void resetSearchToken() {
        sharedPreferences.edit().remove("search_token").apply();
    }

    //page
    public String getPageToken() {
        return sharedPreferences.getString("page_token", null);
    }

    public void updatePageToken(String page_token) {
        editor.putString("page_token", page_token);
        editor.apply();
    }

    public void resetPageToken() {
        sharedPreferences.edit().remove("page_token").apply();
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

}
