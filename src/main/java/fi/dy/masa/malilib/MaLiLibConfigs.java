package fi.dy.masa.malilib;

import java.io.File;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.test.ConfigTestLockedList;
import fi.dy.masa.malilib.test.ConfigTestOptList;
import fi.dy.masa.malilib.test.TestEnumConfig;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.Color4f;

public class MaLiLibConfigs implements IConfigHandler
{
    private static final String CONFIG_FILE_NAME = MaLiLibReference.MOD_ID + ".json";

    private static final String GENERIC_KEY = MaLiLibReference.MOD_ID+".config.generic";
    public static class Generic
    {
        public static final ConfigHotkey            IGNORED_KEYS              = new ConfigHotkey            ("ignoredKeys",      "").apply(GENERIC_KEY);
        public static final ConfigHotkey            OPEN_GUI_CONFIGS          = new ConfigHotkey            ("openGuiConfigs",   "A,C").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed   ENABLE_CONFIG_SWITCHER    = new ConfigBooleanHotkeyed   ("enableConfigSwitcher",    true, "").apply(GENERIC_KEY);
        public static final ConfigBoolean           REALMS_COMMON_CONFIG      = new ConfigBoolean           ("realmsCommonConfig",      true).apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed   ENABLE_ACTIONBAR_MESSAGES = new ConfigBooleanHotkeyed   ("enableActionbarMessages", true, "").apply(GENERIC_KEY);

        public static final ImmutableList<IConfigValue> OPTIONS = ImmutableList.of(
                IGNORED_KEYS,
                OPEN_GUI_CONFIGS,
                ENABLE_CONFIG_SWITCHER,
                REALMS_COMMON_CONFIG,
                ENABLE_ACTIONBAR_MESSAGES
        );

        // Can't add OPEN_GUI_CONFIGS here, because things will break
        public static final List<IHotkey> HOTKEY_LIST = ImmutableList.of(
                ENABLE_CONFIG_SWITCHER,
                ENABLE_ACTIONBAR_MESSAGES
        );
    }

    private static final String DEBUG_KEY = MaLiLibReference.MOD_ID+".config.debug";
    public static class Debug
    {
        public static final ConfigBoolean DEBUG_MESSAGES            = new ConfigBoolean("debugMessages",false).apply(DEBUG_KEY);
        public static final ConfigBoolean CONFIG_ELEMENT_DEBUG      = new ConfigBoolean("configElementDebug", false).apply(DEBUG_KEY);
        public static final ConfigBoolean INPUT_CANCELLATION_DEBUG  = new ConfigBoolean("inputCancellationDebugging", false).apply(DEBUG_KEY);
        public static final ConfigBoolean KEYBIND_DEBUG             = new ConfigBoolean("keybindDebugging", false).apply(DEBUG_KEY);
        public static final ConfigBoolean KEYBIND_DEBUG_ACTIONBAR   = new ConfigBoolean("keybindDebuggingIngame", false).apply(DEBUG_KEY);
        public static final ConfigBoolean MOUSE_SCROLL_DEBUG        = new ConfigBoolean("mouseScrollDebug", false).apply(DEBUG_KEY);
        public static final ConfigBoolean PRINT_TRANSLATION_KEYS    = new ConfigBoolean("printTranslationKeys", false).apply(DEBUG_KEY);

        public static final ImmutableList<IConfigValue> OPTIONS = ImmutableList.of(
                DEBUG_MESSAGES,
                CONFIG_ELEMENT_DEBUG,
                INPUT_CANCELLATION_DEBUG,
                KEYBIND_DEBUG,
                KEYBIND_DEBUG_ACTIONBAR,
                MOUSE_SCROLL_DEBUG,
                PRINT_TRANSLATION_KEYS
        );

        public static final List<IHotkey> HOTKEY_LIST = ImmutableList.of(
        );
    }

