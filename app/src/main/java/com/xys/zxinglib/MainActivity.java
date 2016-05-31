package com.xys.zxinglib;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import java.net.URLEncoder;

public class MainActivity extends Activity {

    private TextView resultTextView;
    private ImageView qrImgImageView;
    private boolean shouldSend = false;
    private String url = "http://cq01-rdqa-dev030.cq01.baidu.com:8787/";
//    private String url = "http://192.168.1.107:2121";

    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
        qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
        // 初始化剪切板
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);


        // 扫描二维码
        Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
        scanBarCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开扫描界面扫描条形码或二维码
                Intent openCameraIntent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });

        // 复制
        Button copyText = (Button)this.findViewById(R.id.btn_copy_str);
        copyText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String copyStr = resultTextView.getText().toString();
                if (copyStr == "") {
                    Toast.makeText(MainActivity.this, "内容为空", Toast.LENGTH_SHORT).show();
                } else {
                    ClipData clipData = ClipData.newPlainText("text", copyStr);
                    mClipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 粘贴
        Button pasteBt = (Button)this.findViewById(R.id.btn_paste_str);
        pasteBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = mClipboardManager.getPrimaryClip();
                String pasteStr = clipData.getItemAt(0).getText().toString();
                if (pasteStr == "") {
                    Toast.makeText(MainActivity.this, "剪切板不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    resultTextView.setText(pasteStr);
                }
            }
        });

        // 清空
        Button clearBt = (Button)this.findViewById(R.id.btn_clear_str);
        clearBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resultTextView.setText("");
            }
        });

        // 发送到浏览器
        Button sendBrower = (Button) this.findViewById(R.id.btn_send_browser);
        sendBrower.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = mClipboardManager.getPrimaryClip();
                String pasteStr = clipData.getItemAt(0).getText().toString();
                AsyncHttpClient client = new AsyncHttpClient();
                try {
                    RequestParams params = new RequestParams();
                    params.put("type", "publish");
                    params.put("content", android.net.Uri.encode(pasteStr));
                    client.get(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                            Toast.makeText(getApplicationContext(), "广播消息成功", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "广播消息失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // 扫码发送
        Button sendUid = (Button) this.findViewById(R.id.btn_send_uid);
        sendUid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开扫描界面扫描条形码或二维码
                shouldSend = true;
                Intent openCameraIntent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });


        // 生成二维码
        Button generateQRCodeButton = (Button) this.findViewById(R.id.btn_add_qrcode);
        generateQRCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentString = resultTextView.getText().toString();
                if (!contentString.equals("")) {
                    Bitmap qrCodeBitmap = EncodingUtils.createQRCode(contentString, 600, 600, null);
                    qrImgImageView.setImageBitmap(qrCodeBitmap);
                } else {
                    Toast.makeText(MainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");

            if (shouldSend) {
                shouldSend = false;
                ClipData clipData = mClipboardManager.getPrimaryClip();
                String pasteStr = clipData.getItemAt(0).getText().toString();
                AsyncHttpClient client = new AsyncHttpClient();
                try {
                    RequestParams params = new RequestParams();
                    params.put("to", scanResult);
                    params.put("type", "publish");
                    params.put("content", android.net.Uri.encode(pasteStr));
                    client.get(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                            Toast.makeText(getApplicationContext(), "扫描发送成功", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "扫描发送失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                resultTextView.setText(scanResult);
            }

        }
    }
}