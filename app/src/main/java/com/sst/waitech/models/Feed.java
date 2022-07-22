package com.sst.waitech.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Feed implements Serializable {

    public String xmlns;
    public List<Category> category = new ArrayList<>();

}