package com.greycodes.zerito.helper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.greycodes.zerito.MainActivity;
import com.greycodes.zerito.UserNameActivity;
import com.greycodes.zerito.VerifyActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegisterService extends Service {
  String name,mob,id,url,results;
    HttpResponse response;
    SharedPreferences sharedPreferences;
    public static final String PROPERTY_REG_ID = "registration_id";
    int tc=0;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            sharedPreferences= getSharedPreferences("zerito", Context.MODE_PRIVATE);
            url="http://ieeelinktest.x20.in/app2/register.php";

            mob=sharedPreferences.getString("mobnum","");
            id=sharedPreferences.getString(PROPERTY_REG_ID,"");
            Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();

           new RegisterAsync().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    class RegisterAsync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("regId", id));
                nameValuePairs.add(new BasicNameValuePair("mob_num", mob));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);
                InputStream inputstream = null;
                HttpEntity entity = response.getEntity();

                inputstream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream,"UTF-8"),8);
                StringBuilder theStringBuilder = new StringBuilder();
                String line = null;
                while((line= reader.readLine())!=null){
                    theStringBuilder.append(line+ '\n');
                }
                results = theStringBuilder.toString();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
         //   Toast.makeText(getApplicationContext(), results, Toast.LENGTH_LONG).show();


            try {
                JSONObject jsonObject = new JSONObject(results);
                if(jsonObject.getBoolean("success")){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("smsverification", true);
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(),UserNameActivity.class);
                    intent.putExtra("name",jsonObject.getString("name"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //UserNameActivity.UpdateUsername(jsonObject.getString("name"));
                    //Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //getApplication().startActivity(intent);
                }else if(!jsonObject.getBoolean("success")){
                    Toast.makeText(getApplicationContext(), "Already registered", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }

                stopSelf();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Coudn't connect to the Internet"+e.toString(),Toast.LENGTH_LONG).show();
                tc++;
                if(tc<5){
                    SystemClock.sleep(1000);
                    new RegisterAsync().execute();
                }

                e.printStackTrace();
            }


        }
    }
}
