package com.baidusoso.wifitransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import timber.log.Timber;

public class WifiConnectChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //监听wifi的开和关
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Timber.d("WIFI_STATE_DISABLED");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Timber.d("WIFI_STATE_DISABLING");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Timber.d("WIFI_STATE_ENABLING");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Timber.d("WIFI_STATE_ENABLED");
                    break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                switch (state) {
                    case CONNECTING:
                        Timber.d("CONNECTING");
                        break;
                    case CONNECTED:
                        Timber.d("CONNECTED");
                        break;
                    case SUSPENDED:
                        Timber.d("SUSPENDED");
                        break;
                    case DISCONNECTING:
                        Timber.d("DISCONNECTING");
                        break;
                    case DISCONNECTED:
                        Timber.d("DISCONNECTED");
                        break;
                    case UNKNOWN:
                        Timber.d("UNKNOWN");
                        break;
                }
            }
        }
    }
}
