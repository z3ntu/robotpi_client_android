package io.github.z3ntu.remoterobot.socket;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import io.github.z3ntu.remoterobot.R;


public class RemoteRobotPiSocket extends AppCompatActivity {

    public static String CONTROL_URL;
    public static String DISTANCE_URL;
    public static boolean UPDATE_DISTANCE;
    public Timer timer;
    private Vibrator vibrator;
    private boolean inSettings = false;

    private SharedPreferences sharedPrefs;
    private ConnectionHandler connectionHandler;
    private HandlerThread connectionHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        CONTROL_URL = sharedPrefs.getString("control_addr", "httpbin.org/get");
        DISTANCE_URL = sharedPrefs.getString("distance_addr", "httpbin.org/ip");
        UPDATE_DISTANCE = sharedPrefs.getBoolean("distance", false);

        setContentView(R.layout.activity_remote_robot_pi);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        final Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.STOP);
            }
        });

        final Button forward = (Button) findViewById(R.id.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.FORWARD);
            }
        });

        final Button backward = (Button) findViewById(R.id.back);
        backward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.BACKWARD);
            }
        });

        final Button left = (Button) findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.LEFT);
            }
        });

        final Button right = (Button) findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.RIGHT);
            }
        });

        final Button rotate_left = (Button) findViewById(R.id.rotate_left);
        rotate_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.ROTATE_LEFT);
            }
        });

        final Button rotate_right = (Button) findViewById(R.id.rotate_right);
        rotate_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.ROTATE_RIGHT);
            }
        });

        final Button auto = (Button) findViewById(R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                request(Mode.AUTO);
            }
        });

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDistance();
            }
        }, 0, 1000);


        connectionHandlerThread = new HandlerThread("ConnectionThread");
        connectionHandlerThread.start();

        connectionHandler = new ConnectionHandler(connectionHandlerThread.getLooper(), sharedPrefs.getString("raw_addr", null));

        Message connect_message = Message.obtain(connectionHandler);
        connect_message.what = ConnectionHandler.MessageCode.CLASS_CONNECTION; // EventClass CONNECTION
        connect_message.arg1 = ConnectionHandler.MessageCode.CONNECTION_CONNECT; // EventAction CONNECT

        connectionHandler.sendMessage(connect_message);
//        connectionHandler.
//        message.call
    }

    public void handleMessage(Message msg) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote_robot_pi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            final TextView mTextView = (TextView) findViewById(R.id.text);
//            Toast.makeText(getApplicationContext(),
//                    "Should open settings! ;)",
//                    Toast.LENGTH_LONG).show();
            showSettingsDialog();
            //TODO: SHOW PREFERENCES
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void request(Mode mode) {
        Message message = Message.obtain(connectionHandler, ConnectionHandler.MessageCode.CLASS_COMMAND, 0, 0, mode.getCommand());
        connectionHandler.sendMessage(message);
    }

    public void showSettingsDialog() {
        if (inSettings)
            return;
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        SettingsFragment mSettingsFragment = new SettingsFragment();

        mSettingsFragment.setRemoteRobotPi(this);

        mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        mFragmentTransaction.replace(android.R.id.content, mSettingsFragment);

/*        mFragmentTransaction.setCustomAnimations(R.anim.animation_test, 0);
        mFragmentTransaction.show(mSettingsFragment);*/
//        mFragmentTransaction.add(mSettingsFragment, "settings");
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
        inSettings = true;
    }

    @Override
    public void onBackPressed() {
        System.out.println("onBackPressed");
        if (inSettings) {
            backFromSettingsFragment();
            return;
        }
        super.onBackPressed();
    }

    private void backFromSettingsFragment() {
        inSettings = false;
        getFragmentManager().popBackStack();
    }

    public void updateDistance() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        request(Mode.DISCONNECT);
//        request(Side.EXIT);

//        Message message = Message.obtain(connectionHandler);
//        message.what = ConnectionHandler.MessageCode.CLASS_CONNECTION; // EventClass CONNECTION
//        message.arg1 = -1; // EventAction CONNECT
//
//        connectionHandler.sendMessage(message);
    }


    public enum Mode {
        FORWARD("F099"), BACKWARD("B099"), LEFT("FR99"), RIGHT("FL99"), ROTATE_LEFT("RL99"), ROTATE_RIGHT("RR99"), STOP("0000"), AUTO("AUTO"), DISCONNECT("C001"), EXIT("EXIT");

        private String command;

        Mode(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command + "\r";
        }
    }
}
