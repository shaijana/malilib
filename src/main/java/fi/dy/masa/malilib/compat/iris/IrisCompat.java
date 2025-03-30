package fi.dy.masa.malilib.compat.iris;

import net.fabricmc.loader.api.FabricLoader;

public class IrisCompat
{
    private static final boolean isSodiumLoaded;
    private static final boolean isIrisLoaded;

    static
    {
        isSodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        isIrisLoaded = FabricLoader.getInstance().isModLoaded("iris");
    }

    public static boolean hasIris()
    {
        return isSodiumLoaded && isIrisLoaded;
    }
}
