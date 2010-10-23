package poc;

public class ByteWorker {
    public static byte[] applyMaps(byte[] orig, byte[] mod,
				   byte[] rmap, byte[] gmap, byte[] bmap) {
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

    public static byte[] plotMaps(byte[] plot,
				  byte[] rmap, byte[] gmap, byte[] bmap) {
	int n = plot.length;
	// clearing plot
	for (int i = 0; i < n; ++i) {
	    plot[i] = 0;
	}
	

	byte[][] maps = {rmap, gmap, bmap};

	for (int color = 0; color < 3; ++color) {
	    byte[] map = maps[color];
	    
	    int end = 255;
	    int start;

	    for (int i = 0; i < 256; ++i) {
		int val = map[i];
		if (val < 0) val += 256;
		start = 255 - val;

		int end_point = end * 768 + i * 3 + color;
		for(int j = start * 768 + i * 3 + color;
		    j <= end_point;
		    j += 768) {
	    	    plot[j] = -1;
		}
		end = start;
	    }
	}
	return plot;
    }
}
	