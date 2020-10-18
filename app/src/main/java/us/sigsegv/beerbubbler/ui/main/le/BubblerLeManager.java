package us.sigsegv.beerbubbler.ui.main.le;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.room.Room;

import com.fitbit.bluetooth.fbgatt.FitbitGatt;
import com.fitbit.bluetooth.fbgatt.GattConnection;
import com.fitbit.bluetooth.fbgatt.GattServerConnection;
import com.fitbit.bluetooth.fbgatt.GattState;
import com.fitbit.bluetooth.fbgatt.TransactionResult;
import com.fitbit.bluetooth.fbgatt.exception.BitGattStartException;
import com.fitbit.bluetooth.fbgatt.tx.GattClientDiscoverServicesTransaction;
import com.fitbit.bluetooth.fbgatt.tx.GattConnectTransaction;
import com.fitbit.bluetooth.fbgatt.tx.ReadGattCharacteristicTransaction;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class BubblerLeManager implements FitbitGatt.FitbitGattCallback {
    private static final String TAG = "BubblerLeManager";
    private static final String DEVICE_NAME = "BeerBubbler";
    private static BubblerLeManager instance;
    private final Handler databaseHandler;
    ScanFilter filter = new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build();
    FitbitGatt gatt = FitbitGatt.getInstance();
    BubbleDatabase database;

    public static BubblerLeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TAG) {
                instance = new BubblerLeManager(context);
            }
        }
        return instance;
    }

    private BubblerLeManager(Context context) {
        this.database = BubbleDatabaseHolder.getInstance(context).getDatabase();
        gatt.registerGattEventListener(this);
        gatt.startGattClient(context);
        gatt.initializeScanner(context);
        gatt.addDeviceNameScanFilter(DEVICE_NAME);
        gatt.getAlwaysConnectedScanner().addScanFilter(context, filter);
        Timber.tag(TAG).v("Initialized bubbler");
        HandlerThread databaseThread = new HandlerThread("Database Thread");
        databaseThread.start();
        databaseHandler = new Handler(databaseThread.getLooper());
    }

    public void startActiveInteraction() {
        // check for connections
        List<GattConnection> connections = gatt.getMatchingConnectionsForDeviceNames(Collections.singletonList(DEVICE_NAME));
        if (connections.isEmpty()) {
            gatt.startHighPriorityScan(gatt.getAppContext());
            Timber.tag(TAG).v("No connections, will wait for one");
        } else {
            for (GattConnection conn : connections) {
                if (DEVICE_NAME.equals(conn.getDevice().getName())) {
                    processConnection(conn);
                }
            }
        }
        // if no connections start scan and wait for on bluetooth peripheral discovered
        // if connection present, start the discover, read, etc ...
    }

    private void processConnection(GattConnection conn) {
        if (!conn.isConnected()) {
            GattConnectTransaction connectTransaction = new GattConnectTransaction(conn, GattState.CONNECTED);
            conn.runTx(connectTransaction, result -> {
                if (result.getResultStatus().equals(TransactionResult.TransactionResultStatus.SUCCESS)) {
                    GattClientDiscoverServicesTransaction discoverServicesTransaction = new GattClientDiscoverServicesTransaction(conn, GattState.DISCOVERY_SUCCESS);
                    conn.runTx(discoverServicesTransaction, result1 -> {
                        if (result1.getResultStatus().equals(TransactionResult.TransactionResultStatus.SUCCESS)) {
                            readDataWriteDatabaseEntry(conn);
                        } else {
                            Timber.tag(TAG).w("Service Discovery Failed");
                        }
                    });
                } else {
                    Timber.tag(TAG).w("Failed to connect");
                }
            });
        } else {
            GattClientDiscoverServicesTransaction discoverServicesTransaction = new GattClientDiscoverServicesTransaction(conn, GattState.DISCOVERY_SUCCESS);
            conn.runTx(discoverServicesTransaction, result1 -> {
                if (result1.getResultStatus().equals(TransactionResult.TransactionResultStatus.SUCCESS)) {
                    readDataWriteDatabaseEntry(conn);
                } else {
                    Timber.tag(TAG).w("Service Discovery Failed");
                }
            });
        }
    }

    private void readDataWriteDatabaseEntry(GattConnection conn) {
        if(conn.getGatt() != null) {
            // get beer bubbler service
            BluetoothGattService service = conn.getGatt().getService(UUID.fromString("621d4d94-0d1d-11eb-b2d5-f367b4cbee46"));
            if (service == null) {
                Timber.tag(TAG).w("Service was null");
                return;
            }
            BluetoothGattCharacteristic bubbleCharacteristic = service.getCharacteristic(UUID.fromString("621d4d00-0d1d-11eb-b2d5-f367b4cbee46"));
            BluetoothGattCharacteristic temperatureCharacteristic = service.getCharacteristic(UUID.fromString("621d4d01-0d1d-11eb-b2d5-f367b4cbee46"));
            BluetoothGattCharacteristic humidityCharacteristic = service.getCharacteristic(UUID.fromString("621d4d02-0d1d-11eb-b2d5-f367b4cbee46"));
            if (bubbleCharacteristic == null || temperatureCharacteristic == null || humidityCharacteristic == null) {
                Timber.tag(TAG).w("One of the value characteristics was null");
                return;
            }
            final int[] counts = new int[3];
            ReadGattCharacteristicTransaction readBubbleCharacteristicTx = new ReadGattCharacteristicTransaction(conn, GattState.READ_CHARACTERISTIC_SUCCESS, bubbleCharacteristic);
            conn.runTx(readBubbleCharacteristicTx, result -> {
                if (result.getResultState().equals(GattState.READ_CHARACTERISTIC_SUCCESS)) {
                    if (result.getData() != null) {
                        counts[0] = ByteBuffer.wrap(result.getData()).getInt();
                        ReadGattCharacteristicTransaction readTempCharacteristicTx = new ReadGattCharacteristicTransaction(conn, GattState.READ_CHARACTERISTIC_SUCCESS, temperatureCharacteristic);
                        conn.runTx(readTempCharacteristicTx, result12 -> {
                            if (result12.getResultState().equals(GattState.READ_CHARACTERISTIC_SUCCESS)) {
                                if (result12.getData() != null) {
                                    counts[1] = ByteBuffer.wrap(result12.getData()).getShort();
                                    ReadGattCharacteristicTransaction readHumidityCharacteristicTx = new ReadGattCharacteristicTransaction(conn, GattState.READ_CHARACTERISTIC_SUCCESS, humidityCharacteristic);
                                    conn.runTx(readHumidityCharacteristicTx, result1 -> {
                                        if (result1.getResultState().equals(GattState.READ_CHARACTERISTIC_SUCCESS)) {
                                            if (result1.getData() != null) {
                                                counts[2] = ByteBuffer.wrap(result1.getData()).getShort();
                                                databaseHandler.post(() -> {
                                                    BubbleEntry newEntry = new BubbleEntry(0,
                                                            new Date().getTime(), counts[0], counts[1], counts[2]);
                                                    database.bubbleDao().insertAll(newEntry);
                                                    Integer count = database.bubbleDao().getBubbleEntryCount();
                                                    Timber.v("Entry count: %d", count);
                                                    Timber.v(
                                                            "Logged off at %d, bubbles %d, temp %d celsius, humidity %d percent", new Date().getTime(), counts[0], counts[1], counts[2]);
                                                    Timber.v("Successfully recorded entry");
                                                });
                                            } else {
                                                Timber.w("Humidity characteristic reported null");
                                            }
                                        } else {
                                            Timber.w("Reading the humidity characteristic failed");
                                        }
                                    });
                                } else {
                                    Timber.w("Temperature characteristic reported null");
                                }
                            } else {
                                Timber.w("Reading the temperature characteristic failed");
                            }
                        });
                    } else {
                        Timber.w("Bubble characteristic reported null");
                    }
                } else {
                    Timber.w("Reading the bubble characteristic failed");
                }
            });
        } else {
            Timber.w("Gatt was null");
        }
    }

    @Override
    public void onBluetoothPeripheralDiscovered(GattConnection connection) {
        Timber.v("Peripheral %s discovered", connection.getDevice().getName());
        processConnection(connection);
    }

    @Override
    public void onBluetoothPeripheralDisconnected(GattConnection connection) {
        Timber.v("Peripherhal %s disconnected", connection.getDevice().getName());
    }

    @Override
    public void onScanStarted() {
        Timber.v("Scan Started");
    }

    @Override
    public void onScanStopped() {
        Timber.v("Scan Stopped");
    }

    @Override
    public void onScannerInitError(BitGattStartException error) {
        Timber.e(error, "The scan couldn't be started");
    }

    @Override
    public void onPendingIntentScanStopped() {
        Timber.v("Pending intent scan stopped");
    }

    @Override
    public void onPendingIntentScanStarted() {
        Timber.v("Pending intent scan started");
    }

    @Override
    public void onBluetoothOff() {
        Timber.v("Bluetooth Down");
    }

    @Override
    public void onBluetoothOn() {
        Timber.v("Bluetooth Up!");
    }

    @Override
    public void onBluetoothTurningOn() {
        Timber.v("Bluetooth is turning on");
    }

    @Override
    public void onBluetoothTurningOff() {
        Timber.v("Bluetooth is turning off");
    }

    @Override
    public void onGattServerStarted(GattServerConnection serverConnection) {
        Timber.v("Gatt server started");
    }

    @Override
    public void onGattServerStartError(BitGattStartException error) {
        Timber.e(error, "Server failed to start");
    }

    @Override
    public void onGattClientStarted() {
        Timber.v("Gatt client was started");
    }

    @Override
    public void onGattClientStartError(BitGattStartException error) {
        Timber.e(error, "Bitgatt client failed to start");
    }

    public void stopActiveInteraction() {
        Timber.i("Stopped active scan");
        gatt.cancelHighPriorityScan(gatt.getAppContext());
    }

    public static class BubbleDatabaseHolder {
        BubbleDatabase database;
        private static BubbleDatabaseHolder instance;
        public static synchronized BubbleDatabaseHolder getInstance(Context context) {
            if (instance == null) {
                instance = new BubbleDatabaseHolder(context);
            }
            return instance;
        }

        private BubbleDatabaseHolder(Context context) {
            this.database = Room.databaseBuilder(
                    context.getApplicationContext(),
                    BubbleDatabase.class, "beerbubbler-database"
                ).build();
        }

        public BubbleDatabase getDatabase(){
            return this.database;
        }
    }
}
