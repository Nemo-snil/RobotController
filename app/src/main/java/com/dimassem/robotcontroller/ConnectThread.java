package com.dimassem.robotcontroller;

import static androidx.core.content.ContextCompat.startActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.LogRecord;

public class ConnectThread extends Thread {
    private final Context myContext;
    Handler handler;

    public ConnectThread(BluetoothDevice device, Context context) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        myContext = context;
        BluetoothSocket tmp = null;
        RobotConnect.mmDevice = device;
        handler = new Handler();

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
        } catch (IOException e) {
            Log.e("Connect", "Socket's create() method failed", e);
        }
        RobotConnect.mmSocket = tmp;
    }

    public void run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(myContext, "Подключение...", Toast.LENGTH_SHORT).show();
                }
            });

            RobotConnect.mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                RobotConnect.mmSocket.close();
            } catch (IOException closeException) {
                Log.e("Connect", "Could not close the client socket", closeException);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(myContext, "Не удалось подключиться", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        myContext.startActivity(new Intent(myContext, Controller.class));
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            RobotConnect.mmSocket.close();
        } catch (IOException e) {
            Log.e("Connect", "Could not close the client socket", e);
        }
    }
}