package fi.dy.masa.malilib.config;

import java.util.Locale;
import javax.annotation.Nullable;
import com.google.gson.JsonElement;

public interface IConfigBase
{
    /**
     * Returns the type of this config. Used by the config GUI to determine what kind of control
     * to use for this config.
     * @return the type of this config
     */
    ConfigType getType();

    /**
     * Returns the config name to display in the config GUIs
     * @return the name of this config
     */
    String getName();

    /**
     * Get the name of the config in lower-case
     * @return (Name)
     */
    default String getLowerName()
    {
        return this.getName().toLowerCase(Locale.ROOT);
    }

    /**
     * Get the name of the config without any '.'
     * @return (Result)
     */
    default String getCleanName()
    {
        String result = this.getName();

        // Swap any '.' with a '_'
        if (result.contains("."))
        {
            result = result.replace('.', '_');
        }

        return result;
    }

    /**
     * Returns the comment displayed when hovering over the config name in the config GUI.
     * Newlines can be added with "\n". Can be null if there is no comment for this config.
     * @return the comment, or null if no comment has been set
     */
    @Nullable
    String getComment();

    /**
     * Returns the "pretty name" for this config.
     * This is used in the possible toggle messages.
     * @return
     */
    default String getPrettyName()
    {
        return this.getName();
    }

    /**
     * Returns the display name used for this config in the config GUIs
     * @return
     */
    default String getConfigGuiDisplayName()
    {
        return this.getTranslatedName();
    }

    /**
     * Get translated name
     * @return (Defaults to Name)
     */
    String getTranslatedName();

    /**
     * Set the below values, if required to do so.
     * @param prettyName
     */
    void setPrettyName(String prettyName);
    void setTranslatedName(String translatedName);
    void setComment(String comment);

    /**
     * Set the value of this config option from a JSON element (is possible)
     * @param element
     */
    void setValueFromJsonElement(JsonElement element);

    /**
     * Return the value of this config option as a JSON element, for saving into a config file.
     * @return
     */
    JsonElement getAsJsonElement();
}
