package poc;

public class ByteWorker {
    public static byte[] work(byte[] orig, byte[] mod, byte[] rmap, byte[] gmap, byte[] bmap) {
	int n = orig.length;
	for (int i = 0; i < n; ++i) {
	    int color = orig[i];
	    if (color < 0) {
		color += 256;
	    }
	    mod[i] = rmap[color];

	    ++i;
	    
	    color = orig[i];
	    if (color < 0) {
		color += 256;
	    }
	    mod[i] = gmap[color];

	    ++i;

	    color = orig[i];
	    if (color < 0) {
		color += 256;
	    }
	    mod[i] = bmap[color];
	}
	return mod;
    }
}
	