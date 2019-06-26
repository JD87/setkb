package com.example.david.setkb;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    OutputStream mOutputStream;
    InputStream mInputStream;

    Boolean bool_stopWorker = false;

    Byte delimiter;
    String str_global_data = " ";


    byte byt_primaryCode;

    boolean caps = false;

    Switch sw_test;

    private void setKeyboardView() {
        final Keyboard keys = new Keyboard(this, R.xml.keys_layout);
        final KeyboardView kv = (KeyboardView) findViewById(R.id.keyboard_view);
        kv.setKeyboard(keys);

        kv.setEnabled(true);
        kv.setClickable(true);

        kv.setPreviewEnabled(true);

        KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {

                if(primaryCode == Keyboard.KEYCODE_SHIFT || primaryCode == 193){
                    caps = !caps;
                    keys.setShifted(caps);
                    kv.invalidateAllKeys();
                }

            }

            @Override
            public void onPress(int primaryCode) {
                TextView tv = (TextView) findViewById(R.id.text_view);
                tv.setText("" + primaryCode);
                byt_primaryCode = (byte) primaryCode;
                //Toast.makeText(getBaseContext(),"hey ohh! " + primaryCode, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRelease(int primaryCode) {

                try {
                    send_to_BT_device(byt_primaryCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onText(CharSequence text) {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeDown() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeLeft() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeRight() {
                // TODO Auto-generated method stub

            }

            @Override
            public void swipeUp() {
                // TODO Auto-generated method stub

            }

        };
        kv.setOnKeyboardActionListener(listener);


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sw_test = (Switch) findViewById(R.id.switch1);

        setKeyboardView();

        sw_test.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    find_BT_device();
                else {
                    try {
                        close_BT_device();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    // -----------------------------------------------------------------------

    void find_BT_device(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {

            // Device doesn't support Bluetooth
            toastMessage("Device doesn't support Bluetooth :(");
            finish();
        }

        if (mBluetoothAdapter.isEnabled()){
            // Bluetooth on
        }
        else {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if( device.getName().equals("HC-05"))
                {
                    mBluetoothDevice = device;
                    toastMessage(device.getName() + " " + device.getAddress() + " found");

                    try {
                        open_BT_device(device);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

    }   //  find_BT_device

    private void open_BT_device(BluetoothDevice device) throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
        mBluetoothSocket.connect();
        mOutputStream = mBluetoothSocket.getOutputStream();
        mInputStream = mBluetoothSocket.getInputStream();

        listen_to_BT_device();

        toastMessage("Bluetooth Opened");
    }   //  open_BT_device

    void close_BT_device() throws IOException {
        bool_stopWorker = true;
        mOutputStream.close();
        mInputStream.close();
        mBluetoothSocket.close();
        toastMessage("Bluetooth Closed");
    }   //  close_BT_device

    void listen_to_BT_device() throws IOException {
        final byte delimiter_lf = 10;      // LF according to ASCII code, sort of like CR or \n... I think
        final byte delimiter_cr = 13;      // CR according to ASCII code, for raspi
        final Handler mhandler = new Handler();

        bool_stopWorker = false;
        final int[] readBufferPosition = {0};
        final byte [] readBuffer = new byte[1024];

        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                mBluetoothAdapter.cancelDiscovery();

                while(!Thread.currentThread().isInterrupted() && !bool_stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];

                                check_for_eol_pref();

                                if(b == delimiter)       //delimiter
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition[0]];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition[0] = 0;


                                    mhandler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            str_global_data = data;
                                            //show_proprietary_data();
                                            // textview here  .setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    if (b == delimiter_lf || b == delimiter_cr){
                                        //  ¯\_(ツ)_/¯
                                        //toastMessage("wrong delimiter");
                                        readBufferPosition[0] = 0;
                                    }
                                    else
                                        readBuffer[readBufferPosition[0]++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        bool_stopWorker = true;
                    }
                }
            }

        });
        //mInputStream.reset();
        workerThread.start();
    }   // listen_to_BT_device

    void send_to_BT_device(byte message_to_send) throws IOException {
        mOutputStream.write(message_to_send);
    }   //  send_to_BT_device

    //  -------------------------------------------------------------------------------------------

    public void check_for_eol_pref(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String str_delimiter = pref.getString("eol_pref","LF");

        if(str_delimiter.equals("LF"))
            delimiter = 10;
        else
            delimiter = 13;
    }   //  check_for_eol_pref

    //  -------------------------------------------------------------------------------------------

    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }   //  toastMessage


}
