package poc;

public class ReadStream {
    private final int width, height, size;
    int row = 0, col = 0;
    public final int[] output;

    public ReadStream(int width, int height) {
	this.width = width;
	this.height = height;
	this.size = width * height;
	this.output = new int[size];
    }

    public final boolean putBlock(int[] block) {
	int outputPtr = row * width + col;
	if (outputPtr >= size) {
	    return false;
	}

	int blockPtr = 0;
	int colEnd = row + 8;
	for(int j = row; j < colEnd && j < height; ++j) {
	    int rowEnd = col + 8;
	    int i = col;
	    for (; i < rowEnd && i < width; ++i) {
		output[outputPtr++] = block[blockPtr++];
	    }
	    outputPtr += width - (i - col);
	    blockPtr += rowEnd - i;
	}
	
	col += 8;
	if (col >= width) {
	    col = 0;
	    row += 8;
	}

	outputPtr = row * width + col;
	if (outputPtr >= size) {
	    return false;
	}
	return true;
    }
}