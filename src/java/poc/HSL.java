package poc;

/** Class that represents HSL color model. buff array holds 3 values:
    <ol>
    <li>H: 0..359</li>
    <li>S: 0..255</li>
    <li>L: 0..255</li>
    </ol>
*/
public class HSL extends ColorModel {
    public final int[] fromRGB
	(final int r, final int g, final int b)
    {
	// max = max(r,g,b)
	int max = r;
	if (g > max) max = g;
	if (b > max) max = b;

	// min = min(r,g,b)
	int min = r;
	if (g < min) min = g;
	if (b < min) min = b;

	int dm = max - min;

	// L = (max + min) / 2 // L: 0..255
	final int L = (max + min) >> 1;
	buff[2] = L;

	if (dm == 0) { // no information about color, grey.
	    // H = 0, S = 0
	    buff[0] = 0;
	    buff[1] = 0;
	} else { // color ftw
	    // Saturation
	    if (L == 0) {
		// S = 0
		buff[1] = 0;
	    } else {
		// S = L < 0.5 ? dm / 2L : dm / (2 - 2L)
		buff[1] = (dm << 7) / ( L < 127 ? L : 255 - L);
	    }

	    int H;
	    // Hue
	    if (max == r) {
		H = ((g - b) * 60) / dm;
		// does the same thing as correctHue, so commented.
		//if (g < b) {
		//H += 360;
		//}
	    } else if (max == g) {
		H = (((b - r) * 60) / dm) + 120;
	    } else { // max == b
		H = (((r - g) * 60) / dm) + 240;
	    }

	    buff[0] = correctHue(H);
	}
	
	return buff;
    }

    public static final int correctHue(int h) {
	h %= 360;
	if (h < 0) {
	    return h + 360;
	} else {
	    return h;
	}
    }

    public static final int correct255(int i) {
	if (i < 0) return 0;
	if (i > 255) return 255;
	return i;
    }

    public final int[] toRGB
	(final int[] color)
    {
	final int H = correctHue(color[0]);
	final int S = correct255(color[1]);
	final int L = correct255(color[2]);
	
	// S == 0 => R=G=B=L
	if (S == 0) {
	    rgbbuff[0] = L;
	    rgbbuff[1] = L;
	    rgbbuff[2] = L;
	} else {
	    // Q = { L * (1.0 + S), L < 0.5
	    //     { L + S - (L * S), L >= 0.5
	    final int Q = L < 127 ? ((L * (255 + S)) >> 8) : (L + S - ((L * S) >> 8));
	    // P = 2.0 * L - Q
	    final int P = (L << 1) - Q;

	    // leaving H in 0..360 range, means that all numbers in Tx
	    // should be *360
	    // final int Tr = correctHue(H + 120);
	    // final int Tg = H;
	    // final int Tb = correctHue(H - 120);
	    int Tc;
	    int rotation = 120;
	    for(int i = 0; i < 3; ++i) {
		Tc = correctHue(H + rotation);
		rotation -= 120;

		if (Tc < 60) { // Tc < 1/6
		    Tc = P + (((Q - P) * 6 * Tc) / 360);
		} else if (Tc < 180) { // Tc < 1/2
		    Tc = Q;
		} else if (Tc < 240) { // Tc < 2/3
		    Tc = P + (((Q - P) * 6 * (240 - Tc)) / 360);
		} else {
		    Tc = P;
		}
		rgbbuff[i] = Tc;
	    }
	}	    
	    
	return rgbbuff;
    }

    public HSL()
    {
	super(3);
    }


    public void test(int r, int g, int b)
    {
	int[] hsl = fromRGB(r,g,b);
	int[] rgb = toRGB(hsl);
	System.out.format("R: %d G: %d B: %d\nH: %d S: %d L: %d\nR: %d G: %d B: %d\n----\n", r, g, b, hsl[0], hsl[1], hsl[2], rgb[0], rgb[1], rgb[2]);
    }
    
    public static void main(String [] a) {
	HSL model = new HSL();
	model.test(0, 0, 0);
	model.test(255, 255, 255);
	model.test(255, 0, 0);
	model.test(0, 255, 0);
	model.test(0, 0, 255);
	model.test(255, 128, 0);
	model.test(0, 255, 128);
	model.test(128, 0, 255);
	model.test(255, 0, 255);
	model.test(200, 150, 100);
    }
}