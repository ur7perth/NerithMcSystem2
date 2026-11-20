package com.nerithmc.system;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class KillMessageListener implements Listener {

    private final NerithMcSystem2 plugin;
    private final Random random = new Random();
    private int last = -1;

    public KillMessageListener(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        FileConfiguration c = plugin.getConfig();
        if (!c.getBoolean("kill-messages.enabled", true)) return;

        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        e.deathMessage(null);

        if (killer == null) {
            String other = c.getString("kill-messages.other", "&c%victim% &7died.");
            Bukkit.broadcast(Util.comp(other.replace("%victim%", victim.getName())));
            return;
        }

        List<String> variants = c.getStringList("kill-messages.player");
        if (variants.isEmpty()) return;

        int index;
        if (variants.size() == 1) {
            index = 0;
        } else {
            do {
                index = random.nextInt(variants.size());
            } while (index == last);
        }
        last = index;

        String msg = variants.get(index)
                .replace("%killer%", killer.getName())
                .replace("%victim%", victim.getName())
                .replace("%item%", itemName(killer.getInventory().getItemInMainHand()));
        Bukkit.broadcast(Util.comp(msg));
    }

    private String itemName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return plugin.getConfig().getString("kill-messages.fists-name", "Fists");
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            return PlainTextComponentSerializer.plainText().serialize(meta.displayName()).replace("&", "");
        }
        return pretty(item.getType());
    }

    private String pretty(Material m) {
        StringBuilder sb = new StringBuilder();
        for (String part : m.name().toLowerCase().split("_")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
