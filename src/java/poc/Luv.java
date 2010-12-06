package poc;

public class Luv extends XYZ {
    
    public static final double Xr_15Yr_3Zr  = Xr + 15.0 * Yr + 3.0 * Zr;
    public static final double Ur = (4.0 * Xr) / Xr_15Yr_3Zr;
    public static final double Vr = (9.0 * Yr) / Xr_15Yr_3Zr;
    
    public final int[] fromRGB
	(final int r, final int g, final int b)
    {
	toXYZ(r, g, b);
	//System.out.format("XYZ: %g %g %g\n", x, y, z);
	final double yr = y / Yr;

	final double div1 = x + 15.0 * y + 3.0 * z;
	final double u1 = (4.0 * x) / div1;
	final double v1 = (9.0 * y) / div1;
	
	final double L = yr > E ? (116.0 * Math.pow(yr, ONE_THIRD)) - 16.0 : K * yr;
	buff[0] = (int) L;
	final double L13 = L * 13.0;
	buff[1] = (int) (L13 * (u1 - Ur));
	buff[2] = (int) (L13 * (v1 - Vr));
	return buff;
    }

    public static final int i116_3 = 116 * 116 * 116;
    public final int[] toRGB
	(final int[] color)
    {
	int Li = color[0];
	if (Li > 100) Li = 100;
	if (Li < 0) Li = 0;
	final double L = Li;

	double y;
	if(L > KE) {
	    long Li3 = Li + 16;
	    Li3 *= Li3 * Li3;
	    y = ((double) Li3) / ((double) i116_3);
	} else {
	    y = L / K;
	}

	final long Li13 = Li * 13;
	final double a = ONE_THIRD * (((double)(Li13 << 2) /
			   (((double)Li13 * Ur) + (double)(color[1]%100))) - 1.0);
	final double b = -5.0 * y;
	final double d = y * (((double)((Li13 << 1) + Li13) /
			       (((double)Li13 * Vr) + (double)(color[2]%100))) - 5.0);

	x = (d - b) / (a + ONE_THIRD);
	z = x * a + b;

	//System.out.format("XYZ: %g %g %g\n", x, y, z);
	fromXYZ();
	return rgbbuff;
    }

    public Luv()
    {
	super(3);
    }
         
    public static void main(String [] a) {
	Luv model = new Luv();
	testAll(model);
    }


    
    public static void testAll(ColorModel model) {
	for (int r = 0; r < 256; ++r) {
	    for (int g = 0; g < 256; ++g) {
		for (int b = 0; b < 256; ++b) {
		    int [] color = model.fromRGB(r, g, b);
		    int [] rgb = model.toRGB(color);
		    if (Math.abs(r - rgb[0]) > 100
			|| Math.abs(g - rgb[1]) > 100
			|| Math.abs(b - rgb[2]) > 100)
		    {
			System.err.format("----\nrgb:%d %d %d\nmod:%d %d %d\nrgb:%d %d %d\n", r, g, b, color[0], color[1], color[2], rgb[0], rgb[1], rgb[2]);
			return;
		    }
		}
	    }
	}
    }
}
