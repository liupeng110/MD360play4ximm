package com.asha.md360player4android;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.ximmerse.input.ControllerInput;
import com.ximmerse.input.PositionalTracking;
import com.ximmerse.sdk.XDeviceApi;

import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class VideoPlayerActivity extends MD360PlayerActivity {

    private static final String tag = "VideoPlayerActivity";
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();

    //-----光点传感器
    protected PositionalTracking mHeadTrack = null;
    protected ControllerInput mControllerInputLeft = null;
    protected ControllerInput mControllerInputRight = null;
    protected Button[] mButtonArray = new Button[11];

    protected int handleHeadTrack;
    protected int handleControllerLeft;
    protected int handleControllerRight;
    //-----光点传感器结束





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XDeviceApi.init(this);
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
                if (getVRLibrary() != null){
                    getVRLibrary().notifyPlayerChanged();
                }
            }
        });



        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(VideoPlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                getVRLibrary().onTextureResize(width, height);
            }
        });

        Uri uri = getUri();
        if (uri != null){
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }

        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayerWrapper.pause();
                mMediaPlayerWrapper.destroy();
                mMediaPlayerWrapper.init();
                mMediaPlayerWrapper.openRemoteFile(DemoActivity.sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4");
                mMediaPlayerWrapper.prepare();
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
            }, 0, 5);//更新ui每5毫秒
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

            Log.i(tag,"red--:"+mControllerInputLeft.toString());
            mControllerInputLeft.getAxis(0);                  //第一个axis值
            mControllerInputLeft.getButton(0);

            if (mControllerInputLeft.getAxis(0)==1){ //乘以矩阵？
//               getVRLibrary().mInteractiveModeManager.handleDrag();//进行拖动


//                float  [] rotation={0.0f,0.0f,0.0f,0.0f};
//                mControllerInputLeft.getRotation(rotation,0);
//
//                Log.i(tag,0+"red--:四元数"+rotation[0]);
//                Log.i(tag,1+"red--:四元数"+rotation[1]);
//                Log.i(tag,2+"red--:四元数"+rotation[2]);
//                Log.i(tag,3+"red--:四元数"+rotation[3]);




            }

        }
    };

    //-----------------光点追踪







    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(R.id.gl_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerWrapper.destroy();
        XDeviceApi.exit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.resume();
    }
}
