package com.example.androidfacedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RQS_LOADIMAGE = 1;
    private Button btnLoad, btnDetFace,btnCam;
    private ImageView imgView;
    private Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCam = (Button)findViewById(R.id.btnCam);

        btnLoad = (Button)findViewById(R.id.btnLoad);
        btnDetFace = (Button)findViewById(R.id.btnDetectFace);
        imgView = (ImageView)findViewById(R.id.imgview);

        btnLoad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, RQS_LOADIMAGE);
            }
        });

        btnDetFace.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(myBitmap == null){
                    Toast.makeText(MainActivity.this,
                            "myBitmap == null",
                            Toast.LENGTH_LONG).show();
                }else{
                    detectFace();

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_LOADIMAGE
                && resultCode == RESULT_OK){

            if(myBitmap != null){
                myBitmap.recycle();
            }

            try {
                InputStream inputStream =
                        getContentResolver().openInputStream(data.getData());
                myBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                imgView.setImageBitmap(myBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    reference:
    https://search-codelabs.appspot.com/codelabs/face-detection
     */
    private void detectFace(){

        //Create a Paint object for drawing with
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.GREEN);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Paint landmarksPaint = new Paint();
        landmarksPaint.setStrokeWidth(10);
        landmarksPaint.setColor(Color.RED);
        landmarksPaint.setStyle(Paint.Style.STROKE);

        Paint smilingPaint = new Paint();
        smilingPaint.setStrokeWidth(4);
        smilingPaint.setColor(Color.YELLOW);
        smilingPaint.setStyle(Paint.Style.STROKE);

        boolean somebodySmiling = false;

        //Create a Canvas object for drawing on
        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);

        //Detect the Faces

        //!!!
        //Cannot resolve method setTrackingEnabled(boolean)
        //FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).build();
        //faceDetector.setTrackingEnabled(false);

        FaceDetector faceDetector =
                new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .build();

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        //Draw Rectangles on the Faces
        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

            //get Landmarks for the first face
            List<Landmark> landmarks = thisFace.getLandmarks();
            for(int l=0; l<landmarks.size(); l++){
                PointF pos = landmarks.get(l).getPosition();
                tempCanvas.drawPoint(pos.x, pos.y, landmarksPaint);
            }

            //check if this face is Smiling
            final float smilingAcceptProbability = 0.5f;
            float smilingProbability = thisFace.getIsSmilingProbability();
            if(smilingProbability > smilingAcceptProbability){
                tempCanvas.drawOval(new RectF(x1, y1, x2, y2), smilingPaint);
                somebodySmiling = true;
            }
        }

        imgView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        if(somebodySmiling){
            Toast.makeText(MainActivity.this,
                    "Done - Happy",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this,
                    "Done - Unhappy",
                    Toast.LENGTH_LONG).show();
        }

    }
    public void startcamera(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
        startActivityForResult(intent, RQS_LOADIMAGE);
      //  detectFace();
    }
    }