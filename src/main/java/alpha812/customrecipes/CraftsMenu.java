package alpha812.customrecipes;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import java.util.*;

public class CraftsMenu implements InventoryHolder {

    protected static final Map<String, ItemStack> specialItems = new HashMap<>();
    protected static ItemStack back;
    protected static ItemStack tempFiller;
    protected static ItemStack mainFiller;
    protected static final List<ItemStack> randItemsList = new ArrayList<>();

    @Override
    public Inventory getInventory() {
        return null;
    }

    public static void openMenu(Plugin plugin, Player player) {
        Inventory inventory = Bukkit.createInventory(new CraftsMenu(), 45, "Крутые крафты");
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, mainFiller);
        }
        inventory.setItem(4, back);
        for (int i = 1; i < 4; i++) {
            inventory.setItem(i * 9, mainFiller);
            inventory.setItem(i * 9 + 1, mainFiller);
            inventory.setItem(i * 9 + 7, mainFiller);
            inventory.setItem(i * 9 + 8, mainFiller);
        }
        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, mainFiller);
        }
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        new BukkitRunnable() {
            int row = 3;
            @Override
            public void run() {
                for (int j = 2; j < 7; j++) inventory.setItem(row * 9 + j, tempFiller);
                row--;
                if (row < 1) cancel();
            }
        }.runTaskTimer(plugin, 2, 1);

        scheduler.runTaskLater(plugin, () -> {
            inventory.setItem(11, new ItemStack(Material.EXPERIENCE_BOTTLE));
            inventory.setItem(12, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
            inventory.setItem(13, new ItemStack(Material.END_CRYSTAL));
            inventory.setItem(14, new ItemStack(Material.NETHER_STAR));
            inventory.setItem(15, specialItems.get("arrow"));
        }, 8);
        scheduler.runTaskLater(plugin, () -> {
            inventory.setItem(20, new ItemStack(Material.CHORUS_FLOWER));
            inventory.setItem(21, new ItemStack(Material.TOTEM_OF_UNDYING));
            inventory.setItem(22, specialItems.get("egg"));
            inventory.setItem(23, new ItemStack(Material.ELYTRA));
            inventory.setItem(24, new ItemStack(Material.END_STONE));
        }, 7);
        scheduler.runTaskLater(plugin, () -> {
            inventory.setItem(29, specialItems.get("layer"));
            inventory.setItem(30, specialItems.get("trap"));
            inventory.setItem(31, specialItems.get("firestorm"));
            inventory.setItem(32, specialItems.get("disorientation"));
            inventory.setItem(33, specialItems.get("dust"));
        }, 6);
        new BukkitRunnable() {
            int sounds = 9;
            @Override
            public void run() {
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 5, 1);
                sounds--;
                if (sounds < 1) cancel();
            }
        }.runTaskTimer(plugin, 1, 1);
        player.openInventory(inventory);
    }
    public static class RecipeMenu implements InventoryHolder {
        public Inventory getInventory() {
            return null;
        }
        public static void showRecipe(Plugin plugin, Player player, @Nonnull String recipeName) {
            ShapedRecipe recipe;
            if (recipeName.equals("arrow")) {
                recipe = new ShapedRecipe(new NamespacedKey(plugin, "arrow"), specialItems.get("arrow")).
                        shape("   ", "AT ", "   ").setIngredient(' ', Material.AIR).
                        setIngredient('A',Material.ARROW).setIngredient('T', Material.TNT);
            }
            else if (recipeName.equals("egg")) {
                recipe = new ShapedRecipe(new NamespacedKey(plugin, "egg"), specialItems.get("egg")).
                        shape(" P ", "PEP", " P ").setIngredient(' ', Material.AIR).setIngredient(
                                'P',Material.GUNPOWDER).setIngredient('E', Material.EGG);
            }
            else {
                recipe = (ShapedRecipe) Bukkit.getRecipe(new NamespacedKey(plugin, recipeName));
            }
            if (recipe == null) {
                return;
            }
            ItemStack result = recipe.getResult();
            String header = result.getItemMeta().getDisplayName();

            Inventory inventory = Bukkit.createInventory(new RecipeMenu(), 45, !header.isEmpty() ? header : new TranslatableComponent(result.getTranslationKey()).toPlainText());
            Map<Character, ItemStack> ingredientMap = recipe.getIngredientMap();
            String[] shape = recipe.getShape();

            for (int i = 1; i < 9; i++) {
                inventory.setItem(i, mainFiller);
            }
            inventory.setItem(0, back);
            for (int i = 1; i < 4; i++) {
                inventory.setItem(i * 9, mainFiller);
                inventory.setItem(i * 9 + 1, mainFiller);
                inventory.setItem(i * 9 + 5, mainFiller);
                inventory.setItem(i * 9 + 7, mainFiller);
                inventory.setItem(i * 9 + 8, mainFiller);
            }
            inventory.setItem(15, mainFiller);
            inventory.setItem(33, mainFiller);
            for (int i = 36; i < 45; i++) {
                inventory.setItem(i, mainFiller);
            }

            BukkitScheduler scheduler = plugin.getServer().getScheduler();

            new BukkitRunnable() {
                byte row = 3;
                @Override
                public void run() {
                    for (int j = 2; j < 5; j++) inventory.setItem(row * 9 + j, tempFiller);
                    row--;
                    if (row < 1) cancel();
                }
            }.runTaskTimer(plugin, 2, 1);

            new BukkitRunnable() {
                byte sounds = 9;
                @Override
                public void run() {
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 5, 1);
                    sounds--;
                    if (sounds < 1) cancel();
                }
            }.runTaskTimer(plugin, 1, 1);

            scheduler.runTaskLater(plugin,() -> {
                inventory.setItem(11, ingredientMap.get(shape[0].charAt(0)));
                inventory.setItem(12, ingredientMap.get(shape[0].charAt(1)));
                inventory.setItem(13, ingredientMap.get(shape[0].charAt(2)));
            }, 8);
            scheduler.runTaskLater(plugin,() -> {
                inventory.setItem(20, ingredientMap.get(shape[1].charAt(0)));
                inventory.setItem(21, ingredientMap.get(shape[1].charAt(1)));
                inventory.setItem(22, ingredientMap.get(shape[1].charAt(2)));
            }, 7);
            scheduler.runTaskLater(plugin,() -> {
                inventory.setItem(29, ingredientMap.get(shape[2].charAt(0)));
                inventory.setItem(30, ingredientMap.get(shape[2].charAt(1)));
                inventory.setItem(31, ingredientMap.get(shape[2].charAt(2)));
            }, 6);

            new BukkitRunnable() {
                private byte randItems = 7;
                private final Random random = new Random();

                @Override
                public void run() {
                    inventory.setItem(24, randItemsList.get(random.nextInt(randItemsList.size())));
                    player.playNote(player.getLocation(), Instrument.XYLOPHONE, new Note(13));
                    player.playNote(player.getLocation(), Instrument.XYLOPHONE, new Note(9));
                    randItems--;
                    if (randItems < 1) {
                        cancel();
                        inventory.setItem(24, result);
                    }
                }
            }.runTaskTimer(plugin,9, 2);

            player.openInventory(inventory);
        }
    }
}
