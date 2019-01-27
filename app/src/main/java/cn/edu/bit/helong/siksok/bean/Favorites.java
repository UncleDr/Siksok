package cn.edu.bit.helong.siksok.bean;

import java.util.Date;

public class Favorites {

    public final long id;
    private long no;
    private String name;
    private String urlVideo;
    private String urlImage;

    public long getId() {
        return id;
    }

    public long getNo() {
        return no;
    }

    public void setNo(long no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Favorites(long id, long no, String name, String urlVideo, String urlImage) {
        this.id = id;
        this.no = no;
        this. name = name;
        this.urlImage = urlImage;
        this.urlVideo = urlVideo;
    }
}
