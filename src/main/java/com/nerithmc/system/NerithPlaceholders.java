package com.nerithmc.system;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class NerithPlaceholders extends PlaceholderExpansion {

    private final NerithMcSystem2 plugin;

    public NerithPlaceholders(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "nerithmc"; }

    @Override
    public @NotNull String getAuthor() { return "NerithMc"; }

    @Override
    public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "koth" -> plugin.koth().placeholder();
            case "gate" -> plugin.gate().placeholder();
            default -> null;
        };
    }
}
