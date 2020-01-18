package io.github.rm2023.RankTokens.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving RankToken information!");
            e.printStackTrace();
        }
    }

    private boolean createEntry(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            plugin.getLogger().log(Level.INFO, "Creating new rank data entry for player " + p.getName());
            ConfigurationSection newPlayer = data.createSection(p.getUniqueId().toString());
            newPlayer.set("rank", 0);
            newPlayer.set("queuedCommands", new ArrayList<String>());
            save();
            return true;
        }
        return false;
    }

    public boolean promote(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            createEntry(p);
        }
        data.getConfigurationSection(p.getUniqueId().toString()).set("rank", getRank(p) + 1);
        plugin.getLogger().log(Level.INFO, p.getName() + " has been promoted to rank " + getRank(p));
        p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You have been promoted to Aeigis Rank " + getRank(p));
        save();
        // TODO: Add command execution stuffs
        return true;
    }

    public boolean demote(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            createEntry(p);
            return false;
        }
        if (getRank(p) == 0) {
            return false;
        }
        data.getConfigurationSection(p.getUniqueId().toString()).set("rank", getRank(p) - 1);
        plugin.getLogger().log(Level.INFO, p.getName() + " has been demoted to rank " + getRank(p));
        save();
        return true;
    }

    public int getRank(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            createEntry(p);
        }
        return data.getConfigurationSection(p.getUniqueId().toString()).getInt("rank", 0);
    }

    public List<String> getQueuedCommands(Player p) {
        if (data.getConfigurationSection(p.getUniqueId().toString()) == null) {
            return new ArrayList<String>();
        }
        return data.getConfigurationSection(p.getUniqueId().toString()).getStringList("queuedCommands");
    }
}