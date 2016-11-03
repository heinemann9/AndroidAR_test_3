package org.mixare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.lib.marker.Marker;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MJ on 2016-10-09.
 */
public class POIActivity extends Activity {

    private static final String TAG_NAME = "name";
    private static final String TAG_Category = "category";
    private static final String TAG_Picture = "picture";
    private static final String TAG_Etc = "etc";
    private static final String TAG_Floor_Info = "floor_info";
    private static final String TAG_Structure_Info= "structure_intro";
    private static final String TAG_Major_Info = "major_info";
    private static final String TAG_Tel_Info = "tel_info";

    JSONArray arr;

    ImageView picture;
    TextView name;
    TextView structure_intro;
    TextView tel_info;
    TextView floor_info;
    TextView major_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_info);

        name = (TextView) findViewById(R.id.POI_name);
        picture = (ImageView)findViewById(R.id.POI_picture);
        structure_intro = (TextView) findViewById(R.id.Structure_intro);
        tel_info = (TextView) findViewById(R.id.Tel_info);
        floor_info = (TextView) findViewById(R.id.Floor_info);
        major_info = (TextView) findViewById(R.id.Major_info);

        JsonToData(getIntent().getStringExtra("json"));
    }

    // data set
    private void JsonToData(String getJson){

        try {
            arr = new JSONArray(getJson);

            String url[] = new String[arr.length()];
            Arrays.fill(url, "http://heinemann.cafe24.com/img/");    // Array 초기화

            for(int i = 0; i<arr.length(); i++){

                JSONObject c = arr.getJSONObject(i);

                // picture 받아오기
                if(c.getString(TAG_Picture) != "null") {
                    url[i] += c.getString(TAG_Picture);
                    LoadBitmap task = new LoadBitmap();
                    task.execute(url[i]);
                }
                // 그 외 data 받아오기
                name.setText(c.getString(TAG_NAME));
                structure_intro.setText(c.getString(TAG_Structure_Info));
                tel_info.setText(c.getString(TAG_Tel_Info));
                floor_info.setText(c.getString(TAG_Floor_Info));
                major_info.setText(c.getString(TAG_Major_Info));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // picture 가져오는 스레드
    public class LoadBitmap extends AsyncTask<String,Integer,Bitmap> {

        Bitmap bmImg;

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
                URL myFileUrl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();

                bmImg = BitmapFactory.decodeStream(is);


            }catch(IOException e){
                e.printStackTrace();
            }
            return bmImg;
        }

        protected void onPostExecute(Bitmap img){
            picture.setImageBitmap(bmImg);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}