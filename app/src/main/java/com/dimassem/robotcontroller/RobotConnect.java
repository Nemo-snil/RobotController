package com.dimassem.robotcontroller;

import static androidx.core.content.ContextCompat.startActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RobotConnect extends AppCompatActivity {
    static public BluetoothAdapter bluetoothAdapter;
    public static BluetoothSocket mmSocket;
    public static BluetoothDevice mmDevice;

    SimpleAdapter adapter;

    ArrayList<BluetoothDevice> devices_list;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    HashMap<String, String> map;

    ListView devices_view;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_connect);

        devices_view = findViewById(R.id.devices_list);

        devices_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();

                ConnectThread connectThread = new ConnectThread(devices_list.get(position), view.getContext());
                connectThread.start();
                devices_view.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (connectThread.isAlive()){}
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                devices_view.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        arrayList = new ArrayList<>();
        devices_list = new ArrayList<>();

        if (!pairedDevices.isEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                map = new HashMap<>();

                map.put("Name", device.getName());
                map.put("Address", device.getAddress());

                arrayList.add(map);
                devices_list.add(device);
            }
        }

        adapter = new SimpleAdapter(this, arrayList, android.R.layout.simple_list_item_2,
                new String[]{"Name", "Address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        devices_view.setAdapter(adapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(reciver, filter);
        Log.d("Finding", String.valueOf(bluetoothAdapter.startDiscovery()));
    }

    private final BroadcastReceiver reciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                map = new HashMap<>();

                map.put("Name", device.getName());
                map.put("Address", device.getAddress());

                arrayList.add(map);
                devices_list.add(device);

                adapter = new SimpleAdapter(context, arrayList, android.R.layout.simple_list_item_2,
                        new String[]{"Name", "Address"},
                        new int[]{android.R.id.text1, android.R.id.text2});
                devices_view.setAdapter(adapter);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(reciver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(reciver);
    }
}