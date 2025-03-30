package fi.dy.masa.malilib.util.data;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

/**
 * Wraps the Mojmap "ResourceLocation" with Identifier
 * -
 * Post-ReWrite code
 */
public class ResourceLocation
{
    public static final Codec<ResourceLocation> CODEC = RecordCodecBuilder.create(
            resourceLocationInstance -> resourceLocationInstance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(get -> get.id)
            ).apply(resourceLocationInstance, ResourceLocation::new)
    );
    public static final PacketCodec<ByteBuf, ResourceLocation> PACKET_CODEC = PacketCodecs.STRING.xmap(ResourceLocation::of, ResourceLocation::toString);
    private final Identifier id;

    public ResourceLocation(String str)
    {
        this.id = Identifier.of(str);
    }

    public ResourceLocation(String name, String path)
    {
        this.id = Identifier.of(name, path);
    }

    public ResourceLocation(Identifier id)
    {
        this.id = id;
    }

    public static ResourceLocation of(String str)
    {
        return new ResourceLocation(str);
    }

    public static ResourceLocation of(String name, String path)
    {
        return new ResourceLocation(name, path);
    }

    public static ResourceLocation ofVanilla(String path)
    {
        return new ResourceLocation("minecraft", path);
    }

    public static ResourceLocation of(Identifier id)
    {
        return new ResourceLocation(id);
    }

    public static List<ResourceLocation> of(List<Identifier> list)
    {
        List<ResourceLocation> newList = new ArrayList<>();

        list.forEach((id) -> newList.add(ResourceLocation.of(id)));

        return newList;
    }

    public @Nullable Identifier getId()
    {
        return this.id;
    }

    public String getNamespace()
    {
        return this.id.getNamespace();
    }

    public String getPath()
    {
        return this.id.getPath();
    }

    public String toTranslationKey()
    {
        return this.id.getNamespace()+"."+this.id.getPath();
    }

    @Override
    public String toString()
    {
        return this.id.toString();
    }
}
