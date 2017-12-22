package com.example.ekene.pushertableinrealtime;

import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.entity.mime.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

     TextView name, age, position, address;
     EditText edtName, edtAge, edtPosition, edtAddress;
     Button BtnSave;
    final   String RECORDS_ENDPOINT = "http://pusher-table-in-realtime.herokuapp.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecordsAdapter recordsAdapter = new RecordsAdapter(this, new ArrayList<Record>());
        final ListView recordsView = (ListView) findViewById(R.id.records_view);
        recordsView.setAdapter(recordsAdapter);

        BtnSave = (Button)findViewById(R.id.BtnSave);
        name = (TextView)findViewById(R.id.name);
        age = (TextView)findViewById(R.id.age);
        position = (TextView)findViewById(R.id.position);
        address = (TextView)findViewById(R.id.address);


        edtName = (EditText)findViewById(R.id.edtName);
        edtName.setOnKeyListener(this);
        edtAddress = (EditText)findViewById(R.id.edtAddress);
        edtAddress.setOnKeyListener(this);
        edtAge = (EditText)findViewById(R.id.edtAge);
        edtAge.setOnKeyListener(this);
        edtPosition = (EditText)findViewById(R.id.edtPosition);
        edtPosition.setOnKeyListener(this);

        BtnSave.setOnClickListener(this);

        Pusher pusher = new Pusher("pusher_key");

        pusher.connect();

        Channel channel = pusher.subscribe("records");

        channel.bind("new_record", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        Record record = gson.fromJson(data, Record.class);
                        recordsAdapter.add(record);
                        recordsView.setSelection(recordsAdapter.getCount() - 1);
                    }

                });
            }

        });

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
            sendRecord();
        }
        return true;
    }

    private void sendRecord() {

        String nametxt = edtName.getText().toString();
        String agetxt = edtAge.getText().toString();
        String positiontxt = edtPosition.getText().toString();
        String addresstxt = edtAddress.getText().toString();

        // return if input fields are empty
        if (nametxt.equals("") && agetxt.equals("") && positiontxt.equals("") && addresstxt.equals("")){
            return;
        }

        RequestParams params = new RequestParams();

        // set our JSON object
        params.put("Name", nametxt);
        params.put("Age", agetxt);
        params.put("Position",positiontxt);
        params.put("Address",addresstxt);

        // create our HTTP client
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(RECORDS_ENDPOINT + "/records", params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edtName.setText("");
                        edtAge.setText("");
                        edtPosition.setText("");
                        edtAddress.setText("");
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(
                        getApplicationContext(),
                        "Something went wrong",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

    }
    @Override
    public void onClick(View view) {
        sendRecord();
    }

}



