package com.example.automatedattendancemonitoring;

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

    /**
     * Default bluetooth adapter
     */
    public static final BluetoothAdapter ADAPTER = BluetoothAdapter.getDefaultAdapter();



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
    public static BroadcastReceiver enableBluetoothAndExecute(Activity activity, Runnable toExecute) {
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


    // Advertising functions


    /**
     * Enables all necessary services and
     * starts Bluetooth LE advertising using {@link #advertise(String, AdvertiseCallback)}.
     * If advertising succeeds, {@code onAdvertisingStarted} is executed.
     * If advertising is failed for some reason, {@code onError} is called.
     * Returns {@link AdvertiseCallback} instance, which can be used to stop advertising
     * by passing it to {@link BluetoothLeAdvertiser#stopAdvertising(AdvertiseCallback)}.
     *
     * @param activity activity, which will be used to request permissions
     * @param data string of data to be advertised
     * @param onAdvertisingStarted function to be executed in case starting advertising is started successfully
     * @param onError function to be executed in case starting advertising is failed
     * @return {@link AdvertiseCallback} instance used in starting advertising
     */
    public static AdvertiseCallback advertise(@NotNull Activity activity,
                                              @NotNull String data,
                                              @Nullable Consumer<AdvertiseSettings> onAdvertisingStarted,
                                              @Nullable Consumer<Integer> onError) {
        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (onAdvertisingStarted != null) onAdvertisingStarted.accept(settingsInEffect);
                Log.i("advertise", "Advertising started successfully");
            }

            @Override
            public void onStartFailure(int errorCode) {
                if (onError != null) onError.accept(errorCode);
                else
                    Log.w("advertise", "Advertising failed, but no onError provided: " + errorCode);
            }
        };

        enableBluetoothAndExecute(activity, () -> advertise(data, callback));

        return callback;
    }

    /**
     * Starts Bluetooth LE advertising with default settings and given {@link AdvertiseCallback}.
     * Advertisement data consists of {@link #UUID} and {@code data} parameter.
     *
     * @param data     string of data to be advertised
     * @param callback callback that will be used to start the advertising
     */
    public static void advertise(@NotNull String data,
                                 @NotNull AdvertiseCallback callback) {
        ADAPTER.getBluetoothLeAdvertiser().startAdvertising(
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
    }


    // Scan functions


    /**
     * Enables all necessary services and
     * starts Bluetooth LE scan using {@link #scan(ScanCallback)}.
     * If scan is successful, passes all received scans to {@code onScanResults}.
     * If scan is failed for some reason, {@code onError} is called.
     * Returns {@link ScanCallback} instance, which can be used to stop scan
     * by passing it to {@link BluetoothLeScanner#stopScan(ScanCallback)}.
     *
     * @param activity activity, which will be used to request permissions
     * @param onScanResults function to pass received scan results to
     * @param onError function to be executed in case starting scan is failed
     * @return {@link ScanCallback} instance used in starting scan
     */
    public static ScanCallback scan(@NotNull Activity activity,
                                    @NotNull Consumer<String> onScanResults,
                                    @Nullable Consumer<Integer> onError) {
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
                onScanResults.accept(data);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    this.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                if (onError != null) onError.accept(errorCode);
                else Log.w("scan", "Scanning failed, but no onError provided: " + errorCode);
            }
        };

        enableBluetoothAndExecute(activity, () -> scan(callback));

        return callback;
    }

    /**
     * Starts Bluetooth LE scan with default settings,
     * one filter by {@link #UUID} and given {@link ScanCallback}.
     *
     * @param callback callback that will be used to start the scan
     */
    public static void scan(@NotNull ScanCallback callback) {
        ADAPTER.getBluetoothLeScanner().startScan(
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
    }

}