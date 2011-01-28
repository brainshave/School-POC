package poc;

public class Quantification {
    public final static double[] MATRIX_LUMINANCE = {
	16, 11, 10, 16, 24, 40, 51, 61,
	12, 12, 14, 19, 26, 58, 60, 55,
	14, 13, 16, 24, 40, 57, 69, 56,
	14, 17, 22, 29, 51, 87, 80, 62,
	18, 22, 37, 56, 68, 109, 103, 77,
	24, 35, 55, 64, 81, 104, 113, 92,
	49, 64, 78, 87, 103, 121, 120, 101,
	72, 92, 95, 98, 112, 100, 103, 99
    };

    public final static double[] MATRIX_AB = {
	17, 18, 24, 47, 99, 99, 99, 99,
	18, 21, 26, 66, 99, 99, 99, 99,
	24, 26, 56, 99, 99, 99, 99, 99,
	47, 66, 99, 99, 99, 99, 99, 99,
	99, 99, 99, 99, 99, 99, 99, 99,
	99, 99, 99, 99, 99, 99, 99, 99,
	99, 99, 99, 99, 99, 99, 99, 99,
	99, 99, 99, 99, 99, 99, 99, 99
    };

    final double [] matrix = new double[64];
    private final int [] ibuff = new int[64];
    private final double[] dbuff = new double[64];

    // public Quantification(double quality) {
    // 	int ptr = 0;
    // 	for (int i=1; i < 9; ++i) {
    // 	    for (int j=1;j<9;++j) {
    // 		matrix[ptr++] = (j < 10 - i) ? (j * (9-i) + i * (9-j)) : 99;
    // 	    }
    // 	}
    // }
    
    public Quantification(double[] matrix, double quality) {
	if (quality > 50) {
	    for(int i = 0 ; i < 64; ++i) {
		this.matrix[i] = (matrix[i] * (101.0 - quality)) / 50;
		if(this.matrix[i] < 9) this.matrix[i] = 9;
	    }
	} else if (quality < 50) {
	    for(int i = 0; i < 64; ++i) {
		this.matrix[i] = (matrix[i] * 50.0) / quality;
	    }
	} else {
	    for(int i = 0; i < 64; ++i) {
		this.matrix[i] = matrix[i];
	    }
	}
    }

    
    public final int[] quantificate(double[] input) {
	for(int i = 0; i < 64; ++i) {
	    ibuff[i] = (int)Math.round(input[i] / matrix[i]);
	}
	return ibuff;
    }

    public final double[] unquantificate(int[] input) {
	for(int i = 0; i < 64; ++i) {
	    dbuff[i] = input[i] * matrix[i];
	}
	return dbuff;
    }
}