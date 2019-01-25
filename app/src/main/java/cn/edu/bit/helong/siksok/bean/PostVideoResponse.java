package cn.edu.bit.helong.siksok.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.18 17:53
 */
public class PostVideoResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("item")
    public Feed item;
}
