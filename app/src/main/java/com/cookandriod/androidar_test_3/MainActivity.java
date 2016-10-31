package com.cookandriod.androidar_test_3;

        import android.Manifest;
        import android.app.Activity;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.support.v4.app.ActivityCompat;
        import android.util.Log;
        import android.view.ContextMenu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

public class MainActivity extends Activity {

    SurfaceView cameraPreview;
    SurfaceHolder previewHolder;
    android.hardware.Camera camera;
    boolean inPreview;

    final static String TAG = "PAAR";
    SensorManager sensorManager;

    CheckBox cbMulti;
    Button btnMapOn,btnMarkerInit,btnInfo;
    ImageButton img1,img2;
    ImageButton menu_img1,menu_img2;
    ImageButton edit_img1,edit_img2;
    RelativeLayout menu_relative1,menu_relative2;
    RelativeLayout edit_relative, main_relative;
    EditText search;
    TextView textAll,textSchool,textFood,textBook,textETC,textOption;

    boolean menu_flag,edit_flag;

    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;

    int accelerometerSensor;
    float xAxis;
    float yAxis;
    float zAxis;

    LocationManager locationManager;
    double latitude;
    double longitude;
    double altitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위 소스 아래부분에  splash.class 호출
        startActivity(new Intent(this, Splash.class));

        startActivity(new Intent(getBaseContext(), org.mixare.MixView.class));

        finish();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 2, locationListener);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);

        inPreview = false;

        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        previewHolder = cameraPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        img1 = (ImageButton)findViewById(R.id.img1);
        img2 = (ImageButton)findViewById(R.id.img2);

        menu_img1 = (ImageButton)findViewById(R.id.menu_img1);
        menu_img2 = (ImageButton)findViewById(R.id.menu_img2);

        menu_relative1 = (RelativeLayout)findViewById(R.id.menu_relative1);
        menu_relative2 = (RelativeLayout)findViewById(R.id.menu_relative2);

        edit_img1 = (ImageButton)findViewById(R.id.edit_img1);
        edit_img2 = (ImageButton)findViewById(R.id.edit_img2);

        main_relative = (RelativeLayout)findViewById(R.id.main_relative);
        edit_relative = (RelativeLayout)findViewById(R.id.edit_relative);

        search = (EditText)findViewById(R.id.edit_search);

        menu_flag = false;
        edit_flag = false;

        textAll = (TextView) findViewById(R.id.textAll);
        textSchool = (TextView) findViewById(R.id.textSchool);
        textFood = (TextView) findViewById(R.id.textFood);
        textBook = (TextView) findViewById(R.id.textBook);
        textETC = (TextView) findViewById(R.id.textETC);
        textOption = (TextView) findViewById(R.id.textOption);

        registerForContextMenu(textOption);


        // 메뉴 클릭시 ..
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlusMenu(menu_flag);
            }
        });

        menu_img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlusMenu(menu_flag);
            }
        });

        // 검색 클릭시 ..
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(edit_flag);
            }
        });

        menu_img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlusMenu(edit_flag);
            }
        });

        edit_img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(edit_flag);
            }
        });

        textOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openContextMenu(textOption);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater mInflater = this.getMenuInflater();
        mInflater.inflate(R.menu.option, menu);

    }

    private void setPlusMenu(boolean mflag){
        if(mflag == false) {
            img1.setVisibility(View.GONE);
            menu_relative1.setVisibility(View.VISIBLE);
            menu_relative2.setVisibility(View.VISIBLE);
            menu_flag = true;
        }else if(mflag == true){
            img1.setVisibility(View.VISIBLE);
            menu_relative1.setVisibility(View.GONE);
            menu_relative2.setVisibility(View.GONE);
            menu_flag = false;
        }
    }

    private void setSearchMenu(boolean sflag){
        if(sflag == false) {
            img1.setVisibility(View.GONE);
            edit_relative.setVisibility(View.VISIBLE);
            main_relative.setVisibility(View.GONE);

            edit_flag = true;
        }else if(sflag == true){
            img1.setVisibility(View.VISIBLE);
            edit_relative.setVisibility(View.GONE);
            main_relative.setVisibility(View.VISIBLE);

            edit_flag = false;
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);

        return true;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();

            Log.d(TAG, "Latitude : " + String.valueOf(latitude));
            Log.d(TAG, "Longitude : " + String.valueOf(longitude));
            Log.d(TAG, "Altitude : " + String.valueOf(altitude));

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                headingAngle = event.values[0];
                pitchAngle = event.values[1];
                rollAngle = event.values[2];

                Log.d(TAG, "heading : " + String.valueOf(headingAngle));
                Log.d(TAG, "Pitch : " + String.valueOf(pitchAngle));
                Log.d(TAG, "Roll : " + String.valueOf(rollAngle));


            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                xAxis = event.values[0];
                yAxis = event.values[1];
                zAxis = event.values[2];

                Log.d(TAG, "X Axis: " + String.valueOf(xAxis));
                Log.d(TAG, "Y Axis: " + String.valueOf(yAxis));
                Log.d(TAG, "Z Axis: " + String.valueOf(zAxis));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
        camera = android.hardware.Camera.open();
    }

    @Override
    protected void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(sensorEventListener);
        camera.release();
        camera=null;
        inPreview = false;

        super.onPause();
    }

    private android.hardware.Camera.Size getBestPreviewSize(int width, int height, android.hardware.Camera.Parameters parameters) {
        android.hardware.Camera.Size result=null;

        for (android.hardware.Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height<=height) {
                if (result==null) {
                    result = size;
                }
                else {
                    int resultArea = result.width*result.height;
                    int newArea = size.width*size.height;

                    if (newArea > resultArea) {
                        result=size;
                    }
                }
            }
        }
        return (result);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            android.hardware.Camera.Parameters parameters= camera.getParameters();
            android.hardware.Camera.Size size = getBestPreviewSize(width, height, parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview=true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };
}