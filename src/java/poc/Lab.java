package poc;

public class Lab extends XYZ {
    public static final double f
	(final double xr)
    {
	return xr > E ? Math.pow(xr, ONE_THIRD) : (K * xr + 16) / 116;
    }

    public final int[] fromRGB
	(final int r, final int g, final int b)
    {
	toXYZ(r, g, b);
	//System.out.format("XYZ: %g %g %g\n", x, y, z);
	final double fx = f(x / Xr);
	final double fy = f(y / Yr);
	final double fz = f(z / Zr);

	buff[0] = (int)((((116.0 * fy) - 16.0)*255.0) / 100.0);
	buff[1] = (int)(500.0 * (fx - fy)) + 128;
	buff[2] = (int)(200.0 * (fy - fz)) + 128;
	return buff;
    }

    public final int[] toRGB
	(final int[] color)
    {
	final double L = ((color[0] * 100.0) / 255.0);
	final double fy = (L + 16.0) / 116.0;
	final double fx = ((color[1] - 128) / 500.0) + fy;
	final double fz = fy - ((color[2] - 128) / 200.0);

	final double fx3 = fx * fx * fx;
	final double fy3 = fy * fy * fy;
	final double fz3 = fz * fz * fz;

	x = Xr * (fx3 > E ? fx3 : (116.0 * fx - 16.0) / K);
	y = Yr * (L > KE ? fy3 : L / K);
	z = Zr * (fz3 > E ? fz3 : (116.0 * fz - 16.0) / K);

	//System.out.format("XYZ: %g %g %g\n", x, y, z);
	fromXYZ();
	return rgbbuff;
    }

    public Lab()
    {
	super(3);
    }
    
        
    public static void main(String [] a) {
	Lab model = new Lab();
	tests(model);
    }


}
