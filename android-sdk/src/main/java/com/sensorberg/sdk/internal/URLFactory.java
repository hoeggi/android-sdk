package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.BuildConfig;

import android.net.Uri;
import android.os.Build;

public class URLFactory {

    private static final String PRODUCTION_BASE_URL     = "connect.sensorberg.com";
    private static final String STAGING_BASE_URL        = "staging-connect.sensorberg.com";
    private static final String TEST_BASE_URL           = "test-connect.sensorberg.com";
    private static final String BIGBANG_BASE_URL        = "172.17.12.103:8091";

    private static final String PRODUCTION_RESOLVER_URL = "https://resolver.sensorberg.com/layout/";
    private static final String STAGING_RESOLVER_URL    = "https://staging-resolver.sensorberg.com/layout/";
    private static final String TEST_RESOLVER_URL       = "https://test-resolver.sensorberg.com/layout/";
    private static final String BIGBANG_RESOLVER_URL    = "http://172.17.12.103:8093/layout/";



    private static String SCHEME = "https";

    private static String BASE_URL = PRODUCTION_BASE_URL;
    private static String customResolverURL = PRODUCTION_RESOLVER_URL;

    public static Conf switchToProductionEnvironment(){
        Conf previousConf = new Conf(BASE_URL, SCHEME, customResolverURL);
        BASE_URL = PRODUCTION_BASE_URL;
        SCHEME = "https";
        customResolverURL = PRODUCTION_RESOLVER_URL;
        return previousConf;
    }

    public static Conf switchToStagingEnvironment(){
        Conf previousConf = new Conf(BASE_URL, SCHEME, customResolverURL);
        BASE_URL = STAGING_BASE_URL;
        SCHEME = "https";
        customResolverURL = STAGING_RESOLVER_URL;
        return previousConf;
    }

    public static Conf switchToTestEnvironment(){
        Conf previousConf = new Conf(BASE_URL, SCHEME, customResolverURL);
        BASE_URL = TEST_BASE_URL;
        SCHEME = "https";
        customResolverURL = TEST_RESOLVER_URL;
        return previousConf;
    }

    public static Conf switchToBigbangEnvironment(){
        Conf previousConf = new Conf(BASE_URL, SCHEME, customResolverURL);
        BASE_URL = BIGBANG_BASE_URL;
        SCHEME = "http";
        customResolverURL = BIGBANG_RESOLVER_URL;
        return previousConf;
    }

    public static Conf switchToMockEnvironment(Uri url) {
        Conf previousConf = new Conf(BASE_URL, SCHEME, customResolverURL);
        BASE_URL = url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
        SCHEME = url.getScheme();
        customResolverURL = null;
        return previousConf;
    }

    private static Uri.Builder BaseUri() {
        return new Uri.Builder()
                .scheme(SCHEME)
                .encodedAuthority(BASE_URL)
                .appendPath("api");
    }

    public static String getPingURL() {
        return BaseUri().appendEncodedPath("status/version").build().toString();
    }

    public static String getSettingsURLString(String apiKey) {
        return getSettingsURLString(null, apiKey);
    }

    public static String getSettingsURLString(Long revision, String apiKey){
        Uri.Builder builder = BaseUri()
                .appendEncodedPath("applications/")
                .appendPath(apiKey)
                .appendPath("settings")
                .appendPath("android")
                .appendPath(BuildConfig.SDK_VERSION).appendPath(Build.VERSION.RELEASE).appendPath(Build.MANUFACTURER)
                .appendPath(android.os.Build.MODEL + ":" + android.os.Build.PRODUCT);

        if (revision != null) {
            builder.appendQueryParameter("revision", revision.toString());
        }
        return builder.toString();
    }

    public static String getResolveURLString() {
        if (customResolverURL != null){
            return customResolverURL;
        }
        return BaseUri().appendPath("layout/").toString();
    }

    public static void setLayoutURL(String newResolverURL) {
        if (newResolverURL == null) {
            customResolverURL = PRODUCTION_RESOLVER_URL;
            return;
        }
        customResolverURL = newResolverURL;
    }

    public static void restorePreviousConf(Conf previousConfiguration) {
        BASE_URL = previousConfiguration.baseUrl;
        SCHEME = previousConfiguration.scheme;
        customResolverURL = previousConfiguration.customResolverURL;
    }

    public static class Conf {
        private final String baseUrl;
        private final String scheme;
        private final String customResolverURL;

        public Conf(String baseUrl, String scheme, String customResolverURL) {
            this.baseUrl = baseUrl;
            this.scheme = scheme;
            this.customResolverURL = customResolverURL;
        }
    }
}
