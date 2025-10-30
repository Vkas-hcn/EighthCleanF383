package azshow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ggc.show.DataCc


class ACanR : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val eIntent = intent?.getParcelableExtra(DataCc.a) as Intent? //注意:广播接收key(改成你们自己提供的字段)
        if (eIntent != null) {
            try {
                context?.startActivity(eIntent)
            }catch (_:Exception){}
        }
    }
}