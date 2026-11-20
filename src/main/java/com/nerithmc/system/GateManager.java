package com.nerithmc.system;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GateManager {

    private record Stored(String world, int x, int y, int z, String data) {
        String key() { return world + ";" + x + ";" + y + ";" + z; }
        String serialize() { return key() + ";" + data; }

        Block block() {
            World w = Bukkit.getWorld(world);
            return w == null ? null : w.getBlockAt(x, y, z);
        }

        static Stored parse(String s) {
            String[] p = s.split(";", 5);
            if (p.length < 5) return null;
            try {
                return new Stored(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), p[4]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private final NerithMcSystem2 plugin;
    private final Map<String, Stored> doors = new LinkedHashMap<>();
    private final Map<String, Stored> rewards = new LinkedHashMap<>();
    private final List<ItemStack> rewardItems = new ArrayList<>();
    private long duration;
    private long remaining;
    private boolean open;

    public GateManager(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    public void load() {
        YamlConfiguration d = plugin.data();
        long def = Util.parseSeconds(plugin.getConfig().getString("gate.default-time", "30m"));
        duration = d.getLong("gate.time", def > 0 ? def : 1800);
        remaining = duration;
        open = false;
        doors.clear();
        rewards.clear();
        rewardItems.clear();
        for (String s : d.getStringList("gate.door")) {
            Stored st = Stored.parse(s);
            if (st != null) doors.put(st.key(), st);
        }
        for (String s : d.getStringList("gate.reward")) {
            Stored st = Stored.parse(s);
            if (st != null) rewards.put(st.key(), st);
        }
        List<?> items = d.getList("gate.reward-items", new ArrayList<>());
        for (Object o : items) {
            if (o instanceof ItemStack item) rewardItems.add(item);
        }
    }

    private void save() {
        YamlConfiguration d = plugin.data();
        d.set("gate.time", duration);
        List<String> door = new ArrayList<>();
        doors.values().forEach(s -> door.add(s.serialize()));
        List<String> reward = new ArrayList<>();
        rewards.values().forEach(s -> reward.add(s.serialize()));
        d.set("gate.door", door);
        d.set("gate.reward", reward);
        d.set("gate.reward-items", new ArrayList<>(rewardItems));
        plugin.saveData();
    }

    public void setDuration(long seconds) {
        duration = seconds;
        remaining = seconds;
        save();
    }

    /** @return number of blocks saved, or -1 if the selection is too large. */
    public int addDoor(Region r) { return add(doors, r); }

    public int addReward(Region r) { return add(rewards, r); }

    private int add(Map<String, Stored> target, Region r) {
        World w = Bukkit.getWorld(r.world);
        if (w == null) return 0;
        long volume = (long) (r.maxX - r.minX + 1) * (r.maxY - r.minY + 1) * (r.maxZ - r.minZ + 1);
        if (volume > plugin.getConfig().getInt("gate.max-blocks", 10000)) return -1;
        int count = 0;
        for (int x = r.minX; x <= r.maxX; x++) {
            for (int y = r.minY; y <= r.maxY; y++) {
                for (int z = r.minZ; z <= r.maxZ; z++) {
                    Block b = w.getBlockAt(x, y, z);
                    if (b.getType().isAir()) continue;
                    Stored st = new Stored(r.world, x, y, z, b.getBlockData().getAsString());
                    target.put(st.key(), st);
                    count++;
                }
            }
        }
        save();
        return count;
    }

    /** /gateremovedoor */
    public void clearDoors() {
        restoreDoors();
        doors.clear();
        save();
    }

    /** /gateremoveset */
    public void clearRewards() {
        rewards.clear();
        save();
    }

    public boolean isReward(Block b) {
        return rewards.containsKey(b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ());
    }

    public List<ItemStack> getRewardItems() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack i : rewardItems) copy.add(i.clone());
        return copy;
    }

    public void setRewardItems(List<ItemStack> items) {
        rewardItems.clear();
        rewardItems.addAll(items);
        save();
    }

    public void tick() {
        if (open) return;
        if (doors.isEmpty() && rewards.isEmpty()) return;
        remaining--;
        if (remaining <= 0) trigger();
    }

    private void trigger() {
        open = true;
        remaining = 0;

        for (Stored s : doors.values()) {
            Block b = s.block();
            if (b != null) b.setType(Material.AIR, false);
        }
        for (Stored s : rewards.values()) {
            place(s);
        }

        long delay = Math.max(1, plugin.getConfig().getInt("gate.close-delay-seconds", 6)) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreDoors();
            open = false;
            remaining = duration;
        }, delay);
    }

    private void restoreDoors() {
        for (Stored s : doors.values()) place(s);
    }

    private void place(Stored s) {
        Block b = s.block();
        if (b == null) return;
        try {
            b.setBlockData(Bukkit.createBlockData(s.data()), false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    /** Called on plugin disable so doors are never left open. */
    public void shutdown() {
        if (open) restoreDoors();
    }

    public String placeholder() {
        String notSet = Util.color(plugin.getConfig().getString("gate.not-set-text", "&7Not Set"));
        if (doors.isEmpty() && rewards.isEmpty()) return notSet;
        if (open) return Util.color(plugin.getConfig().getString("gate.open-text", "&aOpen"));
        return Util.formatTime(remaining);
    }
}
