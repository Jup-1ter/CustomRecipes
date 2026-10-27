package alpha812.customrecipes;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Logger;

import static alpha812.customrecipes.BlockRestorer.temporallyReplaceTrap;
import static alpha812.customrecipes.BlockRestorer.unbreakableBlocks;
import static alpha812.customrecipes.CraftsMenu.RecipeMenu.showRecipe;


public class EvHandler implements Listener {
    private final static HashMap<UUID, HashSet<String>> cooldowned = new HashMap<>();
    final Plugin plugin;
    final Logger logger;
    final FileConfiguration config;

    public EvHandler(Plugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        config = plugin.getConfig();
    }


    @EventHandler
    public void onClick(@Nonnull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack usedItem = event.getItem();
        if (usedItem == null) return;
        ItemMeta usedMeta = usedItem.getItemMeta();
        if (usedMeta == null) return;
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        UUID playerUUID = player.getUniqueId();

        if (usedMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "layer"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (disallowed(playerUUID, playerLocation, "пласт")) return;
            world.playSound(playerLocation, Sound.BLOCK_ANVIL_PLACE, 2, .5f);
            usedItem.setAmount(usedItem.getAmount() - 1);
            int x = playerLocation.getBlockX();
            int z = playerLocation.getBlockZ();
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = z - 1; j <= z + 1; j++) {
                    Block block = world.getBlockAt(i, playerLocation.getBlockY() - 1, j);
                    if (!config.getBoolean("пласт.region-restricted") || inLegalPlace(block.getLocation(),
                            config.getBoolean("пласт.allowed-works"))) BlockRestorer.temporallyReplaceLayer(block);
                }
            }
        }
        else if (usedMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "trap"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (disallowed(playerUUID, playerLocation, "трапка")) return;
            world.playSound(playerLocation, Sound.BLOCK_ANVIL_PLACE, 2, .5f);
            usedItem.setAmount(usedItem.getAmount() - 1);
            int size = 2;
            int playerX = playerLocation.getBlockX();
            int playerY = playerLocation.getBlockY() + 1;
            int playerZ = playerLocation.getBlockZ();
            for (int x = -size; x <= size; x++) for (int y = -size; y <= size; y++) for (int z = -size; z <= size; z++) {
                if (Math.abs(x) == size || Math.abs(y) == size || Math.abs(z) == size) {
                    Block block = world.getBlockAt(playerX + x, playerY + y, playerZ + z);
                    if (!config.getBoolean("трапка.region-restricted") || inLegalPlace(block.getLocation(),
                            config.getBoolean("трапка.allowed-works"))) temporallyReplaceTrap(block);
                }
            }
        }
        else if (usedMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "firestorm"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (disallowed(playerUUID, playerLocation, "огненный смерч")) return;
            usedItem.setAmount(usedItem.getAmount() - 1);
            world.playSound(playerLocation, Sound.ITEM_FIRECHARGE_USE, .5F, .8F);
            new BukkitRunnable() {
                byte time = 0;
                @Override
                public void run() {
                    world.spawnParticle(Particle.FLAME, playerLocation, 200, (double) time / 5 + .2, 1.5, (double) time / 5 + .2, .05);
                    world.getNearbyEntities(playerLocation, 10, 2, 10).forEach(entity -> {
                        if (!entity.equals(player) && (!config.getBoolean("огненный смерч.region-restricted") ||
                                inLegalPlace(entity.getLocation(), config.getBoolean("огненный смерч.allowed-works")))) {
                            final double distance = playerLocation.distance(entity.getLocation());
                            if (20 * distance / 7 <= time && distance <= 10) entity.setFireTicks(20 * 3);
                        }
                    });
                    time += 2;
                    if (time > 30) cancel();
                }
            }.runTaskTimer(plugin, 0, 2);
        }
        else if (usedMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "dust"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (disallowed(playerUUID, playerLocation, "явная пыль")) return;
            usedItem.setAmount(usedItem.getAmount() - 1);
            world.spawnParticle(Particle.FLASH, playerLocation, 5);
            world.spawnParticle(Particle.END_ROD, playerLocation, 500, 1, 1, 1, .3);
            world.getNearbyEntities(playerLocation ,15, 15, 15).forEach(entity -> {
                if (entity instanceof LivingEntity && playerLocation.distance(entity.getLocation()) <= 15 && !entity.equals(player)
                        && (!config.getBoolean("явная пыль.region-restricted") || inLegalPlace(entity.getLocation(),
                        config.getBoolean("явная пыль.allowed-works")))) ((LivingEntity) entity).
                        addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                                20 * 7, 1, true, false, false));
            });
            new BukkitRunnable() {
                byte time = 0;
                @Override
                public void run() {
                    world.getNearbyEntities(playerLocation, 10, 15, 10).forEach(entity -> {
                        final double distance = playerLocation.distance(entity.getLocation());
                        if (20 * distance / 5 <= time && distance <= 10 && entity instanceof LivingEntity && !entity.equals(player))
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
                                    20 * 30, 1, true, false, false));
                    });
                    time += 2;
                    if (time > 20 * 3) cancel();
                }
            }.runTaskTimer(plugin, 20, 2);
        }
        else if (usedMeta.getPersistentDataContainer().has(new NamespacedKey(plugin, "disorientation"), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (disallowed(playerUUID, playerLocation, "дезориентация")) return;
            usedItem.setAmount(usedItem.getAmount() - 1);
            world.playSound(playerLocation, Sound.ENTITY_ENDERMAN_TELEPORT, .7f, .6f);
            new BukkitRunnable() {
                byte time = 0;
                @Override
                public void run() {
                    world.spawnParticle(Particle.PORTAL, playerLocation, 300, (double) time / 5 + .2, 1.5, (double) time / 5 + .2, 0);
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, playerLocation, 100, (double) time / 5 + .2, 1.5, (double) time / 5 + .2, new Particle.DustTransition(Color.PURPLE, Color.PURPLE, 1));
                    world.getNearbyEntities(playerLocation, 10, 3, 10).forEach(entity -> {
                        final double distance = playerLocation.distance(entity.getLocation());
                        if (20 * distance / 5 <= time && distance <= 10 && entity instanceof LivingEntity && !entity.equals(player)
                                && (!config.getBoolean("дезориентация.region-restricted") || inLegalPlace(entity.getLocation(),
                                config.getBoolean("дезориентация.allowed-works")))) {
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 7,
                                    1, true, false, false));
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 7,
                                    1, true, false, false));
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 7,
                                    1, true, false, false));
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 7,
                                    1, true, false, false));
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 7,
                                    1, true, false, false));
                        }
                    });
                    time += 2;
                    if (time > 20 * 2) cancel();
                }
            }.runTaskTimer(plugin, 0, 2);
        }
    }

    @EventHandler
    public void onBlockBreak(@Nonnull BlockBreakEvent event) {
        if (unbreakableBlocks.contains(event.getBlock().getLocation())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CraftsMenu) && !(holder instanceof CraftsMenu.RecipeMenu)) return;
        event.setCancelled(true);
        event.getView().setCursor(new ItemStack(Material.AIR));
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player player)) return;
        Material type = clickedItem.getType();
        if (clickedItem.getItemMeta() == null) return;
        PersistentDataContainer data = clickedItem.getItemMeta().getPersistentDataContainer();
        Optional<NamespacedKey> ownKey = data.getKeys().stream().filter(key -> key.getNamespace().equalsIgnoreCase(plugin.getName())).findFirst();

        if (ownKey.isPresent()) {
            String tag = ownKey.get().getKey();
            if (tag.equals("filler"))
                player.playSound(whoClicked.getLocation(), Sound.BLOCK_GLASS_BREAK, 5, 1);
            else if (tag.equals("back")) {
                if (holder instanceof CraftsMenu.RecipeMenu) CraftsMenu.openMenu(plugin, player);
                else player.chat("/menu");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5, 1);
            }
            else showRecipe(plugin, player, tag);
        }
        else showRecipe(plugin,player,type.name().toLowerCase());
    }

    private boolean disallowed(UUID uuid, Location location, String item) {
        Set<String> playerCooldowns = cooldowned.get(uuid);
        if (config.getBoolean(item + ".region-restricted") && !inLegalPlace(location, config.getBoolean(item + ".allowed-works"))) return true;

        double cooldown = config.getDouble(item + ".cooldown");
        if (cooldown > 0) {
            if (playerCooldowns != null) {
                if (playerCooldowns.contains(item)) return true;
                else playerCooldowns.add(item);
            } else {
                cooldowned.put(uuid, new HashSet<>(List.of(item)));
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> cooldowned.get(uuid).
                    remove(item), (long) (20L * cooldown));
        }
        return false;
    }
    private boolean inLegalPlace(Location location, boolean allowedWorks) {
        World world = location.getWorld();
        if (world == null) return false;
        RegionManager regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionContainer == null) return false;
        if (allowedWorks) for (final String allowedRegionID : plugin.getConfig().getStringList("allowed-regions")) {
            ProtectedRegion allowedRegion = regionContainer.getRegion(allowedRegionID);
            if (allowedRegion != null && allowedRegion.contains(BukkitAdapter.asBlockVector(location))) return true;
        }
        for (final String forbRegionID : plugin.getConfig().getStringList("forbidden-regions")) {
            ProtectedRegion forbRegion = regionContainer.getRegion(forbRegionID);
            if (forbRegion != null && forbRegion.contains(BukkitAdapter.asBlockVector(location))) return false;
        }
        return true;
    }
}
