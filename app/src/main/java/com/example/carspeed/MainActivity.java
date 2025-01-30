package com.example.carspeed;

import androidx.appcompat.app.AppCompatActivity;

import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.property.CarPropertyManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTvSpeed, mTvWarning;
    private Car mCar;
    String[] perms = {"android.car.permission.CAR_SPEED"};
    int permsRequestCode = 200;
    CarPropertyManager mCarPropertyManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvSpeed = findViewById(R.id.tv_speed);
        mTvWarning = findViewById(R.id.tv_warning);
        requestPermissions(perms, permsRequestCode);

    }

    CarPropertyManager.CarPropertyEventCallback mCallBack = new CarPropertyManager.CarPropertyEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
            Log.d(TAG, "onChangeEvent: " + carPropertyValue.toString());
            if (carPropertyValue.getPropertyId() == VehiclePropertyIds.PERF_VEHICLE_SPEED) {
                float value = (Float) carPropertyValue.getValue();
                mTvSpeed.setText("Speed: " + Math.round(value));

                // Show a warning if the speed is above 80
                if (value > 80) {
                    mTvWarning.setText("Warning: Speed is above 80 km/h!");
                    mTvWarning.setVisibility(View.VISIBLE); // Ensure the warning is visible
                    mTvWarning.setTextColor(Color.RED); // Optional: Set the warning text color to red
                } else {
                    mTvWarning.setVisibility(View.GONE); // Hide the warning if speed is below 80
                }
            }
        }

        @Override
        public void onErrorEvent(int i, int i1) {
            Log.e(TAG, "onErrorEvent: " + i);
        }
    };

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        switch (permsRequestCode) {
            case 200:
                Log.d(TAG, "onRequestPermissionsResult: " + permsRequestCode);
                boolean carPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (carPermission) {
                    mCar = Car.createCar(this);
                    mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
                    mCarPropertyManager.registerCallback(mCallBack, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL);

                    Log.d(TAG, "onRequestPermissionsResult: isConnected " + mCar.isConnected());
                } else {
                    requestPermissions(perms, permsRequestCode);
                }
                break;
            default:
                Log.d(TAG, "onRequestPermissionsResult: default " + permsRequestCode);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCar.disconnect();
    }
}