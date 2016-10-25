package org.mixare.data.convert;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;
import org.mixare.POIMarker;
import org.mixare.R;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by KANG on 2016-10-09.
 */
public class GachonDataProcessor extends DataHandler implements DataProcessor{

    public static final int MAX_JSON_OBJECTS = 1000;

    private static final String TAG_NAME = "name";
    private static final String TAG_Altitude = "Altitude";
    private static final String TAG_Latitude = "Latitude";
    private static final String TAG_Longitude = "Longitude";

    String temp,json_temp = null, uri = "";
    JSONArray arr;

    public String[] getUrlMatch() {
        String[] str = {"gachon"};
        return str;
    }

    public String[] getDataMatch() {
        String[] str = {"gachon"};
        return str;
    }

    public boolean matchesRequiredType(String type) {
        if (type.equals(DataSource.TYPE.WIKIPEDIA.name())) {
            return true;
        }
        return false;
    }

    @Override
    public List<Marker> load(String rawData, int taskId, int colour) throws JSONException {
        return null;
    }


    public List<Marker> load(String uri) throws JSONException {

        GetDataJSON json = new GetDataJSON();
        List<Marker> markers = new ArrayList<Marker>();

        try {
            this.uri = uri;
            temp = json.execute(uri).get();
            System.out.println("temp:"+temp);
            JsonToData(temp,markers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*
        JSONObject root = convertToJSON(rawData);
        JSONArray dataArray = root.getJSONArray("geonames");

        int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

        for (int i = 0; i < top; i++) {
            JSONObject jo = dataArray.getJSONObject(i);

            Marker ma = null;
            if (jo.has("title") && jo.has("lat") && jo.has("lng")
                    && jo.has("elevation") && jo.has("GachonUrl")) {

                Log.v(MixView.TAG, "processing Wikipedia JSON object");

                //no unique ID is provided by the web service according to http://www.geonames.org/export/wikipedia-webservice.html
                ma = new POIMarker(
                        "",
                        HtmlUnescape.unescapeHTML(jo.getString("title"), 0),
                        jo.getDouble("lat"),
                        jo.getDouble("lng"),
                        jo.getDouble("elevation"),
                        uri,
                        taskId, colour);
                markers.add(ma);
            }
        }
        */
        return markers;
    }
    private void JsonToData(String getJson,List<Marker> markers){

        try {
            arr = new JSONArray(getJson);
            //jsonObject = new JSONObject(getJson.toString());
            //jsonObject = arr.getJSONObject(0);
            //arr = jsonObject.getJSONArray(TAG_RESULT);

            for(int i = 0; i<arr.length(); i++){
                JSONObject c = arr.getJSONObject(i);
                Marker ma = null;
                ma = new POIMarker(
                        c.getString(TAG_NAME),
                        c.getDouble(TAG_Latitude),
                        c.getDouble(TAG_Longitude),
                        c.getDouble(TAG_Altitude),
                        6,
                        uri);
                markers.add(ma);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class POIDATA {
        double Altitude;
        double Latitude;
        double Longitude;

        public POIDATA(){
            this.Altitude = 0;
            this.Latitude = 0;
            this.Longitude = 0;
        }

        public POIDATA(double Altitude,double Latitude,double Longitude) {
            super();
            this.Altitude = Altitude;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
        }
    }

    private JSONObject convertToJSON(String rawData) {
        try {
            return new JSONObject(rawData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // JSON Data 받아오는 Class
    class GetDataJSON extends AsyncTask<String, String, String> {

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

        public GetDataJSON() {
            super();
            jsonObject = null;
            arr = null;
        }

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
}
