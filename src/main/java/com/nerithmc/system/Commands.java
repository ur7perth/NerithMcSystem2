package com.nerithmc.system;

import com.sun.management.OperatingSystemMXBean;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.management.ManagementFactory;

public class Commands implements CommandExecutor {

    private final NerithMcSystem2 plugin;

    public Commands(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("nerithmc.admin")) return true;

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "kothwand" -> {
                if (sender instanceof Player p) p.getInventory().addItem(plugin.selection().createWand());
            }
            case "sethoth" -> {
                if (!(sender instanceof Player p)) return true;
                Region r = plugin.selection().getRegion(p);
                if (r != null) plugin.koth().setRegion(r);
            }
            case "remkoth" -> plugin.koth().removeRegion();
            case "kothtime" -> {
                long s = timeArg(args);
                if (s > 0) plugin.koth().setDuration(s);
            }
            case "gatetime" -> {
                long s = timeArg(args);
                if (s > 0) plugin.gate().setDuration(s);
            }
            case "gatedoor" -> {
                if (!(sender instanceof Player p)) return true;
                Region r = plugin.selection().getRegion(p);
                if (r != null) plugin.gate().addDoor(r);
            }
            case "gateredward" -> {
                if (!(sender instanceof Player p)) return true;
                Region r = plugin.selection().getRegion(p);
                if (r != null) plugin.gate().addReward(r);
            }
            case "gateremovedoor" -> plugin.gate().clearDoors();
            case "gateremoveset" -> plugin.gate().clearRewards();
            case "gateredwardgui" -> {
                if (sender instanceof Player p) plugin.rewardGui().openGui(p);
            }
            case "ram" -> ram(sender);
            case "cpu" -> cpu(sender);
            case "nerithmc" -> plugin.reloadConfig();
            default -> { return false; }
        }
        return true;
    }

    private long timeArg(String[] args) {
        if (args.length < 1) return -1;
        return Util.parseSeconds(args[0]);
    }

    private void line(CommandSender to, String text) {
        to.sendMessage(Util.comp(text));
    }

    private static String mb(long bytes) {
        return String.format("%.0f MB", bytes / 1024.0 / 1024.0);
    }

    private static String pct(double v) {
        return v < 0 ? "N/A" : String.format("%.1f%%", v * 100);
    }

    private void ram(CommandSender to) {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long allocated = rt.totalMemory();
        long used = allocated - rt.freeMemory();
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long sysTotal = os.getTotalMemorySize();
        long sysUsed = sysTotal - os.getFreeMemorySize();

        line(to, "&8&m      &r &bRAM Usage &8&m      ");
        line(to, "&7Server (JVM) used: &f" + mb(used) + " &7/ &f" + mb(max)
                + " &8(&e" + String.format("%.1f%%", used * 100.0 / max) + "&8)");
        line(to, "&7Server allocated: &f" + mb(allocated));
        line(to, "&7Machine used: &f" + mb(sysUsed) + " &7/ &f" + mb(sysTotal)
                + " &8(&e" + String.format("%.1f%%", sysUsed * 100.0 / sysTotal) + "&8)");
    }

    private void cpu(CommandSender to) {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        line(to, "&8&m      &r &bCPU Usage &8&m      ");
        line(to, "&7Processor: &f" + cpuModel(os));
        line(to, "&7Cores/Threads: &f" + Runtime.getRuntime().availableProcessors());
        line(to, "&7Server (JVM) usage: &e" + pct(os.getProcessCpuLoad()));
        line(to, "&7Machine usage: &e" + pct(os.getCpuLoad()));
    }

    private String cpuModel(OperatingSystemMXBean os) {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.startsWith("model name")) {
                    return l.substring(l.indexOf(':') + 1).trim();
                }
            }
        } catch (Exception ignored) {
        }
        String env = System.getenv("PROCESSOR_IDENTIFIER");
        if (env != null && !env.isBlank()) return env;
        return os.getArch();
    }
}
