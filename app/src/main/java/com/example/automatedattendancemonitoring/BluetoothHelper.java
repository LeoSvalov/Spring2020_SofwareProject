package com.example.automatedattendancemonitoring;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Class with static helper functions for more convenient use of bluetooth (including bluetooth LE)
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public final class BluetoothHelper {

    /**
     * To prevent creating instances of this class
     */
    private BluetoothHelper() {
    }

    /**
     * The UUID used to identify this app
     */
    public static final ParcelUuid UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");



    // Services

    /**
     * Default bluetooth adapter
     */
    public static final BluetoothAdapter ADAPTER = BluetoothAdapter.getDefaultAdapter();

    /**
     * Default bluetooth LE advertiser
     */
    public static final BluetoothLeAdvertiser ADVERTISER = (ADAPTER == null) ? null : ADAPTER.getBluetoothLeAdvertiser();

    /**
     * Default bluetooth LE scanner
     */
    public static final BluetoothLeScanner SCANNER = (ADAPTER == null) ? null : ADAPTER.getBluetoothLeScanner();



    // Request constants

    /**
     * Constant used in {@link Activity#startActivityForResult(Intent, int)}
     * to ask user to enable bluetooth
     */
    public static final int REQUEST_ENABLE = 1;

    /**
     * Constant used in {@link Activity#requestPermissions(String[], int)}
     * to ask for {@link Manifest.permission#ACCESS_FINE_LOCATION}
     */
    public static final int REQUEST_ACCESS_LOCATION_PERMISSION = 2;



    // Helper functions

    /**
     * <p>If bluetooth is enabled, executes {@param toExecute} and returns {@code null}.
     * Otherwise, enables bluetooth and registers {@link BroadcastReceiver},
     * which will execute {@param toExecute} after enabling.</p>
     * <p>Note, that if bluetooth will not be enabled for some reason,
     * {@link BroadcastReceiver} will still be registered and will
     * execute {@param toExecute} if bluetooth will be enabled later.
     * To avoid that, use {@link Activity#unregisterReceiver(BroadcastReceiver)},
     * passing returned {@link BroadcastReceiver}.</p>
     *
     * @param activity  activity to register {@link BroadcastReceiver} in
     * @param toExecute code to be executed when bluetooth will be enabled
     * @return {@link BroadcastReceiver} instance used to capture bluetooth enabling
     * of {@code null}, if bluetooth was already enabled
     */
    public static BroadcastReceiver enableAndExecute(Activity activity, Runnable toExecute) {
        if (ADAPTER.isEnabled()) {
            toExecute.run();
            return null;
        }

        BroadcastReceiver stateChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state != BluetoothAdapter.STATE_ON) return;
                toExecute.run();
                activity.unregisterReceiver(this);
            }
        };
        activity.registerReceiver(stateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        ADAPTER.enable();
        return stateChangedReceiver;
    }

    /**
     * Starts LE advertising with default settings.
     * Advertisement data consists of {@link #UUID} and {@code data} parameter.
     * If advertising succeeds and {@code successCallback} is not {@code null},
     * {@code settingsInEffect} is passed to {@code successCallback}.
     * If and advertising is failed and {@code errorCallback} is not {@code null},
     * {@code errorCode} is passed to {@code errorCallback}.
     * Returns {@link AdvertiseCallback} instance, which can be used to stop advertising
     * by passing it to {@link BluetoothLeAdvertiser#stopAdvertising(AdvertiseCallback)}.
     *
     * @param data string of data to be advertised
     * @param successCallback function to be executed in case starting advertising is started successfully
     * @param errorCallback function to be executed in case starting advertising is failed
     * @return {@link AdvertiseCallback} instance used in starting advertising
     */
    public static AdvertiseCallback advertise(@NotNull String data,
                                              @Nullable Consumer<AdvertiseSettings> successCallback,
                                              @Nullable Consumer<Integer> errorCallback) {
        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (successCallback != null) successCallback.accept(settingsInEffect);
                Log.i("advertise", "Advertising started successfully");
            }

            @Override
            public void onStartFailure(int errorCode) {
                if (errorCallback != null) errorCallback.accept(errorCode);
                else Log.w("advertise", "Advertising failed, but no errorCallback provided: " + errorCode);
            }
        };

        ADVERTISER.startAdvertising(
                // settings
                new AdvertiseSettings.Builder().build(),

                // data
                new AdvertiseData.Builder()
                        .addServiceUuid(BluetoothHelper.UUID)
                        .addServiceData(BluetoothHelper.UUID, data.getBytes(StandardCharsets.UTF_8))
                        .build(),

                // callback
                callback
        );

        return callback;
    }

    /**
     * Starts LE scan with default settings and one filter by {@link #UUID}.
     * If scan is successful, passes all received scans to {@code successCallback}.
     * If advertising is failed and {@code errorCallback} is not {@code null},
     * {@code errorCode} is passed to {@code errorCallback}.
     * Returns {@link ScanCallback} instance, which can be used to stop scan
     * by passing it to {@link BluetoothLeScanner#stopScan(ScanCallback)}.
     *
     * @param activity        activity, which will be used to request
     *                        {@link Manifest.permission#ACCESS_FINE_LOCATION} permission
     * @param successCallback function to pass received scan results to
     * @param errorCallback   function to be executed in case starting scan is failed
     * @return {@link ScanCallback} instance used in starting scan
     */
    public static ScanCallback scan(@NotNull Activity activity,
                                    @NotNull Consumer<String> successCallback,
                                    @Nullable Consumer<Integer> errorCallback) {
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                byte[] dataBytes = null;
                if (result != null && result.getScanRecord() != null) {
                    dataBytes = result.getScanRecord().getServiceData(BluetoothHelper.UUID);
                }
                if (dataBytes == null) {
                    Log.w("scan", "got empty result");
                    return;
                }

                String data = new String(dataBytes, StandardCharsets.UTF_8);
                Log.i("scan", "got scan result: " + data);
                successCallback.accept(data);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    this.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                if (errorCallback != null) errorCallback.accept(errorCode);
                else Log.w("scan", "Scanning failed, but no errorCallback provided: " + errorCode);
            }
        };

        SCANNER.startScan(
                //filters
                Collections.singletonList(
                        new ScanFilter.Builder()
                                .setServiceUuid(BluetoothHelper.UUID)
                                .build()
                ),

                //settings
                new ScanSettings.Builder().build(),

                //callback
                callback
        );

        return callback;
    }

}