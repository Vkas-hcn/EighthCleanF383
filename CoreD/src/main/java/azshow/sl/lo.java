package azshow.sl;



public class lo {

    public static native void loTx(int a, double b, String num);

    public static native void loh(Object context);//1.传应用context.(在主进程里面初始化一次)

    //    @Keep
    public static native void loc(Object context);//1.传透明Activity对象(在透明页面onCreate调用).

    //    @Keep
    public static native void lod(int idex);
}
