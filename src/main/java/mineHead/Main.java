package mineHead;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
//Temp listener for initial debugging only
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
//Temp listeners for initial debugging only
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;

public class Main extends JavaPlugin implements Listener {
	
	@Override
    public void onEnable() {
        // TODO Insert logic to be performed when the plugin is enabled
		getLogger().info("mineHead has invoked onEnable"); 
    }
    
    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    	getLogger().info("mineHead has invoked onDisable"); 
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("headcalibrate")) { 
    		// If the player typed /basic then do the following, note: If you only registered this executor for one command, you don't need this
    		getLogger().info("I've recognised a headCalibrate command");
    		if (args.length < 1) {
    	        sender.sendMessage("This command is angry, it needs an argument!");
    	        return false;
    	      } else if (args.length >1) {
    	        sender.sendMessage("Calm down, too many arguments!");
    	        return false;
    	      } else { 
    	    	  //Do stuff related to the command
    	    	  return true;
    	      }
    	} else if (cmd.getName().equalsIgnoreCase("headtracking")) {
    		//If this has happened the function will return true.
    		getLogger().info("I've recognised a headTracking command");
    		if (args.length < 1) {
    	        sender.sendMessage("This command is angry, it needs at least one argument!");
    	        return false;
    	      } else if (args.length >2) {
    	        sender.sendMessage("Calm down, too many arguments!");
    	        return false;
    	      } else { 
    	    	  //Do stuff related to the command
    	    	  return true;
    	      }
    	}
            // If this hasn't happened the value of false will be returned.
    	return false; 
    }
 // Example listener method 
    @EventHandler
    public void onEntityTame (final EntityTameEvent event) {
     // if (myNotifications[3].status) {
     //   final Player player = (Player)event.getOwner();
     //   final LivingEntity entity = (LivingEntity)event.getEntity();
     //  updateStatus(twitter, player.getName() + " tamed a " + entity.getCustomName());
     //} else {
     //    return;
     // }
     }
}
