/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final String RECIPES_KEY = "Recipes";
    private static final Logger LOGGER = LogManager.getLogger();

    public KnowledgeBookItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound nbtCompound = itemStack.getNbt();
        if (!user.getAbilities().creativeMode) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        if (nbtCompound == null || !nbtCompound.contains(RECIPES_KEY, 9)) {
            LOGGER.error("Tag not valid: {}", (Object)nbtCompound);
            return TypedActionResult.fail(itemStack);
        }
        if (!world.isClient) {
            NbtList nbtList = nbtCompound.getList(RECIPES_KEY, 8);
            ArrayList<Recipe<?>> list = Lists.newArrayList();
            RecipeManager recipeManager = world.getServer().getRecipeManager();
            for (int i = 0; i < nbtList.size(); ++i) {
                String string = nbtList.getString(i);
                Optional<Recipe<?>> optional = recipeManager.get(new Identifier(string));
                if (!optional.isPresent()) {
                    LOGGER.error("Invalid recipe: {}", (Object)string);
                    return TypedActionResult.fail(itemStack);
                }
                list.add(optional.get());
            }
            user.unlockRecipes(list);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}

