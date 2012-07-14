/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mctitan.redharvest.plugin;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.craftbukkit.entity.CraftEntity;

import com.massivecraft.factions.P;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Czahrien
 */
public class RedHarvestPlugin extends JavaPlugin {
    
    String netherFactionName;
    int horizontalAggressionDistance;
    int verticalAggressionDistance;
    
    @Override
    public void onEnable() {
        
        // Create the sponge recipe.
        ShapedRecipe sponge = new ShapedRecipe(new ItemStack(Material.SPONGE));
        sponge.shape("111", "121","111");
        sponge.setIngredient('1', Material.GOLD_NUGGET);
        sponge.setIngredient('2', Material.DIAMOND);
        
        getServer().addRecipe(sponge);
     
        getConfig().options().copyDefaults(true);
        saveConfig();
        netherFactionName = getConfig().getString("NetherFactionName");
        if(Factions.i.getByTag(netherFactionName) == null) {
            System.out.println("ERROR: The specified nether faction " + netherFactionName + " does not exist!");
        }
        horizontalAggressionDistance = getConfig().getInt("MobHorizontalAggressDistance");
        verticalAggressionDistance = getConfig().getInt("MobVerticalAggressDistance");
        
        EntityListener listener = new EntityListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        
        // check near players every second for mobs that attack based on the
        // player's faction
        getServer().getScheduler().scheduleSyncRepeatingTask(this, 
                new Runnable() {
                    @Override
                    public void run() {
                        specialAggressionCheck();
                    }
                }, 20, 20 );
    }
    
    @Override
    public void onDisable() {
        
    }
    
    // TODO: Ignore creative mode players, invisible/vanished players, etc.
    public void specialAggressionCheck() {        
        World w = getServer().getWorlds().get(0);
        for(LivingEntity ent : w.getLivingEntities()) {
            if(ent instanceof PigZombie) {
                if(ent instanceof PigZombie) {
                    PigZombie z = (PigZombie)ent;
                    if(z.getTarget() != null) {
                        for(Entity e : ent.getNearbyEntities(horizontalAggressionDistance, verticalAggressionDistance, horizontalAggressionDistance)) {
                            if(e instanceof Animals || e instanceof Golem ) {
                                z.setTarget((LivingEntity)e);
                                break;
                            }
                        }
                    }
                }
            } else if(ent instanceof Player) {
                Player p = (Player)ent;
                // player should not be in creative mode, the player should be alive,
                // and the player should not be invisible/vanished
                if(p.getGameMode() == GameMode.SURVIVAL && !p.isDead() && !p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    for(Entity e : p.getNearbyEntities(horizontalAggressionDistance, verticalAggressionDistance, horizontalAggressionDistance)) {
                        if((e instanceof PigZombie && !isNetherPlayer(p))) {
                            PigZombie z = (PigZombie)e;
                            if(z.getTarget() == null) {
                                z.setTarget(p);
                            }
                        } else if(isNetherPlayer(p)) {
                            if(e instanceof IronGolem) {
                                CraftIronGolem g = (CraftIronGolem)e;

                                g.setPlayerCreated(false);
                                if(((CraftIronGolem)e).getHandle().lastDamager == null) {
                                    ((net.minecraft.server.EntityLiving)((CraftIronGolem)e).getHandle()).a((net.minecraft.server.EntityLiving)((CraftPlayer)p).getHandle());
                                }
                            } else if(e instanceof Wolf) {
                                if(((CraftWolf)e).getHandle().lastDamager == null) {
                                    ((net.minecraft.server.EntityLiving)((CraftWolf)e).getHandle()).a((net.minecraft.server.EntityLiving)((CraftPlayer)p).getHandle());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public boolean isNetherPlayer(Player p) {
        FPlayer victim = FPlayers.i.get(p);
        Faction f = Factions.i.getByTag(netherFactionName);
        if(f == null) {
            return false;
        }
        return Factions.i.getByTag(netherFactionName).getFPlayers().contains(victim);
    }
}
