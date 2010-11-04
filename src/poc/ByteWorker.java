package poc;

import java.util.Arrays;
import org.eclipse.swt.graphics.*;

public class ByteWorker {
    public static final void applyMaps
	(ImageData orig, ImageData mod,
	 byte[] rmap, byte[] gmap, byte[] bmap)
    {
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
	
	for (int lineStart = 0; lineStart < n; lineStart += lineWidth) {
	    int lineEnd = lineStart + pixelsPerLine;
	    for (int i = lineStart; i < lineEnd; /* bad style ! */) {
		int red   = data[i++];
		int green = data[i++];
		int blue  = data[i++];
		
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
}
	