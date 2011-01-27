package poc;

public class BlockStream {
    private final int width, height;
    private int[] buff = new int[64];
    private int[] input;
    private int row = 0, col = 0;

    public BlockStream(int width, int height, int[] input) {
	this.width = width;
	this.height = height;
	this.input = input;
    }

    public final int[] nextBlock() {
	int sourcePtr = row * width + col;
	if (sourcePtr >= input.length) {
	    return null;
	}
	int buffPtr = 0;
	
	int colEnd = row + 8;
	int j = row;
	int lastVal = 0;
	for (; j < colEnd && j < height; ++j) {
	    int rowEnd = col + 8;
	    int i = col;
	    for (; i < rowEnd && i < width; ++i) {
		buff[buffPtr++] = input[sourcePtr++];
	    }
	    lastVal = buff[buffPtr-1];
	    for (; i < rowEnd; ++i) {
		buff[buffPtr++] = lastVal;
	    }
	    sourcePtr += width - 8;
	}
	while(buffPtr < 64) buff[buffPtr++] = lastVal;
	
	col += 8;
	if (col >= width) {
	    col = 0;
	    row += 8;
	}
	    
	return buff;
    }
}