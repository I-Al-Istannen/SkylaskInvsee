package me.ialistannen.skylaskinvsee.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.perceivedev.perceivecore.command.CommandResult;
import com.perceivedev.perceivecore.command.CommandSenderType;
import com.perceivedev.perceivecore.command.TranslatedCommandNode;
import com.perceivedev.perceivecore.command.argumentmapping.ConvertedParams;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.gui.WatchGui;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;
import me.ialistannen.skylaskinvsee.util.Util;

/**
 * The main command for {@link SkylaskInvsee}
 */
public class CommandInvsee extends TranslatedCommandNode {

    public CommandInvsee() {
        super(new Permission(SkylaskInvsee.getInstance().getConfig().getString("permissions.commands.invsee")),
                  "command.invsee",
                  SkylaskInvsee.getInstance().getLanguage(),
                  CommandSenderType.PLAYER);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> wholeChat, int relativeIndex) {
        return null;
    }

    @ConvertedParams(targetClasses = Player.class)
    public CommandResult execute(Player player, Player target, String[] args, String[] wholeArgs) {
        if (target == null) {
            player.sendMessage(Util.trWithPrefix("command.invsee.target.not.online", wholeArgs[0]));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        boolean modifiable = true;
        if (target.equals(player)) {
            player.sendMessage(Util.trWithPrefix("command.invsee.target.is.player"));
            modifiable = false;
        }

        WatchedPlayers watchedPlayers = SkylaskInvsee.getInstance().getWatchedPlayers();

        if (watchedPlayers.isWatchingAPlayer(player.getUniqueId())) {
            player.sendMessage(Util.trWithPrefix("command.invsee.already.watching.a.player"));
            return CommandResult.SUCCESSFULLY_INVOKED;
        }

        String guiName = Util.tr("command.invsee.gui.name", target.getName(), target.getDisplayName());
        WatchGui gui = new WatchGui(guiName, target, modifiable);

        gui.open(player);

        watchedPlayers.addWatcher(target.getUniqueId(), player.getUniqueId());
        SkylaskInvsee.getInstance().getWatchGuiManager().addWatchGui(player.getUniqueId(), gui);

        return CommandResult.SUCCESSFULLY_INVOKED;
    }
}
