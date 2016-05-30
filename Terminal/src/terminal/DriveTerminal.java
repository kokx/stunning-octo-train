package terminal;

import java.util.*;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * Opel corsa terminal.
 */
public class DriveTerminal {

    static final byte[] OPEL_APPLET_AID = { (byte) 0x3B, (byte) 0x29,
            (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x02 };

    static final CommandAPDU SELECT_APDU = new CommandAPDU(
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, OPEL_APPLET_AID);
    
    boolean statusDoor = false;
    
    byte[] milleage = {'M', 'I', 'L'};

    CardChannel applet;

    public DriveTerminal() {
        run();
    }

    public void run() {
        try {
            TerminalFactory tf = TerminalFactory.getDefault();
            CardTerminals ct = tf.terminals();
            List<CardTerminal> cs = ct.list(CardTerminals.State.CARD_PRESENT);
            if (cs.isEmpty()) {
                System.err.println("No terminals with a card found.");
                return;
            }

           // while (true) {
                try {
                    for(CardTerminal c : cs) {
                        if (c.isCardPresent()) {
                            try {
                                Card card = c.connect("*");
                                try {
                                    applet = card.getBasicChannel();
                                    ResponseAPDU resp = applet.transmit(SELECT_APDU);
                                    if (resp.getSW() != 0x9000) {
                                        throw new Exception("Select failed");
                                    }
                                    //setText(sendKey((byte) '='));
                                    //setEnabled(true);

                                    System.out.println("Card present and selected");
                                    
                                    startEngine();
                                    
                                    int startTime = (int)(System.currentTimeMillis()/100);
				    int i = 0;
                                    while (c.isCardPresent() && i<10){
                                    	int passedTime = (int)(System.currentTimeMillis()/100)
                                    		 - startTime;
                                    	if (passedTime > 1){
                                    		startTime = (int)(System.currentTimeMillis()/100);
                                    		// increaseMilleageTestData is a testfunction 
                                    		//to simulate the car sending his current millage.
                                    		increaseMilleageTestData();
                                    		updateMilleage();
						i++;
                                    	}
                                    	
                                    
                                    };
                                    break;
                                } catch (Exception e) {
                                    System.err.println("Card does not contain OpelApplet?!");
                                    continue;
                                }
                            } catch (CardException e) {
                                System.err.println("Couldn't connect to card!");
                                continue;
                            }
                        } else {
                            System.err.println("No card present!");
                            continue;
                        }
                    }
                } catch (CardException e) {
                    System.err.println("Card status problem!");
                }
           // }
        } catch (Exception e) {
            //setEnabled(false);
            //setText(MSG_ERROR);
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    void increaseMilleageTestData() {
    	milleage[2]++;
    	if(milleage[2] == 0x00) {
    		milleage[1]++;
    		if(milleage[1] == 0x00) {
    		milleage[0]++;
    		}
    	}
    }
    
    void updateMilleage(){
    	byte[] data = {(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
    	while (data[3] != 0x01){
    		ResponseAPDU accept = send((byte) 0x46, milleage);
    		data = accept.getData();
		System.out.println("Mileage was updated:");
		System.out.print(data[0]);
		System.out.print(data[1]);
		System.out.print(data[2]);
		System.out.println("");
    	}
    }
    
    
    void startEngine(){
    	byte[] data = {(byte) 0x00};
    	while (data[0] != 0x01){
    		ResponseAPDU accept = send((byte) 0x45, milleage);
    		data = accept.getData();
    		System.out.println(data[0]);
    	}
    }
    

    void print(ResponseAPDU apdu) {
        byte[] data = apdu.getData();
        for (byte d : data) {
            System.out.print((byte) d);
        }
        System.out.println();
    }

    public ResponseAPDU sendKey(byte ins) {
        CommandAPDU apdu = new CommandAPDU(0, ins, 0, 0, 5);
        try {
            return applet.transmit(apdu);
        } catch (CardException e) {
            return null;
        }
    }
    
    public ResponseAPDU send(byte ins, byte[] data) {
        CommandAPDU apdu = new CommandAPDU(0, ins, 0, 0, data);
        try {
            return applet.transmit(apdu);
        } catch (CardException e) {
            return null;
        }
    }

    public static void main(String[] arg) {
        new DriveTerminal();
    }
}
