package poc;

import org.eclipse.swt.graphics.ImageData;

public class Convolution {
    
    /** First index in mask is number of column, second is number of row; */
    public static final void filter
	(ImageData data_in, ImageData data_out,
	 int[][] mask)
    {
	// dodać opcję by można było jedną wartością wypełnić całą macież (w gui).

	final byte[] in = data_in.data;
	final byte[] out = data_out.data;

	final int height = data_in.height;
	final int width = data_in.width;

	final int row_length = data_in.bytesPerLine;
	final int row_used_length = width * 3;
	final int n = in.length;

	int sum_mask = 0;
	for(int[] column: mask) {
	    for(int x: column) {
		sum_mask += x;
	    }
	}
	
	final int delta_col = mask[0].length / 2;
	final int delta_row = mask.length / 2;
		
	for (int row = 0; row < height; ++row) {
	    for (int color = 0; color < 3; ++color) {
		for (int col = 0; col < width; ++col) {
		    int s = 0;
		    int x = col - delta_col;
		    for (int[] mask_col : mask) {
			int y = row - delta_col;
			for (int f: mask_col) {
			    int pixel = 0;
			    int localx = x;
			    if (localx < 0) localx = -x;
			    if (localx >= width) localx = width *2 - x -2;
			    
			    int localy = y;
			    if (localy < 0) localy = -y;
			    if (localy >= height) localy = height *2 - y -2;
			    
			    pixel = in[localy * row_length + 3 * localx + color];
			    
			    if (pixel < 0) pixel += 256;
			    

			    s += f * pixel;
			    ++y;
			}
			++x;
		    }
		    out[row * row_length + 3 * col + color] = ByteWorker.toByte(s / sum_mask);
		}
	    }
	}
    }
}
