package com.safeq.app.plugins.wifiaware;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "WifiAware")
public class WifiAwarePlugin extends Plugin {

    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession currentAwareSession;
    private static final String TAG = "WifiAwarePlugin";
    public static final String SERVICE_NAME = "com.safeq.app.emergency";

    @Override
    public void load() {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            wifiAwareManager = (WifiAwareManager) getContext().getSystemService(Context.WIFI_AWARE_SERVICE);
        }
    }

    @PluginMethod
    public void startPublishing(PluginCall call) {
        if (wifiAwareManager == null) {
            call.reject("Wi-Fi Aware not supported on this device.");
            return;
        }

        AttachCallback attachCallback = new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                currentAwareSession = session;
                PublishConfig config = new PublishConfig.Builder()
                    .setServiceName(SERVICE_NAME)
                    .build();

                session.publish(config, new DiscoverySessionCallback() {
                    @Override
                    public void onPublishStarted(PublishDiscoverySession session) {
                        Log.d(TAG, "Wi-Fi Aware publish started.");
                        call.resolve();
                    }

                    @Override
                    public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                        // Handle incoming messages from subscribers if needed
                    }
                }, null);
            }

            @Override
            public void onAttachFailed() {
                Log.e(TAG, "Wi-Fi Aware attach failed.");
                call.reject("Wi-Fi Aware attach failed.");
            }
        };

        wifiAwareManager.attach(attachCallback, null);
    }

    @PluginMethod
    public void startSubscribing(PluginCall call) {
        if (wifiAwareManager == null) {
            call.reject("Wi-Fi Aware not supported on this device.");
            return;
        }

        AttachCallback attachCallback = new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                currentAwareSession = session;
                SubscribeConfig config = new SubscribeConfig.Builder()
                    .setServiceName(SERVICE_NAME)
                    .build();

                session.subscribe(config, new DiscoverySessionCallback() {
                    @Override
                    public void onSubscribeStarted(SubscribeDiscoverySession session) {
                        Log.d(TAG, "Wi-Fi Aware subscribe started.");
                        call.resolve();
                    }

                    @Override
                    public void onServiceDiscovered(PeerHandle peerHandle, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                        Log.d(TAG, "Wi-Fi Aware service discovered from peer: " + peerHandle);
                        JSObject ret = new JSObject();
                        ret.put("peerHandle", peerHandle.toString());
                        // Additional info can be sent if needed
                        notifyListeners("serviceDiscovered", ret);
                    }
                }, null);
            }

             @Override
            public void onAttachFailed() {
                Log.e(TAG, "Wi-Fi Aware attach failed.");
                call.reject("Wi-Fi Aware attach failed.");
            }
        };

        wifiAwareManager.attach(attachCallback, null);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        if (currentAwareSession != null) {
            currentAwareSession.close();
            currentAwareSession = null;
            Log.d(TAG, "Wi-Fi Aware session stopped.");
        }
        call.resolve();
    }
}
