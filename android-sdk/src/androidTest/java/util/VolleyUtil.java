package util;

import com.android.sensorbergVolley.Network;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.android.okvolley.OkHttpStack;

import android.content.Context;

import java.io.File;

public class VolleyUtil {

        public static RequestQueue getCachedVolleyQueue(Context context) {
        Network network = new BasicNetwork(new OkHttpStack());
        File cacheDir = new File(context.getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }
}
