package alpha812.customrecipes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public record ACommandExe(Plugin plugin) implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {
        if (label.equalsIgnoreCase("crafts")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Данную команду могут использовать только игроки");
                return false;
            }
            CraftsMenu.openMenu(plugin, (Player) sender);
            return true;
        }
        return false;
    }
}
