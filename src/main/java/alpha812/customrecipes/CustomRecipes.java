package alpha812.customrecipes;

import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static alpha812.customrecipes.BlockRestorer.replacedBlocks;
import static alpha812.customrecipes.BlockRestorer.restore;
import static alpha812.customrecipes.CraftsMenu.*;

public final class CustomRecipes extends JavaPlugin {

    private final Set<String> recipeNames = new HashSet<>();

    Configuration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        BlockRestorer.setPlugin(this);
        configureItems();
        getServer().getPluginManager().registerEvents(new EvHandler(this), this);
        getCommand("crafts").setExecutor(new ACommandExe(this));
        getLogger().info("CustomRecipes enabled");
    }

    @Override
    public void onDisable() {
        for (final Location blockLocation: replacedBlocks.keySet()) restore(blockLocation.getBlock());
        getLogger().info("CustomRecipes disabled");
        recipeNames.forEach(recipeName -> Bukkit.removeRecipe(new NamespacedKey(this, recipeName)));
    }

    private void configureItems() {
        ItemFactory factory = Bukkit.getItemFactory();

        addRecipe(Material.EXPERIENCE_BOTTLE,  5, List.of(" G ", "GDG", " G "), Map.of(
                'G', Material.GOLD_NUGGET, 'D', Material.DIAMOND, ' ', Material.AIR), null, null, null);

        addRecipe(Material.ENCHANTED_GOLDEN_APPLE,  1, List.of("GGG", "GAG", "GGG"), Map.of(
                'G', Material.GOLD_BLOCK, 'A', Material.APPLE), null, null, null);

        addRecipe(Material.END_CRYSTAL,  1, List.of("GGG", "GSG", "TOT"), Map.of(
                'G', Material.GLASS, 'S', Material.GHAST_TEAR, 'O', Material.OBSIDIAN, 'T', Material.TNT), null, null, null);

        addRecipe(Material.NETHER_STAR,  1, List.of("GSG", "STS", "GSG"), Map.of(
                'G', Material.GHAST_TEAR, 'S', Material.WITHER_SKELETON_SKULL, 'T', Material.TOTEM_OF_UNDYING), null, null, null);

        addRecipe(Material.CHORUS_FLOWER,  1, List.of("RYR", "YAY", "RYR"), Map.of(
                'R', Material.POPPY, 'Y', Material.DANDELION, 'A', Material.APPLE), null, null, null);

        addRecipe(Material.TOTEM_OF_UNDYING,  1, List.of("GGG", "GPG", "GGG"), Map.of(
                'G', Material.GOLD_BLOCK, 'P', Material.PUFFERFISH), null, null, null);

        addRecipe(Material.ELYTRA,  1, List.of("BSB", "FCF", "F F"), Map.of(
                'B', Material.BLAZE_POWDER, 'S', Material.WITHER_SKELETON_SKULL, 'C', Material.DIAMOND_CHESTPLATE,
                'F', Material.FEATHER, ' ', Material.AIR), null, null, null);

        addRecipe(Material.END_STONE,  1, List.of("SSS", "STS", "SSS"), Map.of(
                'S', Material.SAND, 'T', Material.STONE), null, null, null);

        addRecipe(Material.valueOf(config.getString("пласт.material")),  1,
                List.of("OOO", "ODO", "OOO"), Map.of('O', Material.OBSIDIAN, 'D', Material.DIAMOND),
                config.getString("пласт.name"), config.getStringList("пласт.description"), "layer");

        addRecipe(Material.valueOf(config.getString("трапка.material")),  1, List.of("OOO", "ONO", "OOO"),
                Map.of('O', Material.OBSIDIAN, 'N', Material.NETHERITE_SCRAP), config.getString("трапка.name"),
                config.getStringList("трапка.description"), "trap");

        addRecipe(Material.valueOf(config.getString("огненный смерч.material")),  1,
                List.of("BCB", "CLC", "BCB"), Map.of('C', Material.COAL, 'L', Material.LAVA_BUCKET, 'B', Material.BLAZE_POWDER),
                config.getString("огненный смерч.name"), config.getStringList("огненный смерч.description"), "firestorm");

        addRecipe(Material.valueOf(config.getString("дезориентация.material")), 1, List.of("PMP", "MEM", "PMP"), 
                Map.of('P', Material.GUNPOWDER, 'M', Material.MAGMA_CREAM, 'E', Material.ENDER_EYE),
                config.getString("дезориентация.name"), config.getStringList("дезориентация.description"), "disorientation");

        addRecipe(Material.valueOf(config.getString("явная пыль.material")), 1, List.of("PSP", "SES", "PSP"),
                Map.of('P', Material.GUNPOWDER, 'S', Material.SUGAR, 'E', Material.ENDER_EYE),
                config.getString("явная пыль.name"), config.getStringList("явная пыль.description"), "dust");

        // Вернуться
        back = new ItemStack(Material.valueOf(config.getString("вернуться.material")));
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(config.getString("вернуться.name"));
        backMeta.setLore(config.getStringList("вернуться.description"));
        backMeta.getPersistentDataContainer().set(new NamespacedKey(this, "back"),
                PersistentDataType.BYTE, (byte) 1);
        back.setItemMeta(backMeta);

        // Заполнители
        mainFiller = new ItemStack(Material.valueOf(config.getString("заполнитель.main-color") + "_STAINED_GLASS_PANE"));
        tempFiller = new ItemStack(Material.valueOf(config.getString("заполнитель.second-color") + "_STAINED_GLASS_PANE"));
        ItemMeta fillerMeta = mainFiller.getItemMeta();
        fillerMeta.setDisplayName(config.getString("заполнитель.name"));
        fillerMeta.setLore(config.getStringList("заполнитель.description"));
        fillerMeta.getPersistentDataContainer().set(new NamespacedKey(this, "filler"), PersistentDataType.BYTE, (byte) 1);
        mainFiller.setItemMeta(fillerMeta);
        tempFiller.setItemMeta(fillerMeta);

        // Взрывоопасное яйцо
        specialItems.put("egg", new ItemStack(Material.valueOf(config.getString("взрывоопасное яйцо.material"))));
        ItemMeta eggMeta = specialItems.get("egg").getItemMeta();
        eggMeta.setDisplayName(config.getString("взрывоопасное яйцо.name"));
        eggMeta.setLore(config.getStringList("взрывоопасное яйцо.description"));
        eggMeta.getPersistentDataContainer().set(new NamespacedKey(this, "egg"), PersistentDataType.BYTE, (byte) 1);
        specialItems.get("egg").setItemMeta(eggMeta);

        // Стрела динамит
        specialItems.put("arrow", new ItemStack(Material.valueOf(config.getString("стрела-динамит.material"))));
        ItemMeta arrowMeta = specialItems.get("arrow").getItemMeta();
        arrowMeta.setDisplayName(config.getString("стрела-динамит.name"));
        arrowMeta.setLore(config.getStringList("стрела-динамит.description"));
        arrowMeta.getPersistentDataContainer().set(new NamespacedKey(this, "arrow"),
                PersistentDataType.BYTE, (byte) 1);
        specialItems.get("arrow").setItemMeta(arrowMeta);
    }
    private void addRecipe(@Nonnull Material material, int amount, @Nonnull List<String> shape, @Nonnull Map<Character,
            Material> ingredientMap, @Nullable String name, @Nullable List<String> lore, @Nullable String recipeName) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (recipeName != null) {
            specialItems.put(recipeName, item);
            meta.getPersistentDataContainer().set(new NamespacedKey(this, recipeName), PersistentDataType.BYTE, (byte) 1);
        }
        if (name != null) {
            meta.setDisplayName(name);
        }
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        recipeName = recipeName != null ? recipeName : material.name().toLowerCase();
        recipeNames.add(recipeName);
        randItemsList.add(item);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, recipeName), item).shape(shape.get(0), shape.get(1), shape.get(2));
        for (final Character key: ingredientMap.keySet()) recipe.setIngredient(key, ingredientMap.get(key));
        Bukkit.addRecipe(recipe);
    }
}
