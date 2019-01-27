package cn.edu.bit.helong.siksok;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import cn.edu.bit.helong.siksok.utils.ResourceUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CommonMethod {
    public static MultipartBody.Part getMultipartFromUri(String name, Uri uri, Context context) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(context, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }
}
