/*
    This program powers the input screen. This program collects the necessary information
    for running the application and checks the input values to make sure that they are valid.
 */

package com.dji.FPVDemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import static java.lang.Float.parseFloat;

public class InputActivity extends Activity implements OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private Button mSubmitBtn;
    private EditText widthInputEditText, lengthInputEditText, passSpacingInputEditText, velocityInputEditText, altitudeInputEditText, transverseAxisInputEditText, cameraOrientationInputEditText;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.value_input);
        //showToast("On new screen");
        handler = new Handler();

        initUI();

    }


    private void initUI() {
        // init mVideoSurface
        mSubmitBtn = (Button) findViewById(R.id.btn_submit_value);
        mSubmitBtn.setOnClickListener(this);
        widthInputEditText = (EditText) findViewById(R.id.widthInput);
        lengthInputEditText = (EditText) findViewById(R.id.lengthInput);
        velocityInputEditText = (EditText) findViewById(R.id.velocityInput);
        altitudeInputEditText = (EditText) findViewById(R.id.altitudeInput);
        passSpacingInputEditText = (EditText) findViewById(R.id.passSpacingInput);
        transverseAxisInputEditText = (EditText) findViewById(R.id.transverseAxisInput);
        cameraOrientationInputEditText = (EditText) findViewById(R.id.cameraOrientationInput);

    }




    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(InputActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_submit_value:{
                Intent mainActivity = new Intent(this, MainActivity.class);
                Bundle valueInputs = new Bundle();
                if(widthInputEditText.getText().toString() == null || widthInputEditText.getText().toString().isEmpty() || lengthInputEditText.getText().toString() == null || lengthInputEditText.getText().toString().isEmpty() || transverseAxisInputEditText.getText().toString() == null || transverseAxisInputEditText.getText().toString().isEmpty() || (!transverseAxisInputEditText.getText().toString().toLowerCase().equals("width") && !transverseAxisInputEditText.getText().toString().toLowerCase().equals("length")) || (!cameraOrientationInputEditText.getText().toString().toLowerCase().equals("down") && !cameraOrientationInputEditText.getText().toString().toLowerCase().equals("forward")) ){
                    showToast("Ensure that you have filled in the correct fields with the appropriate values");
                }

                else{
                    valueInputs.putFloat("width", parseFloat(widthInputEditText.getText().toString()));
                    valueInputs.putFloat("length", parseFloat(widthInputEditText.getText().toString()));
                    valueInputs.putString("transverseAxis", transverseAxisInputEditText.getText().toString());
                    valueInputs.putFloat("velocity", 1);
                    valueInputs.putFloat("altitude", 3);
                    valueInputs.putFloat("passSpacing", 1);

                    if(velocityInputEditText.getText().toString() != null && !velocityInputEditText.getText().toString().isEmpty()){
                        valueInputs.putFloat("velocity", parseFloat(velocityInputEditText.getText().toString()));
                    }


                    if(altitudeInputEditText.getText().toString() != null && !altitudeInputEditText.getText().toString().isEmpty()){
                        valueInputs.putFloat("altitude", parseFloat(altitudeInputEditText.getText().toString()));
                    }


                    if( passSpacingInputEditText.getText().toString() != null && !passSpacingInputEditText.getText().toString().isEmpty()){
                        valueInputs.putFloat("passSpacing", parseFloat((passSpacingInputEditText.getText().toString())));
                    }

                    valueInputs.putString("cameraOrientation", cameraOrientationInputEditText.getText().toString().toLowerCase());


                    mainActivity.putExtras(valueInputs);
                    startActivity(mainActivity);
                }






                break;
            }

            default:
                break;
        }
    }


}