package mineHead;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.text.DecimalFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
//Temp listeners for initial debugging only
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;

public class Main extends JavaPlugin implements Listener {
	
	private boolean calibrated = false;
	private byte calibrateStatus = 0b00000;
	private static final String leftCalibrate = "0b10000";
	private static final String rightCalibrate = "0b01000";
	private static final String upCalibrate = "0b00100";
	private static final String downCalibrate = "0b00010";
	private static final String middleCalibrate = "0b00001";
	String headStatus[] = {"",""};
	
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
    		getLogger().info("I've recognised a headCalibrate command");
    		if (args.length < 1) {
    	        sender.sendMessage("This command is angry, it needs an argument!");
    	        return false;
    	      } else if (args.length >1) {
    	        sender.sendMessage("Calm down, too many arguments!");
    	        return false;
    	      } else { 
    	    	  //Do stuff related to the command
    	    	  //Permitted arguments [up,down,left,right,middle]
    	    	  return true;
    	      }
    	//TODO Consider using headtracking [playername] [start,stop] from console 
    	//TODO Likewise handle headtracking [start,stop] or toggle from player
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
    	    	  if (checkCalibrated()) {
    	    		  sender.sendMessage("Head needs calibrating before use");
    	    		  return false;
    	    	  }
    	    	  //Do stuff related to the command/* Possibly use in future

    	    	  return true;
    	      }
    	}
            // If this hasn't happened the value of false will be returned.
    	return false; 
    }
 // Example listener method 
    @EventHandler
    public void onPlayerMoveEvent (final PlayerMoveEvent event) {
     // if (myNotifications[3].status) {
     //   final Player player = (Player)event.getOwner();
     //   final LivingEntity entity = (LivingEntity)event.getEntity();
     //  updateStatus(twitter, player.getName() + " tamed a " + entity.getCustomName());
     //} else {
     //    return;
     // }
      Location playerLocation = event.getPlayer().getLocation();
  	  DecimalFormat df = new DecimalFormat("#.##");
  	  headStatus[0] = df.format(playerLocation.getPitch());
  	  headStatus[1] = df.format(playerLocation.getYaw());
  	  getLogger().info(headStatus[0] + " teleported from X" + headStatus[1]);
     }
    
    private boolean checkCalibrated () {
    	if (calibrateStatus == 0b11111) {
    		return true;
    	} else {
    		return false;
    	}	
    }
}
