package ecf.jk;



public class Kac {

    public static native void snnPl(int a, double b, String num);

    public static native void nneCs(Object context);//1.传应用context.(在主进程里面初始化一次)

    //    @Keep
    public static native void nneCp(Object context);//1.传透明Activity对象(在透明页面onCreate调用).

    //    @Keep
    public static native void nneCz(int idex);
}
