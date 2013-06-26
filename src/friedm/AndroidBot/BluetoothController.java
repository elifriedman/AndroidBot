package friedm.AndroidBot;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by EliFriedman on 18/6/13.
 */
public class BluetoothController extends Handler implements TeaServer.CommonGatewayInterface {
    private BluetoothSerialService blueSerial;
    private boolean connected;

    public BluetoothController(BluetoothDevice btd) {
        blueSerial = new BluetoothSerialService(this, false);
        blueSerial.connect(btd);
        connected = false;
    }

    @Override
    public String run(Properties parms) {

        return null;
    }

    public void write(byte[] bytes) {
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
