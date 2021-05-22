package handsomec.robotcontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static OutputStream mmOutputStream;
    BluetoothAdapter mBluetoothAdapter;
    public static InputStream mmInputStream;
    public static BluetoothSocket mmSocket;
    static volatile boolean stopWorker;
    public static boolean connectSign=false;
    private ListView listView;
    private static final String DEVICE_LIST = "handsomec.robotcontrol.devicelist";
    private static final String DEVICE_LIST_SELECTED = "handsomec.robotcontrol.devicelistselected";
    byte[] readBuffer;
    int readBufferPosition;
    Thread workerThread;
    Button connect,search;
    public static int addon=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.connect);

        search = (Button) findViewById(R.id.search);
        listView = (ListView) findViewById(R.id.listview);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (savedInstanceState != null) {
            ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if (list != null) {
                initList(list);
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
                if (selectedIndex != -1) {
                    adapter.setSelectedIndex(selectedIndex);
                    connect.setEnabled(true);
                }
            } else {
                initList(new ArrayList<BluetoothDevice>());
            }

        } else {
            initList(new ArrayList<BluetoothDevice>());
        }
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();
                    openBT(device);
                    Intent intent = new Intent(getApplicationContext(), controlpage.class);
                    startActivity(intent);
                    MainActivity.this.showMessage("Connect Succesfully");
                } catch (Exception e) {
                    MainActivity.this.showMessage("Error while connecting");
                }
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
                } else if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 10);
                } else {
                    new SearchDevices().execute();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {//exit function
        AlertDialog.Builder builder = new AlertDialog.Builder( this);
        builder.setMessage("Are you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    MainActivity.super.onBackPressed();
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

    private void initList(List<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                connect.setEnabled(true);
            }
        });
    }


    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {//search function

        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : pairedDevices) {
                listDevices.add(device);
            }
            return listDevices;

        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {//checking bluetooth device
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0) {
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                adapter.replaceItems(listDevices);
            } else {
                showMessage("No paired devices found, please pair your serial BT device and try again");
            }
        }

    }


    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {//bluetooth device list
        private int selectedIndex;
        private final Context context;
        private final int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount() {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }

        public List<BluetoothDevice> getEntireList() {
            return myList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;

            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.tv = (TextView) vi.findViewById(R.id.lstContent);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(getResources().getColor(
                        android.R.color.transparent
                ));
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());
            holder.tv.setTextColor(Color.WHITE);
            return vi;
        }

    }

    public void openBT(BluetoothDevice mmDevice) throws IOException {//open bluetooth
        this.mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        this.mmSocket.connect();
        connectSign=true;
        mmOutputStream = this.mmSocket.getOutputStream();
        mmInputStream = this.mmSocket.getInputStream();
        beginListenForData();
    }

    /* Access modifiers changed, original: 0000 */
    public void beginListenForData() {
        final Handler handler = new Handler();
        this.stopWorker = false;
        this.readBufferPosition = 0;
        this.readBuffer = new byte[1024];
        this.workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !MainActivity.this.stopWorker) {
                    try {
                        int bytesAvailable = MainActivity.this.mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            MainActivity.this.mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == (byte) 10) {
                                    byte[] encodedBytes = new byte[MainActivity.this.readBufferPosition];
                                    System.arraycopy(MainActivity.this.readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    MainActivity.this.readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                        }
                                    });
                                } else {
                                    byte[] bArr = MainActivity.this.readBuffer;
                                    MainActivity mainActivity = MainActivity.this;
                                    int i2 = mainActivity.readBufferPosition;
                                    mainActivity.readBufferPosition = i2 + 1;
                                    bArr[i2] = b;
                                }
                            }
                        }
                    } catch (IOException e) {
                        MainActivity.this.stopWorker = true;
                    }
                }
            }
        });
        this.workerThread.start();
    }

    public static void sendData(Character c) throws IOException {
        mmOutputStream.write(c.charValue());
    }


    public static String getData() throws IOException, InterruptedException {
        byte[] buffer = new byte[256];
        ArrayList<InputStream> inputstream= new ArrayList<InputStream>();
        inputstream.add(mmSocket.getInputStream());
        ArrayList<Integer> bytes= new ArrayList<Integer>();
        String readMessage= null;
        while (true) {
        bytes.add(inputstream.get(addon).read(buffer));
        readMessage = new String(buffer, 0, bytes.get(addon));
        return readMessage;
    }
    }


    /* Access modifiers changed, original: 0000 */
    public static void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    public void showMessage(String a) {
        Toast.makeText(this, a,Toast.LENGTH_LONG).show();
    }

}