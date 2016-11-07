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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

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

    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        category = getIntent().getStringExtra("category");
        if(category.equals("학교건물")){
            setContentView(R.layout.poi_info_school);
        }else if(category.equals("음식")){
            setContentView(R.layout.poi_info_food);
        }else if(category.equals("문구,서적")){
            setContentView(R.layout.poi_info_book);
        }else if(category.equals("기타")){
            setContentView(R.layout.poi_info_etc);
        }
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

                // 그 외 data 받아오기

                if(category.equals("학교건물")){
                    //setContentView(R.layout.poi_info_school);

                    name = (TextView) findViewById(R.id.school_POI_name);
                    picture = (ImageView)findViewById(R.id.school_POI_picture);
                    structure_intro = (TextView) findViewById(R.id.school_Structure_intro);
                    tel_info = (TextView) findViewById(R.id.school_Tel_info);
                    floor_info = (TextView) findViewById(R.id.school_Floor_info);
                    major_info = (TextView) findViewById(R.id.school_Major_info);

                    // 정보없음 시 setText
                    if(c.getString(TAG_NAME) == "null") {
                        name.setText("정보없음");
                    }else{
                        name.setText(c.getString(TAG_NAME));
                    }
                    if(c.getString(TAG_Structure_Info) == "null") {
                        structure_intro.setText("정보없음");
                    }else{
                        structure_intro.setText(c.getString(TAG_Structure_Info));
                    }
                    if(c.getString(TAG_Tel_Info) == "null") {
                        tel_info.setText("정보없음");
                    }else{
                        tel_info.setText(c.getString(TAG_Tel_Info));
                    }
                    if(c.getString(TAG_Floor_Info) == "null") {
                        floor_info.setText("정보없음");
                    }else{
                        floor_info.setText(c.getString(TAG_Floor_Info));
                    }
                    if(c.getString(TAG_Major_Info) == "null") {
                        major_info.setText("정보없음");
                    }else{
                        major_info.setText(c.getString(TAG_Major_Info));
                    }

                }else if(category.equals("음식")){
                    //setContentView(R.layout.poi_info_food);

                    name = (TextView) findViewById(R.id.food_POI_name);
                    picture = (ImageView)findViewById(R.id.food_POI_picture);

                    if(c.getString(TAG_NAME) == "null") {
                        name.setText("정보없음");
                    }else{
                        name.setText(c.getString(TAG_NAME));
                    }
                }else if(category.equals("문구,서적")){
                    //setContentView(R.layout.poi_info_book);

                    name = (TextView) findViewById(R.id.book_POI_name);
                    picture = (ImageView)findViewById(R.id.book_POI_picture);
                    tel_info = (TextView) findViewById(R.id.book_Tel_info);

                    if(c.getString(TAG_NAME) == "null") {
                        name.setText("정보없음");
                    }else{
                        name.setText(c.getString(TAG_NAME));
                    }
                    if(c.getString(TAG_Tel_Info) == "null") {
                        tel_info.setText("정보없음");
                    }else{
                        tel_info.setText(c.getString(TAG_Tel_Info));
                    }
                }else{
                    //etc
                   // setContentView(R.layout.poi_info_etc);

                    name = (TextView) findViewById(R.id.etc_POI_name);
                    picture = (ImageView)findViewById(R.id.etc_POI_picture);
                    tel_info = (TextView) findViewById(R.id.etc_Tel_info);
                    if(c.getString(TAG_NAME) == "null") {
                        name.setText("정보없음");
                    }else{
                        name.setText(c.getString(TAG_NAME));
                    }
                    if(c.getString(TAG_Tel_Info) == "null") {
                        tel_info.setText("정보없음");
                    }else{
                        tel_info.setText(c.getString(TAG_Tel_Info));
                    }
                }

                // picture 받아오기
                if(c.getString(TAG_Picture) != "null") {
                    url[i] += c.getString(TAG_Picture);
                    LoadBitmap task = new LoadBitmap();
                    task.execute(url[i]);
                }
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
        Intent intent = new Intent(POIActivity.this,MixView.class);
        startActivity(intent);
        finish();
    }
}