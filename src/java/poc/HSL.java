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
		buff[1] = (dm >> 1) / ( L < 128 ? L : 255 - L);
	    }

	    // Hue
	    if (max == r) {
		buff[0] = ((g - b) * 60) / dm;
		if (g < b) {
		    buff[0] += 360;
		}
	    } else if (max == g) {
		buff[0] = (((b - r) * 60) / dm) + 120;
	    } else { // max == b
		buff[0] = (((r - g) * 60) / dm) + 240;
	    }
	}
	
	return buff;
    }

    public final int[] toRGB
	(final int[] color)
    {
	return rgbbuff;
    }

    public HSL()
    {
	super(3);
    }
	
}