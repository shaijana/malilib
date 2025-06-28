package fi.dy.masa.malilib.util.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.mixin.recipe.IMixinClientRecipeBook;
import fi.dy.masa.malilib.mixin.recipe.IMixinIngredient;
import fi.dy.masa.malilib.util.log.AnsiLogger;

/**
 * Transferred from ItemScroller & Expanded functionality
 */
public class RecipeBookUtils
{
    private static final AnsiLogger LOGGER = new AnsiLogger(RecipeBookUtils.class, MaLiLibReference.DEBUG_MODE, true);

    /**
     * Enables Debug mode.
     * @param toggle ()
     */
    public static void toggleDebugLog(boolean toggle)
    {
        LOGGER.toggleDebug(toggle);
    }

    /**
     * Enables Debug Ansi Colors.
     * @param toggle ()
     */
    public static void toggleAnsiColorLog(boolean toggle)
    {
        LOGGER.toggleAnsiColor(toggle);
    }

    /**
     * Get RecipeBookCategory as a string
     * @param category ()
     * @return ()
     */
    public static String getRecipeCategoryId(RecipeBookCategory category)
    {
        RegistryKey<RecipeBookCategory> key = Registries.RECIPE_BOOK_CATEGORY.getKey(category).orElse(null);

        if (key != null)
        {
            return key.getValue().toString();
        }

        return "";
    }

    /**
     * Get RecipeBookCategory from a string
     * @param id ()
     * @return ()
     */
    public static @Nullable RecipeBookCategory getRecipeCategoryFromId(String id)
    {
        RegistryEntry.Reference<RecipeBookCategory> catReference = Registries.RECIPE_BOOK_CATEGORY.getEntry(Identifier.tryParse(id)).orElse(null);

        if (catReference != null && catReference.hasKeyAndValue())
        {
            return catReference.value();
        }

        return null;
    }

