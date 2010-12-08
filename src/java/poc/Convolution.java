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

	//int start = 0;
	//int end = row_used_length;
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
			    if (x >= 0 && x < width && y >= 0 && y < height) {
				pixel = in[y * row_length + 3 * x + color];
				if (pixel < 0) pixel += 256;
			    }

			    s += f * pixel;
			    ++y;
			}
			++x;
		    }
		    out[row * row_length + 3 * x + color] = ByteWorker.toByte(s / sum_mask);
		}
	    }
	}
    }
}
		
// final int last_i = mask.length/2;
// final int last_j = mask[0].length/2;
// final int i_start = -last_i;
// final int j_start = -last_j;

// for (int row = 0; row < height; ++row) {
//     for (int col = 0; col < width_used_length; ++col) {
// 	int s = 0;
// 	for (int i = i_start; i < last_i; ++i) {
// 	    int x = col + i * 3;
// 	    if (x < 0) x = -x;
// 	    else if (x >= width) x = 2 * width - i - 2;

// 	    int[] mask_col = mask[i];
// 	    for (int j = j_start; j < last_j; ++j) {
// 		int y = row + j;
// 		if (y < 0) y = -y;
// 		else if (y >= height) y = 2 * height - i - 2;
			
// 		int val = in[y * row_length + x];
// 		if (val < 0) val += 256;
// 		s += mask_col[j] * val;
// 	    }
// 	}
// 	out[row * row_length + 3 * col] = ByteWorker.toByte(s / sum_mask);
//     }
// }
	  