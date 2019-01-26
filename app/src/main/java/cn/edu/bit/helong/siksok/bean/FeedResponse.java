package cn.edu.bit.helong.siksok.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedResponse {
    @SerializedName("feeds")
    public List<Feed> feeds;

    @SerializedName("success")
    public boolean success;
}
