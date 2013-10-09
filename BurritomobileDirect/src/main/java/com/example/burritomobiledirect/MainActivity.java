package com.example.burritomobiledirect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    BluetoothHandler btHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button left = (Button) findViewById(R.id.left);
        Button forward = (Button) findViewById(R.id.forward);
        Button right = (Button) findViewById(R.id.right);
        Button back_left = (Button) findViewById(R.id.back_left);
        Button back = (Button) findViewById(R.id.back);
        Button back_right = (Button) findViewById(R.id.back_right);

        Button restart = (Button) findViewById(R.id.restart);

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Burritocontroller", "Control button clicked.");
                switch (v.getId()) {
                    case R.id.left:
                        btHandler.sendCommand(new byte[]{(byte) 2});
                        break;
                    case R.id.forward:
                        btHandler.sendCommand(new byte[]{(byte) 1});
                        break;
                    case R.id.right:
                        btHandler.sendCommand(new byte[]{(byte) 3});
                        break;
                    case R.id.back_left:
                        btHandler.sendCommand(new byte[]{(byte) 5});
                        break;
                    case R.id.back:
                        btHandler.sendCommand(new byte[]{(byte) 4});
                        break;
                    case R.id.back_right:
                        btHandler.sendCommand(new byte[]{(byte) 6});
                        break;
                }
            }
        };

        RepeatListener repeater = new RepeatListener(0, 100, buttonListener);

        left.setOnTouchListener(repeater);
        forward.setOnTouchListener(repeater);
        right.setOnTouchListener(repeater);
        back_left.setOnTouchListener(repeater);
        back.setOnTouchListener(repeater);
        back_right.setOnTouchListener(repeater);

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
                MainActivity.this.startActivity(getIntent());

            }
        });

        btHandler = new BluetoothHandler();
        btHandler.setup();

    }

    private class BluetoothHandler {

        public void setup() {
            btAdapter = BluetoothAdapter.getDefaultAdapter();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothFoundReceiver, filter); // Don't forget to unregister during onDestroy
            BluetoothDevice device = btAdapter.getRemoteDevice("00:06:66:08:60:0E");
            connect(device);

        }

        public void connect(BluetoothDevice device) {
            btAdapter.cancelDiscovery();
            Log.d("Burritomobile", device.toString());

            try {
                btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                Log.d("Burritomobile", "made socket");
//            btSocket = device.createRfcommSocketToServiceRecord(new DeviceUuidFactory(this).getDeviceUuid());
                btSocket.connect();
                Log.d("Burritomobile", "Connection made.");
            } catch (IOException e) {
//            Log.d("Burritomobile", "Socket creation failed");
                Log.e("Burritomobile", "Socket creation failed", e);
            }
        }

        private void sendCommand(byte[] bytes) {
            try {
                OutputStream outStream = btSocket.getOutputStream();
                Log.d("Burritomobile", "About to send bytes: " );
                for (byte b : bytes) {
                    Log.d("Burritomobile", "Byte: " + (int) b);
                }

                try {
                    outStream.write(bytes);
                } catch (IOException e) {
                    Log.e("Burritomobile", "Bug while sending stuff", e);
                }
            } catch (IOException e) {
                Log.e("Burritomobile", "Bug BEFORE Sending stuff", e);
                finish();
            }
        }

//        private void writeData(String data) {
//            writeData(data.getBytes());
//            Log.d("Burritomobile", "Sent message on BT: " + data);
//        }

        public void onDestroy() {
            unregisterReceiver(bluetoothFoundReceiver);
        }

        // Create a BroadcastReceiver for ACTION_FOUND
        private final BroadcastReceiver bluetoothFoundReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("Burritomobile", "Got a BT result");

                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = device.getName();

//                if (device.getAddress().equals("00:06:66:08:60:0E")) {
//                    connect(device);
//                    return;
//                }
                    if (name != null && !name.equals("")) {
                        if (name.equals("FireFly-600E")) {
                            connect(device);
                            return;
                        }
                        Log.d("Burritomobile", device.getName());
                    } else {
                        Log.d("Burritomobile", device.getAddress());
                    }

                }
            }
        };
    }

//    class BluetoothMaker extends AsyncTask<Void, Void, Socket> {
//
//        @Override
//        protected Socket doInBackground(Void... params) {
//            String serverAddress = "ec2-54-215-239-150.us-west-1.compute.amazonaws.com";
////        String serverAddress = "ngrok.com";
//            Socket s = null;
//            try {
//                s = new Socket(serverAddress, 4000);
//
//                return s;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Socket s) {
//            OutputStream out = null;
//            try {
//                out = s.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            client = new Client(s, out);
//        }
//
//
//    }

    @Override
    public void onDestroy() {
        try {
            btHandler.onDestroy();
        } catch (Exception e) {
            Log.e("Burritodirect", "Error in onDestroy", e);
        }
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
