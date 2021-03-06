package me.ialistannen.skylaskinvsee;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import me.ialistannen.bukkitutilities.command.AbstractCommandNode;
import me.ialistannen.bukkitutilities.command.CommandTree;
import me.ialistannen.bukkitutilities.command.DefaultCommandExecutor;
import me.ialistannen.bukkitutilities.command.DefaultTabCompleter;
import me.ialistannen.bukkitutilities.language.I18N;
import me.ialistannen.bukkitutilities.language.MessageProvider;
import me.ialistannen.bukkitutilities.modulesystem.ModuleManager;
import me.ialistannen.bukkitutilities.utilities.disable.DisableManager;
import me.ialistannen.skylaskinvsee.commands.CommandInvsee;
import me.ialistannen.skylaskinvsee.event.DragListener;
import me.ialistannen.skylaskinvsee.event.MiscPlayerListener;
import me.ialistannen.skylaskinvsee.manager.WatchGuiManager;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;

public class SkylaskInvsee extends JavaPlugin {

    private static SkylaskInvsee instance;

    private DisableManager disableManager;
    private MessageProvider language;

    private WatchGuiManager watchGuiManager;
    private WatchedPlayers watchedPlayers;

    public void onEnable() {
        if (!ModuleManager.INSTANCE.registerPlugin(this)) {
            getLogger().severe("Error loading this plugin, see the log from BukkitUtilities!");
            return;
        }

        instance = this;
        disableManager = new DisableManager(this);

        saveDefaultConfig();

        I18N.copyDefaultFiles(this, false, "me.ialistannen.skylaskinvsee.language");
        language = new I18N(this, "me.ialistannen.skylaskinvsee.language");

        reload();

        watchGuiManager = new WatchGuiManager();
        watchedPlayers = new WatchedPlayers();

        Bukkit.getPluginManager().registerEvents(new DragListener(), this);
        Bukkit.getPluginManager().registerEvents(new MiscPlayerListener(), this);
    }

    private void reload() {
        disableManager.disable();
        disableManager = new DisableManager(this);

        reloadConfig();
        language.reload();

        CommandTree tree = new CommandTree(disableManager);
        CommandExecutor commandExecutor = new DefaultCommandExecutor(tree, language);
        TabCompleter tabCompleter = new DefaultTabCompleter(tree);

        AbstractCommandNode mainCommand = new CommandInvsee();
        tree.addTopLevelChildAndRegister(mainCommand, commandExecutor, tabCompleter, this);
        tree.attachHelp(mainCommand, getConfig().getString("permissions.commands.help"), getLanguage());
    }

    @Override
    public void onDisable() {
        watchGuiManager.closeAll();
        // prevent the old instance from still being around.
        instance = null;
    }

    /**
     * @return The {@link WatchGuiManager}
     */
    public WatchGuiManager getWatchGuiManager() {
        return watchGuiManager;
    }

    /**
     * @return The {@link WatchedPlayers}
     */
    public WatchedPlayers getWatchedPlayers() {
        return watchedPlayers;
    }

    /**
     * @return The language
     */
    public MessageProvider getLanguage() {
        return language;
    }

    /**
     * Returns the plugins instance
     *
     * @return The plugin instance
     */
    public static SkylaskInvsee getInstance() {
        return instance;
    }
}
