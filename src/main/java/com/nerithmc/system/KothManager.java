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
    private final Map<UUID, Integer> progress = new HashMap<>();

    public KothManager(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    public void load() {
        YamlConfiguration d = plugin.data();
        region = Region.load(d.getConfigurationSection("koth.region"));
        long def = Util.parseSeconds(plugin.getConfig().getString("koth.default-time", "1h"));
        duration = d.getLong("koth.time", def > 0 ? def : 3600);
        remaining = duration;
        active = false;
    }

    private void save() {
        YamlConfiguration d = plugin.data();
        d.set("koth.region", null);
        if (region != null) region.save(d.createSection("koth.region"));
        d.set("koth.time", duration);
        plugin.saveData();
    }

    public void setRegion(Region r) {
        this.region = r;
        this.remaining = duration;
        this.active = false;
        progress.clear();
        save();
    }

    public void removeRegion() {
        this.region = null;
        this.active = false;
        progress.clear();
        save();
    }

    public void setDuration(long seconds) {
        this.duration = seconds;
        this.remaining = seconds;
        this.active = false;
        progress.clear();
        save();
    }

    public void tick() {
        if (region == null) return;

        if (!active) {
            remaining--;
            if (remaining <= 0) {
                remaining = 0;
                active = true;
                plugin.broadcast("koth-active");
            }
        }

        boolean canCapture = active || !plugin.getConfig().getBoolean("koth.capture-only-when-active", true);
        int max = plugin.getConfig().getInt("koth.capture-seconds", 60);
        String bar = plugin.getConfig().getString("koth.actionbar", "&e&lKOTH &8» &a%seconds%&7/&a%max%");

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

        if (winner != null) win(winner);
    }

    private void win(Player p) {
        String cmd = plugin.getConfig().getString("koth.reward-command", "crates givekey Koth %player%");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));
        plugin.broadcast("koth-won", "%player%", p.getName());
        progress.clear();
        active = false;
        remaining = duration;
    }

    public String placeholder() {
        if (region == null) return Util.color(plugin.getConfig().getString("koth.not-set-text", "&7Not Set"));
        if (active) return Util.color(plugin.getConfig().getString("koth.active-text", "&aActive"));
        return Util.formatTime(remaining);
    }
}
