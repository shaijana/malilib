package fi.dy.masa.malilib.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.util.StringUtils;

public class ConfigInteger extends ConfigBase<ConfigInteger> implements IConfigInteger
{
    public static final Codec<ConfigInteger> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            PrimitiveCodec.STRING.fieldOf("name").forGetter(ConfigBase::getName),
                            PrimitiveCodec.INT.fieldOf("defaultValue").forGetter(get -> get.defaultValue),
                            PrimitiveCodec.INT.fieldOf("minValue").forGetter(get -> get.minValue),
                            PrimitiveCodec.INT.fieldOf("maxValue").forGetter(get -> get.maxValue),
                            PrimitiveCodec.INT.fieldOf("value").forGetter(get -> get.value),
                            PrimitiveCodec.BOOL.fieldOf("useSlider").forGetter(get -> get.useSlider),
                            PrimitiveCodec.STRING.fieldOf("comment").forGetter(get -> get.comment),
                            PrimitiveCodec.STRING.fieldOf("prettyName").forGetter(get -> get.prettyName),
                            PrimitiveCodec.STRING.fieldOf("translatedName").forGetter(get -> get.translatedName)
                    )
                    .apply(instance, ConfigInteger::new)
    );
    protected final int minValue;
    protected final int maxValue;
    protected final int defaultValue;
    protected int value;
    private boolean useSlider;

    public ConfigInteger(String name, int defaultValue)
    {
        this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, name+" Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, String comment)
    {
        this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, String comment, String prettyName)
    {
        this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment, prettyName, name);
    }

    public ConfigInteger(String name, int defaultValue, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment, prettyName, translatedName);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue)
    {
        this(name, defaultValue, minValue, maxValue, false, name+" Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, String comment)
    {
        this(name, defaultValue, minValue, maxValue, false, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, String comment, String prettyName)
    {
        this(name, defaultValue, minValue, maxValue, false, comment, prettyName, name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, minValue, maxValue, false, comment, prettyName, translatedName);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, boolean useSlider)
    {
        this(name, defaultValue, minValue, maxValue, useSlider, name+" Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment)
    {
        this(name, defaultValue, minValue, maxValue, useSlider, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment, String prettyName)
    {
        this(name, defaultValue, minValue, maxValue, useSlider, comment, prettyName, name);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment, String prettyName, String translatedName)
    {
        super(ConfigType.INTEGER, name, comment, prettyName, translatedName);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.useSlider = useSlider;
    }

    private ConfigInteger(String name, Integer defaultValue, Integer minValue, Integer maxValue, Integer value, Boolean useSlider, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, minValue, maxValue, useSlider, comment, prettyName, translatedName);
        this.value = value;
    }

    @Override
    public boolean shouldUseSlider()
    {
        return this.useSlider;
    }

    @Override
    public void toggleUseSlider()
    {
        this.useSlider = ! this.useSlider;
    }

    @Override
    public int getIntegerValue()
    {
        return this.value;
    }

    @Override
    public int getDefaultIntegerValue()
    {
        return this.defaultValue;
    }

    @Override
    public void setIntegerValue(int value)
    {
        int oldValue = this.value;
        this.value = this.getClampedValue(value);

        if (oldValue != this.value)
        {
            this.onValueChanged();
        }
    }

    @Override
    public int getMinIntegerValue()
    {
        return this.minValue;
    }

    @Override
    public int getMaxIntegerValue()
    {
        return this.maxValue;
    }

    protected int getClampedValue(int value)
    {
        return MathHelper.clamp(value, this.minValue, this.maxValue);
    }

    @Override
    public boolean isModified()
    {
        return this.value != this.defaultValue;
    }

    @Override
    public boolean isModified(String newValue)
    {
        try
        {
            return Integer.parseInt(newValue) != this.defaultValue;
        }
        catch (Exception ignored)
        {
        }

        return true;
    }

    @Override
    public void resetToDefault()
    {
        this.setIntegerValue(this.defaultValue);
    }

    @Override
    public String getStringValue()
    {
        return String.valueOf(this.value);
    }

    @Override
    public String getDefaultStringValue()
    {
        return String.valueOf(this.defaultValue);
    }

    @Override
    public void setValueFromString(String value)
    {
        try
        {
            this.setIntegerValue(Integer.parseInt(value));
        }
        catch (Exception e)
        {
            MaLiLib.LOGGER.warn("Failed to set config value for {} from the string '{}'", this.getName(), value, e);
        }
    }

    @Override
    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.value = this.getClampedValue(element.getAsInt());
            }
            else
            {
                MaLiLib.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element);
            }
        }
        catch (Exception e)
        {
            MaLiLib.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element, e);
        }
    }

    @Override
    public JsonElement getAsJsonElement()
    {
        return new JsonPrimitive(this.value);
    }
}