    /**
     * Get all matching RecipeBook Display Entries for a Crafting Result, and filter by Recipe Types.
     * @param result (Crafting Result Stack)
     * @param types (Recipe Type list)
     * @return (List of all matching recipe's and their corresponding NetworkRecipeId)
     */
    public static List<Pair<NetworkRecipeId, RecipeDisplayEntry>> getDisplayEntryFromRecipeBook(ItemStack result, List<Type> types)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null || mc.player == null)
        {
            return null;
        }

        ClientRecipeBook recipeBook = mc.player.getRecipeBook();
        Map<NetworkRecipeId, RecipeDisplayEntry> recipeMap = ((IMixinClientRecipeBook) recipeBook).malilib_getRecipeMap();
        List<Pair<NetworkRecipeId, RecipeDisplayEntry>> list = new ArrayList<>();

        for (NetworkRecipeId id : recipeMap.keySet())
        {
            RecipeDisplayEntry entry = recipeMap.get(id);
            List<ItemStack> stacks = entry.getStacks(SlotDisplayContexts.createParameters(mc.world));

            if (stacks.isEmpty())
            {
                continue;
            }

            if (areStacksEqual(result, stacks.getFirst()) && entry.craftingRequirements().isPresent() &&
                types.contains(Type.fromRecipeDisplay(entry.display())))
            {
                list.add(Pair.of(id, entry));
            }
        }

        return list;
    }

    /**
     * Match the provided RecipeBookEntry to the crafting result, and input stacks, and filter by Recipe Type.
     * @param result (Crafting Result Stack)
     * @param recipeStacks (Crafting Input Stacks (Shaped requires Empty slots in this))
     * @param entry (RecipeDisplayEntry to match)
     * @param allowed (List of allowed Recipe Types.)
     * @param mc ()
     * @return (True|False)
     */
    public static boolean matchClientRecipeBookEntry(ItemStack result, List<ItemStack> recipeStacks, RecipeDisplayEntry entry, List<Type> allowed, MinecraftClient mc)
    {
        if (mc.world == null || result.isEmpty())
        {
            return false;
        }

        // Mojang breaks their own player recipe book.  Verifying the Category here can cause problems.
        /*
        if (this.getRecipeCategory() != null && !entry.category().equals(this.getRecipeCategory()))
        {
            return false;
        }
         */
        List<ItemStack> stacks = entry.getStacks(SlotDisplayContexts.createParameters(mc.world));

        LOGGER.debug("matchClientRecipeBookEntry() --> [{}] vs [{}]", recipeStacks, stacks.getFirst().toString());

        if (stacks.isEmpty())
        {
            // And why would that be? *cries without essential data*
            MaLiLib.LOGGER.warn("matchClientRecipeBookEntry(): Failed receiving crafting stacks for NetworkRecipeId: [{}] -- is it even a valid recipe?", entry.id().index());
            return false;
        }

        if (areStacksEqual(result, stacks.getFirst()))
        {
            if (entry.craftingRequirements().isPresent())
            {
                return compareStacksAndIngredients(recipeStacks,
                                                   entry.craftingRequirements().get(),
                                                   Type.fromRecipeDisplay(entry.display()),
                                                   allowed
                );
            }

            return true;
        }

        return false;
    }

    /**
     * Match a list of Crafting stacks to a list of Crafting Ingredients, filtered by a list of Recipe Types.
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @param type (RecipeDisplayEntry type)
     * @param allowed (List of allowed recipe types)
     * @return (True|False)
     */
    public static boolean compareStacksAndIngredients(List<ItemStack> left, List<Ingredient> right, Type type, List<Type> allowed)
    {
        if (left.isEmpty() || right.isEmpty())
        {
            LOGGER.debug("compareStacksAndIngredients() --> EMPTY!!!");
            return false;
        }

        LOGGER.debug("compareStacksAndIngredients() Type: [{}] --> START", type.toString());
        if (LOGGER.isDebug())
        {
            dumpStacks(left, "LF");
            dumpIngs(right, "RT");
        }

        if (type == Type.SHAPELESS && allowed.contains(type))
        {
            return compareShapelessRecipe(left, right);
        }
        else if (type == Type.SHAPED && allowed.contains(type))
        {
            return compareShapedRecipe(left, right);
        }
        else if (type == Type.STONECUTTER && allowed.contains(type))
        {
            // TODO --> Check functionality
            return compareStonecutterRecipe(left, right);
        }
        else if (type == Type.FURNACE && allowed.contains(type))
        {
            // TODO --> Add Fuel types
            return compareFurnaceRecipe(left, right);
        }
        else if (type == Type.SMITHING && allowed.contains(type))
        {
            // TODO --> Add template, base, addition types
            return compareSmithingRecipe(left, right);
        }

        // Other recipe type
        return false;
    }

    /**
     * Compare a Shaped Recipe Input to a List of Ingredients
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @return (True|False)
     */
    public static boolean compareShapedRecipe(List<ItemStack> left, List<Ingredient> right)
    {
        LOGGER.debug("compareShapedRecipe() --> size left [{}], right [{}]\n", left.size(), right.size());
        int lPos = 0;

        for (int i = 0; i < right.size(); i++)
        {
            ItemStack lStack = left.get(lPos);

            while (lStack.isEmpty())
            {
                lPos++;

                if (lPos < 9)
                {
                    lStack = left.get(lPos);
                    LOGGER.debug(" compareShapedRecipe() [{}] left [{}] (Advance Left), right [{}]", lPos, lStack.toString(), i);
                }
                else
                {
                    break;
                }
            }

            if (!checkMatchingItemsEach(lStack, lPos, i, right.get(i)))
            {
                LOGGER.debug(" FAIL (Shaped)");
                return false;
            }

            lPos++;
        }

        LOGGER.debug(" PASS (Shaped)");
        return true;
    }

    /**
     * Compare a Shapeless Recipe Input to a List of Ingredients
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @return (True|False)
     */
    public static boolean compareShapelessRecipe(List<ItemStack> left, List<Ingredient> right)
    {
        LOGGER.debug("compareShapelessRecipe() --> size left [{}], right [{}]", left.size(), right.size());

        for (int i = 0; i < left.size(); i++)
        {
            ItemStack lStack = left.get(i);
            boolean pass = false;

            LOGGER.debug(" compareShapelessRecipe() [{}] left [{}] -->", i, lStack.toString());
            if (lStack.isEmpty()) continue;

            for (int rPos = 0; rPos < right.size(); rPos++)
            {
                if (checkMatchingItemsEach(lStack, i, rPos, right.get(rPos)))
                {
                    LOGGER.debug(" PASS-EACH");
                    pass = true;
                }
            }

            if (!pass)
            {
                LOGGER.debug(" FAIL (Shapeless)");
                return false;
            }
        }

        LOGGER.debug(" PASS (Shapeless)");
        return true;
    }

    /**
     * Compare a Stonecutter Recipe Input to a List of Ingredients
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @return (True|False)
     */
    @ApiStatus.Experimental
    public static boolean compareStonecutterRecipe(List<ItemStack> left, List<Ingredient> right)
    {
        LOGGER.debug("compareStonecutterRecipe() --> size left [{}], right [{}]", left.size(), right.size());

        for (int i = 0; i < left.size(); i++)
        {
            ItemStack lStack = left.get(i);
            boolean pass = false;

            LOGGER.debug(" compareStonecutterRecipe() [{}] left [{}] -->", i, lStack.toString());
            if (lStack.isEmpty()) continue;

            for (int rPos = 0; rPos < right.size(); rPos++)
            {
                if (checkMatchingItemsEach(lStack, i, rPos, right.get(rPos)))
                {
                    LOGGER.debug(" PASS-EACH");
                    pass = true;
                }
            }

            if (!pass)
            {
                LOGGER.debug(" FAIL (Stonecutter)");
                return false;
            }
        }

        LOGGER.debug(" PASS (Stonecutter)");
        return true;
    }

    /**
     * Compare a Furnace Recipe Input to a List of Ingredients
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @return (True|False)
     */
    @ApiStatus.Experimental
    public static boolean compareFurnaceRecipe(List<ItemStack> left, List<Ingredient> right)
    {
        LOGGER.debug("compareFurnaceRecipe() --> size left [{}], right [{}]", left.size(), right.size());

        for (int i = 0; i < left.size(); i++)
        {
            ItemStack lStack = left.get(i);
            boolean pass = false;

            LOGGER.debug(" compareFurnaceRecipe() [{}] left [{}] -->", i, lStack.toString());
            if (lStack.isEmpty()) continue;

            for (int rPos = 0; rPos < right.size(); rPos++)
            {
                if (checkMatchingItemsEach(lStack, i, rPos, right.get(rPos)))
                {
                    LOGGER.debug(" PASS-EACH");
                    pass = true;
                }
            }

            if (!pass)
            {
                LOGGER.debug(" FAIL (Furnace)");
                return false;
            }
        }

        LOGGER.debug(" PASS (Furnace)");
        return true;
    }

    /**
     * Compare a Smithing Recipe Input to a List of Ingredients
     * @param left (Crafting Input Stacks)
     * @param right (Crafting Recipe Ingredients (Each Ingredient contains a list of possible inputs))
     * @return (True|False)
     */

    @ApiStatus.Experimental
    public static boolean compareSmithingRecipe(List<ItemStack> left, List<Ingredient> right)
    {
        LOGGER.debug("compareSmithingRecipe() --> size left [{}], right [{}]", left.size(), right.size());

        for (int i = 0; i < left.size(); i++)
        {
            ItemStack lStack = left.get(i);
            boolean pass = false;

            LOGGER.debug(" compareSmithingRecipe() [{}] left [{}] -->", i, lStack.toString());
            if (lStack.isEmpty()) continue;

            for (int rPos = 0; rPos < right.size(); rPos++)
            {
                if (checkMatchingItemsEach(lStack, i, rPos, right.get(rPos)))
                {
                    LOGGER.debug(" PASS-EACH");
                    pass = true;
                }
            }

            if (!pass)
            {
                LOGGER.debug(" FAIL (Smithing)");
                return false;
            }
        }

        LOGGER.debug(" PASS (Smithing)");
        return true;
    }

    private static boolean checkMatchingItemsEach(ItemStack lStack, int lPos, int i, Ingredient ri)
    {
        List<RegistryEntry<Item>> rItems = ((IMixinIngredient) (Object) ri).malilib_getEntries().stream().toList();

        for (RegistryEntry<Item> rItem : rItems)
        {
            LOGGER.debug(" checkMatchingItemsEach() [{}] left [{}] / [{}] right [{}] -->", lPos, lStack, i, rItem.getIdAsString());

            if (ri.test(lStack))
            {
                LOGGER.debug(" valid (Test test)");
                return true;
            }
            else if (areStacksEqual(lStack, new ItemStack(rItem)))
            {
                LOGGER.debug(" valid (Stack test)");
                return true;
            }
        }

        LOGGER.debug(" !not valid (Default)");
        return false;
    }

    /**
     * Compare two item stacks, and return if they are equal.  This method ignores Components, but also considers stack sizes.
     * @param left (Left Side)
     * @param right (Right Side)
     * @return (True|False)
     */
    public static boolean areStacksEqual(ItemStack left, ItemStack right)
    {
        return ItemStack.areItemsEqual(left, right) && left.getCount() == right.getCount();
    }

    private static void dumpStacks(List<ItemStack> stacks, String side)
    {
        int i = 0;

        LOGGER.info("DUMP [{}] -->", side);

        for (ItemStack stack : stacks)
        {
            LOGGER.info(" {}[{}] // [{}]", side, i, stack.toString());
            i++;
        }

        LOGGER.info("DUMP END [{}]\n", side);
    }

    private static void dumpIngs(List<Ingredient> ings, String side)
    {
        int i = 0;

        LOGGER.info("DUMP [{}] -->", side);

        for (Ingredient ing : ings)
        {
            List<RegistryEntry<Item>> items = ((IMixinIngredient) (Object) ing).malilib_getEntries().stream().toList();
            List<String> list = new ArrayList<>();

            for (RegistryEntry<Item> item : items)
            {
                list.add(item.getIdAsString());
            }

            LOGGER.info(" {}[{}] // {}", list.toString());
            i++;
        }

        LOGGER.info("DUMP END [{}]", side);
    }

    /**
     * Crafting Recipe Types -- This provides an easier way to filter and organize Recipe Book Display
     * results by Crafting Type; without the complexity of the Vanilla methods for doing this.
     */
    public enum Type
    {
        FURNACE,
        SHAPED,
        SHAPELESS,
        SMITHING,
        STONECUTTER,
        UNKNOWN;

        public static Type fromRecipeDisplay(RecipeDisplay type)
        {
            return switch (type)
            {
                case FurnaceRecipeDisplay ignored -> FURNACE;
                case ShapelessCraftingRecipeDisplay ignored -> SHAPELESS;
                case ShapedCraftingRecipeDisplay ignored -> SHAPED;
                case SmithingRecipeDisplay ignored -> SMITHING;
                case StonecutterRecipeDisplay ignored -> STONECUTTER;
                case null, default -> UNKNOWN;
            };
        }

        public static @Nullable Type matchFromString(String input)
        {
            for (Type type : values())
            {
                if (type.name().equalsIgnoreCase(input))
                {
                    return type;
                }
            }

            return null;
        }
    }
}
