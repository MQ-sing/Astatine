package com.sing.astatine.client.gui.multilang;

import com.google.common.collect.Lists;
import com.sing.astatine.ConfigurationRuntime;
import com.sing.astatine.utils.Utils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiScreenLanguages extends GuiScreen {
    private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
    private final GuiScreen parentScreen;
    LanguageManager manager;
    GameSettings gameSettings;
    GuiLanguageList availableLanguagesList;
    GuiLanguageList selectedLanguagesList;
    GuiTextField field;
    private List<LanguageEntry> availableLanguages;
    private List<LanguageEntry> selectedLanguages;
    private List<LanguageEntry> availableLanguagesFiltered;
    private boolean changed;

    public GuiScreenLanguages(GuiScreen screen, GameSettings gameSettings, LanguageManager languageManager) {
        this.parentScreen = screen;
        this.manager = languageManager;
        this.gameSettings = gameSettings;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        buttonList.add(new GuiOptionButton(2, this.width / 2 - 154, this.height - 48, GameSettings.Options.FORCE_UNICODE_FONT, this.gameSettings.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
        buttonList.add(new GuiOptionButton(1, this.width / 2 + 4, this.height - 48, I18n.format("gui.done")));
        field = new GuiTextField(3, mc.fontRenderer, this.width / 4, 30, this.width / 2, 15);
        if (!this.changed) {
            this.availableLanguages = Lists.newArrayList();
            final String[] selected = ConfigurationRuntime.languageList;
            this.selectedLanguages = Lists.newArrayList(new LanguageEntry[selected.length]);
            for (Language language : manager.getLanguages()) {
                final int idx = ArrayUtils.indexOf(selected, language.getLanguageCode());
                if (idx != -1) {
                    this.selectedLanguages.set(idx, new LanguageEntry(language, !language.getLanguageCode().equals("en_us")));
                } else this.availableLanguages.add(new LanguageEntry(language));
            }
        }
        availableLanguagesFiltered = new ArrayList<>(availableLanguages);
        this.availableLanguagesList = new GuiLanguageList(this.mc, 200, this.height, this.availableLanguagesFiltered, "Available Languages");
        this.availableLanguagesList.setSlotXBoundsFromLeft(this.width / 2 - 4 - 200);
        this.availableLanguagesList.registerScrollButtons(7, 8);
        this.selectedLanguagesList = new GuiLanguageList(this.mc, 200, this.height, this.selectedLanguages, "Selected Languages");
        this.selectedLanguagesList.setSlotXBoundsFromLeft(this.width / 2 + 4);
        this.selectedLanguagesList.registerScrollButtons(7, 8);
        this.field.setFocused(true);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.selectedLanguagesList.handleMouseInput();
        this.availableLanguagesList.handleMouseInput();
    }

    public boolean hasResourcePackEntry(LanguageEntry langEntry) {
        return this.selectedLanguages.contains(langEntry);
    }

    public List<LanguageEntry> getListContaining(LanguageEntry langEntry) {
        return hasResourcePackEntry(langEntry) ? this.selectedLanguages : this.availableLanguages;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                if (this.changed) {
                    final ArrayList<String> languageNames = Lists.newArrayList();
                    for (LanguageEntry selectedLanguage : selectedLanguages) {
                        languageNames.add(selectedLanguage.language.getLanguageCode());
                    }
                    ConfigurationRuntime.languageList = languageNames.toArray(new String[0]);
//                    net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(net.minecraftforge.client.resource.VanillaResourceType.LANGUAGES);
                    manager.onResourceManagerReload(mc.getResourceManager());
                }
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (button.id == 2) {
                this.gameSettings.setOptionValue(((GuiOptionButton) button).getOption(), 1);
                button.displayString = gameSettings.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
                this.setWorldAndResolution(this.mc, i, j);
            }
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.availableLanguagesList.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectedLanguagesList.mouseClicked(mouseX, mouseY, mouseButton);
        this.field.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void refreshLanguages() {
        final String text = this.field.getText();
        availableLanguagesFiltered.clear();
        for (LanguageEntry langEntry : availableLanguages) {
            final Language lang = langEntry.language;
            if (Utils.anyMatch(text, lang.name, lang.region, lang.getLanguageCode()))
                availableLanguagesFiltered.add(langEntry);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.field.textboxKeyTyped(typedChar, keyCode);
        refreshLanguages();
    }

    /**
     * Called when a mouse button is released.
     */
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground(0);
        this.availableLanguagesList.drawScreen(mouseX, mouseY, partialTicks);
        this.selectedLanguagesList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("options.language"), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.fontRenderer, "(" + I18n.format("options.languageWarning") + ")", this.width / 2, this.height - 25, 8421504);
        field.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Marks the selected resource packs list as changed to trigger a resource reload when the screen is closed
     */
    public void markChanged() {
        this.changed = true;
    }

    public class LanguageEntry implements GuiListExtended.IGuiListEntry {
        private final boolean selectable;
        Language language;

        public LanguageEntry(Language language) {
            this.language = language;
            selectable = true;
        }

        public LanguageEntry(Language language, boolean selectable) {
            this.language = language;
            this.selectable = selectable;
        }

        protected boolean canMoveRight() {
            return !hasResourcePackEntry(this);
        }

        protected boolean canMoveLeft() {
            return hasResourcePackEntry(this);
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            isSelected &= selectable;
            String s = language.name;
            String s1 = language.region;
            int blockRight = x + 180;
            if (!selectable) {
                Gui.drawRect(x, y, blockRight, y + 32, 0x86000000);
            } else if (mc.gameSettings.touchscreen || isSelected) {
                mc.getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);
                Gui.drawRect(x, y, blockRight, y + 32, 0x57909090);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int j = mouseX - x;
                int k = mouseY - y;
                if (this.canMoveRight()) {
                    if (j < 32) {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    } else {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                } else {
                    if (this.canMoveLeft()) {
                        if (j < 16) {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                        } else {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                        }
                    }

                    if (this.canMoveUp()) {
                        if (j < 32 && j > 16 && k < 16) {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                        } else {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                        }
                    }

                    if (this.canMoveDown()) {
                        if (j < 32 && j > 16 && k > 16) {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                        } else {
                            Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                        }
                    }
                }
            }
            mc.fontRenderer.drawStringWithShadow(s, (float) (x + 32 + 2), (float) (y + 1), 16777215);
            List<String> list = mc.fontRenderer.listFormattedStringToWidth(s1, 157);
            for (int l = 0; l < 2 && l < list.size(); ++l) {
                mc.fontRenderer.drawStringWithShadow(list.get(l), (float) (x + 32 + 2), (float) (y + 12 + 10 * l), 8421504);
            }
        }

        protected boolean canMoveUp() {
            List<LanguageEntry> list = getListContaining(this);
            return list.indexOf(this) > 0;
        }

        protected boolean canMoveDown() {
            List<LanguageEntry> list = getListContaining(this);
            int i = list.indexOf(this);
            return i < list.size() - 2;
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (!selectable) return false;
            if (relativeX <= 32) {
                if (this.canMoveRight()) {
                    markChanged();
                    getListContaining(this).remove(this);
                    selectedLanguages.add(0, this);
                    refreshLanguages();
                    return true;
                }

                if (relativeX < 16 && this.canMoveLeft()) {
                    getListContaining(this).remove(this);
                    availableLanguages.add(0, this);
                    refreshLanguages();
                    markChanged();
                    return true;
                }

                if (relativeX > 16 && relativeY < 16 && this.canMoveUp()) {
                    List<LanguageEntry> list1 = getListContaining(this);
                    int k = list1.indexOf(this);
                    list1.remove(this);
                    list1.add(k - 1, this);
                    markChanged();
                    return true;
                }

                if (relativeX > 16 && relativeY > 16 && this.canMoveDown()) {
                    List<LanguageEntry> list = getListContaining(this);
                    int i = list.indexOf(this);
                    list.remove(this);
                    list.add(i + 1, this);
                    markChanged();
                    return true;
                }
            }

            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        }
    }
}
