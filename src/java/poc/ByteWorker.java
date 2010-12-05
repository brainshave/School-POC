package poc;

import java.util.Arrays;
import org.eclipse.swt.graphics.ImageData;

public class ByteWorker {

    public static final boolean isBGR(ImageData data) {
	return data.palette.redMask == 0xff;
    }

    public static final byte toByte(int i) {
	if (i > 255) return -1;
	if (i > 127) return (byte)(i - 256);
	if (i < 0)   return 0;
	return (byte)i;
    }

    public static final void applyMaps
	(ImageData orig, ImageData mod,
	 byte[] rmap, byte[] gmap, byte[] bmap)
    {
	
	if(isBGR(orig)) {
	    byte[] tmp = rmap;
	    rmap = bmap;
	    bmap = tmp;
	}

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
	final int width = orig.width * 3;
	
	for (int line_start = 0; line_start < n; line_start += orig_line_width) {
	    int pixels_end = line_start + width;
	    for (int i = line_start; i < pixels_end; ) {
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
		
		++i;
	    }
	}
    }

    public static final void plotMaps
	(ImageData plotData,
	 byte[] rmap, byte[] gmap, byte[] bmap)
    {
	byte[] plot = plotData.data;
	int n = plot.length;

	// clearing plot
	Arrays.fill(plot, (byte) 0);
	
	byte[][] maps = {rmap, gmap, bmap};

	if(isBGR(plotData)) {
	    maps[0] = bmap;
	    maps[2] = rmap;
	}

	for (int color = 0; color < 3; ++color) {
	    byte[] map = maps[color];
	    
	    int end = 255;
	    int start;

	    for (int i = 0; i < 256; ++i) {
		int val = map[i];
		if (val < 0) val += 256;
		start = 255 - val;
		       
		int end_point = end * 768 + i * 3 + color;
		int jump = 768;
		if (start > end) jump = -768;
		
		for (int j = start * 768 + i * 3 + color;
		     j <= end_point;
		     j += 768) {
	    	    plot[j] = -1;
		}
		end = start;
	    }
	}
    }


    public static final void clearArray(int[] array) {
	for (int i = 0; i < array.length; ++i) {
	    array[i] = 0;
	}
    }

    public static final void calcHistograms 
	(ImageData imageData,
	 int[] rhist, int[] ghist, int[] bhist, int[] rgbhist)
    {
	byte[] data = imageData.data;
	int n = data.length;
	
	int lineWidth = imageData.bytesPerLine;
	int pixelsPerLine = imageData.width * 3;

	clearArray(rhist);
	clearArray(ghist);
	clearArray(bhist);
	clearArray(rgbhist);
	
	boolean bgr = isBGR(imageData);
	int red, green, blue;
	for (int lineStart = 0; lineStart < n; lineStart += lineWidth) {
	    int lineEnd = lineStart + pixelsPerLine;
	    for (int i = lineStart; i < lineEnd; /* bad style ! */) {
		if (bgr) {
		    blue  = data[i++];
		    green = data[i++];
		    red   = data[i++];
		} else {
		    red   = data[i++];
		    green = data[i++];
		    blue  = data[i++];
		} 
		
		if (red   < 0) red   += 256;
		if (green < 0) green += 256;
		if (blue  < 0) blue  += 256;
		
		int rgb = (3 * red + 6 * green + blue) / 10;

		rhist   [red]   ++;
		ghist   [green] ++;
		bhist   [blue]  ++;
		rgbhist [rgb]   ++;
	    }
	}
    }

    private static final int[] zeroes = new int[256];

    public static final void plotHistograms 
	(ImageData plotData, int inputSize,
	 int[] rhist, int[] ghist, int[] bhist, int[] rgbhist)
    {
	if (rhist   == null)  rhist   = zeroes;
	if (ghist   == null)  ghist   = zeroes;
	if (bhist   == null)  bhist   = zeroes;
	if (rgbhist == null)  rgbhist = zeroes;
	
	byte[] plot = plotData.data;
	int n =  plot.length;

	int lineWidth = plotData.bytesPerLine;
	int height = plotData.height;
	int pixelsPerLine = plotData.width * 3;
	
	//czyszczenie:
	Arrays.fill(plot, (byte) 0);
	
	int[][] hists = {rhist, ghist, bhist};

	if(isBGR(plotData)) {
	    hists[0] = bhist;
	    hists[2] = rhist;
	}

	for (int i = 0; i < 256; ++i) {
	    for (int color = 0; color < 3; ++color) {

		int val = (int) (( (double) hists[color][i] * height) / inputSize);
		
		int point = (height - val) * lineWidth + i * 3 + color;
		if (point < 0) point = i * 3 + color;
		    
		for (; point < n; point += lineWidth) {
		    plot[point] = 127;
		}
	    }
	    
	    // overlaying rgb layer
	    int rgbval = (rgbhist[i] * height) / inputSize;
	    
	    int point = (height - rgbval) * lineWidth + i * 3;
	    if (point < 0) point = i * 3;
	    for (; point < n; point += lineWidth) {
		for(int colorPoint = point; colorPoint < point + 3; ++colorPoint) {
		    plot[colorPoint] = plot[colorPoint] == 0 ? (byte) 64 : (byte) -1;
		}
	    }
	}
    }

    public static final void cmykCorrection
	(ImageData in, ImageData out,
	 int corrC, int corrM, int corrY, int corrK)
    {
	if (isBGR(in)) {
	    int tmp = corrC;
	    corrC = corrY;
	    corrY = tmp;
	}

	final int height = in.height;
	final int width = in.width;
	final byte[] in_data = in.data;
	final byte[] out_data = out.data;
	final int n = in_data.length;
	final int line_width = in.bytesPerLine;
	
	if(height != out.height || width != out.width
	   || n != out_data.length || line_width != out.bytesPerLine) {
	    System.err.println("Images don't match");
	}

	final int data_width =  3*width;
	final int padding = line_width - data_width;
	int i = 0;
	int r, g, b, c, m, y, k, kkk;
	for (int line_end = data_width; line_end < n; line_end += line_width) {
	    while (i < line_end) {
		r = in_data[i];
		g = in_data[i+1];
		b = in_data[i+2];

		if (r < 0) r += 256; // converting from bytes (-128..127)
		if (g < 0) g += 256;
		if (b < 0) b += 256;

		c = 255 - r;
		m = 255 - g;
		y = 255 - b;
		///k = r < g ? (r < b ? r : b) : (g < b ? g : b);
		k = c;
		if (m < k) k = m;
		if (y < k) k = y;
				
		kkk = 255 - k - corrK;
		if (kkk < 0) kkk = 0;
		
		// ew. optymalizacja: 2 wym LUT
		r -= ((corrC * kkk) >> 8) + corrK;
		g -= ((corrM * kkk) >> 8) + corrK;
		b -= ((corrY * kkk) >> 8) + corrK;

		out_data[i++] = toByte(r);
		out_data[i++] = toByte(g);
		out_data[i++] = toByte(b);
	    }
	    i += padding;
	}
    }
}
	