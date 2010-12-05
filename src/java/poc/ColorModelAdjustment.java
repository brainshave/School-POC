package poc;

import org.eclipse.swt.graphics.ImageData;

public class ColorModelAdjustment {
    public final void adjust
	(final ImageData in, final ImageData out,
	 final ColorModel model, final int[] adjustments)
    {
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
	int r, g, b;
	int[] color;
	final boolean bgr = ByteWorker.isBGR(in);
	for (int line_end = data_width; line_end < n; line_end += line_width) {
	    while (i < line_end) {
		if (bgr) {
		    b = in_data[i];
		    g = in_data[i+1];
		    r = in_data[i+2];
		} else {
		    r = in_data[i];
		    g = in_data[i+1];
		    b = in_data[i+2];
		}

		if (r < 0) r += 256; // converting from bytes (-128..127)
		if (g < 0) g += 256;
		if (b < 0) b += 256;

		color = model.adjust(r, g, b, adjustments);

		if (bgr) {
		    out_data[i++] = ByteWorker.toByte(color[2]);
		    out_data[i++] = ByteWorker.toByte(color[1]);
		    out_data[i++] = ByteWorker.toByte(color[0]);
		} else {
		    out_data[i++] = ByteWorker.toByte(color[0]);
		    out_data[i++] = ByteWorker.toByte(color[1]);
		    out_data[i++] = ByteWorker.toByte(color[2]);
		}
	    }
	    i += padding;
	}
    }
}
	