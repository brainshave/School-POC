package poc;

/** Abstract class that represents conversion to/from concrete color model. */
public abstract class ColorModel {
    /** Implementation should convert rgb color to appriopriate model,
	store it in buff and return buff. */
    public abstract int[] fromRGB
	(final int r, final int g, final int b);

    /** Implementation should convert color given by color argument to
	rgb, store it in rgbbuff and return it. */
    public abstract int[] toRGB
	(final int[] color);

    /** Uses implementations functions fromRGB and toRGB to do generic
	adjustments in concrete color model.

	adjustments must have same size as concrete model size. */
    public final int[] adjust
	(final int r, final int g, final int b, final int[] adjustments)
    {
	fromRGB(r, g, b); // buff contains actual color values
	for (int i = 0; i < size; ++i) {
	    buff[i] += adjustments[i];
	}
	return toRGB(buff);
    }
    
    /** buff is used to store and return last converted color. It´s
	reused to avoid overhead of allocations/garbage collection.*/
    protected final int[] buff;
    /** rgbbuff is used to store rgb values. Analogical to buff. */
    protected final int[] rgbbuff;
    /** size of buff and adjustments array in adjust method.*/
    protected final int size;

    /** Protected constructor to use only in extending
	classes. Allocates int arrays for buff (of given size) and
	rgbbuff. */
    protected ColorModel
	(int size)
    {
	this.size = size;
	this.buff = new int[size];
	this.rgbbuff = new int[3];
    }
}