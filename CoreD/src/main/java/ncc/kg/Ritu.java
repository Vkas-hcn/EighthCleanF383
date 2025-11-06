package ncc.kg;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import ecf.jk.Kac;



public class Ritu extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView webView, int i10) {
        super.onProgressChanged(webView, i10);
        Log.e("TAG", "Ritu-onProgressChanged-1");
        if (i10 == 100) {
            Log.e("TAG", "Ritu-onProgressChanged-2");
            Kac.nneCz(i10);
        }

    }
}
