package com.google.ar.sceneform.samples.hellosceneform;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.ScaleController;
import com.google.ar.sceneform.ux.TransformableNode;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.BasicAdder;

//import .python.chaquopy.main.*;
//import chaquopy.demo.test.UIDemoActivity;


public class HelloSceneformActivity extends AppCompatActivity implements Node.OnTapListener, Scene.OnUpdateListener {
    private static final String TAG = HelloSceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    ArrayList<Float> arrayList1 = new ArrayList<>();
    ArrayList<Float> arrayList2 = new ArrayList<>();
    private ArFragment arFragment;
    private AnchorNode lastAnchorNode;
    private TextView txtDistance;
    Button btnDist, btnHeight, btnClear, btnMyAction, btnTestHit, btnTakePhoto, btnMyActionList, btnTestPython;
    PrintStream pPRINT = null;
    ModelRenderable cubeRenderable, heightRenderable;
    boolean btnHeightClicked, btnLengthClicked, btnMyActionClicked, btnTestHitClicked, btnTakePhotoClicked, btnMyActionListClicked;
    Vector3 point1, point2;

    @SuppressLint("SetTextI18n")
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        txtDistance = findViewById(R.id.txtDistance);
        btnDist = findViewById(R.id.btnDistance);
        btnDist.setOnClickListener(v -> {
            btnLengthClicked = true;
            btnHeightClicked = false;
            btnMyActionClicked = false;
            btnTestHitClicked = false;
            btnMyActionListClicked = false;
            onClear();
        });
        btnHeight = findViewById(R.id.btnHeight);
        btnHeight.setOnClickListener(v -> {
            btnHeightClicked = true;
            btnLengthClicked = false;
            btnMyActionClicked = false;
            btnTestHitClicked = false;
            btnMyActionListClicked = false;
            onClear();
        });
        btnMyAction = findViewById(R.id.btnMyAction);
        btnMyAction.setOnClickListener(v -> {
//            Toast.makeText(getApplicationContext(), "MyAction started", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), String.valueOf(arFragment.isArRequired()), Toast.LENGTH_SHORT).show();
            btnHeightClicked = false;
            btnLengthClicked = false;
            btnMyActionClicked = true;
            btnTestHitClicked = false;
            btnMyActionListClicked = false;
            onClear();
        });
        btnTestHit = findViewById(R.id.btnTestHit);
        btnTestHit.setOnClickListener(v -> {
//            Toast.makeText(getApplicationContext(), "MyAction started", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), String.valueOf(arFragment.isArRequired()), Toast.LENGTH_SHORT).show();
            btnHeightClicked = false;
            btnLengthClicked = false;
            btnMyActionClicked = false;
            btnTestHitClicked = true;
            btnMyActionListClicked = false;
            onClear();
        });
        btnMyActionList = findViewById(R.id.btnMyActionList);
        btnMyActionList.setOnClickListener(v -> {
//            Toast.makeText(getApplicationContext(), "MyAction started", Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), String.valueOf(arFragment.isArRequired()), Toast.LENGTH_SHORT).show();
            btnHeightClicked = false;
            btnLengthClicked = false;
            btnMyActionClicked = false;
            btnTestHitClicked = false;
            btnMyActionListClicked = true;
            onClear();
        });
        btnTestPython = findViewById(R.id.btnTestPython);
        btnTestPython.setOnClickListener(v -> {

            if (! Python.isStarted()){
                Python.start(new AndroidPlatform(getApplicationContext()));
            }
            Python py = Python.getInstance();
            try {
                PyObject a = py.getModule("main").callAttr("test", 1);
                Toast.makeText(this, "1+10: " + String.valueOf(a.toInt()), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                PyObject BA = py.getModule("main").get("BasicAdder");
                PyObject ba_po = BA.call(22);
                BasicAdder ba = ba_po.toJava(BasicAdder.class);
                Toast.makeText(this, "22+3: " + String.valueOf(ba.add(3)), Toast.LENGTH_SHORT).show();
//                assertEquals(45, ba.add(3));
//                assertSame(ba_po, PyObject.fromJava(ba));
            } catch (Exception e) {
                e.printStackTrace();
            }

//            UIDemoActivity.test();
        });
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Convert！", Toast.LENGTH_SHORT).show();
            // Get Bitmap for corner detection
            Bitmap bitmap = getBitmapFromView();

            // БОЛЬШОЙ КОСТЫЛЬ!!!!
            Bitmap newbitmap = Bitmap.createBitmap(bitmap,92, 0, bitmap.getWidth() - 92 * 2, bitmap.getHeight());
            // Save Bitmap to album
            saveBmp2Gallery(newbitmap,"aaaa_");

            // Save Bitmap to album
            saveBmp2Gallery(bitmap,"aaaa");


            ArrayList<Point> points = CornorDetect.getCorner(bitmap);
            if (points == null){
                Toast.makeText(this, "Detection failed", Toast.LENGTH_SHORT).show();
            }else{
                Log.d(TAG, "cornors: "+points.size());
                Log.d(TAG, "p1"+points.get(0).x+", "+points.get(0).y);
                Log.d(TAG, "p2"+points.get(1).x+", "+points.get(1).y);
                Log.d(TAG, "p3"+points.get(2).x+", "+points.get(2).y);
                Log.d(TAG, "p4"+points.get(3).x+", "+points.get(3).y);
                Toast.makeText(this, "Successful detection", Toast.LENGTH_SHORT).show();

                showCornerAnchor(points);
            }
        });


        btnClear = findViewById(R.id.clear);
        btnClear.setOnClickListener(v -> {
            try {
                if (pPRINT != null) {
                    pPRINT.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            onClear();
        });

        MaterialFactory.makeTransparentWithColor(this, new Color(0F, 0F, 244F))
                .thenAccept(
                        material -> {
                            Vector3 vector3 = new Vector3(0.01f, 0.01f, 0.01f);
                            cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material);
                            cubeRenderable.setShadowCaster(false);
                            cubeRenderable.setShadowReceiver(false);
                        });

        MaterialFactory.makeTransparentWithColor(this, new Color(0F, 0F, 244F))
                .thenAccept(
                        material -> {
                            Vector3 vector3 = new Vector3(0.007f, 0.1f, 0.007f);
                            heightRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material);
                            heightRenderable.setShadowCaster(false);
                            heightRenderable.setShadowReceiver(false);
                        });


        arFragment.setOnTapArPlaneListener(
                    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (cubeRenderable == null) {
                        return;
                    }

                    if (btnHeightClicked) {
                        if (lastAnchorNode != null) {
                            Toast.makeText(this, "Please click clear button", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                        transformableNode.setParent(anchorNode);
                        transformableNode.setRenderable(heightRenderable);
                        transformableNode.select();
                        ScaleController scaleController = transformableNode.getScaleController();
                        scaleController.setMaxScale(10f);
                        scaleController.setMinScale(0.01f);
                        transformableNode.setOnTapListener(this);
                        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
                        lastAnchorNode = anchorNode;
                    }

                    if (btnLengthClicked) {
                        if (lastAnchorNode == null) {
                            Anchor anchor = hitResult.createAnchor();
                            AnchorNode anchorNode = new AnchorNode(anchor);
                            anchorNode.setParent(arFragment.getArSceneView().getScene());

                            Pose pose = anchor.getPose();
                            if (arrayList1.isEmpty()) {
                                arrayList1.add(pose.tx());
                                arrayList1.add(pose.ty());
                                arrayList1.add(pose.tz());
                            }
                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();
                            lastAnchorNode = anchorNode;
                        } else {
                            int val = motionEvent.getActionMasked();
                            float axisVal = motionEvent.getAxisValue(MotionEvent.AXIS_X, motionEvent.getPointerId(motionEvent.getPointerCount() - 1));
                            Log.e("Values:", String.valueOf(val) + String.valueOf(axisVal));
                            Anchor anchor = hitResult.createAnchor();
                            AnchorNode anchorNode = new AnchorNode(anchor);
                            anchorNode.setParent(arFragment.getArSceneView().getScene());

                            Pose pose = anchor.getPose();


                            if (arrayList2.isEmpty()) {
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                            } else {
                                arrayList1.clear();
                                arrayList1.addAll(arrayList2);
                                arrayList2.clear();
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                            }

                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();

                            Vector3 point1, point2;
                            point1 = lastAnchorNode.getWorldPosition();
                            point2 = anchorNode.getWorldPosition();

                            final Vector3 difference = Vector3.subtract(point1, point2);
                            final Vector3 directionFromTopToBottom = difference.normalized();
                            final Quaternion rotationFromAToB =
                                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                                    .thenAccept(
                                            material -> {
                                                ModelRenderable model = ShapeFactory.makeCube(
                                                        new Vector3(.01f, .01f, difference.length()),
                                                        Vector3.zero(), material);
                                                Node node = new Node();
                                                node.setParent(anchorNode);
                                                node.setRenderable(model);
                                                node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                                node.setWorldRotation(rotationFromAToB);
                                            }
                                    );
                            lastAnchorNode = anchorNode;
                        }
                    }

                    if (btnMyActionClicked) {
                        if (lastAnchorNode == null) {
                            AnchorNode anchorNode = new AnchorNode();
                            Anchor anchor = null;

                            float phone_width, phone_height;
                            Display display = getWindowManager().getDefaultDisplay();
                            android.graphics.Point outSize = new android.graphics.Point();
                            display.getSize(outSize);
                            phone_width = outSize.x;
                            phone_height = outSize.y;

                            try {
                                Frame frame = arFragment.getArSceneView().getArFrame();
                                anchor = frame.hitTest(phone_width/2, phone_height/2 + 100).get(0).createAnchor();
                                anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arFragment.getArSceneView().getScene());
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), "error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            Pose pose = anchor.getPose();
                            if (arrayList1.isEmpty()) {
                                arrayList1.add(pose.tx());
                                arrayList1.add(pose.ty());
                                arrayList1.add(pose.tz());
                            }

                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();
                            lastAnchorNode = anchorNode;

//                            Toast.makeText(getApplicationContext(), "lastAnchorNode == null: " + String.valueOf(lastAnchorNode == null), Toast.LENGTH_SHORT).show();
                        } else {
                            int val = motionEvent.getActionMasked();
                            float axisVal = motionEvent.getAxisValue(MotionEvent.AXIS_X, motionEvent.getPointerId(motionEvent.getPointerCount() - 1));
                            Log.e("Values in MyAction:", String.valueOf(val) + String.valueOf(axisVal));

//                            Camera camera = arFragment.getArSceneView().getScene().getCamera();
//                            Ray ray = new Ray(camera.getLocalPosition(), camera.getForward());
//                            HitTestResult result = arFragment.getArSceneView().getScene().hitTest(ray);
//                            Vector3 resultPosition = result.getPoint();


                            AnchorNode anchorNode = new AnchorNode();
                            Anchor anchor = null;
                            List<HitResult> hitResults = null;
////                            anchorNode.setParent(arFragment.getArSceneView().getScene());
//                            anchorNode.setParent(lastAnchorNode.getParent());
//                            Vector3 WorldPosition = result.getPoint();
//                            anchorNode.setWorldPosition(WorldPosition);

                            float phone_width, phone_height;
                            Display display = getWindowManager().getDefaultDisplay();
                            android.graphics.Point outSize = new android.graphics.Point();
                            display.getSize(outSize);
                            phone_width = outSize.x;
                            phone_height = outSize.y;

                            try {
                                Frame frame = arFragment.getArSceneView().getArFrame();
                                hitResults = frame.hitTest(phone_width/2, phone_height/2 + 100);
                                anchor = hitResults.get(0).createAnchor();
                                anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arFragment.getArSceneView().getScene());
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), "error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }

//                            Vector3.subtract()

                            Toast.makeText(getApplicationContext(), String.valueOf(hitResults.size()), Toast.LENGTH_SHORT).show();

//                            Anchor anchor = hitResult.createAnchor();
//                            AnchorNode anchorNode = new AnchorNode(anchor);
//                            anchorNode.setParent(arFragment.getArSceneView().getScene());
//
                            Pose pose = anchor.getPose();

                            if (arrayList2.isEmpty()) {
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                            } else {
                                arrayList1.clear();
                                arrayList1.addAll(arrayList2);
                                arrayList2.clear();
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                            }

                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();

                            Vector3 point1, point2;
                            point1 = lastAnchorNode.getWorldPosition();
                            point2 = anchorNode.getWorldPosition();

                            final Vector3 difference = Vector3.subtract(point1, point2);
                            final Vector3 directionFromTopToBottom = difference.normalized();
                            final Quaternion rotationFromAToB =
                                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

                            AnchorNode finalAnchorNode = anchorNode;
                            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                                    .thenAccept(
                                            material -> {
                                                ModelRenderable model = ShapeFactory.makeCube(
                                                        new Vector3(.01f, .01f, difference.length()),
                                                        Vector3.zero(), material);
                                                Node node = new Node();
                                                node.setParent(finalAnchorNode);
                                                node.setRenderable(model);
                                                node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                                node.setWorldRotation(rotationFromAToB);
                                            }
                                    );
                            lastAnchorNode = anchorNode;
                        }
                    }

                    if (btnMyActionListClicked) {
                        if (lastAnchorNode == null) {
                            AnchorNode anchorNode = new AnchorNode();
                            Anchor anchor = null;

                            float phone_width, phone_height;
                            Display display = getWindowManager().getDefaultDisplay();
                            android.graphics.Point outSize = new android.graphics.Point();
                            display.getSize(outSize);
                            phone_width = outSize.x;
                            phone_height = outSize.y;

                            try {
                                Frame frame = arFragment.getArSceneView().getArFrame();
                                anchor = frame.hitTest(phone_width/2, phone_height/2 + 100).get(0).createAnchor();
                                anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arFragment.getArSceneView().getScene());
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), "error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            Pose pose = anchor.getPose();
                            if (arrayList1.isEmpty()) {
                                arrayList1.add(pose.tx());
                                arrayList1.add(pose.ty());
                                arrayList1.add(pose.tz());
                            }

                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();
                            lastAnchorNode = anchorNode;

//                            Toast.makeText(getApplicationContext(), "lastAnchorNode == null: " + String.valueOf(lastAnchorNode == null), Toast.LENGTH_SHORT).show();
                        } else {
                            int val = motionEvent.getActionMasked();
                            float axisVal = motionEvent.getAxisValue(MotionEvent.AXIS_X, motionEvent.getPointerId(motionEvent.getPointerCount() - 1));
                            Log.e("Values in MyAction:", String.valueOf(val) + String.valueOf(axisVal));

//                            ((AnchorNode) node).getAnchor().detach();

                            AnchorNode anchorNode = new AnchorNode();
                            Anchor anchor = null;
                            List<HitResult> hitResults = null;

                            float phone_width, phone_height;
                            Display display = getWindowManager().getDefaultDisplay();
                            android.graphics.Point outSize = new android.graphics.Point();
                            display.getSize(outSize);
                            phone_width = outSize.x;
                            phone_height = outSize.y;

                            try {
                                Frame frame = arFragment.getArSceneView().getArFrame();
                                hitResults = frame.hitTest(phone_width/2, phone_height/2 + 100);
                                anchor = hitResults.get(0).createAnchor();
                                anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arFragment.getArSceneView().getScene());
                            } catch (Exception exception) {
                                Toast.makeText(getApplicationContext(), "error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(getApplicationContext(), String.valueOf(hitResults.size()), Toast.LENGTH_SHORT).show();


                            Pose pose = anchor.getPose();

                            if (arrayList2.isEmpty()) {
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                                writeFile( "measurements", String.valueOf(d));
                            } else {
                                arrayList1.clear();
                                arrayList1.addAll(arrayList2);
                                arrayList2.clear();
                                arrayList2.add(pose.tx());
                                arrayList2.add(pose.ty());
                                arrayList2.add(pose.tz());
                                float d = getDistanceMeters(arrayList1, arrayList2);
                                txtDistance.setText("Distance: " + String.valueOf(d));
                                writeFile( "measurements", String.valueOf(d));
                            }


                            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                            transformableNode.setParent(anchorNode);
                            transformableNode.setRenderable(cubeRenderable);
                            transformableNode.select();

                            Vector3 point1, point2;
                            point1 = lastAnchorNode.getWorldPosition();
                            point2 = anchorNode.getWorldPosition();

                            final Vector3 difference = Vector3.subtract(point1, point2);
                            final Vector3 directionFromTopToBottom = difference.normalized();
                            final Quaternion rotationFromAToB =
                                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

                            AnchorNode finalAnchorNode = anchorNode;
//                            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
//                                    .thenAccept(
//                                            material -> {
//                                                ModelRenderable model = ShapeFactory.makeCube(
//                                                        new Vector3(.01f, .01f, difference.length()),
//                                                        Vector3.zero(), material);
//                                                Node node = new Node();
//                                                node.setParent(finalAnchorNode);
//                                                node.setRenderable(model);
//                                                node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
//                                                node.setWorldRotation(rotationFromAToB);
//                                            }
//                                    );
                            lastAnchorNode.getAnchor().detach();
                            lastAnchorNode = anchorNode;
                        }
                    }

                    if (btnTestHitClicked) {
                        int val = motionEvent.getActionMasked();
                        float axisVal = motionEvent.getAxisValue(MotionEvent.AXIS_X, motionEvent.getPointerId(motionEvent.getPointerCount() - 1));
                        Log.e("Values in MyAction:", String.valueOf(val) + String.valueOf(axisVal));

                        ArrayList<Point> points = new ArrayList<>();
                        points.add(new Point(480/2, 640/2));

                        showCornerAnchor(points);
//                            Toast.makeText(getApplicationContext(), "lastAnchorNode == null: " + String.valueOf(lastAnchorNode == null), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    };

    void writeFile(String name, String msg) {

        try {
            if (pPRINT == null) {
                String fileName = null;
                FileOutputStream outStream = null;
                //System album catalog
                String filePath = Environment.getExternalStorageDirectory()
                        + File.separator + Environment.DIRECTORY_DCIM
                        + File.separator + "Out_stream" + File.separator;


                // Declare file objects
                File file = null;
                // Declare output stream
                //            FileOutputStream outStream = null;


                // If there is a Target file, get the file object directly, otherwise create a file with filename as the name
                file = new File(filePath, name + ".txt");

                // Get file relative path
                fileName = file.toString();
                // Get the output stream, if there is content in the file, append the content
                outStream = new FileOutputStream(fileName);
                if (null != outStream) {
                    pPRINT = new PrintStream(outStream);
                    pPRINT.println(msg + "\n");
                }
            }else {
                pPRINT.printf(msg + "\n");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }finally {
//            try {
//                if (outStream != null) {
//                    outStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    private void showCornerAnchor(ArrayList<Point> points) {
        AnchorNode anchorNode = new AnchorNode();
        Anchor anchor = null;
        List<HitResult> hitResults = null;

        float phone_width, phone_height;
        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point outSize = new android.graphics.Point();
        display.getSize(outSize);
        phone_width = outSize.x;
        phone_height = outSize.y;
        int pic_width = 480;
        int pic_height = 640;

        for (Point point : points) {
            try {
                Frame frame = arFragment.getArSceneView().getArFrame();
                hitResults = frame.hitTest((float) point.x * phone_width / pic_width, (float) point.y * phone_height / pic_height + 100);

                for (HitResult hitResult1 : hitResults) {
                    anchor = hitResult1.createAnchor();
                    anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(cubeRenderable);
                    transformableNode.select();
                    lastAnchorNode = anchorNode;
                }

            } catch (Exception exception) {
                Toast.makeText(getApplicationContext(), "error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(), String.valueOf(hitResults.size()), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromView(){
        Bitmap bitmap = null;
        try {
            Image image = arFragment.getArSceneView().getArFrame().acquireCameraImage();
            byte[] bytes = UtilsBitmap.imageToByte(image);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
            bitmap = UtilsBitmap.rotateBitmap(bitmap, 90);
        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private void onClear() {
        List<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
            if (!(node instanceof Camera) && !(node instanceof Sun)) {
                node.setParent(null);
            }
        }
        arrayList1.clear();
        arrayList2.clear();
        lastAnchorNode = null;
        point1 = null;
        point2 = null;
        txtDistance.setText("");
    }

    private float getDistanceMeters(ArrayList<Float> arayList1, ArrayList<Float> arrayList2) {

        float distanceX = arayList1.get(0) - arrayList2.get(0);
        float distanceY = arayList1.get(1) - arrayList2.get(1);
        float distanceZ = arayList1.get(2) - arrayList2.get(2);
        return (float) Math.sqrt(distanceX * distanceX +
                distanceY * distanceY +
                distanceZ * distanceZ);
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Node node = hitTestResult.getNode();
        Box box = (Box) node.getRenderable().getCollisionShape();
        assert box != null;
        Vector3 renderableSize = box.getSize();
        Vector3 transformableNodeScale = node.getWorldScale();
        Vector3 finalSize =
                new Vector3(
                        renderableSize.x * transformableNodeScale.x,
                        renderableSize.y * transformableNodeScale.y,
                        renderableSize.z * transformableNodeScale.z);
        txtDistance.setText("Height: " + String.valueOf(finalSize.y));
        Log.e("FinalSize: ", String.valueOf(finalSize.x + " " + finalSize.y + " " + finalSize.z));
        //Toast.makeText(this, "Final Size is " + String.valueOf(finalSize.x + " " + finalSize.y + " " + finalSize.z), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
//        Collection<Anchor> updatedAnchors = frame.getUpdatedAnchors();
//        for (Anchor anchor : updatedAnchors) {
//            Handle updated anchors...
//        }
    }

    private void saveBmp2Gallery(Bitmap bmp, String picName) {

        String fileName = null;
        //System album catalog
        String galleryPath= Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                +File.separator+"Camera"+File.separator;


        // Declare file objects
        File file = null;
        // Declare output stream
        FileOutputStream outStream = null;

        try {
            // If there is a Target file, get the file object directly, otherwise create a file with filename as the name
            file = new File(galleryPath, picName+ ".jpg");

            // Get file relative path
            fileName = file.toString();
            // Get the output stream, if there is content in the file, append the content
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.PNG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        }finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaStore.Images.Media.insertImage(getContentResolver(), bmp, fileName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);

        Toast.makeText(this, "Finish saving！", Toast.LENGTH_SHORT).show();
    }

}
