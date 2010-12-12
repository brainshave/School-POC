package poc;

import org.eclipse.swt.graphics.ImageData;

public class UnsharpMask {
    
    /** First index in mask is number of column, second is number of row; */
    public static final void filter
	(ImageData data_orig, ImageData data_gauss, ImageData data_out,
	 double amount)
    {
	final byte[] orig = data_orig.data;
	final byte[] gauss = data_gauss.data;
	final byte[] out = data_out.data;

	final int n = orig.length;
	final int iamount = (int) (amount * 256);

	int pixel_orig = 0;
	int pixel_gauss = 0;
	for (int i = 0; i < n; ++i) {
	    pixel_orig = orig[i];
	    pixel_gauss = gauss[i];
	    if (pixel_orig < 0) pixel_orig += 256;
	    if (pixel_gauss < 0) pixel_gauss += 256;
	    out[i] = ByteWorker.toByte(pixel_orig + ((iamount * (pixel_orig - pixel_gauss)) >> 8));
	}
    }
}