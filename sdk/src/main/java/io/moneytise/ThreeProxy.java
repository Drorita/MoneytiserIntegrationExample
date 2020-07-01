package io.moneytise;

public class ThreeProxy {

	static {
		java.lang.System.loadLibrary("3proxy");
	}

	// 3proxy's main() function
	public static native int start(String[] cmd);

	// 3proxy's reload signal
	public static native void reload();

	// 3proxy's terminate signal
	public static native void stop();

}