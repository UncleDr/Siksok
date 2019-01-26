package cn.edu.bit.helong.siksok.bean;

import com.google.gson.annotations.SerializedName;


public class PostVideoResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("item")
    public Feed item;
}
