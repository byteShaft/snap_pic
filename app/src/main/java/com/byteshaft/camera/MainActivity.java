package com.byteshaft.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.util.List;
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
            imageButton.setImageBitmap(photo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            Session session = new Session();
            session.addOnJoinListener((session1, details) -> {
                CompletableFuture<List<Object>> res = session1.call(
                        "io.crossbar.detect_faces", new TypeReference<List<Object>>() {}, null,
                        byteArray);
                res.whenComplete((cords, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    } else {
                        System.out.println(cords);
                    }
                });
            });
            Client client = new Client(session, "ws://192.168.1.9:8080/ws", "realm1");
            client.connect().whenComplete((exitInfo, throwable) -> System.out.println(exitInfo));
        }
    }
}
