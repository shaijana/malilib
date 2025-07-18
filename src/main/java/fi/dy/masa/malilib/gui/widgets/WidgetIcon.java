package fi.dy.masa.malilib.gui.widgets;

import net.minecraft.client.gui.DrawContext;

import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;

public class WidgetIcon extends WidgetBase
{
    protected final IGuiIcon icon;

    public WidgetIcon(int x, int y, IGuiIcon icon)
    {
        super(x, y, icon.getWidth(), icon.getHeight());

        this.icon = icon;
    }

    public void render(DrawContext drawContext, boolean enabled, boolean selected)
    {
//        RenderUtils.color(1f, 1f, 1f, 1f);
//        this.bindTexture(this.icon.getTexture(), drawContext);
        this.icon.renderAt(drawContext, this.x, this.y, this.zLevel, enabled, selected);

        if (selected)
        {
            RenderUtils.drawOutlinedBox(drawContext, this.x, this.y, this.width, this.height, 0x20C0C0C0, 0xE0FFFFFF);
        }
    }
}
