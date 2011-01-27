package poc;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.SWT;

public class Compression {
    public static int[][] imageToLab (ImageData image) {
	boolean bgr = ByteWorker.isBGR(image);
	Lab labConv = new Lab();
	int pixelsPerLine = image.width * 3;
	int lineWidth = image.bytesPerLine;
	int height = image.height;

	int[] Ls = new int[image.width * height];
	int[] as = new int[((image.width + 1) * (height+1)) / 4];
	int[] bs = new int[as.length];
	
	int Lptr = 0;
	int abptr = 0;

	byte[] data = image.data;
	int end, r, g, b;
	int[] lab;
	boolean even_row = false;
	for (int lineStart = 0; lineStart < data.length; lineStart += lineWidth) {
	    even_row = !even_row;
	    end = lineStart + pixelsPerLine;
	    for (int i = lineStart; i < end; ) {
		if (bgr) {
		    b = data[i++];
		    g = data[i++];
		    r = data[i++];
		} else {
		    r = data[i++];
		    g = data[i++];
		    b = data[i++];
		}

		if (r < 0) r += 256;
		if (g < 0) g += 256;
		if (b < 0) b += 256;

		lab = labConv.fromRGB(r, g, b);
		Ls[Lptr++] = lab[0];
		if (even_row && Lptr % 2 == 0) {
		    as[abptr] = lab[1];
		    bs[abptr] = lab[2];
		    abptr++;
		}
	    }
	}
	int [][] ret = {Ls, as, bs};
	return ret;
    }

    public static ImageData labToImage(int[] Ls, int[] as, int[] bs, int width, int height) {
	ImageData imageData = new ImageData(width, height, 24,
					    new PaletteData(0xff0000, 0xff00, 0xff));
	byte[] data = imageData.data;
	int lineWidth = imageData.bytesPerLine;
	int pixelsPerLine = width * 3;

	int end;
	Lab labConv = new Lab();
	int[] lab = new int[3];
	int[] rgb;
	int Lptr = 0;
	int abptr = 0;
	final int abRowWidth = width / 2;
	int abptrStart = -abRowWidth;

	boolean evenRow = false;
	for(int lineStart = 0; lineStart < data.length; lineStart += lineWidth) {
	    end = lineStart + pixelsPerLine;
	    evenRow = !evenRow;
	    if (evenRow) abptrStart += abRowWidth;
	    abptr = abptrStart;
	    for (int i = lineStart; i < end; ) {
		lab[0] = Ls[Lptr++];
		lab[1] = as[abptr];
		lab[2] = bs[abptr];
		if(Lptr % 2 == 0) abptr++;

		rgb = labConv.toRGB(lab);
		data[i++] = ByteWorker.toByte(rgb[0]);
		data[i++] = ByteWorker.toByte(rgb[1]);
		data[i++] = ByteWorker.toByte(rgb[2]);
	    }
	}
	return imageData;
    }

    public static void main(String[] args) {
	ImageData data = new ImageData(args[0]);
	int[][] lab = imageToLab(data);
	ImageData data2 = labToImage(lab[0], lab[1], lab[2], data.width, data.height);
	ImageLoader loader = new ImageLoader();
	loader.data = new ImageData[] {data2};
	loader.save(args[0] + ".png", SWT.IMAGE_PNG);
    }
}
	