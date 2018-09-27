package mineHead;
	
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ByteOrder;

public class EV3 {
	static final short ID_VENDOR_LEGO = (short) 0x0694;
	static final short ID_PRODUCT_EV3 = (short) 0x0005;
	static final byte  EP_IN          = (byte)  0x81;
	static final byte  EP_OUT         = (byte)  0x01;
	
	//Operations
	static final byte  opNop                  	= (byte)  0x01;
	static final byte  opProgram_Start 			= (byte)  0x03;
	static final byte  opUI_Write 				= (byte)  0x82;
	static final byte  opUI_Button 				= (byte)  0x83;
	static final byte  opUI_Draw 				= (byte)  0x84;
	static final byte  opTimer_Wait 			= (byte)  0x85;
	static final byte  opTimer_Ready 			= (byte)  0x86;
	static final byte  opSound 					= (byte)  0x94; 
	static final byte  opSound_Ready 			= (byte)  0x96;
	static final byte  opFile 					= (byte)  0xC0;
	static final byte  opCom_Set 				= (byte)  0xD4;
 
	//Commands
	static final byte  UPDATE  					= (byte)  0x00;
	static final byte  BREAK 					= (byte)  0x00;
	static final byte  TONE 					= (byte)  0x01;
	static final byte  PLAY 					= (byte)  0x02;
	static final byte  REPEAT					= (byte)  0x03;
	static final byte  LINE						= (byte)  0x03;
	static final byte  WAIT_FOR_PRESS			= (byte)  0x03;
	static final byte  PRESS					= (byte)  0x05;
	static final byte  LOAD_IMAGE 				= (byte)  0x08;
	static final byte  SET_BRICKNAME 			= (byte)  0x08;
	static final byte  TOPLINE 					= (byte)  0x12;
	static final byte  FILLWINDOW 				= (byte)  0x13;
	static final byte  LED 						= (byte)  0x1B;
	static final byte  BMPFILE 					= (byte)  0x1C;
	
	//Buttons
	static final byte  NO_BUTTON 				= (byte)  0x00;
	static final byte  UP_BUTTON 				= (byte)  0x01;
	static final byte  ENTER_BUTTON 			= (byte)  0x02;
	static final byte  DOWN_BUTTON 				= (byte)  0x03;
	static final byte  RIGHT_BUTTON 			= (byte)  0x04;
	static final byte  LEFT_BUTTON 				= (byte)  0x05;
	static final byte  BACK_BUTTON 				= (byte)  0x06;
	static final byte  ANY_BUTTON 				= (byte)  0x07;
	
	//Patterns
	static final byte 	LED_OFF 				= (byte)  0x00;
	static final byte 	LED_GREEN 				= (byte)  0x01;
	static final byte   LED_RED 				= (byte)  0x02;
	static final byte   LED_ORANGE 				= (byte)  0x03;
	static final byte 	LED_GREEN_FLASH 		= (byte)  0x04;
	static final byte   LED_RED_FLASH 			= (byte)  0x05;
	static final byte   LED_ORANGE_FLASH 		= (byte)  0x06;
	static final byte 	LED_GREEN_PULSE 		= (byte)  0x07;
	static final byte   LED_RED_PULSE 			= (byte)  0x08;
	static final byte   LED_ORANGE_PULSE 		= (byte)  0x09;
	
	//Communications
	static final byte  DIRECT_COMMAND_REPLY     = (byte)  0x00;
	static final byte  DIRECT_COMMAND_NO_REPLY  = (byte)  0x80;
	static final byte  STD 						= (byte)  0x00;
	static final byte  SYNC 					= (byte)  0x01;
	static final byte  ASYNC 					= (byte)  0x02;
	static final byte  LCS_Leading 				= (byte)  0x84;
	static final byte  LCS_Trailing				= (byte)  0x00;

	static DeviceHandle handle;
	static private short counter = 41;
	Boolean verbosity = true;
	Boolean buttonWait = true;
	int local = 0;
	int global = 0;
	byte sync_mode = SYNC;

