package org.mixare;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KANG on 2016-09-22.
 */
class GetDataJSON extends AsyncTask<String, String, String> {

    Context c;
    View v;
    LinearLayout linear;

    String uri, json;
    BufferedReader br;

    JSONObject jsonObject;
    JSONArray arr;

    private static final String TAG_RESULT = "result";
    private static final String TAG_Altitude = "Altitude";
    private static final String TAG_Latitude = "Latitude";
    private static final String TAG_Longitude = "Longitude";

    //double Altitude,Latitude,Longitude;
    ArrayList<HashMap<String,Double>> PoiList;

    /*
    public GetDataJSON(Context c, View v) {
        super();
        this.c = c;
        this.v = v;

        jsonObject = null;
        arr = null;
    }
    */

    @Override
    protected String doInBackground(String... params) {
        uri = params[0];

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            StringBuilder sb = new StringBuilder();

            br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
            while ((json = br.readLine()) != null) {
                sb.append(json + "\n");
                //System.out.println(sb);
            }

            return sb.toString();

        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


}