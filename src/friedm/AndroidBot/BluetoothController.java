package friedm.AndroidBot;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by EliFriedman on 18/6/13.
 */
public class BluetoothController extends Handler implements TeaServer.CommonGatewayInterface {
    private static final String TAG = BuildConfig.TAG + "BluetoothController";
    private static final boolean D = BuildConfig.DEBUG;

    private BluetoothSerialService blueSerial;
    private boolean connected;

    public BluetoothController(BluetoothDevice btd) {
        blueSerial = new BluetoothSerialService(this, false);
        blueSerial.connect(btd);
        connected = false;
    }

    @Override
    public String run(Properties parms) {
        //Control the left/right and forward/back movement of the robot.
        //Data works as one unsigned byte: 0bABXXXXXX
        //'A' represents the axis to control (1 = y)
        //'B' represents the direction (1 = up / right, 0 = down / left)
        //XXXXXX represents the speed.
        byte horiz = 0, vert = (byte) 0x80;
        int x = Byte.valueOf(parms.getProperty("x"));
        int y = Byte.valueOf(parms.getProperty("y"));

        if (x < 0) {
            horiz &= 0xBF; //Set 'B' = 0
        } else if (x > 0) {
            horiz |= 0x40; //Set 'B' = 1
        }
        //Get the bits 0b00XXXXXX. abs value it just in case it was negative
        horiz |= Math.abs(x) & 0x3F;

        if (y < 0) {
            vert &= 0xBF; //Set 'B' = 0
        } else if (y > 0) {
            vert |= 0x40; //Set 'B' = 1
        }
        //Get the bits 0b00XXXXXX. abs value it just in case it was negative
        vert |= Math.abs(y) & 0x3F;

        write(new byte[]{horiz, vert});
        if (D)
            Log.d(TAG, "(" + Integer.toBinaryString(horiz) + "," + Integer.toBinaryString(vert).substring(24, 32) + ")");
        return "OK";
    }

    public void write(byte[] bytes) {
        if (blueSerial.getState() == BluetoothSerialService.STATE_CONNECTED)
            blueSerial.write(bytes);
    }

    public void write(byte bite) {
        blueSerial.write(new byte[]{bite});
    }

    @Override
    public InputStream streaming(Properties parms) {
        return null;
    }

    /**
     * Called whenever BluetoothSerialService has a message to send.
     *
     * @param msg The message that BluetoothSerialService sends.
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BluetoothSerialService.STATE_CHANGE:

                break;
            case BluetoothSerialService.MESSAGE_READ:

                break;
            case BluetoothSerialService.MESSAGE_TOAST:

                break;
            case BluetoothSerialService.MESSAGE_WRITE:

                break;
        }

    }

}
