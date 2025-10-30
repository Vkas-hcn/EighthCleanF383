package azshow;


import android.os.Handler;
import android.os.Message;

import azshow.sl.lo;



public class NcH extends Handler {
    @Override
    public void handleMessage(Message message) {
        lo.lod(message.what);
    }
}
