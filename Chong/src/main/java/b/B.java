package b;

import com.september.nine.chong.ttuser.GetJkUtils;

public class B {
    public static void b(Boolean canRetry, String name, String key1, String keyValue1) {
        GetJkUtils.INSTANCE.postPointFun(canRetry, name, key1, keyValue1);
    }
}
