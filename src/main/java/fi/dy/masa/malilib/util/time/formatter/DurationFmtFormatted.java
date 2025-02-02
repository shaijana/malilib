package fi.dy.masa.malilib.util.time.formatter;

import javax.annotation.Nullable;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.ApiStatus;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.time.DurationFormat;

/**
 * Ported from CoreLib by Sakura Ryoko
 */
@ApiStatus.Internal
public class DurationFmtFormatted extends DurationFmt
{
    private final String defaultFormat;

    public DurationFmtFormatted(DurationFormat fmt)
    {
        super(fmt);
        this.defaultFormat = DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN;
        this.formatString = defaultFormat;
    }

    @Override
    public String format(long duration, @Nullable String fmt)
    {
        if (fmt != null && !fmt.isEmpty())
        {
            try
            {
                return DurationFormatUtils.formatDuration(duration, fmt, false);
            }
            catch (Exception err)
            {
                MaLiLib.LOGGER.error("format(): {}",
                                     StringUtils.translate("malilib.gui.label.duration_format.error.invalid_format", err.getMessage()));
            }
        }

        return DurationFormatUtils.formatDuration(duration, this.defaultFormat, false);
    }
}