    private static final String TEST_KEY = MaLiLibReference.MOD_ID+".config.test";
    public static class Test
    {
        public static final ConfigBoolean           TEST_CONFIG_BOOLEAN             = new ConfigBoolean("testBoolean", false, "Test Boolean").apply(TEST_KEY);
        public static final ConfigBooleanHotkeyed   TEST_CONFIG_BOOLEAN_HOTKEYED    = new ConfigBooleanHotkeyed("testBooleanHotkeyed", false, "A,K").apply(TEST_KEY);
        public static final ConfigColor             TEST_CONFIG_COLOR               = new ConfigColor("testColor", "0x3022FFFF", "Test Color").apply(TEST_KEY);
        public static final ConfigColorList         TEST_CONFIG_COLOR_LIST          = new ConfigColorList("testColorList", ImmutableList.of(new Color4f(0, 0, 0), new Color4f(255, 255, 255, 255)), "Test Color List").apply(TEST_KEY);
        public static final ConfigDouble            TEST_CONFIG_DOUBLE              = new ConfigDouble("testDouble", 0.5, 0, 1, true, "Test Double").apply(TEST_KEY);
        public static final ConfigFloat             TEST_CONFIG_FLOAT               = new ConfigFloat("testFloat", 0.5f, 0.0f, 1.0f, true, "Test Float").apply(TEST_KEY);
        public static final ConfigHotkey            TEST_CONFIG_HOTKEY              = new ConfigHotkey("testHotkey", "", "Test Hotkey").apply(TEST_KEY);
        public static final ConfigInteger           TEST_CONFIG_INTEGER             = new ConfigInteger("testInteger", 0, "Test Integer").apply(TEST_KEY);
        public static final ConfigOptionList        TEST_CONFIG_OPTIONS_LIST        = new ConfigOptionList("testOptionList", ConfigTestOptList.TEST1, "Test Option List").apply(TEST_KEY);
        public static final ConfigString            TEST_CONFIG_STRING              = new ConfigString("testString", "testString", "Test String").apply(TEST_KEY);
        public static final ConfigStringList        TEST_CONFIG_STRING_LIST         = new ConfigStringList("testStringList", ImmutableList.of("testString1", "testString2"), "Test String List").apply(TEST_KEY);
        public static final ConfigLockedList        TEST_CONFIG_LOCKED_LIST         = new ConfigLockedList("testLockedConfigList", ConfigTestLockedList.INSTANCE, "Test Locked List").apply(TEST_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                TEST_CONFIG_BOOLEAN,
                TEST_CONFIG_BOOLEAN_HOTKEYED,
                TEST_CONFIG_COLOR,
                TEST_CONFIG_COLOR_LIST,
                TEST_CONFIG_DOUBLE,
                TEST_CONFIG_FLOAT,
                TEST_CONFIG_HOTKEY,
                TEST_CONFIG_INTEGER,
                TEST_CONFIG_OPTIONS_LIST,
                TEST_CONFIG_STRING,
                TEST_CONFIG_STRING_LIST,
                TEST_CONFIG_LOCKED_LIST
        );

        public static final List<IHotkey> HOTKEY_LIST = ImmutableList.of(
                TEST_CONFIG_BOOLEAN_HOTKEYED
        );
    }

    // Stuff used by any Post-Rewrite Code
    private static final String EXPERIMENTAL_KEY = MaLiLibReference.MOD_ID+".config.experimental";
    public static class Experimental
    {
        // Generic
        public static final ConfigBoolean           SORT_CONFIGS_BY_NAME            = new ConfigBoolean("sortConfigsByName", false).apply(EXPERIMENTAL_KEY);
        public static final ConfigBoolean           SORT_EXTENSION_MOD_OPTIONS      = new ConfigBoolean("sortExtensionModOptions", false).apply(EXPERIMENTAL_KEY);

        // Internal
        public static final ConfigString            ACTIVE_CONFIG_PROFILE           = new ConfigString("activeConfigProfile", "").apply(EXPERIMENTAL_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                // Generic
                SORT_CONFIGS_BY_NAME,
                SORT_EXTENSION_MOD_OPTIONS,

                // Internal
                ACTIVE_CONFIG_PROFILE
        );
    }

    public static void loadFromFile()
    {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "Debug", Debug.OPTIONS);

                if (MaLiLibReference.DEBUG_MODE)
                {
                    ConfigUtils.readConfigBase(root, "Test", Test.OPTIONS);
                    ConfigUtils.readHotkeyToggleOptions(root, "TestEnumHotkeys", "TestEnumToggles", TestEnumConfig.VALUES);
                }

                if (MaLiLibReference.EXPERIMENTAL_MODE)
                {
                    ConfigUtils.readConfigBase(root, "Experimental", Experimental.OPTIONS);
                }
            }
        }
    }

    public static void saveToFile()
    {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Debug", Debug.OPTIONS);

            if (MaLiLibReference.DEBUG_MODE)
            {
                ConfigUtils.writeConfigBase(root, "Test", Test.OPTIONS);
                ConfigUtils.writeHotkeyToggleOptions(root, "TestEnumHotkeys", "TestEnumToggles", TestEnumConfig.VALUES);
            }

            if (MaLiLibReference.EXPERIMENTAL_MODE)
            {
                ConfigUtils.writeConfigBase(root, "Experimental", Experimental.OPTIONS);
            }

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void onConfigsChanged()
    {
        saveToFile();
        loadFromFile();
    }

    @Override
    public void load()
    {
        loadFromFile();
    }

    @Override
    public void save()
    {
        saveToFile();
    }
}
