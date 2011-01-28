package poc;

public class ZigZag {
    private int[] ibuff = new int[64];
    private byte[] bbuff = new byte[64];
    

    private final static int[] indexes = {
	0, 1, 5, 6, 14, 15, 27, 28,
	2, 4, 7, 13, 16, 26, 29, 42,
	3, 8, 12, 17, 25, 30, 41, 43,
	9, 11, 18, 24, 31, 40, 44, 53,
	10, 19, 23, 32, 39, 45, 52, 54,
	20, 22, 33, 38, 46, 51, 55, 60,
	21, 34, 37, 47, 50, 56, 59, 61,
	35, 36, 48, 49, 57, 58, 62, 63
    };

    public byte[] zig(int[] input) {
	for (int i = 0; i < 64; ++i) {
	    bbuff[i] = (byte) input[indexes[i]];
	}
	return bbuff;
    }
    public int[] zag(byte[] input) {
	for (int i = 0; i < 64; ++i) {
	    ibuff[indexes[i]] = input[i];
	}
	return ibuff;
    }

    public static void main(String[] asdf) {
	ZigZag zz = new ZigZag();
	int[] a = new int[64];
	for(int o = 0; o < 64; ++o) {
	    a[o] = o - 64;
	}
	Compression.visualize(a);
	byte[] z = zz.zig(a);
	Compression.visualize(z);
	int[] xa = zz.zag(z.clone());
	Compression.visualize(xa);
    }
}