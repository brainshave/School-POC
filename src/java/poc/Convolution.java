package poc;

import org.eclipse.swt.graphics.ImageData;

interface ConvolveTask {
    void add(int x);
    int pop();
}

class NormalConvolution implements ConvolveTask {
    private int sum_mask;
    private int s = 0;
    public NormalConvolution(int[][] mask)
    {
	sum_mask = 0;
	for(int[] column: mask) {
	    for(int x: column) {
		sum_mask += x;
	    }
	}
	if (sum_mask == 0) {
	    sum_mask = 1;
	}
    }
    public void add(int x)
    {
	s += x;
    }
    
    public int pop()
    {
	int result = s / sum_mask;
	s = 0;
	return result;
    }
}

class MinimumConvolution implements ConvolveTask {
    private int min = Integer.MAX_VALUE;
    public void add(int x) {
	if (x < min) min = x;
    }
    public int pop() {
	int result = min;
	min = Integer.MAX_VALUE;
	return result;
    }
}

class MaximumConvolution implements ConvolveTask {
    private int max = Integer.MIN_VALUE;
    public void add(int x) {
	if (x > max) max = x;
    }
    public int pop() {
	int result = max;
	max = Integer.MIN_VALUE;
	return result;
    }
}

class MedianConvolution implements ConvolveTask {
    private int[] map = new int[256];
    private int count = 0;
    public void add(int x) {
	++count;
	if (x > 255) {
	    ++map[255];
	} else if (x < 0) {
	    ++map[0];
	} else {
	    ++map[x];
	}
    }
    public int pop() {
	int i = -1;
	int sum = 0;
	count >>= 1;
	while(sum < count) {
	    ++i;
	    sum += map[i];
	    map[i] = 0;
	}

	for (int k = i + 1; k < 256; ++k) {
	    map[k] = 0;
	}
	count = 0;

	return i;
    }
}

public class Convolution {

    public static final void filter
	(ImageData data_in, ImageData data_out,
	 int[][] mask)
    {
	filter(data_in, data_out, mask, new NormalConvolution(mask));
    }


    public static final void minimum
	(ImageData data_in, ImageData data_out,
	 int[][] mask)
    {
	filter(data_in, data_out, mask, new MinimumConvolution());
    }
    
    public static final void maximum
	(ImageData data_in, ImageData data_out,
	 int[][] mask)
    {
	filter(data_in, data_out, mask, new MaximumConvolution());
    }

    public static final void median
	(ImageData data_in, ImageData data_out,
	 int[][] mask)
    {
	filter(data_in, data_out, mask, new MedianConvolution());
    }
	
    
    /** First index in mask is number of column, second is number of row; */
    public static final void filter
	(ImageData data_in, ImageData data_out,
	 int[][] mask, ConvolveTask task)
    {
	// dodać opcję by można było jedną wartością wypełnić całą macież (w gui).

	final byte[] in = data_in.data;
	final byte[] out = data_out.data;

	final int height = data_in.height;
	final int width = data_in.width;

	final int row_length = data_in.bytesPerLine;
	final int row_used_length = width * 3;
	final int n = in.length;

		
	final int delta_col = mask[0].length / 2;
	final int delta_row = mask.length / 2;
		
	for (int row = 0; row < height; ++row) {
	    for (int color = 0; color < 3; ++color) {
		for (int col = 0; col < width; ++col) {
		    //int s = 0;
		    int x = col - delta_col;
		    for (int[] mask_col : mask) {
			int localx = x;
			if (localx < 0) localx = -x;
			if (localx >= width) localx = width *2 - x -2;

			int y = row - delta_col;
			for (int f: mask_col) {
			    int localy = y;
			    if (localy < 0) localy = -y;
			    if (localy >= height) localy = height *2 - y -2;
			    
			    int pixel = in[localy * row_length + 3 * localx + color];
			    
			    if (pixel < 0) pixel += 256;
			    
			    //s += f * pixel;
			    if (f != 0) task.add(f * pixel);
			    ++y;
			}
			++x;
		    }
		    out[row * row_length + 3 * col + color] = ByteWorker.toByte(task.pop());
		}
	    }
	}
    }
}
