package io.github.gronnmann.passwordwhitelist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
		String player = p.getName();
		if (approvedList.contains(player))return true;
		else return false;
	}
	
	
	@EventHandler
	public void joinManager(PlayerJoinEvent e) {
		if (!approved(e.getPlayer())) {
			e.getPlayer().sendMessage(ChatColor.RED + "Please specify password to get interaction access.");
		}
	}
	
	@EventHandler
	public void checkForPassword(AsyncPlayerChatEvent e) {
		if (!approved(e.getPlayer()) && e.getMessage().equals(password)) {
			approvedList.add(e.getPlayer().getName());
			e.getPlayer().sendMessage(ChatColor.GREEN + "Verified successfully. Welcome :)");
			e.setCancelled(true);
			
			saveApproved();
		}
	}
	
	@EventHandler
	public void stopInteract(PlayerInteractEvent e) {
		if (!approved(e.getPlayer())) {
			e.getPlayer().sendMessage(ChatColor.RED + "Please specify password to get interaction access.");
			e.setCancelled(true);
		}
	}
	
	
}
