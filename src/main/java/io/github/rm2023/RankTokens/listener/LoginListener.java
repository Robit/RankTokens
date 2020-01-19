package io.github.rm2023.RankTokens.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import io.github.rm2023.RankTokens.Main;

public class LoginListener implements Listener {
    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        Main.data.onLogin(event.getPlayer());
    }
}
