package mineHead;

import java.nio.ByteBuffer;
//import java.util.concurrent.TimeUnit;

import org.usb4java.LibUsb;

public class Interface {
	public static void main(String args[]) throws InterruptedException {
		System.out.printf("Starting Interface");
		System.out.println();		
	}
	
	@SuppressWarnings("unused")
	private static void basicTest () {
		EV3 myEV3 = new EV3();
		ByteBuffer ops = ByteBuffer.allocateDirect(1);
		ops.put(EV3.opNop);
		myEV3.verbosity = false;		
		System.out.printf("Interface made verbosity false");
		System.out.println();
		myEV3.main(ops);
		myEV3.verbosity = true;
		System.out.printf("Interface made verbosity true");
		System.out.println();
		//EV3.connectUsb();
		myEV3.main(ops);
		myEV3.global = 6;		
		System.out.printf("Interface made global = 6");
		System.out.println();
		myEV3.main(ops);
		myEV3.local = 16;		
		System.out.printf("Interface made local = 16");
		System.out.println();
		myEV3.main(ops);
		System.out.println();
	}
	
	@SuppressWarnings("unused")
	private static void testSync() {
		EV3 myEV3 = new EV3();
		ByteBuffer ops = ByteBuffer.allocateDirect(1);
		ops.put(EV3.opNop);
		myEV3.sync_mode = EV3.SYNC;
		System.out.printf("sync_mode set to SYNC");
		System.out.println();
		myEV3.main(ops);
		System.out.println();
		System.out.println();
			
		myEV3.sync_mode = EV3.ASYNC;
		myEV3.global = 0;
		System.out.printf("sync_mode set to ASYNC, global memory set to 0");
		System.out.println();
		myEV3.main(ops);
		System.out.println();
		System.out.println();
		
		myEV3.global = 6;
		System.out.printf("sync_mode set to ASYNC, global memory set to 6");
		System.out.println();
		EV3.connectUsb();
		short counter1 = myEV3.sendDirectCmd(ops, myEV3.local, myEV3.global);
		short counter2 = myEV3.sendDirectCmd(ops, myEV3.local, myEV3.global);
		ByteBuffer reply1 = myEV3.waitForReply(myEV3.global, counter1);
		ByteBuffer reply2 = myEV3.waitForReply(myEV3.global, counter2);
		int received1 = 1019 - reply1.remaining();
		int received2 = 1019 - reply2.remaining();
		System.out.printf("received1 = " + received1);
		System.out.println();
		System.out.printf("received2 = " + received2);
		System.out.println();
		LibUsb.releaseInterface(EV3.handle, 0);
		LibUsb.close(EV3.handle);
		System.out.println();
		System.out.println();
		
		myEV3.sync_mode = EV3.STD;
		myEV3.global = 0;
		System.out.printf("sync_mode set to STD, global memory set to 0");
		System.out.println();
		myEV3.main(ops);
		System.out.println();
		System.out.println();
		myEV3.global = 6;
		System.out.printf("sync_mode set to STD, global memory set to 6");
		System.out.println();
		myEV3.main(ops);
	}
	
	@SuppressWarnings("unused")
	private static void testTones() {
		EV3 myEV3 = new EV3();
		myEV3.sync_mode = EV3.ASYNC;
		myEV3.playTone(1,262,500);
		myEV3.playTone(1,330,500);
		myEV3.playTone(1,392,500);
		myEV3.playTone(1,523,1000);
	}
	
	public static void disco() throws InterruptedException {
		EV3 myEV3 = new EV3();
		myEV3.verbosity = false;
		myEV3.sync_mode = EV3.ASYNC;
		System.out.print("[mineHead][Interface]Constructed EV3 object");
		myEV3.setLED(EV3.LED_RED);
		Thread.sleep(250);
		myEV3.setLED(EV3.LED_GREEN);
		Thread.sleep(250);
		myEV3.setLED(EV3.LED_ORANGE);
		Thread.sleep(250);
		myEV3.setLED(EV3.LED_RED_FLASH);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_GREEN_FLASH);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_ORANGE_FLASH);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_RED_PULSE);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_GREEN_PULSE);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_ORANGE_PULSE);
		Thread.sleep(500);
		myEV3.setLED(EV3.LED_OFF);
	}
	
	@SuppressWarnings("unused")
	private static void testButton() {
		EV3 myEV3 = new EV3();
		myEV3.sync_mode = EV3.SYNC;
		myEV3.buttonWait = true;
		myEV3.pressButton(EV3.BACK_BUTTON);
		myEV3.pressButton(EV3.RIGHT_BUTTON);
		myEV3.sync_mode = EV3.ASYNC;
		myEV3.pressButton(EV3.ENTER_BUTTON);
	}	
}
