package fi.dy.masa.malilib.util.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.ApiStatus;

import fi.dy.masa.malilib.util.MathUtils;

/**
 * Post-ReWrite code
 */
@ApiStatus.Experimental
public class Color4f
{
    public static final Pattern HEX_8 = Pattern.compile("(?:0x|#)([a-fA-F0-9]{8})");
    public static final Pattern HEX_6 = Pattern.compile("(?:0x|#)([a-fA-F0-9]{6})");
    public static final Pattern HEX_4 = Pattern.compile("(?:0x|#)([a-fA-F0-9]{4})");
    public static final Pattern HEX_3 = Pattern.compile("(?:0x|#)([a-fA-F0-9]{3})");

    public static final Color4f WHITE = new Color4f(1.0F, 1.0F, 1.0F, 1.0F);
    public final float r;
    public final float g;
    public final float b;
    public final float a;
    public final int ri;
    public final int gi;
    public final int bi;
    public final int ai;
    public final int intValue;

    public Color4f(float r, float g, float b)
    {
        this(r, g, b, 1.0F);
    }

    public Color4f(float r, float g, float b, float a)
    {
        if (r == -0.0F) { r = 0.0F; }
        if (g == -0.0F) { g = 0.0F; }
        if (b == -0.0F) { b = 0.0F; }
        if (a == -0.0F) { a = 0.0F; }

        r = MathUtils.clamp(r, 0.0F, 1.0F);
        g = MathUtils.clamp(g, 0.0F, 1.0F);
        b = MathUtils.clamp(b, 0.0F, 1.0F);
        a = MathUtils.clamp(a, 0.0F, 1.0F);

        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        this.ai = (int) (a * 255);
        this.ri = (int) (r * 255);
        this.gi = (int) (g * 255);
        this.bi = (int) (b * 255);

        this.intValue = (this.ai << 24) | (this.ri << 16) | (this.gi << 8) | this.bi;
    }

    /**
     * @return a new copy of the color using the given alpha value
     */
    public Color4f withAlpha(float alpha)
    {
        return fromColor(this.intValue, alpha);
    }

    @Override
    public String toString()
    {
        return getHexColorString(this.intValue);
    }

    public String getDebugString()
    {
        return String.format("Color4f{hex=%s, a = %f, r = %f, g = %f, b = %f, intValue = %d}",
                             getHexColorString(this.intValue), this.a, this.r, this.g, this.b, this.intValue);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}
        Color4f color4f = (Color4f) o;
        return this.intValue == color4f.intValue;
    }

    @Override
    public int hashCode()
    {
        return this.intValue;
    }

    /**
     * @return a color value parsed from the given AARRGGBB formatted int value
     */
    public static Color4f fromColor(int color)
    {
        float alpha = ((color & 0xFF000000) >>> 24) / 255.0F;
        return fromColor(color, alpha);
    }

    /**
     * @return a color value parsed from the given String argument.
     * The supported formats are 3, 4, 6 or 8 digit HEX representations,
     * with either a leading '#' or '0x' (one of them is required).
     */
    public static Color4f fromString(String str)
    {
        return fromColor(getColorFromString(str, 0xFFFFFFFF));
    }

    /**
     * @return a color value parsed from the given (AA)RRGGBB formatted int value,
     * but using the separately given alpha value
     */
    public static Color4f fromColor(int color, float alpha)
    {
        float r = ((color & 0x00FF0000) >>> 16) / 255.0F;
        float g = ((color & 0x0000FF00) >>>  8) / 255.0F;
        float b = ((color & 0x000000FF)       ) / 255.0F;

        return new Color4f(r, g, b, alpha);
    }

    /**
     * @return the hex color string with a hashtag in front (in the format "#30505050")
     */
    public static String getHexColorString(int color)
    {
        return String.format("#%08X", color);
    }

    /**
     * Tries to parse the given string as a hexadecimal value,
     * if it begins with '#' or '0x'.<br>
     * Accepts 8, 6, 4 or 3 digits long values.
     * The 4 and 3 digits long values will repeat each digit for each color channel,
     * so for example F159 will become FF115599.<br>
     * The 6 and 3 long versions will use 0xFF for the alpha channel.<br>
     * If the hex parsing fails, then the input it attempted to be parsed as a regular base 10 integer.
     * @param colorStr the string representation of the color to parse
     * @param defaultColor the fallback color if the parsing fails
     * @return the parsed color as an AARRGGBB int, or the fallback color if the parsing fails
     */
    public static int getColorFromString(String colorStr, int defaultColor)
    {
        Matcher matcher = HEX_8.matcher(colorStr);

        if (matcher.matches())
        {
            try
            {
                return (int) Long.parseLong(matcher.group(1), 16);
            }
            catch (NumberFormatException ignore) {}
        }

        matcher = HEX_6.matcher(colorStr);

        if (matcher.matches())
        {
            try
            {
                return 0xFF000000 | (int) Long.parseLong(matcher.group(1), 16);
            }
            catch (NumberFormatException ignore) { return defaultColor; }
        }

        matcher = HEX_4.matcher(colorStr);

        if (matcher.matches())
        {
            try
            {
                String str = matcher.group(1);
                int orig = Integer.parseInt(str, 16);
                int a = ((orig & 0xF000) >>> 12) * 17;
                int r = ((orig & 0x0F00) >>>  8) * 17;
                int g = ((orig & 0x00F0) >>>  4) * 17;
                int b = ((orig & 0x000F)       ) * 17;
                return a << 24 | r << 16 | g << 8 | b;
            }
            catch (NumberFormatException ignore) {}
        }

        matcher = HEX_3.matcher(colorStr);

        if (matcher.matches())
        {
            try
            {
                String str = matcher.group(1);
                int orig = Integer.parseInt(str, 16);
                int r = ((orig & 0x0F00) >>>  8) * 17;
                int g = ((orig & 0x00F0) >>>  4) * 17;
                int b = ((orig & 0x000F)       ) * 17;
                return 0xFF000000 | r << 16 | g << 8 | b;
            }
            catch (NumberFormatException ignore) {}
        }

        try { return Integer.parseInt(colorStr, 10); }
        catch (NumberFormatException e) { return defaultColor; }
    }

    public static int getColorFromHue(int hue)
    {
        return 0xFF000000 | (java.awt.Color.HSBtoRGB((float) (hue % 360) / 360.0F, 1.0F, 1.0F) & 0x00FFFFFF);
    }

    public static float[] convertRgb2Hsv(int color)
    {
        float[] hsv = new float[3];
        int r = ((color >>> 16) & 0xFF);
        int g = ((color >>>  8) & 0xFF);
        int b = ( color         & 0xFF);

        java.awt.Color.RGBtoHSB(r, g, b, hsv);

        return hsv;
    }

    public static int toArgbInt(float a, float r, float g, float b)
    {
        int ai = (int) (a * 255f);
        int ri = (int) (r * 255f);
        int gi = (int) (g * 255f);
        int bi = (int) (b * 255f);

        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }
}
