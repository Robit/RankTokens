package io.github.rm2023.RankTokens.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import io.github.rm2023.RankTokens.Main;

public class RespawnListener implements Listener {
    @EventHandler
    public void respawnEvent(PlayerRespawnEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            Main.data.onRespawn(event.getPlayer());
        });
    }
}
