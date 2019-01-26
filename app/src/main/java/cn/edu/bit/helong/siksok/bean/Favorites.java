package cn.edu.bit.helong.siksok.bean;

import java.util.Date;

public class Favorites {

    public final long id;
    private long no;
    private String name;
    private String urlVideo;
    private String urlImage;

    public Favorites(long id, long no, String name, String urlVideo, String urlImage) {
        this.id = id;
        this.no = no;
        this. name = name;
        this.urlImage = urlImage;
        this.urlVideo = urlVideo;
    }
}
