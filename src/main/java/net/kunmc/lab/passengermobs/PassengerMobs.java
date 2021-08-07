package net.kunmc.lab.passengermobs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class PassengerMobs extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PassengerMobsプラグインが有効になりました。");
        for (World world : Bukkit.getWorlds()) {
            List<LivingEntity> entityList = world.getLivingEntities();
            entityList.removeIf(this::isNotCreature);
            onEnableInitialize(entityList);
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    private void onEnableInitialize(List<LivingEntity> creatureList) {
        Double radius = Config.radius;

        creatureList.removeIf(this::isCreaturePassenger);
        if (creatureList.size() == 0) {
            return;
        }

        creatureList = creatureList.stream()
                .sorted(Comparator.comparingInt(this::getAllPassengersSize))
                .collect(Collectors.toList());
        LivingEntity creature = creatureList.get(0);

        Entity topPassenger = getTopPassenger(creature);
        double height = getPassengersHeight(creature);

        int i = 0;
        while (i == 0) {
            List<Entity> entityList = topPassenger.getNearbyEntities(radius, radius, radius);
            i++;
            for (Entity entity : entityList) {
                if ((entity.getType() == topPassenger.getType())
                        && (isCreatureVehicle(entity))
                        && (!isCreaturePassenger(entity))
                        && (topPassenger != entity)) {
                    double distance = entity.getLocation().distance(creature.getLocation().add(0D, height, 0D));
                    if (distance <= radius) {
                        topPassenger.addPassenger(entity);
                        topPassenger = entity;
                        height += entity.getHeight();
                        i = 0;
                        break;
                    }
                }

            }
        }

        creatureList.remove(creature);
        if (creatureList.size() > 0) onEnableInitialize(creatureList);
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
            double distance = entity.getLocation().distance(creature.getLocation());
            if ((entity.getType() == creature.getType())
                    && (isCreatureVehicle(entity))
                    && (distance <= radius)) {
                distanceList.add(distance);
            } else {
                distanceList.add(radius + 1D);
            }
        }
        if (Collections.min(distanceList) != radius + 1D) {
            entityList.get(distanceList.indexOf(Collections.min(distanceList))).addPassenger(creature);
        }
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
                    if (Double.parseDouble(args[1]) >= 0D) {
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

    private boolean isNotCreature(Entity entity) {
        return !(entity instanceof Creature);
    }

    private boolean isCreaturePassenger(Entity creature) {
        for (LivingEntity entity : creature.getWorld().getLivingEntities()) {
            if (entity.getPassengers().contains(creature)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCreatureVehicle(Entity creature) {
        return creature.getPassengers().size() == 0;
    }

    private Entity getTopPassenger(Entity creature) {
        while (creature.getPassengers().size() > 0) {
            creature = creature.getPassengers().get(0);
        }
        return creature;
    }

    private Double getPassengersHeight(Entity creature) {
        double height = 0D;
        while (creature.getPassengers().size() > 0) {
            creature = creature.getPassengers().get(0);
            height += creature.getHeight();
        }
        return height;
    }

    private int getAllPassengersSize(Entity creature) {
        int i = 0;
        while (creature.getPassengers().size() > 0) {
            creature = creature.getPassengers().get(0);
            i++;
        }
        return i;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PassengerMobsプラグインが無効になりました。");
    }
}
