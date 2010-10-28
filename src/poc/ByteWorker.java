package poc;

import org.eclipse.swt.graphics.ImageData;

public class ByteWorker {
    public static void applyMaps(ImageData orig, ImageData mod,
				   byte[] rmap, byte[] gmap, byte[] bmap) {
	// int n = orig.length;
	final int orig_height = orig.height;
	final int orig_line_width = orig.bytesPerLine;

	final int mod_height = mod.height;
	final int mod_line_width = mod.bytesPerLine;

	if (orig_height != mod_height || orig_line_width != mod_line_width) {
	    System.err.println("Bad line widths and heights: " + orig_height +
			       " " + orig_line_width + " : " + mod_height + " "
			       + mod_line_width);
	    return;
	}
	
	final byte[] orig_data = orig.data;
	final byte[] mod_data = mod.data;
	
	
	final int n = orig_data.length;
	final int width = orig.width;
	
	for (int line_start = 0; line_start < n; line_start += orig_line_width) {
	    int pixels_end = line_start + 3 * width;
	    for (int i = line_start; i < pixels_end; i++) {
		int color = orig_data[i];
		if (color < 0) {
		    color += 256;
		}
		mod_data[i] = rmap[color];

		++i;
	    
		color = orig_data[i];
		if (color < 0) {
		    color += 256;
		}
		mod_data[i] = gmap[color];

		++i;

		color = orig_data[i];
		if (color < 0) {
		    color += 256;
		}
		mod_data[i] = bmap[color];
	    }
	}
	// }
	// return mod;
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
	