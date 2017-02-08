package com.example.blackfh.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static int VAL_ACC_X = 0;
    public static int VAL_ACC_Y = 0;
    public static int VAL_ACC_Z = 0;
    public static int VAL_GYR_X = 0;
    public static int VAL_GYR_Y = 0;
    public static int VAL_GYR_Z = 0;
    public static float VAL_ANG_X = 0;
    public static float VAL_ANG_Y = 0;
    public static float VAL_ANG_Z = 0;
    public static int VAL_PID_ROL_P=0,VAL_PID_ROL_I=0,VAL_PID_ROL_D=0,VAL_PID_PIT_P=0,VAL_PID_PIT_I=0,VAL_PID_PIT_D=0,
            VAL_PID_YAW_P=0,VAL_PID_YAW_I=0,VAL_PID_YAW_D=0;
    public static int VAL_PID_PID1_P=0,VAL_PID_PID1_I=0,VAL_PID_PID1_D=0,VAL_PID_PID2_P=0,VAL_PID_PID2_I=0,VAL_PID_PID2_D=0,
            VAL_PID_PID3_P=0,VAL_PID_PID3_I=0,VAL_PID_PID3_D=0;
    public static int VAL_VOTAGE1 = 0;



    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothRfcommClient mRfcommClient = null;

    // Message types sent from the BluetoothRfcommClient Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothRfcommClient Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Menu
    private MenuItem mItemConnect;
    private MenuItem mItemOptions;
    private MenuItem mItemAbout;

    private TextView mTxtStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开启蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        mRfcommClient = new BluetoothRfcommClient(this, mHandler);
        Button myButton,mbutton,mybutton;
        myButton=(Button)findViewById(R.id.controller_button);
        myButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,ControllerActivity.class);
                startActivity(intent);
            }
        });

        mybutton=(Button)findViewById(R.id.gravity_controller_button);
        mybutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,ActivityControl.class);
                startActivity(intent);
            }
        });

        mbutton = (Button)findViewById(R.id.bluetooth_button);
        mbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent();
                serverIntent.setClass(MainActivity.this,ActivityBTDeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("exit")
                .setMessage("Are you sure to exit")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(ActivityBTDeviceList.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mRfcommClient.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:break;
        }
    }


    // The Handler that gets information back from the BluetoothRfcommClient
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothRfcommClient.STATE_CONNECTED:
                            mTxtStatus.setText(R.string.title_connected_to);
                            mTxtStatus.append(" " + mConnectedDeviceName);
                            break;
                        case BluetoothRfcommClient.STATE_CONNECTING:
                            mTxtStatus.setText(R.string.title_connecting);
                            break;
                        case BluetoothRfcommClient.STATE_NONE:
                            mTxtStatus.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    DataAnl(readBuf,msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    static void SendData(String message)
    {
        // Check that we're actually connected before trying anything
        if (mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {
            // Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothRfcommClient to write
            byte[] send = message.getBytes();
            mRfcommClient.write(send);
        }
    }
    static void SendData_Byte(byte[] data)
    {
        // Check that we're actually connected before trying anything
        if (mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {
            // Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        mRfcommClient.write(data);
    }
    static void Send_Command(byte data)
    {
        byte[] bytes = new byte[6];
        byte sum=0;
        // Check that we're actually connected before trying anything
        if (mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {
            // Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        bytes[0] = (byte) 0xaa;
        bytes[1] = (byte) 0xaf;
        bytes[2] = (byte) 0x01;
        bytes[3] = (byte) 0x01;
        bytes[4] = data;
        for(int i=0;i<5;i++) sum += bytes[i];
        bytes[5] = sum;
        SendData_Byte(bytes);
    }


    static int COM_BUF_LEN = 1000;
    static byte[] RX_Data = new byte[COM_BUF_LEN];	//���յ������ݣ�AA��ͷ
    static int rxstate = 0;
    static int rxlen = 0;//��֡�Ѿ����յ��ĳ���
    static int rxcnt = 0;//��д�����ֽ�
    static void DataAnl(byte[] data, int len)
    {
        for(int i=0;i<len;i++)
        {
            if(rxstate==0)//Ѱ�ҿ�ͷAA
            {
                if(data[i]==(byte)0xaa)
                {
                    rxstate = 1;
                    RX_Data[0] = (byte) 0xaa;
                }
            }
            else if(rxstate==1)//Ѱ�ҵڶ���AA
            {
                if(data[i]==(byte)0xaa)
                {
                    rxstate = 2;
                    RX_Data[1] = (byte) 0xaa;
                }
                else
                    rxstate = 0;
            }
            else if(rxstate==2)//���չ�����
            {
                rxstate = 3;
                RX_Data[2] = data[i];
            }
            else if(rxstate==3)//����len
            {
                if(data[i]>45)
                    rxstate = 0;
                else
                {
                    rxstate = 4;
                    RX_Data[3] = data[i];
                    rxlen = RX_Data[3];
                    if(rxlen<0)
                        rxlen = -rxlen;
                    rxcnt = 4;
                }
            }
            else if(rxstate==4)
            {
                rxlen--;
                RX_Data[rxcnt] = data[i];
                rxcnt++;
                if(rxlen<=0)
                    rxstate = 5;
            }
            else if(rxstate==5)//����sum
            {
                RX_Data[rxcnt] = data[i];
                if(rxcnt<=(COM_BUF_LEN-1))
                    FrameAnl(rxcnt+1);
                //Toast.makeText(getApplicationContext(), "DataAnl OK", Toast.LENGTH_SHORT).show();
                rxstate = 0;
            }
        }
    }
    static void FrameAnl(int len)
    {
        byte sum = 0;
        for(int i=0;i<(len-1);i++)
            sum += RX_Data[i];
        if(sum==RX_Data[len-1])
        {
            //Toast.makeText(getApplicationContext(), "FrameAnl OK", Toast.LENGTH_SHORT).show();
            if(RX_Data[2]==1)//status
            {
                VAL_ANG_X = ((float)(BytetoUint(4)))/100;
                VAL_ANG_Y = ((float)(BytetoUint(6)))/100;
                VAL_ANG_Z = ((float)(BytetoUint(8)))/100;
            }
            if(RX_Data[2]==2)//senser
            {
                VAL_ACC_X = BytetoUint(4);
                VAL_ACC_Y = BytetoUint(6);
                VAL_ACC_Z = BytetoUint(8);
                VAL_GYR_X = BytetoUint(10);
                VAL_GYR_Y = BytetoUint(12);
                VAL_GYR_Z = BytetoUint(14);
            }
            if(RX_Data[2]==5)//votage
            {
                VAL_VOTAGE1 = BytetoUint(4);
            }
        }
    }
    static short BytetoUint(int cnt)
    {
        short r = 0;
        r <<= 8;
        r |= (RX_Data[cnt] & 0x00ff);
        r <<= 8;
        r |= (RX_Data[cnt+1] & 0x00ff);
        return r;
    }
}
