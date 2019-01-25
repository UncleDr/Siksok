package cn.edu.bit.helong.siksok.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {
    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("video_url")
    public String videoUrl;

    @SerializedName("student_id")
    public String studentId;

    @SerializedName("user_name")
    public String userName;
}