	public static void connectUsb () {
		System.out.print("[mineHead][EV3]Attempting to connect to USB");
		int result = LibUsb.init(null);
		Device device = null;
		DeviceList list = new DeviceList();
		result = LibUsb.getDeviceList(null, list);
		if (result < 0){
			throw new RuntimeException("Unable to get device list. Result=" + result);
		}
		boolean found = false;
		for (Device dev: list) {
			DeviceDescriptor descriptor = new DeviceDescriptor();
			result = LibUsb.getDeviceDescriptor(dev, descriptor);
			if (result != LibUsb.SUCCESS) {
				throw new LibUsbException("Unable to read device descriptor", result);
			}
			if (  descriptor.idVendor()  == ID_VENDOR_LEGO
			|| descriptor.idProduct() == ID_PRODUCT_EV3) {
				device = dev;
				found = true;
				break;
			}
		}
		LibUsb.freeDeviceList(list, true);
		if (! found) throw new RuntimeException("Lego EV3 device not found.");
		
		handle = new DeviceHandle();
		result = LibUsb.open(device, handle);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to open USB device", result);
		}
		boolean detach = LibUsb.kernelDriverActive(handle, 0) != 0;

		if (detach) result = LibUsb.detachKernelDriver(handle, 0);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to detach kernel driver", result);
		}

		result = LibUsb.claimInterface(handle, 0);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to claim interface", result);
		}
	}

	public short sendDirectCmd (ByteBuffer operations,int local_mem, int global_mem) {
		counter++;
		ByteBuffer buffer = ByteBuffer.allocateDirect(operations.position() + 7);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort((short) (operations.position() + 5));   // length
		buffer.putShort((short) counter);                       // counter
		if (sync_mode == SYNC || global_mem > 0) {
			buffer.put(DIRECT_COMMAND_REPLY);  					// type
		} else {
			buffer.put(DIRECT_COMMAND_NO_REPLY); 				// type
		}
		buffer.putShort((short) (local_mem*1024 + global_mem)); // header
		for (int i=0; i < operations.position(); i++) {         // operations
			buffer.put(operations.get(i));	
			IntBuffer transferred = IntBuffer.allocate(1);
			int result = LibUsb.bulkTransfer(handle, EP_OUT, buffer, transferred, 100); 
			if (result != LibUsb.SUCCESS) {
				throw new LibUsbException("Unable to write data", transferred.get(0));
			}
		}
		if (verbosity) {
			printHex("Sent", buffer);
		} else {
			//System.out.print("Suppressing sent message");
			//System.out.println();
		}
		return counter;
	}			
	
	public ByteBuffer waitForReply (int global_mem, short counter) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		IntBuffer transferred = IntBuffer.allocate(1);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int result = LibUsb.bulkTransfer(handle, EP_IN, buffer, transferred, 100);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to read data", result);
		}
		buffer.position(global_mem + 5);
		if (verbosity) {
			printHex("Recv", buffer);
		} else {
			System.out.print("Suppressing recv message");
			System.out.println();
		}
	return buffer;
	}

	public static void printHex(String desc, ByteBuffer buffer) {
		System.out.print(desc + " 0x|");
		for (int i= 0; i < buffer.position() - 1; i++) {
			System.out.printf("%02X:", buffer.get(i));
		}
		System.out.printf("%02X|", buffer.get(buffer.position() - 1));
		System.out.println();
	}
	
	public static byte[] LCS(String name) {
		int length = name.length();
		//System.out.print("Name length is " + length);
		//System.out.println();
		byte[] array = new byte[length+2];
		array[0] = LCS_Leading;
		byte[] tempName = name.getBytes();
		System.arraycopy(tempName, 0, array, 1, length);
		array[length+1] = LCS_Trailing;
		return array;
	}
	
	public static byte[] LCX(int value) {
		int mag = Math.abs(value);
		byte[] array = null;
		if (mag > 32767) {
			//32 bit LC4, 5 byte leading 0x83 VVVV VVVV VVVV VVVV VVVV VVVV SVVV VVVV	
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.order(ByteOrder.LITTLE_ENDIAN); 
			buffer.putInt(value);
			byte[] tempArray = buffer.array();
			array = new byte[5];
			array[0] = (byte) 0x83;
			System.arraycopy(tempArray, 0, array, 1, 4);
		} else if (mag >127) {
			//16 bit LC2, 3 byte leading 0x82 then VVVV VVVV SVVV VVVV
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.order(ByteOrder.LITTLE_ENDIAN); 
			buffer.putInt(value);
			byte[] tempArray = buffer.array();
			array = new byte[3];
			array[0] = (byte) 0x82;
			System.arraycopy(tempArray, 0, array, 1, 2);	
		} else if (mag > 31) {
			//8 bit LC1, 2 byte leading 0x81 then SVVV VVVV
			array = new byte[2];
			array[0] = (byte) 0x81;
			array[1] = (byte) value;	
		} else {
			//5 bit LC0, 2 bit leading 0b00 then SV VVVV
			array = new byte[1];
			array[0] = (byte) value;
		}
		return array;
	}
	
	public static byte[] LVX(int value) {
		int mag = Math.abs(value);
		byte[] array = null;
		if (mag > 65536) {
			//32 bit address, 5 byte leading 0xC3 VVVV VVVV VVVV VVVV VVVV VVVV VVVV VVVV	
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.order(ByteOrder.LITTLE_ENDIAN); 
			buffer.putInt(value);
			byte[] tempArray = buffer.array();
			array = new byte[5];
			array[0] = (byte) 0xC3;
			System.arraycopy(tempArray, 0, array, 1, 4);
		} else if (mag > 255) {
			//16 bit address, 3 byte leading 0xC2 then VVVV VVVV VVVV VVVV
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.order(ByteOrder.LITTLE_ENDIAN); 
			buffer.putInt(value);
			byte[] tempArray = buffer.array();
			array = new byte[3];
			array[0] = (byte) 0xC2;
			System.arraycopy(tempArray, 0, array, 1, 2);	
		} else if (mag > 31) {
			//8 bit address, 2 byte leading 0xC1 then VVVV VVVV
			array = new byte[2];
			array[0] = (byte) 0xC1;
			array[1] = (byte) value;	
		} else {
			//5 bit address, 3 bit leading 0b010 then V VVVV
			array = new byte[1];
			array[0] = (byte) (value + 0b01000000);
		}
		return array;
	}

	public void main (ByteBuffer operations) {
		try {
			connectUsb();

			//Sends operation and returns counter for referencing
			short msg_count = sendDirectCmd(operations, local, global);

			//Gets reply based on message message count
			if (sync_mode == SYNC || (sync_mode == STD && global > 0)) {
				ByteBuffer reply = waitForReply(global, msg_count);
				int returnedCounter =  reply.get(2);
				while (returnedCounter != msg_count) {
					System.out.print("Reply rejected as returned counter is  " + returnedCounter + " and msg_count is " + msg_count + "\n");
					ByteBuffer unusedReply = waitForReply(global, msg_count);
					System.out.print("Test message 1 \n");
					returnedCounter =  unusedReply.get(2);					
				}
				int received = 1019 - reply.remaining();
				System.out.print("Received " + received + " integers in reply.");
				System.out.println();
			} else {
				//System.out.print("Not waiting for reply as either ASYNC or STD with global = 0");
				//System.out.println();
			}
			LibUsb.releaseInterface(handle, 0);
			LibUsb.close(handle);
		} catch (Exception e) {
	     e.printStackTrace(System.err);
		}
	}
	
	public void playTone (int volume, int frequency, int duration) {
		byte[] myLCXVolume = EV3.LCX(volume);
		byte[] myLCXFreq = EV3.LCX(frequency);
		byte[] myLCXDuration = EV3.LCX(duration);
		int parameterLengths = myLCXVolume.length + myLCXFreq.length + myLCXDuration.length + 3;
		ByteBuffer operations = ByteBuffer.allocateDirect(parameterLengths);
		operations.put(opSound);
		operations.put(TONE);
		operations.put(myLCXVolume);
		operations.put(myLCXFreq);
		operations.put(myLCXDuration);
		operations.put(opSound_Ready);
		main(operations);
	}
	
	public void setBrickName(String brickName) {
		int brickNameLength = brickName.length();
		ByteBuffer ops = ByteBuffer.allocateDirect(brickNameLength + 4);
		ops.put(EV3.opCom_Set);
		ops.put(EV3.SET_BRICKNAME);
		byte[] myLCSName = EV3.LCS(brickName);
		for (int i=0; i < myLCSName.length; i++) {
			ops.put(myLCSName[i]);
		}
		main(ops);
	}
	
	public void setLED(Byte type) {
		ByteBuffer ops = ByteBuffer.allocateDirect(3);
		ops.put(opUI_Write);
		ops.put(LED);
		ops.put(type);
		main(ops);
	}
	
	public void pressButton(Byte button) {
		int bufferLength;
		if (buttonWait) {
			bufferLength = 5;
		} else {
			bufferLength = 3;
		}
		ByteBuffer ops = ByteBuffer.allocateDirect(bufferLength);
		ops.put(opUI_Button);
		ops.put(PRESS);
		ops.put(button);		
		if (buttonWait) {
		ops.put(opUI_Button);	
		ops.put(WAIT_FOR_PRESS); 
		}
		main(ops);
	}	
	
}