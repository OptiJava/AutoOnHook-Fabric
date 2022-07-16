/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class CreativeInventoryScreen
extends AbstractInventoryScreen<CreativeScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final String TAB_TEXTURE_PREFIX = "textures/gui/container/creative_inventory/tab_";
    private static final String CUSTOM_CREATIVE_LOCK_KEY = "CustomCreativeLock";
    private static final int field_32337 = 5;
    private static final int field_32338 = 9;
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    static final SimpleInventory INVENTORY = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = new TranslatableText("inventory.binSlot");
    private static final int WHITE = 0xFFFFFF;
    private static int selectedTab = ItemGroup.BUILDING_BLOCKS.getIndex();
    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Map<Identifier, Tag<Item>> searchResultTags = Maps.newTreeMap();

    public CreativeInventoryScreen(PlayerEntity player) {
        super(new CreativeScreenHandler(player), player.getInventory(), LiteralText.EMPTY);
        player.currentScreenHandler = this.handler;
        this.passEvents = true;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (!this.client.interactionManager.hasCreativeInventory()) {
            this.client.setScreen(new InventoryScreen(this.client.player));
        } else if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    @Override
    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.setCursorToEnd();
            this.searchBox.setSelectionEnd(0);
        }
        boolean bl = actionType == SlotActionType.QUICK_MOVE;
        SlotActionType slotActionType = actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
        if (slot != null || selectedTab == ItemGroup.INVENTORY.getIndex() || actionType == SlotActionType.QUICK_CRAFT) {
            if (slot != null && !slot.canTakeItems(this.client.player)) {
                return;
            }
            if (slot == this.deleteItemSlot && bl) {
                for (int i = 0; i < this.client.player.playerScreenHandler.getStacks().size(); ++i) {
                    this.client.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
                }
            } else if (selectedTab == ItemGroup.INVENTORY.getIndex()) {
                if (slot == this.deleteItemSlot) {
                    ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                    ItemStack i = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
                    ItemStack itemStack = slot.getStack();
                    this.client.player.dropItem(i, true);
                    this.client.interactionManager.dropCreativeStack(i);
                    this.client.interactionManager.clickCreativeStack(itemStack, ((CreativeSlot)slot).slot.id);
                } else if (actionType == SlotActionType.THROW && !((CreativeScreenHandler)this.handler).getCursorStack().isEmpty()) {
                    this.client.player.dropItem(((CreativeScreenHandler)this.handler).getCursorStack(), true);
                    this.client.interactionManager.dropCreativeStack(((CreativeScreenHandler)this.handler).getCursorStack());
                    ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                } else {
                    this.client.player.playerScreenHandler.onSlotClick(slot == null ? slotId : ((CreativeSlot)slot).slot.id, button, actionType, this.client.player);
                    this.client.player.playerScreenHandler.sendContentUpdates();
                }
            } else if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                ItemStack i = ((CreativeScreenHandler)this.handler).getCursorStack();
                ItemStack itemStack = slot.getStack();
                if (actionType == SlotActionType.SWAP) {
                    if (!itemStack.isEmpty()) {
                        ItemStack itemStack2 = itemStack.copy();
                        itemStack2.setCount(itemStack2.getMaxCount());
                        this.client.player.getInventory().setStack(button, itemStack2);
                        this.client.player.playerScreenHandler.sendContentUpdates();
                    }
                    return;
                }
                if (actionType == SlotActionType.CLONE) {
                    if (((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && slot.hasStack()) {
                        ItemStack itemStack2 = slot.getStack().copy();
                        itemStack2.setCount(itemStack2.getMaxCount());
                        ((CreativeScreenHandler)this.handler).setCursorStack(itemStack2);
                    }
                    return;
                }
                if (actionType == SlotActionType.THROW) {
                    if (!itemStack.isEmpty()) {
                        ItemStack itemStack2 = itemStack.copy();
                        itemStack2.setCount(button == 0 ? 1 : itemStack2.getMaxCount());
                        this.client.player.dropItem(itemStack2, true);
                        this.client.interactionManager.dropCreativeStack(itemStack2);
                    }
                    return;
                }
                if (!i.isEmpty() && !itemStack.isEmpty() && i.isItemEqualIgnoreDamage(itemStack) && ItemStack.areNbtEqual(i, itemStack)) {
                    if (button == 0) {
                        if (bl) {
                            i.setCount(i.getMaxCount());
                        } else if (i.getCount() < i.getMaxCount()) {
                            i.increment(1);
                        }
                    } else {
                        i.decrement(1);
                    }
                } else if (itemStack.isEmpty() || !i.isEmpty()) {
                    if (button == 0) {
                        ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                    } else {
                        ((CreativeScreenHandler)this.handler).getCursorStack().decrement(1);
                    }
                } else {
                    ((CreativeScreenHandler)this.handler).setCursorStack(itemStack.copy());
                    i = ((CreativeScreenHandler)this.handler).getCursorStack();
                    if (bl) {
                        i.setCount(i.getMaxCount());
                    }
                }
            } else if (this.handler != null) {
                ItemStack i = slot == null ? ItemStack.EMPTY : ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                ((CreativeScreenHandler)this.handler).onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
                if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
                    for (int itemStack = 0; itemStack < 9; ++itemStack) {
                        this.client.interactionManager.clickCreativeStack(((CreativeScreenHandler)this.handler).getSlot(45 + itemStack).getStack(), 36 + itemStack);
                    }
                } else if (slot != null) {
                    ItemStack itemStack = ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                    this.client.interactionManager.clickCreativeStack(itemStack, slot.id - ((CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                    int itemStack2 = 45 + button;
                    if (actionType == SlotActionType.SWAP) {
                        this.client.interactionManager.clickCreativeStack(i, itemStack2 - ((CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                    } else if (actionType == SlotActionType.THROW && !i.isEmpty()) {
                        ItemStack itemStack3 = i.copy();
                        itemStack3.setCount(button == 0 ? 1 : itemStack3.getMaxCount());
                        this.client.player.dropItem(itemStack3, true);
                        this.client.interactionManager.dropCreativeStack(itemStack3);
                    }
                    this.client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        } else if (!((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
            if (button == 0) {
                this.client.player.dropItem(((CreativeScreenHandler)this.handler).getCursorStack(), true);
                this.client.interactionManager.dropCreativeStack(((CreativeScreenHandler)this.handler).getCursorStack());
                ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
            }
            if (button == 1) {
                ItemStack i = ((CreativeScreenHandler)this.handler).getCursorStack().split(1);
                this.client.player.dropItem(i, true);
                this.client.interactionManager.dropCreativeStack(i);
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    @Override
    protected void applyStatusEffectOffset() {
        int i = this.x;
        super.applyStatusEffectOffset();
        if (this.searchBox != null && this.x != i) {
            this.searchBox.setX(this.x + 82);
        }
    }

    @Override
    protected void init() {
        if (this.client.interactionManager.hasCreativeInventory()) {
            super.init();
            this.client.keyboard.setRepeatEvents(true);
            this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 80, this.textRenderer.fontHeight, new TranslatableText("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setDrawsBackground(false);
            this.searchBox.setVisible(false);
            this.searchBox.setEditableColor(0xFFFFFF);
            this.addSelectableChild(this.searchBox);
            int i = selectedTab;
            selectedTab = -1;
            this.setSelectedTab(ItemGroup.GROUPS[i]);
            this.client.player.playerScreenHandler.removeListener(this.listener);
            this.listener = new CreativeInventoryListener(this.client);
            this.client.player.playerScreenHandler.addListener(this.listener);
        } else {
            this.client.setScreen(new InventoryScreen(this.client.player));
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }
        this.client.keyboard.setRepeatEvents(false);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        }
        if (selectedTab != ItemGroup.SEARCH.getIndex()) {
            return false;
        }
        String string = this.searchBox.getText();
        if (this.searchBox.charTyped(chr, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.search();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (selectedTab != ItemGroup.SEARCH.getIndex()) {
            if (this.client.options.keyChat.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroup.SEARCH);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
        boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
        if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        }
        String string = this.searchBox.getText();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.search();
            }
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        ((CreativeScreenHandler)this.handler).itemList.clear();
        this.searchResultTags.clear();
        String string = this.searchBox.getText();
        if (string.isEmpty()) {
            for (Item item : Registry.ITEM) {
                item.appendStacks(ItemGroup.SEARCH, ((CreativeScreenHandler)this.handler).itemList);
            }
        } else {
            SearchableContainer<ItemStack> searchable;
            if (string.startsWith("#")) {
                string = string.substring(1);
                searchable = this.client.getSearchableContainer(SearchManager.ITEM_TAG);
                this.searchForTags(string);
            } else {
                searchable = this.client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
            }
            ((CreativeScreenHandler)this.handler).itemList.addAll(searchable.findAll(string.toLowerCase(Locale.ROOT)));
        }
        this.scrollPosition = 0.0f;
        ((CreativeScreenHandler)this.handler).scrollItems(0.0f);
    }

    private void searchForTags(String id2) {
        Object string;
        Predicate<Identifier> predicate;
        int i = id2.indexOf(58);
        if (i == -1) {
            predicate = id -> id.getPath().contains(id2);
        } else {
            string = id2.substring(0, i).trim();
            String string2 = id2.substring(i + 1).trim();
            predicate = arg_0 -> CreativeInventoryScreen.method_15874((String)string, string2, arg_0);
        }
        string = ItemTags.getTagGroup();
        string.getTagIds().stream().filter(predicate).forEach(arg_0 -> this.method_15873((TagGroup)string, arg_0));
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
        if (itemGroup.shouldRenderName()) {
            RenderSystem.disableBlend();
            this.textRenderer.draw(matrices, itemGroup.getTranslationKey(), 8.0f, 6.0f, 0x404040);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double)this.x;
            double e = mouseY - (double)this.y;
            for (ItemGroup itemGroup : ItemGroup.GROUPS) {
                if (!this.isClickInTab(itemGroup, d, e)) continue;
                return true;
            }
            if (selectedTab != ItemGroup.INVENTORY.getIndex() && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double)this.x;
            double e = mouseY - (double)this.y;
            this.scrolling = false;
            for (ItemGroup itemGroup : ItemGroup.GROUPS) {
                if (!this.isClickInTab(itemGroup, d, e)) continue;
                this.setSelectedTab(itemGroup);
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hasScrollbar() {
        return selectedTab != ItemGroup.INVENTORY.getIndex() && ItemGroup.GROUPS[selectedTab].hasScrollbar() && ((CreativeScreenHandler)this.handler).shouldShowScrollbar();
    }

    private void setSelectedTab(ItemGroup group) {
        Object itemStack;
        int k;
        int j;
        Object hotbarStorage;
        int i = selectedTab;
        selectedTab = group.getIndex();
        this.cursorDragSlots.clear();
        ((CreativeScreenHandler)this.handler).itemList.clear();
        if (group == ItemGroup.HOTBAR) {
            hotbarStorage = this.client.getCreativeHotbarStorage();
            for (j = 0; j < 9; ++j) {
                HotbarStorageEntry hotbarStorageEntry = ((HotbarStorage)hotbarStorage).getSavedHotbar(j);
                if (hotbarStorageEntry.isEmpty()) {
                    for (k = 0; k < 9; ++k) {
                        if (k == j) {
                            itemStack = new ItemStack(Items.PAPER);
                            ((ItemStack)itemStack).getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
                            Text text = this.client.options.keysHotbar[j].getBoundKeyLocalizedText();
                            Text text2 = this.client.options.keySaveToolbarActivator.getBoundKeyLocalizedText();
                            ((ItemStack)itemStack).setCustomName(new TranslatableText("inventory.hotbarInfo", text2, text));
                            ((CreativeScreenHandler)this.handler).itemList.add((ItemStack)itemStack);
                            continue;
                        }
                        ((CreativeScreenHandler)this.handler).itemList.add(ItemStack.EMPTY);
                    }
                    continue;
                }
                ((CreativeScreenHandler)this.handler).itemList.addAll(hotbarStorageEntry);
            }
        } else if (group != ItemGroup.SEARCH) {
            group.appendStacks(((CreativeScreenHandler)this.handler).itemList);
        }
        if (group == ItemGroup.INVENTORY) {
            hotbarStorage = this.client.player.playerScreenHandler;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((CreativeScreenHandler)this.handler).slots);
            }
            ((CreativeScreenHandler)this.handler).slots.clear();
            for (j = 0; j < ((ScreenHandler)hotbarStorage).slots.size(); ++j) {
                int hotbarStorageEntry;
                if (j >= 5 && j < 9) {
                    int itemStack2 = j - 5;
                    text = itemStack2 / 2;
                    text2 = itemStack2 % 2;
                    hotbarStorageEntry = 54 + text * 54;
                    k = 6 + text2 * 27;
                } else if (j >= 0 && j < 5) {
                    hotbarStorageEntry = -2000;
                    k = -2000;
                } else if (j == 45) {
                    hotbarStorageEntry = 35;
                    k = 20;
                } else {
                    int itemStack3 = j - 9;
                    text = itemStack3 % 9;
                    text2 = itemStack3 / 9;
                    hotbarStorageEntry = 9 + text * 18;
                    k = j >= 36 ? 112 : 54 + text2 * 18;
                }
                itemStack = new CreativeSlot(((ScreenHandler)hotbarStorage).slots.get(j), j, hotbarStorageEntry, k);
                ((CreativeScreenHandler)this.handler).slots.add(itemStack);
            }
            this.deleteItemSlot = new Slot(INVENTORY, 0, 173, 112);
            ((CreativeScreenHandler)this.handler).slots.add(this.deleteItemSlot);
        } else if (i == ItemGroup.INVENTORY.getIndex()) {
            ((CreativeScreenHandler)this.handler).slots.clear();
            ((CreativeScreenHandler)this.handler).slots.addAll(this.slots);
            this.slots = null;
        }
        if (this.searchBox != null) {
            if (group == ItemGroup.SEARCH) {
                this.searchBox.setVisible(true);
                this.searchBox.setFocusUnlocked(false);
                this.searchBox.setTextFieldFocused(true);
                if (i != group.getIndex()) {
                    this.searchBox.setText("");
                }
                this.search();
            } else {
                this.searchBox.setVisible(false);
                this.searchBox.setFocusUnlocked(true);
                this.searchBox.setTextFieldFocused(false);
                this.searchBox.setText("");
            }
        }
        this.scrollPosition = 0.0f;
        ((CreativeScreenHandler)this.handler).scrollItems(0.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.hasScrollbar()) {
            return false;
        }
        int i = (((CreativeScreenHandler)this.handler).itemList.size() + 9 - 1) / 9 - 5;
        this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
        ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
        return true;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(ItemGroup.GROUPS[selectedTab], mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + 112;
            this.scrollPosition = ((float)mouseY - (float)i - 7.5f) / ((float)(j - i) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        for (ItemGroup itemGroup : ItemGroup.GROUPS) {
            if (this.renderTabTooltipIfHovered(matrices, itemGroup, mouseX, mouseY)) break;
        }
        if (this.deleteItemSlot != null && selectedTab == ItemGroup.INVENTORY.getIndex() && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrices, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (selectedTab == ItemGroup.SEARCH.getIndex()) {
            Map<Enchantment, Integer> map;
            List<Text> list = stack.getTooltip(this.client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
            ArrayList<Text> list2 = Lists.newArrayList(list);
            Item item = stack.getItem();
            ItemGroup itemGroup = item.getGroup();
            if (itemGroup == null && stack.isOf(Items.ENCHANTED_BOOK) && (map = EnchantmentHelper.get(stack)).size() == 1) {
                Enchantment enchantment = map.keySet().iterator().next();
                for (ItemGroup itemGroup2 : ItemGroup.GROUPS) {
                    if (!itemGroup2.containsEnchantments(enchantment.type)) continue;
                    itemGroup = itemGroup2;
                    break;
                }
            }
            this.searchResultTags.forEach((id, tag) -> {
                if (stack.isIn((Tag<Item>)tag)) {
                    list2.add(1, new LiteralText("#" + id).formatted(Formatting.DARK_PURPLE));
                }
            });
            if (itemGroup != null) {
                list2.add(1, itemGroup.getTranslationKey().shallowCopy().formatted(Formatting.BLUE));
            }
            this.renderTooltip(matrices, list2, stack.getTooltipData(), x, y);
        } else {
            super.renderTooltip(matrices, stack, x, y);
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
        for (ItemGroup itemGroup2 : ItemGroup.GROUPS) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (itemGroup2.getIndex() == selectedTab) continue;
            this.renderTabIcon(matrices, itemGroup2);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier(TAB_TEXTURE_PREFIX + itemGroup.getTexture()));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.x + 175;
        int j = this.y + 18;
        int k = j + 112;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        if (itemGroup.hasScrollbar()) {
            this.drawTexture(matrices, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
        }
        this.renderTabIcon(matrices, itemGroup);
        if (itemGroup == ItemGroup.INVENTORY) {
            InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, this.x + 88 - mouseX, this.y + 45 - 30 - mouseY, this.client.player);
        }
    }

    protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
        int i = group.getColumn();
        int j = 28 * i;
        int k = 0;
        if (group.isSpecial()) {
            j = this.backgroundWidth - 28 * (6 - i) + 2;
        } else if (i > 0) {
            j += i;
        }
        k = group.isTopRow() ? (k -= 32) : (k += this.backgroundHeight);
        return mouseX >= (double)j && mouseX <= (double)(j + 28) && mouseY >= (double)k && mouseY <= (double)(k + 32);
    }

    protected boolean renderTabTooltipIfHovered(MatrixStack matrices, ItemGroup group, int mouseX, int mouseY) {
        int i = group.getColumn();
        int j = 28 * i;
        int k = 0;
        if (group.isSpecial()) {
            j = this.backgroundWidth - 28 * (6 - i) + 2;
        } else if (i > 0) {
            j += i;
        }
        k = group.isTopRow() ? (k -= 32) : (k += this.backgroundHeight);
        if (this.isPointWithinBounds(j + 3, k + 3, 23, 27, mouseX, mouseY)) {
            this.renderTooltip(matrices, group.getTranslationKey(), mouseX, mouseY);
            return true;
        }
        return false;
    }

    protected void renderTabIcon(MatrixStack matrices, ItemGroup group) {
        boolean bl = group.getIndex() == selectedTab;
        boolean bl2 = group.isTopRow();
        int i = group.getColumn();
        int j = i * 28;
        int k = 0;
        int l = this.x + 28 * i;
        int m = this.y;
        int n = 32;
        if (bl) {
            k += 32;
        }
        if (group.isSpecial()) {
            l = this.x + this.backgroundWidth - 28 * (6 - i);
        } else if (i > 0) {
            l += i;
        }
        if (bl2) {
            m -= 28;
        } else {
            k += 64;
            m += this.backgroundHeight - 4;
        }
        this.drawTexture(matrices, l, m, j, k, 28, 32);
        this.itemRenderer.zOffset = 100.0f;
        int n2 = bl2 ? 1 : -1;
        ItemStack itemStack = group.getIcon();
        this.itemRenderer.renderInGuiWithOverrides(itemStack, l += 6, m += 8 + n2);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, l, m);
        this.itemRenderer.zOffset = 0.0f;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
        ClientPlayerEntity clientPlayerEntity = client.player;
        HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
        HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
        if (restore) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                ItemStack itemStack = ((ItemStack)hotbarStorageEntry.get(i)).copy();
                clientPlayerEntity.getInventory().setStack(i, itemStack);
                client.interactionManager.clickCreativeStack(itemStack, 36 + i);
            }
            clientPlayerEntity.playerScreenHandler.sendContentUpdates();
        } else if (save) {
            for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                hotbarStorageEntry.set(i, clientPlayerEntity.getInventory().getStack(i).copy());
            }
            Text i = client.options.keysHotbar[index].getBoundKeyLocalizedText();
            Text itemStack = client.options.keyLoadToolbarActivator.getBoundKeyLocalizedText();
            client.inGameHud.setOverlayMessage(new TranslatableText("inventory.hotbarSaved", itemStack, i), false);
            hotbarStorage.save();
        }
    }

    private /* synthetic */ void method_15873(TagGroup tagGroup, Identifier id) {
        this.searchResultTags.put(id, tagGroup.getTag(id));
    }

    private static /* synthetic */ boolean method_15874(String string, String string2, Identifier id) {
        return id.getNamespace().contains(string) && id.getPath().contains(string2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class CreativeScreenHandler
    extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;

        public CreativeScreenHandler(PlayerEntity player) {
            super(null, 0);
            int i;
            this.parent = player.playerScreenHandler;
            PlayerInventory playerInventory = player.getInventory();
            for (i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }
            for (i = 0; i < 9; ++i) {
                this.addSlot(new Slot(playerInventory, i, 9 + i * 18, 112));
            }
            this.scrollItems(0.0f);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        public void scrollItems(float position) {
            int i = (this.itemList.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(position * (float)i) + 0.5);
            if (j < 0) {
                j = 0;
            }
            for (int k = 0; k < 5; ++k) {
                for (int l = 0; l < 9; ++l) {
                    int m = l + (k + j) * 9;
                    if (m >= 0 && m < this.itemList.size()) {
                        INVENTORY.setStack(l + k * 9, this.itemList.get(m));
                        continue;
                    }
                    INVENTORY.setStack(l + k * 9, ItemStack.EMPTY);
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        @Override
        public ItemStack transferSlot(PlayerEntity player, int index) {
            Slot slot;
            if (index >= this.slots.size() - 9 && index < this.slots.size() && (slot = (Slot)this.slots.get(index)) != null && slot.hasStack()) {
                slot.setStack(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != INVENTORY;
        }

        @Override
        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != INVENTORY;
        }

        @Override
        public ItemStack getCursorStack() {
            return this.parent.getCursorStack();
        }

        @Override
        public void setCursorStack(ItemStack stack) {
            this.parent.setCursorStack(stack);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CreativeSlot
    extends Slot {
        final Slot slot;

        public CreativeSlot(Slot slot, int invSlot, int x, int y) {
            super(slot.inventory, invSlot, x, y);
            this.slot = slot;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.slot.onTakeItem(player, stack);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.slot.canInsert(stack);
        }

        @Override
        public ItemStack getStack() {
            return this.slot.getStack();
        }

        @Override
        public boolean hasStack() {
            return this.slot.hasStack();
        }

        @Override
        public void setStack(ItemStack stack) {
            this.slot.setStack(stack);
        }

        @Override
        public void markDirty() {
            this.slot.markDirty();
        }

        @Override
        public int getMaxItemCount() {
            return this.slot.getMaxItemCount();
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return this.slot.getMaxItemCount(stack);
        }

        @Override
        @Nullable
        public Pair<Identifier, Identifier> getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        @Override
        public ItemStack takeStack(int amount) {
            return this.slot.takeStack(amount);
        }

        @Override
        public boolean isEnabled() {
            return this.slot.isEnabled();
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.slot.canTakeItems(playerEntity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LockableSlot
    extends Slot {
        public LockableSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            if (super.canTakeItems(playerEntity) && this.hasStack()) {
                return this.getStack().getSubNbt(CreativeInventoryScreen.CUSTOM_CREATIVE_LOCK_KEY) == null;
            }
            return !this.hasStack();
        }
    }
}

