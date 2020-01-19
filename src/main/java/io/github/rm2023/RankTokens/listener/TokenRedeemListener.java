package io.github.rm2023.RankTokens.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.rm2023.RankTokens.Main;
import net.md_5.bungee.api.ChatColor;

public class TokenRedeemListener implements Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void redeemEvent(PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().isSimilar(Main.data.getToken())) {
            if (event.getPlayer().hasPermission("ranktokens.redeemtoken")) {
                if (event.getPlayer().getInventory().removeItem(Main.data.getToken()).isEmpty()) {
                    Main.data.promote(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "An error occurred.");
                    Main.plugin.getLogger().severe("An error occured while redeeming " + event.getPlayer().getName() + "'s token.");
                }
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to redeem this token!");
            }
            event.setCancelled(true);
        }
    }
}
