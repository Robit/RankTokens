package io.github.rm2023.RankTokens;

import java.util.LinkedHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;
import io.github.rm2023.RankTokens.data.Data;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        // Construct data backbone
        Data data = new Data(this);
        data.load();

        // Make command argument templates
        LinkedHashMap<String, Argument> empty = new LinkedHashMap<String, Argument>();
        LinkedHashMap<String, Argument> onePlayer = new LinkedHashMap<String, Argument>();
        onePlayer.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));

        // Make commands... isn't this so much easier than Bukkit <3
        CommandAPI.getInstance().register("promote", CommandPermission.fromString("ranktokens.promote"), onePlayer, (sender, args) -> {
            Player toPromote = (Player) args[0];
            data.promote(toPromote);
            if (!sender.equals(toPromote))
            {
                sender.sendMessage(ChatColor.GREEN + toPromote.getName() + "'s rank was set to " + data.getRank(toPromote));
            }
        });
        CommandAPI.getInstance().register("demote", CommandPermission.fromString("ranktokens.demote"), onePlayer, (sender, args) -> {
            Player toDemote = (Player) args[0];
            data.demote(toDemote);
            if (!sender.equals(toDemote)) {
                sender.sendMessage(ChatColor.GREEN + toDemote.getName() + "'s rank was set to " + data.getRank(toDemote));
            }
        });
        CommandAPI.getInstance().register("checkOther", CommandPermission.fromString("ranktokens.checkOther"), onePlayer, (sender, args) -> {
            Player toCheck = (Player) args[0];
            sender.sendMessage(ChatColor.GREEN + "Player " + toCheck.getName() + " has a rank of " + data.getRank(toCheck) + ".");
        });
        CommandAPI.getInstance().register("check", CommandPermission.fromString("ranktokens.check"), empty, (sender, args) -> {
            Player toCheck = (Player) sender;
            sender.sendMessage(ChatColor.GREEN + "You, " + toCheck.getName() + " have a rank of " + data.getRank(toCheck) + ".");
        });
    }
}
