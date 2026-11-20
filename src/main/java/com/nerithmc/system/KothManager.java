package com.nerithmc.system;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KothManager {

    private final NerithMcSystem2 plugin;
    private Region region;
    private long duration;
    private long remaining;
    private boolean active;
    private int idle;
    private final Map<UUID, Integer> progress = new HashMap<>();

    public KothManager(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    public void load() {
        YamlConfiguration d = plugin.data();
        region = Region.load(d.getConfigurationSection("koth.region"));
        long def = Util.parseSeconds(plugin.getConfig().getString("koth.default-time", "1h"));
        duration = d.getLong("koth.time", def > 0 ? def : 3600);
        reset();
    }

    private void save() {
        YamlConfiguration d = plugin.data();
        d.set("koth.region", null);
        if (region != null) region.save(d.createSection("koth.region"));
        d.set("koth.time", duration);
        plugin.saveData();
    }

    /** Back to the countdown (KOTH not active). */
    private void reset() {
        remaining = duration;
        active = false;
        idle = 0;
        progress.clear();
    }

    public void setRegion(Region r) {
        this.region = r;
        reset();
        save();
    }

    public void removeRegion() {
        this.region = null;
        reset();
        save();
    }

    public void setDuration(long seconds) {
        this.duration = seconds;
        reset();
        save();
    }

    public void tick() {
        if (region == null) return;

        if (!active) {
            remaining--;
            if (remaining <= 0) {
                remaining = 0;
                active = true;
                idle = 0;
            }
        }

        boolean canCapture = active || !plugin.getConfig().getBoolean("koth.capture-only-when-active", true);
        int max = plugin.getConfig().getInt("koth.capture-seconds", 60);
        String bar = plugin.getConfig().getString("koth.actionbar", "&8&lKoth &f» &7التكمله &f%seconds%&7/&f%max%");

        Set<UUID> inZone = new HashSet<>();
        Player winner = null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (!region.contains(p.getLocation())) continue;
            inZone.add(p.getUniqueId());
            if (!canCapture) continue;

            int seconds = progress.merge(p.getUniqueId(), 1, Integer::sum);
            p.sendActionBar(Util.comp(bar.replace("%seconds%", "" + Math.min(seconds, max)).replace("%max%", "" + max)));
            if (seconds >= max && winner == null) {
                winner = p;
            }
        }

        progress.keySet().retainAll(inZone);

        if (winner != null) {
            win(winner);
            return;
        }

        // Idle reset: Active KOTH with nobody inside goes back to the timer.
        if (active) {
            if (!inZone.isEmpty()) {
                idle = 0;
            } else {
                idle++;
                int limit = plugin.getConfig().getInt("koth.idle-reset-seconds", 90);
                if (limit > 0 && idle >= limit) reset();
            }
        }
    }

    private void win(Player p) {
        String cmd = plugin.getConfig().getString("koth.reward-command", "crates givekey Koth %player%");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));
        reset();
    }

    public String placeholder() {
        if (region == null) return Util.color(plugin.getConfig().getString("koth.not-set-text", "&7Not Set"));
        if (active) return Util.color(plugin.getConfig().getString("koth.active-text", "&aActive"));
        return Util.formatTime(remaining);
    }
}
