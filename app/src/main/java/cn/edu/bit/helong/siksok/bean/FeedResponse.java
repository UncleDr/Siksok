package cn.edu.bit.helong.siksok.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:17
 */
public class FeedResponse {
    @SerializedName("feeds")
    public List<Feed> feeds;

    @SerializedName("success")
    public boolean success;
}
