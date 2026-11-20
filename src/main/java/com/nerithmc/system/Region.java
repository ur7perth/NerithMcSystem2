package com.nerithmc.system;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Region {

    public final String world;
    public final int minX, minY, minZ, maxX, maxY, maxZ;

    public Region(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = world;
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }

    public static Region of(Location a, Location b) {
        return new Region(a.getWorld().getName(),
                Math.min(a.getBlockX(), b.getBlockX()), Math.min(a.getBlockY(), b.getBlockY()), Math.min(a.getBlockZ(), b.getBlockZ()),
                Math.max(a.getBlockX(), b.getBlockX()), Math.max(a.getBlockY(), b.getBlockY()), Math.max(a.getBlockZ(), b.getBlockZ()));
    }

    public boolean contains(Location l) {
        if (l.getWorld() == null || !l.getWorld().getName().equals(world)) return false;
        int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void save(ConfigurationSection s) {
        s.set("world", world);
        s.set("minX", minX); s.set("minY", minY); s.set("minZ", minZ);
        s.set("maxX", maxX); s.set("maxY", maxY); s.set("maxZ", maxZ);
    }

    public static Region load(ConfigurationSection s) {
        if (s == null || !s.contains("world")) return null;
        return new Region(s.getString("world"),
                s.getInt("minX"), s.getInt("minY"), s.getInt("minZ"),
                s.getInt("maxX"), s.getInt("maxY"), s.getInt("maxZ"));
    }
}
