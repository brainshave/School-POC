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
		if (even_row && (i - lineStart) % 2 == 1) {
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
	final int abRowWidth = (width + 1) / 2;
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
		if((i - lineStart) % 2 == 1) abptr++;

		rgb = labConv.toRGB(lab);
		data[i++] = ByteWorker.toByte(rgb[0]);
		data[i++] = ByteWorker.toByte(rgb[1]);
		data[i++] = ByteWorker.toByte(rgb[2]);
	    }
	}
	return imageData;
    }

    public static int[] testTransfering(BlockStream in, ReadStream out, double[] matrix, double quality) {
	CosineTransform cosine = new CosineTransform();
	Quantification quant = new Quantification(matrix, quality);
	int i = 0;
	int max = Integer.MIN_VALUE;
	int min = Integer.MAX_VALUE;
	try {
	    for(int[] block = in.nextBlock(); block != null; block = in.nextBlock()) {
		++i;
		double[] cos = cosine.cosine(block);
		int[] quanted = quant.quantificate(cos);
		for (int g = 0; g < 64; g++) {
		    int a = quanted[g];
		    if (a > max) max = a;
		    if (a < min) min = a;
		    //if(a > 127) quanted[g] = 127;
		    //else if(a < -128) quanted[g] = -128;
		}
		double[] unquanted = quant.unquantificate(quanted);
		int[] uncos = cosine.uncosine(unquanted);
		if(i == 3479) {
		    for(int g = 0; g < 64; ++g) {
			System.out.format("%10d %10f %10d %10f %10d\n",
					  block[g], cos[g], quanted[g],
					  unquanted[g], uncos[g]);
		    }
		}
		out.putBlock(uncos);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    System.out.println("Blokow: " + i);
	    System.out.println("Min: " + min + " Max: " + max);
	}
	return out.output;
    }
    
    public static void main(String[] args) {
	ImageData data = new ImageData(args[0]);
	int[][] lab = imageToLab(data);

	int abwidth = (data.width + 1) / 2;
	int abheight = (data.height + 1) / 2;
	BlockStream Los = new BlockStream(data.width, data.height, lab[0]);
	BlockStream aos = new BlockStream(abwidth, abheight, lab[1]);
	BlockStream bos = new BlockStream(abwidth, abheight, lab[2]);
	ReadStream Lis = new ReadStream(data.width, data.height);
	ReadStream ais = new ReadStream(abwidth, abheight);
	ReadStream bis = new ReadStream(abwidth, abheight);

	double quality = Integer.parseInt(args[1]);
	
	ImageData data2 = labToImage(
       	     testTransfering(Los, Lis, Quantification.MATRIX_LUMINANCE, quality),
	     testTransfering(aos, ais, Quantification.MATRIX_AB, quality),
	     testTransfering(bos, bis, Quantification.MATRIX_AB, quality),
	    data.width, data.height);
	ImageLoader loader = new ImageLoader();
	loader.data = new ImageData[] {data2};
	loader.save(args[0] + ".png", SWT.IMAGE_PNG);

	int[] testblock = {
	    52, 55, 61, 66, 70, 61, 64, 73,
	    63, 59, 55, 90, 109, 85, 69, 72,
	    62, 59, 68, 113, 144, 104, 66, 73,
	    63, 58, 71, 122, 154, 106, 70, 69,
	    67, 61, 68, 104, 126, 88, 68, 70,
	    79, 65, 60, 70, 77, 68, 58, 75,
	    85, 71, 64, 59, 55, 61, 65, 83,
	    87, 79, 69, 68, 65, 76, 78, 94
	};
	
	CosineTransform ct = new CosineTransform();
	double[] cto = ct.cosine(testblock);
	visualize(cto);
	
	Quantification quant = new Quantification(Quantification.MATRIX_LUMINANCE, 50);
	int[] quanted = quant.quantificate(cto);
	visualize(quanted);

	double [] unqua = quant.unquantificate(quanted);
	visualize(unqua);
	
	int[] back = ct.uncosine(unqua);
	visualize(back);
    }
    
    final static void visualize(int[] table) {	
	for(int r = 0; r < 8; ++r) {
	    for(int c = 0; c < 8; ++c) {
		System.out.format("%6d ", table[r*8 + c]);
	    }
	    System.out.println();
	}
	System.out.println();
    }
    
    final static void visualize(double[] table) {	
	for(int r = 0; r < 8; ++r) {
	    for(int c = 0; c < 8; ++c) {
		System.out.format("%8.2f ", table[r*8 + c]);
	    }
	    System.out.println();
	}
	System.out.println();
    }
}
	