package com.sensorberg.sdk.internal;

import android.net.Uri;

class UriHelper {
    public static CharSequence toString(Uri uri) {
        if (uri.getAuthority() != null && uri.getPath() != null){
            return uri.getAuthority() + uri.getPath();
        }
        return uri.toString();
    }
}
