package jp.techacademy.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Cursor cursor; // 取得画像情報
    Timer mTimer;


    Handler mHandler = new Handler();
    Button buttonPlay;
    Button buttonNext;
    Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonBack = (Button) findViewById(R.id.buttonBack);


        // 初期起動で画像情報を取りに行く
        checkStoragePermission();

        // 再生/停止ボタン
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // mTimreがnull:停止状態
                if (mTimer == null) {
                    mTimer = new Timer();
                    buttonNext.setEnabled(false);
                    buttonBack.setEnabled(false);
                    buttonPlay.setText("停止");

                    // スライドショーを開始する（2秒ごとに画像を更新）
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {


                                    if (cursor.moveToNext()) {
                                        // 次の画像がある
                                        dispImage();
                                    } else {
                                        // 次の画像がない
                                        if (cursor.moveToFirst()) {
                                            dispImage();
                                        }
                                    }
                                    Log.d("saki", "スライドショーを実行中");
                                }
                            });
                        }
                    }, 2000, 2000);
                } else {
                    // スライドショーを停止する

                    mTimer.cancel();
                    mTimer = null;
                    buttonNext.setEnabled(true);
                    buttonBack.setEnabled(true);
                    buttonPlay.setText("再生");
                    
                    Log.d("saki", "スライドショー停止");

                }
            }
        });

        // 進むボタン
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToNext()) {
                    // 次の画像があれば表示する
                    dispImage();
                } else {
                    // 次の画像がなければ、最初に戻る

                    if (cursor.moveToFirst()) {
                        dispImage();
                        Log.d("saki", "次の画像がないので最初に戻る");
                    }
                }
            }
        });


        // 戻るボタン
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToPrevious()) {
                    // 前の画像があれば表示する
                    dispImage();
                } else {
                    // 前の画像がなければ最後に戻る
                    if (cursor.moveToLast()) {
                        dispImage();
                        Log.d("saki", "前の画像がないので最後に戻る");
                    }
                }
            }
        });
    }

    private void checkStoragePermission() {

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

            getContentInfo();

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("saki", "許可ダイアログで許可された");

                    getContentInfo();
                } else {

                    // 許可されなかったのでボタンを押下不可にしてポップアップ
                    Log.d("saki", "許可ダイアログで許可されなかった");

                    buttonBack.setEnabled(false);
                    buttonNext.setEnabled(false);
                    buttonPlay.setEnabled(false);

                    showAlertDialog();

                }
                break;
            default:
                break;
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Alert");
        adb.setMessage("画像へのアクセス許可がないため、画像を表示できませんでした。アプリを終了してください。");

        adb.setPositiveButton("了解",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog ad = adb.create();
        ad.show();

    }

    // 画像情報を取得して画像を表示する
    private void getContentInfo() {

        Log.d("saki", "Called:getContentInfo()");
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            dispImage();
        } else {
            // 端末に画像が存在しない場合
        }
    }

    private void dispImage() {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
        Log.d("saki", "URI" + imageUri.toString());
    }
}
