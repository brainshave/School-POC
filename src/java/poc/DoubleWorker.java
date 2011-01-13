package poc;

import java.nio.DoubleBuffer;
import org.eclipse.swt.graphics.ImageData;

public class DoubleWorker {
    public static final void fillComplexColor(int diff, ImageData from, DoubleBuffer to) {
	final byte[] data = from.data;
	final int pixelsPerLine = from.width * 3;
	to.rewind();
	for(int lineStart = 0; lineStart < data.length; lineStart += from.bytesPerLine) {
	    int iend = lineStart + pixelsPerLine;
	    for(int i = lineStart + diff; i < iend; i+=3) {
		int c = data[i];
		if (c < 0) c += 256;
		to.put(c);
		to.put(0.0); // cz. urojona
	    }
	}
    }
    
    public static final void renderColor(int diff, DoubleBuffer from, ImageData to) {
	final byte[] data = to.data;
	final int pixelsPerLine = to.width * 3;
	int pixel;
	final int cap = from.capacity();
	double r, i, max = 0;

	//for (int p = 0; p < cap; p+=2) {
	//  r = from.get(p);
	    //from.get(p+1);
	    //r = Math.log(1.0 + renderType.calc(r, i));
	    //r = Math.log(1.0 + Math.sqrt(r*r + i*i));
	    //if (r > max) max = r;
	    //from.put(p, r);
	//}

	//final double factor = 255.0/max;
	for (int lineStart = 0; lineStart < data.length; lineStart += to.bytesPerLine) {
	    int pend = lineStart + pixelsPerLine;
	    for(int p = lineStart; p < pend; ++p) {
		byte value = ByteWorker.toByte((int) (from.get()));
		from.get(); // unreal
		data[p] = value;
	    }
	}
	from.rewind();
    }

    public static final void fillComplex(ImageData from, DoubleBuffer to) {
	final byte[] data = from.data;
	final int pixelsPerLine = from.width * 3;
	to.rewind();
	for(int lineStart = 0; lineStart < data.length; lineStart += from.bytesPerLine) {
	    int iend = lineStart + pixelsPerLine;
	    for(int i = lineStart; i < iend; ++i) {
		int r = data[i++];
		int g = data[i++];
		int b = data[i];
		if (r < 0) r += 256;
		if (g < 0) g += 256;
		if (b < 0) b += 256;
		to.put(r + g + b);
		to.put(0.0); // cz. urojona
	    }
	}
    }

    public enum RenderType {
	R { double calc(double r, double i) { return Math.abs(r); }},
	    I { double calc(double r, double i) { return Math.abs(i); }},
		Mod { double calc(double r, double i) { return Math.sqrt(r*r + i*i); }},
		    atan { double calc(double r, double i) {
			    return Math.abs(Math.atan(i / r));}};
		    abstract double calc (double r, double i);
    };
    
    public static final void render(DoubleBuffer from, ImageData to, RenderType renderType) {
	final byte[] data = to.data;
	final int pixelsPerLine = to.width * 3;
	int pixel;
	final int cap = from.capacity();
	double r, i, max = 0;

	for (int p = 0; p < cap; p+=2) {
	    r = from.get(p);
	    i = from.get(p+1);
	    r = Math.log(1.0 + renderType.calc(r, i));
	    //r = Math.log(1.0 + Math.sqrt(r*r + i*i));
	    if (r > max) max = r;
	    from.put(p, r);
	}

	final double factor = 255.0/max;
	
	final int firstHorizHalfStart = pixelsPerLine/2;
	final int horizBorder = to.bytesPerLine * (to.height / 2);
	
	from.rewind();
	
	for (int lineStart = horizBorder; lineStart < data.length; lineStart += to.bytesPerLine) {
	    int pend = lineStart + pixelsPerLine;
	    for(int p = lineStart + firstHorizHalfStart; p < pend; ++p) {
		byte value = ByteWorker.toByte((int) (from.get() * factor));
		data[p++] = value;
		data[p++] = value;
		data[p] = value;
		from.get(); // unreal
	    }
	    pend = lineStart + firstHorizHalfStart;
	    for(int p = lineStart; p < pend; ++p) {
		byte value = ByteWorker.toByte((int) (from.get() * factor));
		data[p++] = value;
		data[p++] = value;
		data[p] = value;
		from.get(); // unreal
	    }
	}
	for (int lineStart = 0; lineStart < horizBorder;  lineStart += to.bytesPerLine) {
	    int pend = lineStart + pixelsPerLine;
	    for(int p = lineStart + firstHorizHalfStart; p < pend; ++p) {
		byte value = ByteWorker.toByte((int) (from.get() * factor));
		data[p++] = value;
		data[p++] = value;
		data[p] = value;
		from.get(); // unreal
	    }
	    pend = lineStart + firstHorizHalfStart;
	    for(int p = lineStart; p < pend; ++p) {
		byte value = ByteWorker.toByte((int) (from.get() * factor));
		data[p++] = value;
		data[p++] = value;
		data[p] = value;
		from.get(); // unreal
	    }
	}
    }
}