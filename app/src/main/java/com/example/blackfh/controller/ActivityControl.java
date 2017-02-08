package com.example.blackfh.controller;


import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;



public class ActivityControl extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mSensor;

    buttonListener btnliListener1 = new buttonListener();

    private int VAL_THR,VAL_YAW=1500,VAL_ROL,VAL_PIT;
    private int DeadAngle = 1;
    private int MAXAngle = 50;
    private int YAW_D = 2500;
    private int YAW_A = 500;

    Timer send_timer = new Timer( );

    private float acc_x,acc_y,acc_z;

    TextView text_thr;
    TextView text_yaw;
    TextView text_rol;
    TextView text_pit;
    TextView tv_ang_rol;
    TextView tv_ang_pit;
    TextView tv_ang_yaw;
    TextView tv_acc_x;
    TextView tv_acc_y;
    TextView tv_acc_z;
    TextView tv_gyr_x;
    TextView tv_gyr_y;
    TextView tv_gyr_z;
    TextView tv_votage1;

    private final Handler ui_handler = new Handler();

    private final Runnable ui_task = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            ui_handler.postDelayed(this, 20);
            if( thr_bar.Value * 1000 / thr_bar.Value_max-150<=0)
                VAL_THR = 0;
            else
                VAL_THR = thr_bar.Value * 1000 / thr_bar.Value_max ;

            double temp_rol = Math.atan2(acc_y,acc_z)*180/Math.PI;
            double temp_pit = -Math.atan2(acc_x,acc_z)*180/Math.PI;

            if (temp_rol > MAXAngle)
                temp_rol = MAXAngle;
            else if (temp_rol < -MAXAngle)
                temp_rol = -MAXAngle;
            if (temp_rol < DeadAngle && temp_rol > -DeadAngle)
                temp_rol = 0;

            if (temp_pit > MAXAngle)
                temp_pit = MAXAngle;
            else if (temp_pit < -MAXAngle)
                temp_pit = -MAXAngle;
            if (temp_pit < DeadAngle && temp_pit > -DeadAngle)
                temp_pit = 0;

            VAL_ROL = (int)(-temp_rol * 1000 / MAXAngle + 1500);
            VAL_PIT = (int)(temp_pit * 1000 / MAXAngle + 1500);
            text_thr.setText(""+VAL_THR);
            text_yaw.setText(""+VAL_YAW);
            text_rol.setText(""+VAL_ROL);
            text_pit.setText(""+VAL_PIT);
            tv_ang_rol.setText(""+MainActivity.VAL_ANG_X);
            tv_ang_pit.setText(""+MainActivity.VAL_ANG_Y);
            tv_ang_yaw.setText(""+MainActivity.VAL_ANG_Z);
            tv_acc_x.setText(""+MainActivity.VAL_ACC_X);
            tv_acc_y.setText(""+MainActivity.VAL_ACC_Y);
            tv_acc_z.setText(""+MainActivity.VAL_ACC_Z);
            tv_gyr_x.setText(""+MainActivity.VAL_GYR_X);
            tv_gyr_y.setText(""+MainActivity.VAL_GYR_Y);
            tv_gyr_z.setText(""+MainActivity.VAL_GYR_Z);
            tv_votage1.setText(""+MainActivity.VAL_VOTAGE1);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//����ģʽ
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_control);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionbar=getSupportActionBar();
        if(actionbar!=null){
            actionbar.hide();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        if (null == mSensorManager) {
            Log.d(TAG, "deveice not support SensorManager");
        }
        // �����������ľ�׼��
        mSensorManager.registerListener(myAccelerometerListener, mSensor,  SensorManager. SENSOR_DELAY_GAME);// SENSOR_DELAY_GAME

        text_thr = (TextView)findViewById(R.id.text_col_thr);
        text_yaw = (TextView)findViewById(R.id.text_col_yaw);
        text_rol = (TextView)findViewById(R.id.text_col_rol);
        text_pit = (TextView)findViewById(R.id.text_col_pit);
        tv_ang_rol = (TextView)findViewById(R.id.text_val_rol);
        tv_ang_pit = (TextView)findViewById(R.id.text_val_pit);
        tv_ang_yaw = (TextView)findViewById(R.id.text_val_yaw);
        tv_acc_x = (TextView)findViewById(R.id.text_val_acc_x);
        tv_acc_y = (TextView)findViewById(R.id.text_val_acc_y);
        tv_acc_z = (TextView)findViewById(R.id.text_val_acc_z);
        tv_gyr_x = (TextView)findViewById(R.id.text_val_gyr_x);
        tv_gyr_y = (TextView)findViewById(R.id.text_val_gyr_y);
        tv_gyr_z = (TextView)findViewById(R.id.text_val_gyr_z);
        tv_votage1 = (TextView)findViewById(R.id.text_val_votage1);

        Button mbutton = (Button) findViewById(R.id.btn_yaw_d);
        mbutton.setOnTouchListener(btnliListener1);
        mbutton = (Button) findViewById(R.id.btn_yaw_a);
        mbutton.setOnTouchListener(btnliListener1);

        mbutton = (Button) findViewById(R.id.btn_stop);
        mbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Send_Command((byte)0xa0);
            }
        });
        mbutton = (Button) findViewById(R.id.btn_suoding);
        mbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Send_Command((byte)0xa0);
            }
        });
        mbutton = (Button) findViewById(R.id.btn_jiesuo);
        mbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Send_Command((byte)0xa1);
            }
        });

        send_timer.schedule(send_task,1000,10);
        ui_handler.postDelayed(ui_task, 20);
    }



    class buttonListener implements OnTouchListener{
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            if(v.getId() == R.id.btn_yaw_d){
                if(event.getAction() == MotionEvent.ACTION_UP){
                    VAL_YAW = 1500;
                }
                if(event.getAction() == MotionEvent.ACTION_CANCEL){
                    VAL_YAW = 1500;
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    VAL_YAW = YAW_D;
                }
            }
            if(v.getId() == R.id.btn_yaw_a){
                if(event.getAction() == MotionEvent.ACTION_UP){
                    VAL_YAW = 1500;
                }
                if(event.getAction() == MotionEvent.ACTION_CANCEL){
                    VAL_YAW = 1500;
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    VAL_YAW = YAW_A;
                }
            }
            return false;
        }
    }
    /*
   * SensorEventListener�ӿڵ�ʵ�֣���Ҫʵ����������
   * ����1 onSensorChanged �����ݱ仯��ʱ�򱻴�������
   * ����2 onAccuracyChanged ��������ݵľ��ȷ����仯��ʱ�򱻵��ã�����ͻȻ�޷��������ʱ
   * */
    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        //��дonSensorChanged����
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                Log.i(TAG,"onSensorChanged");

                //ͼ�����Ѿ���������ֵ�ĺ���
                acc_x = sensorEvent.values[0];
                acc_y = sensorEvent.values[1];
                acc_z = sensorEvent.values[2];
                Log.i(TAG,"\n heading "+acc_x);
                Log.i(TAG,"\n pitch "+acc_y);
                Log.i(TAG,"\n roll "+acc_z);
            }
        }
        //��дonAccuracyChanged����
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    public void onPause(){
        /*
         * �ܹؼ��Ĳ��֣�ע�⣬˵���ĵ����ᵽ����ʹactivity���ɼ���ʱ�򣬸�Ӧ����Ȼ������Ĺ��������Ե�ʱ����Է��֣�û��������ˢ��Ƶ��
         * Ҳ��ǳ��ߣ�����һ��Ҫ��onPause�����йرմ����������򽲺ķ��û������������ܲ�����
         * */
        mSensorManager.unregisterListener(myAccelerometerListener);
        super.onPause();
    }

    TimerTask send_task = new TimerTask( ) {
        byte[] bytes = new byte[20];
        public void run ( )
        {
            byte sum=0;

            bytes[0] = (byte) 0xaa;
            bytes[1] = (byte) 0xc0;
            bytes[2] = (byte) 16;
            bytes[3] = (byte) (VAL_THR/0xff);
            bytes[4] = (byte) (VAL_THR%0xff);
            bytes[5] = (byte) (VAL_YAW/0xff);
            bytes[6] = (byte) (VAL_YAW%0xff);
            bytes[7] = (byte) (VAL_ROL/0xff);
            bytes[8] = (byte) (VAL_ROL%0xff);
            bytes[9] = (byte) (VAL_PIT/0xff);
            bytes[10] = (byte) (VAL_PIT%0xff);
            bytes[11] = 0;
            bytes[12] = 0;
            bytes[13] = 0;
            bytes[14] = 0;
            bytes[15] = 0;
            bytes[16] = 0;
            bytes[17] = 0;
            bytes[18] = 0;
            for(int i=0;i<19;i++) sum += bytes[i];
            bytes[19] = sum;

            MainActivity.SendData_Byte(bytes);
        }
    };

    protected void onDestroy ( ) {
        if (send_timer != null)
        {
            send_timer.cancel( );
            send_timer = null;
        }
        super.onDestroy( );
    }

}
