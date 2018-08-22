package jp.techacademy.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Cursor cursor; // 取得画像情報

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPlay = (Button) findViewById(R.id.buttonPlay);
        Button buttonNext = (Button) findViewById(R.id.buttonNext);
        Button buttonBack = (Button) findViewById(R.id.buttonBack);



        /* 初期起動で画像情報を取りに行く */

        // Android 6.0以降の許可確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission許可状態確認
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                Log.d("saki", "6以降許可されている");
                getContentInfo();
            } else {
                // 許可されていないので許可ダイアログを表示
                Log.d("saki", "許可されていないのでダイアログ表示");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

            }
            // Android 5系以下の場合　
        } else {
            Log.d("saki", "5系以下");
            ///////////////////////// 許可がない場合の条件分岐 //////////////////////////////////
            getContentInfo();
        }


        // 進むボタン
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.moveToNext()) {
                    // 次の画像があれば表示する
                    dispImage();
                }else{
                    // 次の画像がなければ、最初に戻る

                    if(cursor.moveToFirst()){
                        dispImage();
                        Log.d("saki","次の画像がないので最初に戻る");
                    }
                }
            }
        });

        /*
        @Override
        protected void onStart(){
            super.onStart();
            checkStoragePermission();
            getContentInfo();
        }*/

        // 戻るボタン
        buttonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(cursor.moveToPrevious()){
                    // 前の画像があれば表示する
                    dispImage();
                }else{
                    // 前の画像がなければ最後に戻る
                    if(cursor.moveToLast()){
                        dispImage();
                        Log.d("saki","前の画像がないので最後に戻る");
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("saki", "許可ダイアログで許可された");
                    getContentInfo();
                } else {
                    Log.d("saki", "許可ダイアログで許可されなかった");
                }
                break;
            default:
                break;
        }
    }

    // 画像情報を取得して画像を表示する
    private void getContentInfo() {

        Log.d("saki","Called:getContentInfo()");
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){
            dispImage();
        }else{
            // 端末に画像が存在しない場合
        }
    }

    private void dispImage(){
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
        Log.d("saki","URI"+ imageUri.toString());
    }
}
