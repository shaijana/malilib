package fi.dy.masa.malilib.event;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationDispatcher;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InitializationHandler implements IInitializationDispatcher
{
    private static final InitializationHandler INSTANCE = new InitializationHandler();

    private final List<IInitializationHandler> handlers = new ArrayList<>();

    public static IInitializationDispatcher getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void registerInitializationHandler(IInitializationHandler handler)
    {
        if (this.handlers.contains(handler) == false)
        {
            this.handlers.add(handler);
        }
    }

    @ApiStatus.Internal
    public void onPreGameInit(Path runDir)
    {
        if (this.handlers.isEmpty() == false)
        {
            for (IInitializationHandler handler : this.handlers)
            {
                handler.preGameInit(runDir);
            }
        }
    }

    @ApiStatus.Internal
    public void onGameInitDone()
    {
        if (this.handlers.isEmpty() == false)
        {
            for (IInitializationHandler handler : this.handlers)
            {
                handler.registerModHandlers();
            }
        }

        ((ConfigManager) ConfigManager.getInstance()).loadAllConfigs();
        InputEventHandler.getKeybindManager().updateUsedKeys();
    }
}
