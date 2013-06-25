package friedm.AndroidBot;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class MainActivity extends Activity
        implements CameraView.CameraReadyCallback, OverlayView.UpdateDoneCallback {


    boolean inProcessing = false;
    final int maxVideoNumber = 3;
    VideoFrame[] videoFrames = new VideoFrame[maxVideoNumber];
    byte[] preFrame = new byte[1024 * 1024 * 8];

    TeaServer webServer = null;
    private CameraView cameraView_;
    private OverlayView overlayView_;
    private Button btnExit;
    private TextView tvMessage1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new BluetoothController();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        btnExit = (Button) findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(exitAction);
        tvMessage1 = (TextView) findViewById(R.id.tv_message1);

        for (int i = 0; i < maxVideoNumber; i++) {
            videoFrames[i] = new VideoFrame(1024 * 1024 * 2);
        }


        initCamera();

    }


    @Override
    public void onCameraReady() {
        if (initWebServer()) {
            int wid = cameraView_.Width();
            int hei = cameraView_.Height();
            cameraView_.StopPreview();
            cameraView_.setupCamera(wid, hei, previewCb_);
            cameraView_.StartPreview();
        }
    }

    @Override
    public void onUpdateDone() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        inProcessing = true;
        if (webServer != null)
            webServer.stop();
        cameraView_.StopPreview();

        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void initCamera() {
        SurfaceView cameraSurface = (SurfaceView) findViewById(R.id.surface_camera);

        cameraView_ = new CameraView(cameraSurface);
        cameraView_.setCameraReadyCallback(this);

        overlayView_ = (OverlayView) findViewById(R.id.surface_overlay);
        overlayView_.setUpdateDoneCallback(this);
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    //if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress() ) {
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.d(TAG, ex.toString());
        }
        return null;
    }

    private void addCGI(String link, TeaServer.CommonGatewayInterface tscgi) {
        webServer.registerCGI(link, tscgi);
    }

    private boolean initWebServer() {
        String ipAddr = getLocalIpAddress();
        if (ipAddr != null) {
            try {
                webServer = new TeaServer(8080, this);
                webServer.registerCGI("/cgi/query", doQuery);
                webServer.registerCGI("/cgi/setup", doSetup);
                webServer.registerCGI("/stream/live.jpg", doCapture);
                webServer.registerCGI("/cgi/toggleCam", stopCamera);
            } catch (IOException e) {
                webServer = null;
            }
        }
        if (webServer != null) {
            return true;
        } else {
            tvMessage1.setText(getString(R.string.msg_error));
            return false;
        }

    }

    private OnClickListener exitAction = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onPause();
        }
    };

    private PreviewCallback previewCb_ = new PreviewCallback() {
        public void onPreviewFrame(byte[] frame, Camera c) {
            if (!inProcessing) {
                inProcessing = true;

                int picWidth = cameraView_.Width();
                int picHeight = cameraView_.Height();
                ByteBuffer bbuffer = ByteBuffer.wrap(frame);
                bbuffer.get(preFrame, 0, picWidth * picHeight + picWidth * picHeight / 2);

                inProcessing = false;
            }
        }
    };

    private TeaServer.CommonGatewayInterface stopCamera = new TeaServer.CommonGatewayInterface() {
        @Override
        public String run(Properties parms) {
            String toggle = parms.getProperty("toggle");
            //Log.d("!!!!",toggle);
            if (toggle.equals("stop")) {
                //Log.d("!!!!","stopping");
                cameraView_.StopPreview();
            } else {
                //Log.d("!!!!", "starting");
                int wid = cameraView_.Width();
                int hei = cameraView_.Height();
                cameraView_.setupCamera(wid, hei, previewCb_);
                cameraView_.StartPreview();
            }

            return "OK";
        }

        @Override
        public InputStream streaming(Properties parms) {
            return null;
        }
    };

    private TeaServer.CommonGatewayInterface doQuery = new TeaServer.CommonGatewayInterface() {
        @Override
        public String run(Properties parms) {
            String ret = "";
            List<Camera.Size> supportSize = cameraView_.getSupportedPreviewSize();
            ret = ret + "" + cameraView_.Width() + "x" + cameraView_.Height() + "|";
            for (int i = 0; i < supportSize.size() - 1; i++) {
                ret = ret + "" + supportSize.get(i).width + "x" + supportSize.get(i).height + "|";
            }
            int i = supportSize.size() - 1;
            ret = ret + "" + supportSize.get(i).width + "x" + supportSize.get(i).height;
            return ret;
        }

        @Override
        public InputStream streaming(Properties parms) {
            return null;
        }
    };

    private TeaServer.CommonGatewayInterface doSetup = new TeaServer.CommonGatewayInterface() {
        @Override
        public String run(Properties parms) {
            int wid = Integer.parseInt(parms.getProperty("wid"));
            int hei = Integer.parseInt(parms.getProperty("hei"));

            cameraView_.StopPreview();
            cameraView_.setupCamera(wid, hei, previewCb_);
            cameraView_.StartPreview();
            return "OK";
        }

        @Override
        public InputStream streaming(Properties parms) {
            return null;
        }
    };


    private TeaServer.CommonGatewayInterface doCapture = new TeaServer.CommonGatewayInterface() {
        @Override
        public String run(Properties parms) {
            return null;
        }

        @Override
        public InputStream streaming(Properties parms) {
            VideoFrame targetFrame = null;
            for (int i = 0; i < maxVideoNumber; i++) {
                if (videoFrames[i].acquire()) {
                    targetFrame = videoFrames[i];
                    break;
                }
            }
            // return 503 internal error
            if (targetFrame == null) {
                //Log.d("TEAONLY", "No free videoFrame found!");
                return null;
            }

            // compress yuv to jpeg
            int picWidth = cameraView_.Width();
            int picHeight = cameraView_.Height();
            YuvImage newImage = new YuvImage(preFrame, ImageFormat.NV21, picWidth, picHeight, null);
            targetFrame.reset();
            boolean ret;
            inProcessing = true;
            try {
                ret = newImage.compressToJpeg(new Rect(0, 0, picWidth, picHeight), 30, targetFrame);
            } catch (Exception ex) {
                ret = false;
            }
            inProcessing = false;

            // compress success, return ok
            if (ret) {
                parms.setProperty("mime", "image/jpeg");

                return targetFrame.getInputStream();
            }
            // send 503 error
            targetFrame.release();

            return null;
        }
    };
}