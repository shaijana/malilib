package fi.dy.masa.malilib.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.util.StringUtils;

public class ConfigOptionList extends ConfigBase<ConfigOptionList> implements IConfigOptionList, IStringRepresentable
{
//    public static final Codec<ConfigOptionList> CODEC = RecordCodecBuilder.create(
//            inst -> inst.group(
//                    Codec.PASSTHROUGH.dispatchStable()
//            ).apply(inst, ConfigColorList::new)
//    );
    private final IConfigOptionListEntry defaultValue;
    private IConfigOptionListEntry value;

    public ConfigOptionList(String name, IConfigOptionListEntry defaultValue)
    {
        this(name, defaultValue, name+" Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigOptionList(String name, IConfigOptionListEntry defaultValue, String comment)
    {
        this(name, defaultValue, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigOptionList(String name, IConfigOptionListEntry defaultValue, String comment, String prettyName)
    {
        this(name, defaultValue, comment, prettyName, name);
    }

    public ConfigOptionList(String name, IConfigOptionListEntry defaultValue, String comment, String prettyName, String translatedName)
    {
        super(ConfigType.OPTION_LIST, name, comment, prettyName, translatedName);

        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public IConfigOptionListEntry getOptionListValue()
    {
        return this.value;
    }

    @Override
    public IConfigOptionListEntry getDefaultOptionListValue()
    {
        return this.defaultValue;
    }

    @Override
    public void setOptionListValue(IConfigOptionListEntry value)
    {
        IConfigOptionListEntry oldValue = this.value;
        this.value = value;

        if (oldValue != this.value)
        {
            this.onValueChanged();
        }
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
            return this.value.fromString(newValue) != this.defaultValue;
        }
        catch (Exception ignored)
        {
        }

        return true;
    }

    @Override
    public void resetToDefault()
    {
        this.setOptionListValue(this.defaultValue);
    }

    @Override
    public String getStringValue()
    {
        return this.value.getStringValue();
    }

    @Override
    public String getDefaultStringValue()
    {
        return this.defaultValue.getStringValue();
    }

    @Override
    public void setValueFromString(String value)
    {
        this.value = this.value.fromString(value);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.setValueFromString(element.getAsString());
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
        return new JsonPrimitive(this.getStringValue());
    }
}
