package ncc.kg;


import android.os.Handler;
import android.os.Message;

import ecf.jk.Kac;



public class SnnA extends Handler {
    @Override
    public void handleMessage(Message message) {
        Kac.nneCz(message.what);
    }
}
