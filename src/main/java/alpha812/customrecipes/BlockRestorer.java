package alpha812.customrecipes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BlockRestorer {

    private static JavaPlugin plugin;

    public static void setPlugin(final JavaPlugin pl) {
        plugin = pl;
    }

    static final Map<Location, BlockState> replacedBlocks = new HashMap<>();
    private static final Map<Location, BukkitTask> runRestoreTasksLayer = new HashMap<>();
    private static final Map<Location, BukkitTask> runRestoreTasksTrap = new HashMap<>();
    static final Set<Location> unbreakableBlocks = new HashSet<>();
    static void restore(final Block block) {
        replacedBlocks.get(block.getLocation()).update(true, false);
    }

    private static void restoreLaterLayer(final Block block) {
        final Location blockLocation = block.getLocation();
        runRestoreTasksLayer.computeIfPresent(blockLocation, (key, task) -> {task.cancel(); return null;});
        runRestoreTasksLayer.put(blockLocation, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!runRestoreTasksTrap.containsKey(blockLocation)) {
                restore(block);
                replacedBlocks.remove(blockLocation);
                unbreakableBlocks.remove(blockLocation);
            }
            runRestoreTasksLayer.remove(blockLocation);
        }, 20 * 90));
    }

    static void temporallyReplaceLayer(final Block block) {
        final Location blockLocation = block.getLocation();
        replacedBlocks.putIfAbsent(blockLocation, block.getState());
        block.setType(Material.OBSIDIAN, false);
        unbreakableBlocks.add(blockLocation);
        restoreLaterLayer(block);
    }

    static void temporallyReplaceTrap(final Block block) {
        final Location blockLocation = block.getLocation();
        replacedBlocks.putIfAbsent(blockLocation, block.getState());
        block.setType(Material.OBSIDIAN, false);
        unbreakableBlocks.add(blockLocation);
        restoreLaterTrap(block);
    }

    private static void restoreLaterTrap(final Block block) {
        final Location blockLocation = block.getLocation();
        runRestoreTasksTrap.computeIfPresent(blockLocation, (key, task) -> {task.cancel(); return null;});
        runRestoreTasksTrap.put(blockLocation, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!runRestoreTasksLayer.containsKey(blockLocation)) {
                restore(block);
                replacedBlocks.remove(blockLocation);
                unbreakableBlocks.remove(blockLocation);
            }
            runRestoreTasksTrap.remove(blockLocation);
        }, 20 * 15));
    }
}
