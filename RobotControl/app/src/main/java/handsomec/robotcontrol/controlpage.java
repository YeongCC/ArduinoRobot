package handsomec.robotcontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class controlpage extends AppCompatActivity implements SensorEventListener{
    int ab = 0,ac=0;
    Button auto_btn, start_btn,change_btn,stop_btn1,check_data;
    private float last_x;
    private float last_z;
    private android.hardware.Sensor senAccelerometer;
    private SensorManager senSensorManager;
    private TextView status, x_view, z_view,distance;
    final Handler bluetoothIn = new Handler();
    public static boolean disbool =true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpage);
        this.senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.senAccelerometer = this.senSensorManager.getDefaultSensor(1);
        this.senSensorManager.registerListener(this, this.senAccelerometer, 50000);
        this.x_view = (TextView) findViewById(R.id.xaxis);
        this.z_view = (TextView) findViewById(R.id.zaxis);
        this.status = (TextView) findViewById(R.id.status);
        this.auto_btn = (Button) findViewById(R.id.auto_btn);
        this.start_btn = (Button) findViewById(R.id.start_btn);
        this.change_btn =(Button) findViewById(R.id.change_button);
        this.stop_btn1 =(Button) findViewById(R.id.stop_btn1);
        this.check_data =(Button) findViewById(R.id.check_Distance);
        this.distance =(TextView) findViewById(R.id.distance);

        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlpage.this.startActivity(new Intent(controlpage.this, control_key.class));
            }
        });

        auto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('o'));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });


        stop_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('t'));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });

        check_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        MainActivity.sendData(Character.valueOf('k'));
                        check_data.setText("FINISH CHANCE");
                        distance.setText(MainActivity.getData()+" CM");
                        check_data.setEnabled(false);
                    } catch (IOException | InterruptedException e2) {
                        e2.printStackTrace();
                    }
            }
        });

    }

    @Override
    public void onBackPressed() {//exit function
        AlertDialog.Builder builder = new AlertDialog.Builder( this);
        builder.setMessage("Did you want to disconnect?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if(MainActivity.connectSign != true){
                                msg("Did not Connect Devise");
                            }else{
                                MainActivity.closeBT();
                                controlpage.super.onBackPressed();
                                msg("Disconnect Succesfully");
                            }
                        } catch (IOException e) {
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        this.senSensorManager.unregisterListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        this.senSensorManager.registerListener(this, this.senAccelerometer, 3);
    }

    public void onSensorChanged(SensorEvent event) {
        int i = 0;
        if (this.ac == 1 && event.sensor.getType() == 1) {
            int i2;
            float z = event.values[2] * 10.0f;
            this.last_x = (float) Math.round(event.values[0] * 10.0f);
            this.x_view.setText(String.valueOf(this.last_x));
            this.last_z = (float) Math.round(z);
            this.z_view.setText(String.valueOf(this.last_z));
            if (this.last_z <= -10.0f) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if ((i2 & (this.ab == 0 ? 1 : 0)) != 0) {
                this.ab = 1;
                try {
                    MainActivity.sendData(Character.valueOf('b'));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.status.setTextColor(-65536);
                this.status.setText("BACKWARD");
            } else if (this.last_z <= 0.0f || this.last_z >= 50.0f || this.last_x <= -20.0f || this.last_x >= 20.0f || this.ab != 1) {
                if (((this.last_z >= 70.0f ? 1 : 0) & (this.ab == 0 ? 1 : 0)) != 0) {
                    this.ab = 1;
                    try {
                        MainActivity.sendData(Character.valueOf('f'));
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    this.status.setTextColor(-65536);
                    this.status.setText("FORWARD");
                    return;
                }
                if (((this.last_x <= -50.0f ? 1 : 0) & (this.ab == 0 ? 1 : 0)) != 0) {
                    this.ab = 1;
                    try {
                        MainActivity.sendData(Character.valueOf('r'));
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                    this.status.setTextColor(-16776961);
                    this.status.setText("RIGHT");
                    return;
                }
                i2 = this.last_x >= 50.0f ? 1 : 0;
                if (this.ab == 0) {
                    i = 1;
                }
                if ((i2 & i) != 0) {
                    this.ab = 1;
                    try {
                        MainActivity.sendData(Character.valueOf('l'));
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                    this.status.setTextColor(-16776961);
                    this.status.setText("LEFT");
                }
            } else {
                try {
                    MainActivity.sendData(Character.valueOf('s'));
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
                this.status.setTextColor(-16711936);
                this.status.setText("NEUTRAL");
            }
        }
    }



    public void fun2(View view) throws IOException {
        if (this.ac == 0) {
            msg("Motion control started");
            this.ac = 1;
            this.start_btn.setText("Stop");
        } else if (this.ac == 1) {
            msg("Motion control stopped");
            this.ac = 0;
            MainActivity.sendData(Character.valueOf('s'));
            this.start_btn.setText("Start");
        }
    }


    public void msg(String a) {
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();
    }

    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
    }

}