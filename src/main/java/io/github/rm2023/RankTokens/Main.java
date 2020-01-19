package io.github.rm2023.RankTokens;

import java.util.LinkedHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.jorelali.commandapi.api.arguments.GreedyStringArgument;
import io.github.jorelali.commandapi.api.arguments.IntegerArgument;
import io.github.jorelali.commandapi.api.arguments.LiteralArgument;
import io.github.rm2023.RankTokens.data.Data;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        // Construct data backend
        Data data = new Data(this);
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
        arguments.put("literal", new LiteralArgument("demote"));
        arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            Player toDemote = (Player) args[0];
            data.demote(toDemote);
            sender.sendMessage(ChatColor.GREEN + toDemote.getName() + "'s rank was set to " + data.getRank(toDemote));
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("checkOther"));
        arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            Player toCheck = (Player) args[0];
            sender.sendMessage(ChatColor.GREEN + "Player " + toCheck.getName() + " has a rank of " + data.getRank(toCheck) + ".");
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
        arguments.put("literal", new LiteralArgument("unQueue"));
        arguments.put("rank", new IntegerArgument());
        arguments.put("command", new GreedyStringArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.unQueue((String) args[1], (Integer) args[0]);
            sender.sendMessage(ChatColor.GREEN + "Command " + data.formatCommand((String) args[1], ChatColor.BLUE + "%player%" + ChatColor.GREEN, ChatColor.BLUE + "%rank%" + ChatColor.GREEN) + " has been unqueued for players currently in rank " + (Integer) args[0] + " and above.");
        });

        arguments = new LinkedHashMap<String, Argument>();
        arguments.put("literal", new LiteralArgument("resetQueue"));
        arguments.put("rank", new IntegerArgument());
        CommandAPI.getInstance().register("ranktokens", CommandPermission.fromString("ranktokens.admin"), arguments, (sender, args) -> {
            data.unQueue((String) args[1], (Integer) args[0]);
            sender.sendMessage("The queue has been reset for players currently in rank " + (Integer) args[0] + " and above.");
        });

        // Register user commands
        CommandAPI.getInstance().register("checkRank", CommandPermission.fromString("ranktokens.user"), new LinkedHashMap<String, Argument>(), (sender, args) -> {
            Player toCheck = (Player) sender;
            sender.sendMessage(ChatColor.GREEN + "You, " + toCheck.getName() + " have a rank of " + data.getRank(toCheck) + ".");
        });
    }
}
