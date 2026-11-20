package com.nerithmc.system;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NerithMcSystem2 extends JavaPlugin {

    private File dataFile;
    private YamlConfiguration data;
    private SelectionManager selection;
    private KothManager koth;
    private GateManager gate;
    private GateRewardListener rewardGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dataFile = new File(getDataFolder(), "data.yml");
        data = YamlConfiguration.loadConfiguration(dataFile);

        selection = new SelectionManager(this);
        koth = new KothManager(this);
        gate = new GateManager(this);
        rewardGui = new GateRewardListener(this);
        koth.load();
        gate.load();

        Bukkit.getPluginManager().registerEvents(selection, this);
        Bukkit.getPluginManager().registerEvents(rewardGui, this);
        Bukkit.getPluginManager().registerEvents(new KillMessageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemDespawnListener(this), this);

        Commands commands = new Commands(this);
        for (String name : List.of("kothwand", "sethoth", "kothtime", "remkoth", "gatetime",
                "gatedoor", "gateremovedoor", "gateredward", "gateremoveset", "gateredwardgui",
                "ram", "cpu", "nerithmc")) {
            PluginCommand cmd = getCommand(name);
            if (cmd != null) cmd.setExecutor(commands);
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            koth.tick();
            gate.tick();
        }, 20L, 20L);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new NerithPlaceholders(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found. Scoreboard placeholders are disabled.");
        }
    }

    @Override
    public void onDisable() {
        if (gate != null) gate.shutdown();
        saveData();
    }

    public YamlConfiguration data() { return data; }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public SelectionManager selection() { return selection; }
    public KothManager koth() { return koth; }
    public GateManager gate() { return gate; }
    public GateRewardListener rewardGui() { return rewardGui; }
}
