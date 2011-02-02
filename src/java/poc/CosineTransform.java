package poc;

import java.nio.DoubleBuffer;

public class CosineTransform {

    private double buff[] = new double[64];
    private int ibuff[] = new int[64];

    private final double sqrt2 = Math.pow(2.0, 0.5);
    private final double pi8 = Math.PI / 8.0;

    public final double[] cosine(int[] block) {
	int uvptr = 0;
	double val = 0;
	for (int v = 0; v < 8; ++v) {
	    double pi8v = pi8 * v;
	    for (int u = 0; u < 8; ++u) {
		double pi8u = pi8 * u;
		val = 0;
		int xyptr = 0;
		for (int y = 0; y < 8; ++y) {
		    for (int x = 0; x < 8; ++x) {
			val += (double)(block[xyptr]) * Math.cos(pi8u * ((double)x + 0.5)) * Math.cos(pi8v * ((double)y + 0.5));
			xyptr++;
		    }
		}
		if (u > 0 && v > 0) val *= 2.0;
		else if(u > 0 || v > 0) val *= sqrt2;
		buff[uvptr] = val / 8.0;
		++uvptr;
	    }
	}
	buff[0] -= 1024;
	return buff;
    }

    public final int[] uncosine(double[] block) {
	block[0] += 1024;
	int xyptr = 0;
	double val;
	for (int y = 0; y < 8; ++y) {
	    double pi8y = pi8 * (((double)y) + 0.5); 
	    for (int x = 0; x < 8; ++x) {
		double pi8x = pi8 * (((double)x) + 0.5);
		int uvptr = 0;
		val = 0.0;
		for (int v = 0; v < 8; ++v) {
		    for (int u = 0; u < 8; ++u) {
			val +=
			    (((u == 0 && v == 0) ? 0.5 :
			      ((u == 0 || v == 0) ? 1.0/sqrt2 : 1.0))
			     * Math.cos(pi8x * ((double)u))
			     * Math.cos(pi8y * ((double)v)))
			    * block[uvptr];
			++uvptr;
		    }
		}
		ibuff[xyptr] = (int)Math.round(val / 4.0);
		++xyptr;
	    }
	}
	return ibuff;	
    }
}