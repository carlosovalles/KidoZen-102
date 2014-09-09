package com.kidozen.kidozenblankproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.graphics.*;
import android.widget.RelativeLayout;
import android.graphics.Bitmap.CompressFormat;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import kidozen.client.InitializationException;
import kidozen.client.KZApplication;
import kidozen.client.LogLevel;
import kidozen.client.ServiceEvent;
import kidozen.client.ServiceEventListener;
import kidozen.client.Storage;
import kidozen.client.Files;
import kidozen.client.SMSSender;



public class HelloKidoActivity extends Activity {

    public static final String TAG = "HelloKido Activity";
    final String TENANT = "https://tenantName.kidocloud.com/";
    final String APPLICATION = "AppName";
    final String AppKey = "AppKey";
    final String KidoZenProvider = "Kidozen";
    final String KidoZenUser = "tenant@kidozen.com";
    final String KidoZenPassword = "?????";

    Button authButton ;
    TextView textMessage;

    KZApplication app;

    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_kido);
        textMessage = (TextView)findViewById(R.id.textViewMessages);

        authButton = (Button)findViewById(R.id.buttonAuthenticate);
        authButton.setOnClickListener(authenticateWithKidozen);
        authButton.setEnabled(false);

        try {
            app = new KZApplication(TENANT,APPLICATION,AppKey,true);

            app.Initialize(new ServiceEventListener() {
                @Override
                public void onFinish(ServiceEvent e) {
                    authButton.setEnabled(true);
                }
            });


        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hello_kido, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


    // HERE IS WHERE THE AUTHENTICATION HAPPENS
    private View.OnClickListener authenticateWithKidozen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            try {
                app.Authenticate(KidoZenProvider, KidoZenUser, KidoZenPassword, new ServiceEventListener() {
                    @Override
                    public void onFinish(ServiceEvent e) {
                        if (e.StatusCode != HttpStatus.SC_OK) {
                            Log.d(TAG, "**** ERROR MESSAGE: Unable to reach the kidozen server. Make sure your KidoZenAppCenterUrl and KidoZenAppName are correct");
                        } else {
                            textMessage.setText("Hello: " + app.GetKidoZenUser().Claims.get("name"));
                            Log.d(TAG, app.GetKidoZenUser().Claims.get("name"));
                            Log.d(TAG, "KidoZen autentication sucessful.");

                            //Launch Kidozen TestServices method
                            LaunchKidozenServicesDemo();
                        }

                    }
                });
            } catch (InitializationException e) {
                e.printStackTrace();
            }
        }
    };


    private void LaunchKidozenServicesDemo(){


        // 1) STORAGE SERVICE SAMPLE:
        try {
            CreateOrder(123124,"create order #124123");
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 2) FILES SERVICE SAMPLE:
//        try {
//        GetFile ("test.png");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 3) LOG SERVICE SAMPLE:
//        try {
//            LogString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 4) SEND SMS SERVICE SAMPLE:
//        try {
//            SendSMS("1800555555","Hi There");
//        } catch (Exception e) {
//           e.printStackTrace();
//        }

    }


    public void CreateOrder(int id, String detail) throws  Exception{
        Storage orders = app.Storage("order");
        JSONObject order = new JSONObject().put("id", id).put("detail", detail);
        orders.Create(order, new ServiceEventListener() {

            JSONObject mMetadata;
            @Override
            public void onFinish(ServiceEvent e) {
                if (e.StatusCode==HttpStatus.SC_CREATED) {
                    // keep a reference of the response, which has the unique identifier of the message
                    mMetadata = (JSONObject) e.Response;
                    Log.d(TAG,mMetadata.toString());
                }
            }
        });
    }


    public void GetFile(String path) throws Exception {
        app.FileStorage().Download(path, new ServiceEventListener() {
            @Override
            public void onFinish (ServiceEvent e){
                if (e.StatusCode == HttpStatus.SC_OK) {
                    ByteArrayOutputStream response = (ByteArrayOutputStream) e.Response;
                    Bitmap bitmap;
                    bitmap = BitmapFactory.decodeByteArray(response.toByteArray(), 0, response.size());

                    ImageView image = new ImageView(HelloKidoActivity.this);
                    image.setImageBitmap(bitmap);
                }
            }

        });
    }

    public void LogString() throws Exception {
        final CountDownLatch lcd = new CountDownLatch(1);
        String data = String.format("application has been launched at : %s", new Date().getTime());
        app.WriteLog("Start",data, LogLevel.LogLevelCritical,new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {
                if (e.StatusCode==HttpStatus.SC_CREATED) {
                    //notify
                }
            }
        });
    }

    public void SendSMS(String number, String message) throws Exception {
        SMSSender sender = app.SMSSender(number);
        sender.Send(message, new ServiceEventListener() {
            @Override
            public void onFinish(ServiceEvent e) {

                Log.d(TAG, "SMS Sent successfully.");

                if (e.StatusCode==HttpStatus.SC_CREATED) {
                    // Notify UI
                }
            }
        });
    }


}
