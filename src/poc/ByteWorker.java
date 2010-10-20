package poc;

public class ByteWorker {
    public static byte[] work(byte[] orig, byte[] mod, byte[] mapping) {
	int n = orig.length;
	for (int i = 0; i < n; ++i) {
	    int color = orig[i];
	    if (color < 0) {
		color += 256;
	    }
	    mod[i] = mapping[color];
	}
	return mod;
    }
}
	