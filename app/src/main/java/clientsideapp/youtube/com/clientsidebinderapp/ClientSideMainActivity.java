package clientsideapp.youtube.com.clientsidebinderapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ClientSideMainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG=ClientSideMainActivity.class.getSimpleName();
    private Context mContext;

    public static final int GET_RANDOM_NUMBER_FLAG =0;
    private boolean mIsBound;
    private int randomNumberValue;

    Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;

    private Intent serviceIntent;

    private TextView textViewRandomNumber;
    private Button buttonBindService,buttonUnBindService,buttonGetRandomNumber;

    class RecieveRandomNumberHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            randomNumberValue =0;
            switch (msg.what) {
                case GET_RANDOM_NUMBER_FLAG:
                    randomNumberValue =msg.arg1;
                    textViewRandomNumber.setText("Random Number: "+ randomNumberValue);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    ServiceConnection randomNumberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            randomNumberRequestMessenger=null;
            randomNumberReceiveMessenger=null;
            mIsBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            randomNumberRequestMessenger=new Messenger(binder);
            randomNumberReceiveMessenger=new Messenger(new RecieveRandomNumberHandler());
            mIsBound=true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext=getApplicationContext();
        textViewRandomNumber=(TextView)findViewById(R.id.textViewRandomNumber);

        buttonBindService=(Button)findViewById(R.id.buttonBindService);
        buttonUnBindService=(Button)findViewById(R.id.buttonUnBindService);
        buttonGetRandomNumber=(Button)findViewById(R.id.buttonGetRandomNumber);

        buttonGetRandomNumber.setOnClickListener(this);
        buttonBindService.setOnClickListener(this);
        buttonUnBindService.setOnClickListener(this);

        serviceIntent=new Intent();
        serviceIntent.setComponent(new ComponentName("servicesideapp.youtube.com.servicesideapp","servicesideapp.youtube.com.servicesideapp.MyService"));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonBindService: bindToRomoteService();
                    break;
            case R.id.buttonUnBindService: unbindFromRemoteSevice();
                    break;
            case R.id.buttonGetRandomNumber: fetchRandomNumber();
                    break;
            default:break;
        }
    }

    private void bindToRomoteService(){
        bindService(serviceIntent, randomNumberServiceConnection, BIND_AUTO_CREATE);
        Toast.makeText(mContext,"Service bound",Toast.LENGTH_SHORT).show();
    }

    private void unbindFromRemoteSevice(){
        unbindService(randomNumberServiceConnection);
        mIsBound=false;
        Toast.makeText(mContext,"Service Unbound",Toast.LENGTH_SHORT).show();
    }

    private void fetchRandomNumber(){
        if (mIsBound == true) {
            Message requestMessage=Message.obtain(null, GET_RANDOM_NUMBER_FLAG);
            requestMessage.replyTo=randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mContext,"Service Unbound, can't get random number",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        randomNumberServiceConnection=null;
    }
}
