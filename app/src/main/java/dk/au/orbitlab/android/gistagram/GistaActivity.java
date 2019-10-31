package dk.au.orbitlab.android.gistagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class GistaActivity extends AppCompatActivity implements ImageUploadListener {

    public final static int PERMISSION_CAMERA_REQUEST = 8201;
    public final static int REQUEST_CODE_TAKE_PICTURE = 8101;
    public static final String TAG = "GISTAGRAM";

    private RequestQueue queue;
    private String URL;
    private String BUCKET;
    private ImageUploadListener imageUploadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImageUploadListener(this);
    }

    protected void sendBitmap(String username, Bitmap bitmap){
        if(URL==null || URL.length()<1 || BUCKET==null || BUCKET.length()<1){
            Toast.makeText(this, "Please set up the server for Gistagram with url and bucket", Toast.LENGTH_SHORT).show();
            return;
        }

        if(queue==null){
            queue = Volley.newRequestQueue(this);
        }
        JSONObject jsonObject = null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
            jsonObject = new JSONObject();
            jsonObject.put("user", username);
            jsonObject.put("bucket", BUCKET);
            jsonObject.put("image", encodedImage);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e(TAG, jsonObject.toString());
                        queue.getCache().clear();
                        Toast.makeText(getApplication(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        if(imageUploadListener != null) {
                            imageUploadListener.uploadSucceeded();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Volley error", volleyError.toString());
                if(imageUploadListener != null) {
                    imageUploadListener.uploadFailed();
                }
            }
        });
        queue.add(jsonObjectRequest);
    }

    protected void takePicture() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
            return;
        } else {

        }

        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pictureIntent, REQUEST_CODE_TAKE_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_TAKE_PICTURE:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    onPictureTaken(imageBitmap);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(this, "Need permission to camera to take picture", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    protected void setupServer(String url, String bucket){
        this.URL = url;
        this.BUCKET = bucket;
    }

    protected void onPictureTaken(Bitmap pic){
    }

    public void setImageUploadListener(ImageUploadListener imageUploadListener) {
        this.imageUploadListener = imageUploadListener;
    }

    @Override
    public void uploadFailed() {
    }

    @Override
    public void uploadSucceeded() {
    }
}
