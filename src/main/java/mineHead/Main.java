package mineHead;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.text.DecimalFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin implements Listener {

	private boolean ready = false;
	private byte calibrateStatus = 0b00000;
	private static final byte leftCalibrate = 0b10000;
	private static final byte rightCalibrate = 0b01000;
	private static final byte upCalibrate = 0b00100; 
	private static final byte downCalibrate = 0b00010;
	private static final byte middleCalibrate = 0b00001;
	//TODO Check what values types are returned by EV3
	private float left;
	private float right;
	private float up;
	private float down;
	private float middle;
	//Dummy variable to simulate value from EV3
	private float valueFromEV3;
	String newHeadStatus[] = {"",""};
	String oldHeadStatus[] = {"",""};
	
	@Override
    public void onEnable() {
		//TODO Force recalibration on reload, possibly replace by reading from config file
		calibrateStatus = 0b00000;
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
    	    	switch (args[0].toLowerCase()) {
    	    		case "up": 	calibrateStatus = (byte)(calibrateStatus + upCalibrate);
    	    		//Store up data
    	    		up = valueFromEV3;
    	    		//return true;
    	    		break;
    	    		case "down": 	calibrateStatus = (byte)(calibrateStatus + downCalibrate);
    	    		//Store down data
    	    		down = valueFromEV3;
    	    		//return true;
    	    		break;
    	    		case "left": 	calibrateStatus = (byte)(calibrateStatus + leftCalibrate);
    	    		//Store left data
    	    		left = valueFromEV3;
    	    		//return true;
    	    		break;
    	    		case "right": 	calibrateStatus = (byte)(calibrateStatus + rightCalibrate);
    	    		//Store right data
    	    		right = valueFromEV3;
    	    		//return true;
    	    		break;
    	    		case "middle": 	calibrateStatus = (byte)(calibrateStatus + middleCalibrate);
    	    		//Store middle data
    	    		middle = valueFromEV3;
    	    		//return true;
    	    		break;  
    	    		default:	sender.sendMessage(args[0] + " is not a valid argument.");
    	    		return false;
    	    	}
    	    	return true; 
    	      }  		
    	//TODO Consider using headtracking [playername] [start,stop] from console 
    	//TODO Likewise, handle headtracking [start,stop] or toggle from player
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
    	    	  if (!checkCalibrated(sender)) {
    	    		  sender.sendMessage("Head needs calibrating before use");
    	    		  return false;
    	    	  }
    	    	  ready = true;
    	    	  //Do stuff related to the command 
    	    	  return true;  	      }
    	}
        // If this hasn't happened the value of false will be returned.
    	sender.sendMessage("This is not a recognised command");
    	return false; 
    }
    
 // Detect head movement
    @EventHandler
    private void onPlayerMoveEvent (final PlayerMoveEvent event) {
    	if (ready) {
      Location playerLocation = event.getPlayer().getLocation();
      Player player = event.getPlayer();
  	  DecimalFormat df = new DecimalFormat("#.##");
  	  newHeadStatus[0] = df.format(playerLocation.getPitch());
  	  newHeadStatus[1] = df.format(playerLocation.getYaw());
  	  if (newHeadStatus == oldHeadStatus) {
  		getLogger().info(player + " has moved but head has not");
  	  } else {
  	  getLogger().info(player + "moved their head\n" + "Pitch is " + newHeadStatus[0] + "\n Yaw is " + newHeadStatus[1]);
  	  }
  	  oldHeadStatus = newHeadStatus;
    	} else {
    		getLogger().info("Not calibrated and initialised.");
    	}
    }
    
//Test if head is calibrated before initialising, indicate status if not 
    private boolean checkCalibrated (CommandSender sender) {
    	if (calibrateStatus == 0b11111) {
    		return true;
    	} else {
    		//TODO Send a message displaying calibration status
    		sender.sendMessage("Left is missing right is not etc");
    		return false;
    	}	
    }
}
