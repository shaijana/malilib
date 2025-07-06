package fi.dy.masa.malilib.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.RenderUtils;

public abstract class WidgetBase
{
    protected final MinecraftClient mc;
    protected final TextRenderer textRenderer;
    protected final int fontHeight;
    protected DrawContext drawContext;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int zLevel;

    public WidgetBase(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.mc = MinecraftClient.getInstance();
        this.textRenderer = this.mc.textRenderer;
        this.fontHeight = this.textRenderer.fontHeight;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setZLevel(int zLevel)
    {
        this.zLevel = zLevel;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseX < this.x + this.width &&
               mouseY >= this.y && mouseY < this.y + this.height;
    }

    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.isMouseOver(mouseX, mouseY))
        {
            return this.onMouseClickedImpl(mouseX, mouseY, mouseButton);
        }

        return false;
    }

    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        this.onMouseReleasedImpl(mouseX, mouseY, mouseButton);
    }

    public void onMouseReleasedImpl(int mouseX, int mouseY, int mouseButton)
    {
    }

    public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount)
    {
        if (this.isMouseOver(mouseX, mouseY))
        {
            return this.onMouseScrolledImpl(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return false;
    }

    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double horizontalAmount, double verticalAmount)
    {
        return false;
    }

    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers)
    {
        return this.onKeyTypedImpl(keyCode, scanCode, modifiers);
    }

    protected boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }

    public boolean onCharTyped(char charIn, int modifiers)
    {
        return this.onCharTypedImpl(charIn, modifiers);
    }

    protected boolean onCharTypedImpl(char charIn, int modifiers)
    {
        return false;
    }

    /**
     * Returns true if this widget can be selected by clicking at the given point
     */
    public boolean canSelectAt(int mouseX, int mouseY, int mouseButton)
    {
        return this.isMouseOver(mouseX, mouseY);
    }

//    public VertexConsumer bindTexture(Identifier texture, DrawContext context)
//    {
//        return RenderUtils.bindGuiTexture(texture, context);
//    }
//
//    public VertexConsumer bindOverlayTexture(Identifier texture, DrawContext context)
//    {
//        return RenderUtils.bindGuiOverlayTexture(texture, context);
//    }

    public int getStringWidth(String text)
    {
        return this.textRenderer.getWidth(text);
    }

    public void drawString(DrawContext drawContext, int x, int y, int color, String text)
    {
        drawContext.drawText(this.textRenderer, text, x, y, color, false);
    }

    public void drawCenteredString(DrawContext drawContext, int x, int y, int color, String text)
    {
        drawContext.drawText(this.textRenderer, text, x - this.getStringWidth(text) / 2, y, color, false);
    }

    public void drawStringWithShadow(DrawContext drawContext, int x, int y, int color, String text)
    {
        drawContext.drawTextWithShadow(this.textRenderer, text, x, y, color);
    }

    public void drawCenteredStringWithShadow(DrawContext drawContext, int x, int y, int color, String text)
    {
//        final int startX = x + 2;
//        final int endX = x + width - 2;
//        final int endY = y + height;
//        final int centerX = (startX + endX) / 2;
//        final int textWidth = this.getStringWidth(text);
////        final int xAdj = endX - startX;
//        final int yAdj = (y + endY - 9) / 2 + 1;
//        final int centerAdj = MathHelper.clamp(centerX, startX + textWidth / 2, endX - textWidth / 2);

        drawContext.drawCenteredTextWithShadow(this.textRenderer, text, x, y, color);
    }

    public void drawBackgroundMask(DrawContext drawContext)
    {
        RenderUtils.drawTexturedRect(drawContext, GuiBase.BG_TEXTURE, this.x + 1, this.y + 1, 0, 0, this.width - 2, this.height - 2);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected)
    {
        if (this.drawContext == null || !this.drawContext.equals(drawContext))
        {
            this.drawContext = drawContext;
        }
    }

    public void postRenderHovered(DrawContext drawContext, int mouseX, int mouseY, boolean selected)
    {
        if (this.drawContext == null || !this.drawContext.equals(drawContext))
        {
            this.drawContext = drawContext;
        }
    }
}
