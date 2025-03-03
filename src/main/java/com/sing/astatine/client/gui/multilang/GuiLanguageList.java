package com.sing.astatine.client.gui.multilang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiLanguageList extends GuiListExtended {
    private final String header;
    private final List<GuiScreenLanguages.LanguageEntry> list;

    public GuiLanguageList(Minecraft mcIn, int p_i45055_2_, int p_i45055_3_, List<GuiScreenLanguages.LanguageEntry> list, String header) {
        super(mcIn, p_i45055_2_, p_i45055_3_, 50, p_i45055_3_ - 55 + 4, 36);
        this.centerListVertically = false;
        this.setHasListHeader(true, (int) ((float) mcIn.fontRenderer.FONT_HEIGHT * 1.5F));
        this.header = TextFormatting.UNDERLINE.toString() + TextFormatting.BOLD + header;
        this.list = list;
    }

    /**
     * Handles drawing a list's header row.
     */
    protected void drawListHeader(int insideLeft, int insideTop, @NotNull Tessellator tessellatorIn) {
        this.mc.fontRenderer.drawString(header, insideLeft + this.width / 2 - this.mc.fontRenderer.getStringWidth(header) / 2, Math.min(this.top + 3, insideTop), 16777215);
    }

    protected int getSize() {
        return list.size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiScreenLanguages.@NotNull LanguageEntry getListEntry(int index) {
        return list.get(index);
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth() {
        return this.width;
    }

    protected int getScrollBarX() {
        return this.right - 6;
    }
}
