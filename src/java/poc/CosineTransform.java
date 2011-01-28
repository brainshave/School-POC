package poc;

import java.nio.DoubleBuffer;
import static com.schwebke.jfftw3.JFFTW3.*;

public class CosineTransform {

    private double buff[] = new double[64];
    private int ibuff[] = new int[64];
    long dctDataPtr = jfftw_real_malloc(64);
    DoubleBuffer dctData = jfftw_real_get(dctDataPtr);
    long forwardPlanPtr = jfftw_plan_r2r_2d(8,8, dctDataPtr, dctDataPtr,
					    JFFTW_REDFT10, JFFTW_REDFT10, 0);
    long backwardPlanPtr = jfftw_plan_r2r_2d(8,8, dctDataPtr, dctDataPtr,
					     JFFTW_REDFT01, JFFTW_REDFT01, 0);

    private final double sqrt2 = Math.pow(2.0, 0.5);
    private final double pi8 = Math.PI / 8.0;
    //private final double pi16 = Math.PI / 16.0;

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
	    double pi8y = pi8 * (((double)y) + 0.5); //Math.PI * (2.0 * y + 1.0) / 16.0;
	    for (int x = 0; x < 8; ++x) {
		double pi8x = pi8 * (((double)x) + 0.5); //Math.PI * (2.0 * x + 1.0) / 16.0;
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
    
    public final double[] cosineFTW(int[] block) {
	dctData.rewind();
	for(int i: block) dctData.put(i);
	
	dctData.rewind();
	jfftw_execute(forwardPlanPtr);
	
	dctData.rewind();
	for (int i = 0; i < 64; ++i) {
	    buff[i] = dctData.get();
	}
	//buff[0] -= 1024;
	return buff;
    }

    public final int[] uncosineFTW(double[] block) {
	dctData.rewind();
	//block[0] += 1024;
	for(double d: block) dctData.put(d);

	dctData.rewind();
	jfftw_execute(backwardPlanPtr);
	for (int i = 0; i < 64; ++i) {
	    ibuff[i] = (int) (dctData.get()/256.0);
	}
	return ibuff;
    }

    protected void finalize() throws Throwable
    {
	jfftw_real_free(dctDataPtr);
	jfftw_complex_free(forwardPlanPtr);
	jfftw_complex_free(backwardPlanPtr);
    }
}