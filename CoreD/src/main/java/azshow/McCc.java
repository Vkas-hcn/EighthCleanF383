package azshow;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import azshow.sl.lo;



public class McCc extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView webView, int i10) {
        super.onProgressChanged(webView, i10);
        if (i10 == 100) {
            lo.lod(i10);
        }
    }
}
