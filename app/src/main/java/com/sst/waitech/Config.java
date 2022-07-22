package com.sst.waitech;

import com.sst.waitech.utils.Constant;

public class Config {

    //define your hosted file type from Google Drive or Direct JSON Url
    //supported value : Constant.GOOGLE_DRIVE or Constant.JSON_URL
    public static final int JSON_FILE_HOST_TYPE = Constant.GOOGLE_DRIVE;

    //only used if JSON_FILE_HOST_TYPE = Constant.GOOGLE_DRIVE;
    public static final String GOOGLE_DRIVE_JSON_FILE_ID = "1YK7mZLPyprTKm7vy9KPNTn2AU7UqAw2D";

    //only used if json file is stored on Hosting with direct url, JSON_FILE_HOST_TYPE = Constant.JSON_URL
    public static final String JSON_URL = "https://ioninvest.co/config.json";

    //"published": Order by the date the post was published
    //"updated": Order by the date the post was last updated
    public static final String DISPLAY_POST_ORDER = "published";

    //show short description in post list
    public static final boolean DISPLAY_POST_LIST_SHORT_DESCRIPTION = true;

    //if it's true the first image in the post details will be the main image
    public static final boolean FIRST_POST_IMAGE_AS_MAIN_IMAGE = true;

    //show posts date in the application
    public static final boolean DISPLAY_DATE_LIST_POST = true;

    //show post list with line divider
    public static final boolean DISPLAY_POST_LIST_DIVIDER = false;

    //display related posts
    public static final boolean DISPLAY_RELATED_POSTS = true;

    //label sorting, supported value : Constant.LABEL_NAME_ASCENDING, Constant.LABEL_NAME_DESCENDING or Constant.LABEL_DEFAULT
    public static final String LABELS_SORTING = Constant.LABEL_NAME_ASCENDING;

    //category columns count, supported value : Constant.GRID_3_COLUMNS or Constant.GRID_2_COLUMNS
    public static final int CATEGORY_COLUMN_COUNT = Constant.GRID_3_COLUMNS;

    //category layout style, supported value : Constant.GRID_SMALL or Constant.GRID_MEDIUM
    public static final String CATEGORY_LAYOUT_STYLE = Constant.GRID_SMALL;

    //category image style, supported value : Constant.CIRCULAR or Constant.ROUNDED
    public static final String CATEGORY_IMAGE_STYLE = Constant.CIRCULAR;

    //enable copy text in the story content
    public static final boolean ENABLE_TEXT_SELECTION = false;

    //RTL direction, e.g : for Arabic Language
    public static final boolean ENABLE_RTL_MODE = false;

    //GDPR EU Consent
    public static final boolean LEGACY_GDPR = false;

    //delay splash when remote config finish loading
    public static final int SPLASH_DURATION = 100;

}