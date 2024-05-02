package com.dimassem.robotcontroller;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public Runnable runnable;
    private byte[] mmBuffer; // mmBuffer store for the stream

    public ConnectedThread() {
        mmSocket = RobotConnect.mmSocket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            Log.e("Connect", "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("Connect", "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
            } catch (IOException e) {
                Log.d("Connect", "Input stream was disconnected", e);
                handler.post(runnable);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(String text) {
        try {
            mmOutStream.write((text + "\n").getBytes());
            Log.d("Out", text);
        } catch (IOException e) {
            Log.e("Connect", "Error occurred when sending data", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("Connect", "Could not close the connect socket", e);
        }
    }
}