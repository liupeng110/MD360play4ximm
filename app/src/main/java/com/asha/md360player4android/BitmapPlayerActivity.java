package com.asha.md360player4android;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.texture.Gxzz;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.ximmerse.input.ControllerInput;
import com.ximmerse.input.PositionalTracking;
import com.ximmerse.sdk.XDeviceApi;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class BitmapPlayerActivity extends MD360PlayerActivity {

    private static final String tag = "BitmapPlayerActivity";

    private Uri nextUri;
    Gxzz gxzz=new Gxzz();

    //-----光点传感器
    protected PositionalTracking mHeadTrack = null;
    protected ControllerInput mControllerInputLeft = null;
    protected ControllerInput mControllerInputRight = null;
    protected Button[] mButtonArray = new Button[11];

    protected int handleHeadTrack;
    protected int handleControllerLeft;
    protected int handleControllerRight;
    //-----光点传感器结束




    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XDeviceApi.init(this);//光点追踪

        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busy();
                nextUri = getDrawableUri(R.drawable.texture);
                getVRLibrary().notifyPlayerChanged();
            }
        });


        updateUi();//更新ui

    }





    //-----------------光点追踪
    private void updateUi(){

        handleHeadTrack = XDeviceApi.getInputDeviceHandle("XHawk-0");
        handleControllerLeft = XDeviceApi.getInputDeviceHandle("XCobra-0");
        handleControllerRight = XDeviceApi.getInputDeviceHandle("XCobra-1");

        mHeadTrack = new PositionalTracking(handleHeadTrack,"XHawk-0");
        mControllerInputLeft = new ControllerInput(handleControllerLeft,"XCobra-0");
        mControllerInputRight = new ControllerInput(handleControllerRight,"XCobra-1");


        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                runOnUiThread(updateUI);
            }
        }, 0, 50);//更新ui每5毫秒
    }


    public Runnable updateUI = new Runnable() {
        @Override public void run() {
            mControllerInputLeft.updateState(); //sdk中控制器 状态更新
            mControllerInputRight.updateState();
            mHeadTrack.updateState();
            mHeadTrack.timestamp = 1;//时间戳
//            bule.setText( String.format("{HeadTrack position=%.3f %.3f %.3f \r\n"
//                    , mHeadTrack.getPositionX(2)
//                    , mHeadTrack.getPositionY(2)
//                    , mHeadTrack.getPositionZ(2)));                     //蓝色
//            red.setText(mControllerInputLeft.toString());               //红色
//                        green.setText(mControllerInputRight.toString());//绿色

//            Log.i(tag,"red--:"+mControllerInputLeft.toString());


            mControllerInputLeft.getAxis(0);                  //第一个axis值
            mControllerInputLeft.getButton(0);

            if (mControllerInputLeft.getAxis(0)==1){ //乘以矩阵？
//               getVRLibrary().mInteractiveModeManager.handleDrag();//进行拖动

                float  [] rotation={0.0f,0.0f,0.0f,0.0f};
                mControllerInputLeft.getRotation(rotation,0);

                 float x = rotation[0];
                 float y = rotation[1];
                 float z = rotation[2];
                 float w = rotation[3];

                  float[] rotations = new float[16];
                rotations[0]=1.0f - 2.0f * y * y - 2.0f * z * z;
                rotations[1]=2.0f * x * y - 2.0f * z * w;
                rotations[2]=2.0f * x * z + 2.0f * y * w;
                rotations[3]= 0.0f;
                rotations[4]=2.0f * x * y + 2.0f * z * w;
                rotations[5]=1.0f - 2.0f * x * x - 2.0f * z * z;
                rotations[6]=2.0f * y * z - 2.0f * x * w;
                rotations[7]=0.0f;
                rotations[8]=2.0f * x * z - 2.0f * y * w;
                rotations[9]=2.0f * y * z + 2.0f * x * w;
                rotations[10]=1.0f - 2.0f * x * x - 2.0f * y * y;
                rotations[11]=0.0f;
                rotations[12]=0.0f;
                rotations[13]=0.0f;
                rotations[14]=0.0f;
                rotations[15]= 0.0f;
//
//                float[] rotationasd = new float[]{
//                    1.0f - 2.0f * y * y - 2.0f * z * z,
//                        2.0f * x * y - 2.0f * z * w,
//                        2.0f * x * z + 2.0f * y * w,
//                        0.0f,
//                            2.0f * x * y + 2.0f * z * w,
//                        1.0f - 2.0f * x * x - 2.0f * z * z,
//                        2.0f * y * z - 2.0f * x * w,
//                        0.0f,
//
//                            2.0f * x * z - 2.0f * y * w,
//                        2.0f * y * z + 2.0f * x * w,
//                        1.0f - 2.0f * x * x - 2.0f * y * y,
//                        0.0f,
//                            0.0f, 0.0f, 0.0f, 1.0f
//                };

                for (int a=0;a<15;a++){
                    Log.i(tag,a+"red--:矩阵:"+rotations[a]);
                }
                Log.i(tag,"red--:矩阵结束--------------------------------------------");


                gxzz.rotations=rotations;

                EventBus.getDefault().postSticky(gxzz);


                Log.i(tag,0+"red--:四元数"+rotation[0]);
                Log.i(tag,1+"red--:四元数"+rotation[1]);
                Log.i(tag,2+"red--:四元数"+rotation[2]);
                Log.i(tag,3+"red--:四元数"+rotation[3]);

            }

        }
    };

    //-----------------光点追踪






    private Target mTarget;// keep the reference for picasso.

    private void loadImage(Uri uri, final MD360BitmapTexture.Callback callback){
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(tag, "loaded image, size:" + bitmap.getWidth() + "," + bitmap.getHeight());

                // notify if size changed
                getVRLibrary().onTextureResize(bitmap.getWidth(), bitmap.getHeight());

                // texture
                callback.texture(bitmap);
                cancelBusy();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Log.d(tag, "load image with max texture size:" + callback.getMaxTextureSize());
        Picasso.with(getApplicationContext())
                .load(uri)
                .resize(callback.getMaxTextureSize(),callback.getMaxTextureSize())
                .onlyScaleDown()
                .centerInside()
                .memoryPolicy(NO_CACHE, NO_STORE)
                .into(mTarget);



    }

    private Uri currentUri(){
        if (nextUri == null){
            return getUri();
        } else {
            return nextUri;
        }
    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .asBitmap(new MDVRLibrary.IBitmapProvider() {
                    @Override
                    public void onProvideBitmap(final MD360BitmapTexture.Callback callback) {
                        loadImage(currentUri(), callback);
                    }
                })
                .listenTouchPick(new MDVRLibrary.ITouchPickListener() {
                    @Override
                    public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                        Log.d(tag,"Ray:" + ray + ", hitHotspot:" + hitHotspot);
                    }
                })
                .pinchEnabled(true)
                .projectionFactory(new CustomProjectionFactory())
                .build(R.id.gl_view);
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }
}
