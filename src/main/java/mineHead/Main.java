package mineHead;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
//import java.text.DecimalFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
//import org.bukkit.command.ConsoleCommandSender;
//import java.util.List;

public class Main extends JavaPlugin implements Listener {
	
	//Usually false by default but set to true for testing purposes
	private boolean running;
	String calibrateName[] = {"left", "right", "up", "down", "middle"};
	// Forces recalibration upon load (set to true for testing)
	Boolean calibrateStatus[] = {true, true, true, true, true};
	//TODO Check what values types are returned by EV3
	private float left;
	private float right;
	private float up;
	private float down;
	private float middle;
	//Dummy variable to simulate value from EV3
	private float valueFromEV3 = 666;
	private Player activePlayer;
	private int threshold = 5;
	float oldPitch;
	float oldYaw;
	private boolean hasMoved = false;
	
	@Override
    public void onEnable() {
		//Rest active player on reload
		running = false;
		activePlayer = null;		
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("[mineHead]mineHead has invoked onEnable"); 
    }
    
    @Override
    public void onDisable() {
    	getLogger().info("[mineHead]mineHead has invoked onDisable"); 
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
    	    		case "left": 	
    	    			calibrateStatus[0] = true;
    	    			//Store left data
    	    			left = valueFromEV3;
    	    			return true;
    	    			//break;
    	    		case "right": 	
    	    			calibrateStatus[1] = true;
    	    			//Store right data
    	    			right = valueFromEV3;
    	    			return true;
    	    			//break;
    	    		case "up": 	
    	    			calibrateStatus[2] = true;
    	    			//Store up data
    	    			up = valueFromEV3;
    	    			return true;
    	    			//break;
    	    		case "down": 	
    	    			calibrateStatus[3] = true;
    	    			//Store down data
    	    			down = valueFromEV3;
    	    			return true;
    	    			//break;
    	    		case "centre": 	
    	    			calibrateStatus[4] = true;
    	    			//Store middle data
    	    			middle = valueFromEV3;
    	    			return true;
    	    			//break;  
    	    		default:	
    	    			sender.sendMessage(args[0] + " is not a valid argument.");
    	    			return false; 
    	    	}
    	    }
    	//Handle headtracking [start,stop] from player
    	} else if (cmd.getName().equalsIgnoreCase("headtracking")) {
    		getLogger().info("I've recognised a headTracking command");
    		//If not calibrated then not worth expending clock cycles
    		if (!checkCalibrated(sender)) {
	    		  sender.sendMessage("Head needs calibrating before use");
	    		  return false;
	    	} else if (args.length < 1) {
	    		sender.sendMessage("This command is angry, it needs an argument!");
	    		return false;
	    	} else if (args.length >1) {
	    		sender.sendMessage("Calm down, too many arguments!");
	    		return false;
	    	} 
    		getLogger().info("Calibrated and correct number of arguments, checking if player");
    		if (sender instanceof Player) {
	    		getLogger().info("Sent by a player, checking arguments");
	    		//Sender is a player
	    		if (args[0].equalsIgnoreCase("start")) {
	    			getLogger().info("Recognised start");
	    			//if (activePlayer.isEmpty()) {
	    			if (activePlayer == null) {	
	    				getLogger().info("Attempting to start player");
	    				activePlayer = (Player)sender;
	    				running = true;
	    				getLogger().info("Active player is " + activePlayer.getName());
	    				sender.sendMessage("Headtracking started.");
	    				return true;
	    			} else {
	    				sender.sendMessage(activePlayer.getName() + "is already using this."); 
	    				return false;
	    			}
	    		} else if (args[0].equalsIgnoreCase("stop")) {
	    			getLogger().info("Recognised stop");
	    			if (running == false) {
	    				sender.sendMessage("Already stopped.");
	    				return false;
	    			} else if (activePlayer == sender) {
	    				getLogger().info("Stopping " + sender.getName()); 
	    				activePlayer = null;
	    				running = false;
	    				sender.sendMessage("Stopping.");
	    				return true;
	    			} else {
	    				sender.sendMessage(activePlayer.getName() + "is already using this."); 
	    				return false;
	    			}
	    		} else {
		    		// Not a recognised argument
		    		sender.sendMessage("Not a recognised argument");
		    		sender.sendMessage("Command should be in the format headTracking [start/stop]");
		    		return false;
		    	}
    		} else {    
	    		// Must have come from some other entity
    			sender.sendMessage("Not a player that sent this request, ignoring");
    			return false;
	    	}  
	    } else if (cmd.getName().equalsIgnoreCase("disco")) {
	        	getLogger().info("[mineHead]I've recognised a disco command");
	        	//Don't bother checking arguments, don't need any, not worth it
	        	try {
					Interface.disco();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	getLogger().info("[mineHead]We just wanted you to get down"); 
	        	return true;
	    } else if (cmd.getName().equalsIgnoreCase("headThreshold")) { 
    		getLogger().info("I've recognised a headThreshold command");
    		if (args.length < 1) {
    	        sender.sendMessage("This command is angry, it needs an argument!");
    	        return false;
    	    } else if (args.length >1) {
    	        sender.sendMessage("Calm down, too many arguments!");
    	        return false;
    	    } else if (isInteger(args[0])){  
    	    	int tempThreshold = Integer.parseInt( args[0] );
    	    	if (tempThreshold < 1 || tempThreshold > 20) {
    	    		sender.sendMessage(tempThreshold + " is out of the valid range of 1~20.");
    	    		return false;  
    	    	} else {
    	    		sender.sendMessage("Threshold successfully set to " + tempThreshold);
    	    		threshold = tempThreshold;
    	    		return true;
    	    	}
    	    } else {
    	    		sender.sendMessage(args[0] + " is not a valid argument.");
    	    		return false;  
    	    }
    	} else {    
    		// Otherwise not a recognised command
    		sender.sendMessage("This is not a recognised command");
			return false; 
    	}  
    }
    
