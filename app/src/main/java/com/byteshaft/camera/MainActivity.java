package com.byteshaft.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;

public class MainActivity extends AppCompatActivity {

    private ImageView imageButton;
    private static final int CAMERA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap altered = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            Canvas canvas = new Canvas(altered);
            Session session = new Session();
            session.addOnJoinListener((session1, details) -> {
                CompletableFuture<List<Map<String, Integer>>> res = session1.call(
                        "io.crossbar.detect_faces",
                        new TypeReference<List<Map<String, Integer>>>() {}, null, byteArray);
                res.whenComplete((cords, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    } else {
                        for (Map<String, Integer> co: cords) {
                            test(canvas, co);
                        }
                        imageButton.setImageBitmap(altered);
                    }
                });
            });
            Client client = new Client(session, "ws://192.168.1.8:8080/ws", "realm1");
            client.connect().whenComplete((exitInfo, throwable) -> System.out.println(exitInfo));
        }
    }

    private void test(Canvas c, Map<String, Integer> co) {
        Paint myPaint = new Paint();
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setColor(Color.rgb(255, 0, 0));
        myPaint.setStrokeWidth(1);
        c.drawRect(co.get("x"), co.get("y"), co.get("x") + co.get("w"), co.get("y") + co.get("h"), myPaint);
    }
}
