package net.kunmc.lab.passengermobs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PassengerMobs extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PassengerMobsプラグインが有効になりました。");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    List<LivingEntity> entityList = world.getLivingEntities();
                    getLogger().info(String.valueOf(entityList.size()));
                    while (entityList.size() > 0){
                        entityList = onEnableInitialize(entityList);
                    }
                }
            }
        }.runTaskLater(this, 20);

        /*for (World world : Bukkit.getWorlds()) {
            List<LivingEntity> entityList = world.getLivingEntities();
            getLogger().info(String.valueOf(entityList.size()));
            for (LivingEntity creature : world.getLivingEntities()) {
                if (isCreaturePassenger(creature)) {
                    entityList.remove(creature);
                }
            }
            getLogger().info(String.valueOf(entityList.size()));
            while (entityList.size() > 0){
                entityList = onEnableInitialize(entityList);
                new BukkitRunnable() {
                    public void run() {}
                }.runTask(this);
            }
            *//*while (n > 0) {
                n = 0;
                getLogger().info(world.getEntities().toString());
                for (LivingEntity creature : world.getLivingEntities()) {
                    if (creature.getPassengers().size() == 0) {
                        if (!isCreaturePassenger(creature)) {
                            if(creaturePassage(creature)){
                                n++;
                            }
                        }
                    }
                }
                new BukkitRunnable() {
                    public void run() {}
                }.runTask(this);
            }*//*
        }*/

        getServer().getPluginManager().registerEvents(this, this);
    }

    private boolean isCreaturePassenger(LivingEntity creature) {
        for (LivingEntity entity : creature.getWorld().getLivingEntities()) {
            if (entity.getPassengers().contains(creature)) {
                return true;
            }
        }
        return false;
    }

    private List<LivingEntity> onEnableInitialize(List<LivingEntity> creatureList) {
        Double radius = Config.radius;
        LivingEntity creature = creatureList.get(0);
        if ((isCreaturePassenger(creature))){
            getLogger().info("isPassage");
            creatureList.remove(creature);
            return creatureList;
        }
        List<Entity> entityList = creature.getNearbyEntities(radius, radius, radius);
        List<Double> distanceList = new ArrayList<>();
        for (Entity entity : entityList) {
            if ((entity.getType() == creature.getType()) && (entity.getPassengers().size() == 0)) {
                distanceList.add(entity.getLocation().distance(creature.getLocation()));
            } else {
                distanceList.add(radius * radius * radius + 1D);
            }
        }

        if (Collections.min(distanceList) != radius * radius * radius + 1D) {
            getLogger().info("passage");
            Entity vehicle = entityList.get(distanceList.indexOf(Collections.min(distanceList)));
            vehicle.addPassenger(creature);
        }
        creatureList.remove(creature);
        return creatureList;
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;
        creaturePassage(e.getEntity());
    }

    private void creaturePassage(LivingEntity creature) {
        Double radius = Config.radius;
        List<Entity> entityList = creature.getNearbyEntities(radius, radius, radius);
        if (entityList.size() == 0)
            return;
        List<Double> distanceList = new ArrayList<>();
        for (Entity entity : entityList) {
            if ((entity.getType() == creature.getType()) && (entity.getPassengers().size() == 0)) {
                distanceList.add(entity.getLocation().distance(creature.getLocation()));
            } else {
                distanceList.add(radius * radius * radius + 1D);
            }
        }
        if (Collections.min(distanceList) != radius * radius * radius + 1D) {
            entityList.get(distanceList.indexOf(Collections.min(distanceList))).addPassenger(creature);
        }
        return;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("引数が足りません。");
            return false;
        } else if (args.length > 2) {
            sender.sendMessage("引数が多すぎます。");
            return false;
        }

        try {
            switch (args[0]) {
                case Const.COMMAND_RADIUS:
                    if (Double.valueOf(args[1]) >= 0D) {
                        Config.radius = Double.valueOf(args[1]);
                        sender.sendMessage("重ね範囲を " + args[1] + " に変更しました。");
                        return true;
                    } else {
                        sender.sendMessage("引数は0以上でなければなりません。");
                    }
                default:
                    sender.sendMessage("引数が不正です。");
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage("引数が不正です。");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1:
                completions.add(Const.COMMAND_RADIUS);
            case 2:
                if (args[0].equals(Const.COMMAND_RADIUS)) {
                    completions.add("<number>");
                }
        }

        return completions;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PassengerMobsプラグインが無効になりました。");
    }
}
