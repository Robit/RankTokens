package io.github.rm2023.RankTokens.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.rm2023.RankTokens.Main;
import net.md_5.bungee.api.ChatColor;

public class Data {
    protected Main plugin;
    protected File configFile;
    protected File dataFile;
    protected FileConfiguration config;
    protected FileConfiguration data;

    public Data(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
            /*
             * ItemStack defaultToken = new ItemStack(Material.DRAGON_BREATH);
             * 
             * ItemMeta meta = defaultToken.getItemMeta();
             * meta.setDisplayName("Elixr of Aegis");
             * meta.setLore(Arrays.asList("A powerful artifact of time itself",
             * "Right click to redeem...")); defaultToken.setItemMeta(meta);
             * config.set("token", defaultToken); ConfigurationSection ranksSection =
             * config.createSection("ranks"); ConfigurationSection levelOne =
             * ranksSection.createSection("1"); levelOne.set("commandsOnLevelUp",
             * Arrays.asList("yourcommandhere", "yourcommandhere2"));
             * levelOne.set("commandsOnRespawn", Arrays.asList("samplecommand",
             * "samplecommand2")); levelOne.set("description",
             * Arrays.asList("yourdescriptionhere", "yourdescriptionhere"));
             * ConfigurationSection levelTwo = ranksSection.createSection("2");
             * levelTwo.set("commandsOnLevelUp", Arrays.asList("yourcommandhere",
             * "yourcommandhere2")); levelTwo.set("commandsOnRespawn",
             * Arrays.asList("samplecommand", "samplecommand2"));
             * levelTwo.set("description", Arrays.asList("yourdescriptionhere",
             * "yourdescriptionhere")); config.set("playtimeRewardTimes", Arrays.asList(60,
             * 360)); config.save(configFile);
             */
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading RankToken information!");
            e.printStackTrace();
            return;
        }

        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.saveResource("data.yml", false);
        }

        data = new YamlConfiguration();
        try {
            data.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading RankToken information!");
            e.printStackTrace();
            return;
        }
        plugin.getLogger().log(Level.INFO, "Load complete.");
    }

    public void save() {
        try {
            config.save(configFile);
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving RankToken information!");
            e.printStackTrace();
        }
    }

    protected boolean createEntry(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            plugin.getLogger().log(Level.INFO, "Creating new rank data entry for player " + p.getName());
            ConfigurationSection newPlayer = data.createSection(p.getUniqueId().toString());
            newPlayer.set("name", p.getName());
            newPlayer.set("rank", 0);
            newPlayer.set("queuedCommands", new ArrayList<String>());
            newPlayer.set("playtime", 0);
            save();
            return true;
        }
        return false;
    }

    protected ConfigurationSection getPlayerData(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            createEntry(p);
        }
        return data.getConfigurationSection(p.getUniqueId().toString());
    }

    protected ConfigurationSection getPlayerData(String p) {
        for (String playerId : data.getKeys(false)) {
            if (data.getConfigurationSection(playerId).getString("name").equals(p)) {
                return data.getConfigurationSection(playerId);
            }
        }
        return null;
    }

    protected ConfigurationSection getRankInfo(int rank) {
        return config.getConfigurationSection("ranks").getConfigurationSection(Integer.toString(rank));
    }

    protected List<ConfigurationSection> getPlayers(int rank) {
        LinkedList<ConfigurationSection> toReturn = new LinkedList<ConfigurationSection>();
        for (String playerId : data.getKeys(false)) {
            if (data.getConfigurationSection(playerId).getInt("rank") >= rank) {
                toReturn.add(data.getConfigurationSection(playerId));
            }
        }
        return toReturn;
    }

    public boolean promote(Player p) {
        getPlayerData(p).set("rank", getRank(p) + 1);
        plugin.getLogger().log(Level.INFO, p.getName() + " has been promoted to rank " + getRank(p));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
        p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You have been promoted to Aeigis Rank " + getRank(p));
        save();
        ConfigurationSection rankInfo = getRankInfo(getRank(p));
        if (rankInfo == null) {
            return true;
        }
        List<String> description = rankInfo.getStringList("description");
        if (description != null) {
            p.sendMessage(description.toArray(new String[description.size()]));
        }
        List<String> commands = rankInfo.getStringList("commandsOnLevelUp");
        if (commands != null) {
            for (String cmd : commands) {
                String command = formatCommand(cmd, p.getName(), Integer.toString(getRank(p)));
                plugin.getLogger().info("Running levelup command " + command);
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            }
        }
        return true;
    }

    public boolean promote(String p) {
        ConfigurationSection playerData = getPlayerData(p);
        if (playerData == null) {
            return false;
        }
        playerData.set("rank", getRank(p) + 1);
        plugin.getLogger().log(Level.INFO, p + " has been promoted to rank " + getRank(p) + " (level up commands queued for next login)");
        save();
        ConfigurationSection rankInfo = getRankInfo(getRank(p));
        if (rankInfo == null) {
            return true;
        }
        List<String> commands = rankInfo.getStringList("commandsOnLevelUp");
        if (commands != null) {
            for (String cmd : commands) {
                queue(p, cmd);
            }
        }
        return true;
    }

    public boolean demote(Player p) {
        if (getRank(p) == 0) {
            return false;
        }
        getPlayerData(p).set("rank", getRank(p) - 1);
        plugin.getLogger().log(Level.INFO, p.getName() + " has been demoted to rank " + getRank(p));
        save();
        return true;
    }

    public boolean demote(String p) {
        if (getRank(p) == 0) {
            return false;
        }
        getPlayerData(p).set("rank", getRank(p) - 1);
        plugin.getLogger().log(Level.INFO, p + " has been demoted to rank " + getRank(p));
        save();
        return true;
    }

    public int getRank(Player p) {
        return getPlayerData(p).getInt("rank", 0);
    }

    public int getRank(String p) {
        if (getPlayerData(p) == null) {
            return 0;
        }
        return getPlayerData(p).getInt("rank", 0);
    }

    public String formatCommand(String cmd, String name, String rank) {
        return cmd.replace("%player%", name).replace("%rank%", rank);
    }

    public boolean queue(String command, int rank) {
        for (ConfigurationSection player : getPlayers(rank)) {
            List<String> newQueue = player.getStringList("queuedCommands");
            newQueue.add(command);
            player.set("queuedCommands", newQueue);
        }
        save();
        return true;
    }

    public boolean queue(String command, String p) {
        ConfigurationSection player = getPlayerData(p);
        if (player == null) {
            return false;
        }
        List<String> newQueue = player.getStringList("queuedCommands");
        newQueue.add(command);
        player.set("queuedCommands", newQueue);
        save();
        return true;
    }

    public boolean unQueue(String command, int rank) {
        for (ConfigurationSection player : getPlayers(rank)) {
            List<String> newQueue = player.getStringList("queuedCommands");
            newQueue.remove(command);
            player.set("queuedCommands", newQueue);
        }
        save();
        return true;
    }

    public boolean unQueue(String command, String p) {
        ConfigurationSection player = getPlayerData(p);
        if (player == null) {
            return false;
        }
        List<String> newQueue = player.getStringList("queuedCommands");
        newQueue.remove(command);
        player.set("queuedCommands", newQueue);
        save();
        return true;
    }

    public boolean resetQueue(int rank) {
        for (ConfigurationSection player : getPlayers(rank)) {
            player.set("queuedCommands", new ArrayList<String>());
        }
        save();
        return true;
    }

    public boolean resetQueue(String p) {
        ConfigurationSection player = getPlayerData(p);
        if (player == null) {
            return false;
        }
        player.set("queuedCommands", new ArrayList<String>());
        save();
        return true;
    }

    public List<String> getQueuedCommands(String p) {
        if (getPlayerData(p) == null) {
            return new ArrayList<String>();
        }
        return getPlayerData(p).getStringList("queuedCommands");
    }

    public void update(Player p) {
        ConfigurationSection player = getPlayerData(p);
        player.set("name", p.getName());
        for (String cmd : player.getStringList("queuedCommands")) {
            String command = formatCommand(cmd, p.getName(), Integer.toString(getRank(p)));
            plugin.getLogger().info("Running queued command " + command);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }
        player.set("queuedCommands", new ArrayList<String>());
        int playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 1200; // 'cuz the stat measures ticks, not minutes played...
        for (Integer playtimeThreshold : config.getIntegerList("playtimeRewardTimes")) {
            if (player.getInt("playtime") < playtimeThreshold && playtime >= playtimeThreshold) {
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You have been automatically given an Aegis Rank for playing " + playtimeThreshold + " minutes!");
                promote(p);
            }
        }
        player.set("playtime", playtime);
        save();
    }

    public void setToken(ItemStack token) {
        config.set("token", token);
        save();
    }

    public ItemStack getToken() {
        return config.getItemStack("token");
    }
}
