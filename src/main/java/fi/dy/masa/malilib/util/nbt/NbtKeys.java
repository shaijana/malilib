package fi.dy.masa.malilib.util.nbt;

/**
 * NBT Tag Constants Collection.
 */
public class NbtKeys
{
    // Generic
    public static final String ID                  = "id";
    public static final String UUID                = "UUID";
    public static final String COMPONENTS          = "components";
    public static final String AGE                 = "Age";
    public static final String POS                 = "Pos";

    // Inventory / Single items
    public static final String ITEMS               = "Items";
    public static final String INVENTORY           = "Inventory";
    public static final String ITEM                = "item";
    public static final String ITEM_2              = "Item";
    public static final String SLOT                = "Slot";
    public static final String COUNT               = "count";
    public static final String ENDER_ITEMS         = "EnderItems";

    // Block Entity
    public static final String DISABLED_SLOTS      = "disabled_slots";
    public static final String PRIMARY_EFFECT      = "primary_effect";
    public static final String SECONDARY_EFFECT    = "secondary_effect";
    public static final String FLOWER              = "flower_pos";
    public static final String BEES                = "bees";
    public static final String OUTPUT_SIGNAL       = "OutputSignal";
    public static final String VIBRATION           = "last_vibration_frequency";
    public static final String LISTENER            = "listener";
    public static final String EXIT                = "exit_portal";
    public static final String FRONT_TEXT          = "front_text";
    public static final String BACK_TEXT           = "back_text";
    public static final String WAXED               = "is_waxed";
    public static final String BOOK                = "Book";
    public static final String PAGE                = "Page";
    public static final String RECORD              = "RecordItem";
    public static final String SKULL_NAME          = "custom_name";
    public static final String NOTE                = "note_block_sound";
    public static final String PROFILE             = "profile";
    public static final String RECIPES_USED        = "RecipesUsed";
    public static final String COOK_TIME_SPENT     = "cooking_time_spent";
    public static final String COOK_TIME_TOTAL     = "cooking_total_time";
    public static final String BURN_TIME           = "lit_time_remaining";
    public static final String BURN_TIME_TOTAL     = "lit_total_time";

    // Entity
    public static final String ATTRIB              = "attributes";
    public static final String EFFECTS             = "active_effects";
    public static final String CUSTOM_NAME         = "CustomName";
    public static final String HEALTH              = "Health";
    public static final String HAND_ITEMS          = "HandItems";
    public static final String ARMOR_ITEMS         = "ArmorItems";
    public static final String BODY_ARMOR          = "body_armor_item";
    public static final String SADDLE              = "SaddleItem";
    public static final String OWNER               = "Owner";
    public static final String FORCED_AGE          = "ForcedAge";
    public static final String OFFERS              = "Offers";
    public static final String VILLAGER            = "VillagerData";
    public static final String ZOMBIE_CONVERSION   = "ConversionTime";
    public static final String CONVERSION_PLAYER   = "ConversionPlayer";
    public static final String DROWNED_CONVERSION  = "DrownedConversionTime";
    public static final String IN_WATER            = "InWaterTime";
    public static final String STRAY_CONVERSION    = "StrayConversionTime";
    public static final String LEASH               = "leash";
    public static final String MAIN_GENE           = "MainGene";
    public static final String HIDDEN_GENE         = "HiddenGene";
    public static final String ITEM_ROTATION       = "ItemRotation";
    public static final String FACING              = "facing";
    public static final String FACING_2            = "Facing";
    public static final String VARIANT             = "variant";
    public static final String VARIANT_2           = "Variant";
    public static final String COLLAR              = "CollarColor";
    public static final String COLOR               = "Color";
    public static final String BUCKET_VARIANT      = "BucketVariantTag";
    public static final String RABBIT_TYPE         = "RabbitType";
    public static final String STRENGTH            = "Strength";
    public static final String EXP_LEVEL           = "XpLevel";
    public static final String EXP_TOTAL           = "XpTotal";
    public static final String EXP_PROGRESS        = "XpP";
    public static final String FOOD_LEVEL          = "foodLevel";
    public static final String FOOD_TIMER          = "foodTickTimer";
    public static final String FOOD_SATURATION     = "foodSaturationLevel";
    public static final String FOOD_EXHAUSTION     = "foodExhaustionLevel";
    public static final String RECIPE_BOOK         = "recipeBook";
    public static final String RECIPES             = "recipes";
    public static final String DISPLAYED           = "toBeDisplayed";

    // Other/Outdated Tags (There are tons more not listed)
    // 24w09a (Data Components)
    public static final String OLD_TAGS            = "tags";
    public static final String OLD_COUNT           = "Count";
    public static final String OLD_DISPLAY         = "display";
    public static final String OLD_BLOCK_ENTITY    = "BlockEntityTag";
    public static final String OLD_BLOCK_STATE     = "BlockStateTag";
    public static final String OLD_ENTITY          = "EntityTag";
    public static final String OLD_DAMAGE          = "Damage";
    public static final String OLD_REPAIR          = "RepairCost";
    public static final String OLD_UNBREAKABLE     = "Unbreakable";
    public static final String OLD_BEES            = "Bees";
    public static final String OLD_SKULL_OWNER     = "SkullOwner";
    public static final String OLD_PATTERNS        = "Patterns";
    public static final String OLD_DECORATIONS     = "Decorations";
    public static final String OLD_MAP_ID          = "map";
    public static final String OLD_MAP_COLOR       = "MapColor";
    public static final String OLD_NAME            = "Name";
    public static final String OLD_LORE            = "Lore";
    // 24w18a? (Attributes / Enchantments)
    public static final String OLD_ATTRIBUTES      = "AttributeModifiers";
    public static final String OLD_ENCHANTMENTS    = "Enchantments";
    // 1.21.4-pre2 (AbstractFurnaces)
    public static final String OLD_COOK_TIME_SPENT = "CookTime";
    public static final String OLD_COOK_TIME_TOTAL = "CookTimeTotal";
    public static final String OLD_BURN_TIME       = "BurnTime";
}
