package com.asha.vrlib;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.asha.vrlib.common.VRUtil;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.texture.Gxzz;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * response for model * view * projection
 */
public class MD360Director {

    private static final String TAG = "MD360Director";
    private static final float sNear = 0.7f;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float mEyeX = 0f;
    private float mEyeY = 0f;
    private float mEyeZ = 0f;
    private float mLookX = 0f;
    private float mLookY = 0f;
    private float mRatio = 0f;
    private float mNearScale = 0f;
    private final MDPosition mCameraRotatePosition;
    private int mViewportWidth = 2;
    private int mViewportHeight = 1;

    private float[] mWorldRotationMatrix = new float[16];
    private float[] mWorldRotationInvertMatrix = new float[16];
    private float[] mCurrentRotationPost = new float[16];
    private float[] mSensorMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];

    private float mDeltaX;
    private float mDeltaY;

    private boolean mCameraMatrixInvalidate = true;
    private boolean mWorldRotationMatrixInvalidate = true;

    protected MD360Director(Builder builder) {
        this.mRatio = builder.mRatio;
        this.mNearScale = builder.mNearScale;
        this.mEyeX = builder.mEyeX;
        this.mEyeY = builder.mEyeY;
        this.mEyeZ = builder.mEyeZ;
        this.mLookX = builder.mLookX;
        this.mLookY = builder.mLookY;
        this.mCameraRotatePosition = builder.mRotation;
        initModel();
        EventBus.getDefault().register(this);
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float mDeltaY) {
        this.mDeltaY = mDeltaY;
        mWorldRotationMatrixInvalidate = true;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float mDeltaX) {
        this.mDeltaX = mDeltaX;
        mWorldRotationMatrixInvalidate = true;
    }

    private void initModel(){
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mSensorMatrix, 0);
    }

    public void beforeShot(){
        updateViewMatrixIfNeed();
    }

    public void shot(MD360Program program, MDPosition modelPosition) {
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, modelPosition.getMatrix(), 0);

        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        // Pass in the model view matrix
        GLES20.glUniformMatrix4fv(program.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(program.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
    }

    private void updateViewMatrixIfNeed(){
        if (mCameraMatrixInvalidate || mWorldRotationMatrixInvalidate){
            if (mCameraMatrixInvalidate){
                updateCameraMatrix();
                mCameraMatrixInvalidate = false;
            }

            if (mWorldRotationMatrixInvalidate){
                updateWorldRotationMatrix();
                mWorldRotationMatrixInvalidate = false;
            }

            Matrix.multiplyMM(mViewMatrix, 0, mCameraMatrix, 0, mWorldRotationMatrix, 0);
        }
    }

    public void updateViewport(int width, int height){
        // Projection Matrix
        mViewportWidth = width;
        mViewportHeight = height;
        mRatio = width * 1.0f / height;
        updateProjection();
    }

    // call from gl thread
    public void updateProjectionNearScale(float scale){
        mNearScale = scale;
        updateProjection();
    }

    protected void updateProjection(){
        final float left = -mRatio/2;
        final float right = mRatio/2;
        final float bottom = -0.5f;
        final float top = 0.5f;
        final float far = 500;
        Matrix.frustumM(getProjectionMatrix(), 0, left, right, bottom, top, getNear(), far);
    }


   //更新view矩阵 测试
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Gxzz temp) {
        float [] rotation =temp.rotations;
        updateViewmatrix(rotation);
    }



    protected float getNear(){
        return mNearScale * sNear;
    }

    protected float getRatio(){
        return mRatio;
    }

    public float[] getProjectionMatrix(){
        return mProjectionMatrix;
    }

    public int getViewportWidth() {
        return mViewportWidth;
    }

    public int getViewportHeight() {
        return mViewportHeight;
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    private void updateCameraMatrix() {
        final float eyeX = mEyeX;
        final float eyeY = mEyeY;
        final float eyeZ = mEyeZ;
        final float lookX = mLookX;
        final float lookY = mLookY;
        final float lookZ = -1.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setLookAtM(mCameraMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    private void updateWorldRotationMatrix(){
        Matrix.setIdentityM(mWorldRotationMatrix, 0);
        Matrix.rotateM(mWorldRotationMatrix, 0, -mDeltaY, 1.0f, 0.0f, 0.0f);
        Matrix.setIdentityM(mCurrentRotationPost, 0);
        Matrix.rotateM(mCurrentRotationPost, 0, -mDeltaX, 0.0f, 1.0f, 0.0f);

        Matrix.setIdentityM(mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mCurrentRotationPost, 0, mCameraRotatePosition.getMatrix(), 0);
        Matrix.multiplyMM(mCurrentRotationPost, 0, mSensorMatrix, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mWorldRotationMatrix, 0, mCurrentRotationPost, 0);
        System.arraycopy(mTempMatrix, 0, mWorldRotationMatrix, 0, 16);

        boolean success = VRUtil.invertM(mWorldRotationInvertMatrix, mWorldRotationMatrix);
        if (!success){
            Matrix.setIdentityM(mWorldRotationInvertMatrix, 0);
        }

    }

    // call in gl thread
    public void updateSensorMatrix(float[] sensorMatrix) {
        System.arraycopy(sensorMatrix, 0, mSensorMatrix, 0, 16);
        mWorldRotationMatrixInvalidate = true;
    }

    // call in gl thread
    public void reset(){
        mDeltaX = mDeltaY = 0;
        Matrix.setIdentityM(mSensorMatrix,0);
        mWorldRotationMatrixInvalidate = true;
    }


    //自己添加 传感器更新view矩阵
    public void updateViewmatrix(float [] rotations){
//        System.arraycopy(rotations, 0, mViewMatrix, 0, 16);
        Matrix.multiplyMM(mViewMatrix, 0, mViewMatrix, 0, rotations, 0);
        mCameraMatrixInvalidate = true;
    }



    public static Builder builder(){ return new Builder(); }

    public float[] getWorldRotationInvert() {
        return mWorldRotationInvertMatrix;
    }

    public static class Builder {
        private float mEyeX = 0f;
        private float mEyeY = 0f;
        private float mEyeZ = 0f;
        private float mRatio = 1.5f;
        private float mNearScale = 1f;
        private float mLookX = 0f;
        private float mLookY = 0f;
        private MDMutablePosition mRotation = MDMutablePosition.newInstance();

        public Builder setLookX(float mLookX) {
            this.mLookX = mLookX;
            return this;
        }

        public Builder setLookY(float mLookY) {
            this.mLookY = mLookY;
            return this;
        }

        public Builder setEyeX(float mEyeX) {
            this.mEyeX = mEyeX;
            return this;
        }

        public Builder setEyeY(float mEyeY) {
            this.mEyeY = mEyeY;
            return this;
        }

        public Builder setEyeZ(float mEyeZ) {
            this.mEyeZ = mEyeZ;
            return this;
        }

        public Builder setRoll(float roll){
            mRotation.setRoll(roll);
            return this;
        }

        public Builder setPitch(float pitch){
            mRotation.setPitch(pitch);
            return this;
        }

        public Builder setYaw(float yaw){
            mRotation.setYaw(yaw);
            return this;
        }

        public Builder setRatio(float mRatio) {
            this.mRatio = mRatio;
            return this;
        }

        public Builder setNearScale(float scale) {
            this.mNearScale = scale;
            return this;
        }

        public MD360Director build(){
            return new MD360Director(this);
        }
    }
}
