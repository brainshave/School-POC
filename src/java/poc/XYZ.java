package poc;

public abstract class XYZ extends ColorModel {
    protected double x, y, z;
    protected static final double GAMMA = 2.2;
    protected static final double GAMMA_REV = 1.0 / GAMMA;
    protected static final double K = 903.3;
    protected static final double E = 0.008856;
    protected static final double KE = K*E;
    protected static final double Xr = 0.9505;
    protected static final double Yr = 1.0;
    protected static final double Zr = 1.0891;    
    
    /** Sets x, y and z */
    protected final void toXYZ
	(final int r, final int g, final int b)
    {
	double rr = Math.pow(r / 255.0f, GAMMA);
	double gg = Math.pow(g / 255.0f, GAMMA);
	double bb = Math.pow(b / 255.0f, GAMMA);

	x = rr * 0.4124564 + gg * 0.3575761 + bb * 0.1804375;
	y = rr * 0.2126729 + gg * 0.7151522 + bb * 0.0721750;
	z = rr * 0.0193339 + gg * 0.1191920 + bb * 0.9503041;
    }

    /** Sets rgbbuff */
    protected final void fromXYZ()
    {
	rgbbuff[0] = (int)(Math.pow(x * 3.2404542 +
				    y * -1.5371385 +
				    z * -0.4985314, GAMMA_REV) * 255);
	rgbbuff[1] = (int)(Math.pow(x * -0.9692660 +
				    y *  1.8760108 +
				    z *  0.0415560, GAMMA_REV) * 255);
	rgbbuff[2] = (int)(Math.pow(x * 0.0556434 +
				    y * -0.2040259 +
				    z *  1.0572252, GAMMA_REV) * 255);
    }

    protected XYZ(int size) {
	super(size);
    }
}