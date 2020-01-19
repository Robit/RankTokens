package io.github.rm2023.RankTokens;

import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.jorelali.commandapi.api.arguments.GreedyStringArgument;
import io.github.jorelali.commandapi.api.arguments.IntegerArgument;
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;
import io.github.jorelali.commandapi.api.arguments.StringArgument;
import io.github.rm2023.RankTokens.data.Data;
import io.github.rm2023.RankTokens.listener.LoginListener;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

    static public Data data;
    static public Main plugin;

    @Override
    public void onLoad() {
        // Construct data backend
        plugin = this;
        data = new Data(this);
        data.load();

        // Make commands... isn't this so much easier than Bukkit <3
        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("promote"));
        arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            Player toPromote = (Player) args[0];
            data.promote(toPromote);
            if (!sender.equals(toPromote))
            {
                sender.sendMessage(ChatColor.GREEN + toPromote.getName() + "'s rank was set to " + data.getRank(toPromote));
            }
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("promoteOffline"));
        arguments.put("player", new StringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            String toPromote = (String) args[0];
            if (!data.promote(toPromote)) {
                sender.sendMessage(ChatColor.RED + "An error occured. Player is not in database");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + toPromote + "'s rank was set to " + data.getRank(toPromote) + " (level up commands are queued for next login)");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("demote"));
        arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            Player toDemote = (Player) args[0];
            if (!data.demote(toDemote)) {
                sender.sendMessage(ChatColor.RED + "An error occured. Player is at the minimum rank.");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + toDemote.getName() + "'s rank was set to " + data.getRank(toDemote));
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("demoteOffline"));
        arguments.put("player", new StringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            String toDemote = (String) args[0];
            if (!data.demote(toDemote)) {
                sender.sendMessage(ChatColor.RED + "An error occured. Player is not in database or is at the minimum rank.");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + toDemote + "'s rank was set to " + data.getRank(toDemote));
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("check"));
        arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            Player toCheck = (Player) args[0];
            sender.sendMessage(ChatColor.GREEN + "Player " + toCheck.getName() + " has a rank of " + data.getRank(toCheck) + ".");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("checkOffline"));
        arguments.put("player", new StringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            String toCheck = (String) args[0];
            sender.sendMessage(ChatColor.GREEN + "Player " + toCheck + " has a rank of " + data.getRank(toCheck) + ".");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("reload"));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.load();
            sender.sendMessage(ChatColor.GREEN + "Reload complete.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("queue"));
        arguments.put("rank", new IntegerArgument());
        arguments.put("command", new GreedyStringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.queue((String) args[1], (Integer) args[0]);
            sender.sendMessage(ChatColor.GREEN + "Command " + data.formatCommand((String) args[1], ChatColor.BLUE + "%player%" + ChatColor.GREEN, ChatColor.BLUE + "%rank%" + ChatColor.GREEN) + " has been queued for players currently in rank " + (Integer) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("queueOffline"));
        arguments.put("player", new StringArgument());
        arguments.put("command", new GreedyStringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            if (!data.queue((String) args[1], (String) args[0])) {
                sender.sendMessage(ChatColor.RED + "An error occured. Player is not in database.");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + "Command " + data.formatCommand((String) args[1], ChatColor.BLUE + "%player%" + ChatColor.GREEN, ChatColor.BLUE + "%rank%" + ChatColor.GREEN) + " has been queued for player " + (String) args[0] + ".");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("unQueue"));
        arguments.put("rank", new IntegerArgument());
        arguments.put("command", new GreedyStringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.unQueue((String) args[1], (Integer) args[0]);
            sender.sendMessage(ChatColor.GREEN + "Command " + data.formatCommand((String) args[1], ChatColor.BLUE + "%player%" + ChatColor.GREEN, ChatColor.BLUE + "%rank%" + ChatColor.GREEN) + " has been unqueued for players currently in rank " + (Integer) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("unQueueOffline"));
        arguments.put("player", new StringArgument());
        arguments.put("command", new GreedyStringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            if (!data.unQueue((String) args[1], (String) args[0])) {
                sender.sendMessage(ChatColor.GREEN + "An error occured. Player is not in database.");
                return;
            }
            sender.sendMessage(ChatColor.GREEN + "Command " + data.formatCommand((String) args[1], ChatColor.BLUE + "%player%" + ChatColor.GREEN, ChatColor.BLUE + "%rank%" + ChatColor.GREEN) + " has been unqueued for player " + (String) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("resetQueue"));
        arguments.put("rank", new IntegerArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.resetQueue((Integer) args[0]);
            sender.sendMessage("The queue has been reset for players currently in rank " + (Integer) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("resetQueueOffline"));
        arguments.put("player", new StringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            if (!data.resetQueue((String) args[0])) {
                sender.sendMessage(ChatColor.GREEN + "An error occured. Player is not in database.");
                return;
            }
            sender.sendMessage("The queue has been reset for players currently in rank " + (Integer) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("setToken"));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            if (!data.resetQueue((String) args[0])) {
                sender.sendMessage(ChatColor.GREEN + "An error occured. Player is not in database.");
                return;
            }
            sender.sendMessage("The queue has been reset for players currently in rank " + (Integer) args[0] + " and above.");
        });

        // Register user commands
        CommandAPI.getInstance().register("checkRank", CommandPermission.fromString("ranktokens.check"), new LinkedHashMap<String, Argument>(), (sender, args) -> {
            Player toCheck = (Player) sender;
            sender.sendMessage(ChatColor.GREEN + "You, " + toCheck.getName() + " have a rank of " + data.getRank(toCheck) + ".");
        });
    }

    public void onEnable() {
        // HEY! LISTEN!
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        new UpdateTask().runTaskTimer(this, 20, 1200);
    }

    private class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            for (Player p : Bukkit.getOnlinePlayers()) {
                data.update(p);
            }
        }
    }
}
