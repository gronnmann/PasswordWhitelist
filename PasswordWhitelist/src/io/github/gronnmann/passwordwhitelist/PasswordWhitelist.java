package io.github.gronnmann.passwordwhitelist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class PasswordWhitelist extends JavaPlugin implements Listener{
	
	
	private FileConfiguration approved;
	private File approvedFile;
	
	private List<String> approvedList;
	
	private String password;
	
	public void onEnable() {
		
		if (!this.getDataFolder().exists())this.getDataFolder().mkdirs();
		
		approvedFile = new File(this.getDataFolder(), "approved.yml");
		
		if (!approvedFile.exists()) {
			try {
				approvedFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		approved = YamlConfiguration.loadConfiguration(approvedFile);
		
		approvedList = approved.getStringList("approved");
		
		saveDefaultConfig();
		
		if ((password = getConfig().getString("password")) == null) {
			password = "entry";
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
	}
	
	public void onDisable() {
		saveApproved();
	}
	
	
	private void saveApproved() {
		approved.set("approved", approvedList);
		
		try {
			approved.save(approvedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean approved(Player p) {
		return approved(p, true);
	}
	
	private boolean approved(Player p, boolean sendMsg) {
		String player = p.getUniqueId().toString();
		if (approvedList.contains(player))return true;
		else{
			if (sendMsg) {
				p.sendMessage(ChatColor.RED + "Please specify password to interact with the world.");
			}
			return false;
		}
	}
	
	@EventHandler
	public void joinManager(PlayerJoinEvent e) {
		if (!approved(e.getPlayer())) {
			e.getPlayer().setGameMode(GameMode.ADVENTURE);
		}
	}
	
	@EventHandler
	public void checkForPassword(AsyncPlayerChatEvent e) {
		if (!approved(e.getPlayer(), false) && e.getMessage().equals(password)) {
			approvedList.add(e.getPlayer().getUniqueId().toString());
			e.getPlayer().sendMessage(ChatColor.GREEN + "Verified successfully. Welcome :)");
			e.setCancelled(true);
			Bukkit.getScheduler().runTask(this, ()->{
				e.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode());
			});
			saveApproved();
		}
	}
	
	@EventHandler
	public void stopInteract(PlayerInteractEvent e) {
		if (!approved(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void stopDrop(PlayerDropItemEvent e) {
		if (!approved(e.getPlayer()))e.setCancelled(true);
	}
	
	@EventHandler
	public void stopPickup(PlayerPickupItemEvent e) {
		if (!approved(e.getPlayer()))e.setCancelled(true);
	}
	
	@EventHandler
	public void stopEntityTarget(EntityTargetEvent e) {
		if (!(e.getTarget() instanceof Player))return;
		Player p = (Player)e.getTarget();
		
		if (!approved(p))e.setCancelled(true);
	}
	
	@EventHandler
	public void stopAttack(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player))return;
		Player p = (Player)e.getDamager();
		
		if (!approved(p))e.setCancelled(true);
		
	}
	
	@EventHandler
	public void disallowCommands(PlayerCommandPreprocessEvent e) {
		if (!approved(e.getPlayer()))e.setCancelled(true);
	}
}
