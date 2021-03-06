package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * RoD event:
 * All players are tp'd to a start location
 * Once player enters win region they win event
 *
 * @author Nicbo
 * @since 2020-03-10
 */

public final class RoD extends InvadedEvent {
    private WorldGuardPlugin worldGuardPlugin;

    private BukkitRunnable didPlayerFinish;
    private ProtectedRegion winRegion;
    private Location startLoc;

    private final int TIME_LIMIT;

    public RoD() {
        super("Race of Death", "rod");

        this.worldGuardPlugin = plugin.getWorldGuardPlugin();
        String regionName = eventConfig.getString("win-region");
        try {
            this.winRegion = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("RoD region '" + regionName + "' does not exist");
        }

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);
        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");
    }

    @Override
    public void init() {
        initPlayerCheck();
        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (winRegion.contains(worldGuardPlugin.wrapPlayer(player).getPosition())) {
                        playerWon(player);
                        this.cancel();
                    }
                }
            }
        };
    }

    @Override
    public void start() {
        plugin.getServer().getScheduler().runTask(plugin, this::tpApplyInvisibility);
        didPlayerFinish.runTaskTimerAsynchronously(plugin, 0, 1);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        startTimer(TIME_LIMIT);
    }

    @Override
    public void over() {
        didPlayerFinish.cancel();
        playerCheck.cancel();
        eventTimer.cancel();;
    }

    private void tpApplyInvisibility() {
        for (Player player : players) {
             player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 1, false, false));
             player.teleport(startLoc);
        }
    }
}
