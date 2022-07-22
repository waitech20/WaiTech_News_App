package com.sst.waitech.callbacks;

import com.sst.waitech.models.Ads;
import com.sst.waitech.models.App;
import com.sst.waitech.models.Blog;
import com.sst.waitech.models.Category;
import com.sst.waitech.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class CallbackConfig {

    public Blog blog = null;
    public App app = null;
    public Notification notification = null;
    public Ads ads = null;
    public List<Category> labels = new ArrayList<>();

}
