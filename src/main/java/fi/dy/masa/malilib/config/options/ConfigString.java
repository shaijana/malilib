package fi.dy.masa.malilib.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.util.StringUtils;

public class ConfigString extends ConfigBase<ConfigString> implements IConfigValue
{
    public static final Codec<ConfigString> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            PrimitiveCodec.STRING.fieldOf("name").forGetter(ConfigBase::getName),
                            PrimitiveCodec.STRING.fieldOf("defaultValue").forGetter(get -> get.defaultValue),
                            PrimitiveCodec.STRING.fieldOf("value").forGetter(get -> get.value),
                            PrimitiveCodec.STRING.fieldOf("previousValue").forGetter(get -> get.previousValue),
                            PrimitiveCodec.STRING.fieldOf("comment").forGetter(get -> get.comment),
                            PrimitiveCodec.STRING.fieldOf("prettyName").forGetter(get -> get.prettyName),
                            PrimitiveCodec.STRING.fieldOf("translatedName").forGetter(get -> get.translatedName)
                    )
                    .apply(instance, ConfigString::new)
    );
    private final String defaultValue;
    private String value;
    private String previousValue;

    public ConfigString(String name, String defaultValue)
    {
        this(name, defaultValue, name+" Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigString(String name, String defaultValue, String comment)
    {
        this(name, defaultValue, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigString(String name, String defaultValue, String comment, String prettyName)
    {
        this(name, defaultValue, comment, prettyName, name);
    }

    public ConfigString(String name, String defaultValue, String comment, String prettyName, String translatedName)
    {
        super(ConfigType.STRING, name, comment, prettyName, translatedName);

        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.previousValue = defaultValue;
    }

    private ConfigString(String name, String defaultValue, String value, String previousValue, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, comment, prettyName, translatedName);
        this.value = value;
        this.previousValue = previousValue;
    }

    @Override
    public String getStringValue()
    {
        return this.value;
    }

    @Override
    public String getDefaultStringValue()
    {
        return this.defaultValue;
    }

    public String getOldStringValue()
    {
        return this.previousValue;
    }

    @Override
    public void setValueFromString(String value)
    {
        this.previousValue = this.value;
        this.value = value;

        if (this.previousValue.equals(this.value) == false)
        {
            this.onValueChanged();
        }
    }

    @Override
    public void resetToDefault()
    {
        this.setValueFromString(this.defaultValue);
    }

    @Override
    public boolean isModified()
    {
        return this.value.equals(this.defaultValue) == false;
    }

    @Override
    public boolean isModified(String newValue)
    {
        return this.defaultValue.equals(newValue) == false;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.value = element.getAsString();
                this.previousValue = this.value;
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