// Detect head movement
    @EventHandler
    public void onPlayerMoveEvent (final PlayerMoveEvent event) {
    	//Check if calibrated and initialised
    	if (running) {
    		//TODO Change this to use getFrom() and getTo()
    		Player currentPlayer = event.getPlayer();  
    		//Ignore if not initialised player
    		if (currentPlayer == activePlayer) {
        		if (!hasMoved) {
        			//Handle case where this is first movement
        			Location oldLocation = event.getFrom();
        			//Yaw 0/360 degrees is South, 180 is North, East is 270,West is 90
        			//Pitch -90 degrees is up, +90 degrees is down
            		oldPitch = oldLocation.getPitch();
            		oldYaw  = oldLocation.getYaw();
            		hasMoved = true;
        		} 
        		//else ignore oldLocation
        		Location newLocation = event.getTo();
        		float newPitch = newLocation.getPitch();
        		float newYaw  = newLocation.getYaw();
        		//Ignore if head has not moved
    			if (oldPitch == newPitch && oldYaw == newYaw) {
    				currentPlayer.sendMessage(currentPlayer.getName() + " has moved but head has not");
    			//Otherwise register head movement
    			} else if (Math.abs(oldPitch - newPitch) > threshold || Math.abs(oldYaw - newYaw) > threshold) {
    				currentPlayer.sendMessage("Old pitch is " + oldPitch + ", old yaw is " + oldYaw);
            		currentPlayer.sendMessage("New pitch is " + newPitch + ", new yaw is " + newYaw);
    				//TODO Code subsequent actions
            		currentPlayer.sendMessage("Perform action based on movement");
            		oldPitch = newPitch;
            		oldYaw = newYaw;currentPlayer.sendMessage("Movement below threshold level for change, ignoring");
    			} else {
    				//currentPlayer.sendMessage("Movement below threshold level for change, ignoring");
    			}
    		} else {
    			//Test line for debugging
    			//getLogger().info("Not activated player.");
    		}
    	} else {	
    		//Test line for debugging
        	//getLogger().info("Not calibrated and/or initialised.");
        }
    }
    
//Test if head is calibrated before initialising, indicate status if not 
    private boolean checkCalibrated (CommandSender sender) {
    	int calibrateCount = 0;
    	String returnStatus = "";
    	for (int i=0; i<calibrateStatus.length; i++){
    		if (calibrateStatus[i]) {
    			calibrateCount++;
    		} else {
    			returnStatus = returnStatus + calibrateName[i];
    		}
    	}
    	if (calibrateCount == 5) {
    		return true;
    	} else {
    		returnStatus = "The following need calibrating" + returnStatus;
    		sender.sendMessage(returnStatus);
    		return false;
    	}	
    }
    
    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
