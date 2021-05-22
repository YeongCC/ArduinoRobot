package handsomec.robotcontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class control_key extends AppCompatActivity {
    Button  UP_btn, LEFT_btn, RIGHT_btn, BOTTOM_btn,Stop_btn;
    TextView status_controlkey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_key);
        this.UP_btn = (Button) findViewById(R.id.Up);
        this.LEFT_btn = (Button) findViewById(R.id.Left);
        this.RIGHT_btn = (Button) findViewById(R.id.Right);
        this.BOTTOM_btn =(Button) findViewById(R.id.Back);
        this.Stop_btn =(Button) findViewById(R.id.stop_btn);
        this.status_controlkey =(TextView) findViewById(R.id.status_controlkey);


        UP_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('f'));
                    status_controlkey.setText("FORWARD");
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });

        LEFT_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('l'));
                    status_controlkey.setText("LEFT");
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        });

        RIGHT_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('r'));
                    status_controlkey.setText("RIGHT");
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        });

        BOTTOM_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('b'));
                    status_controlkey.setText("BACKWARD");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.sendData(Character.valueOf('s'));
                    status_controlkey.setText("STOP");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}