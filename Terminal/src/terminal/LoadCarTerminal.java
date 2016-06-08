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
public class LoadCarTerminal {

    static final byte[] OPEL_APPLET_AID = { (byte) 0x3B, (byte) 0x29,
            (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x02 };

    static final CommandAPDU SELECT_APDU = new CommandAPDU(
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, OPEL_APPLET_AID);
    
    boolean statusDoor = false;
    
    byte[] milleage = {'M', 'I', 'L'};

    CardChannel applet;

    public LoadCarTerminal() {
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
									testVerifyPin(c);
                                    loadCard(c);

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
    
    void loadCard(CardTerminal c){
    	byte[] data = {(byte) 0x00};
    	byte[] carID = {(byte)0x01,(byte) 0x57,
    		(byte) 0x3C,(byte) 0x83,(byte) 0xD9,(byte) 0x57, (byte)0x5C,(byte) 0xBB,(byte) 0xE3};
try{    	while (data[0] != 0x01 && data[0] != 0x02 && c.isCardPresent()){

    		ResponseAPDU accept = send((byte) 0x47, carID);
    		data = accept.getData();
		int starttime = ((carID[1]&0xFF) <<24 | (carID[2]&0xFF) << 16 | (carID[3]&0xFF) <<8 | (carID[4]&0xFF) );
		int endtime = ((carID[5]&0xFF) <<24 | (carID[6]&0xFF) << 16 | (carID[7]&0xFF) <<8 | (carID[8]&0xFF) );
		System.out.println("Car " + carID[0] + " is loaded on your card from " + starttime + " till " +endtime);}
} catch (CardException e) {System.out.println(e);}
    	
		if(data[0] == 0x02) {
			System.out.println("pin not verified");
		}
		if(data[0] == 0x01) {
			System.out.println("car loaded onto card");
		}
}
    
	void testVerifyPin(CardTerminal c){
		byte[] pin = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
		byte[] pin2 = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
		ResponseAPDU accept = send((byte) 0x43, pin);
		System.out.println("pin should be accepted thus 0x01");
		print (accept);
		System.out.println("try to load car after pin is verified");
		loadCard(c);
		System.out.println("remove card");
try{		while(c.isCardPresent()) {}

} catch (CardException e) {System.out.println(e);}
		System.out.println("insert card");
try{		while(!c.isCardPresent()) {}

} catch (CardException e) {System.out.println(e);}
		System.out.println("try to load car after reinjecting card \n pin should no longer be verified");
		loadCard(c);
		accept = send((byte) 0x43, pin2);
		System.out.println("pin should be invalid attempt 1 thus 0x00");
		print (accept);
		accept = send((byte) 0x43, pin2);
		System.out.println("pin should be invalid attempt 2 thus 0x00");
		print (accept);
		accept = send((byte) 0x43, pin2);
		System.out.println("pin should be invalid attempt 3 thus 0x00");
		print (accept);
		accept = send((byte) 0x43, pin2);
		System.out.println("pin limit should be exceeded thus 0x02");
		print (accept);
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
        new LoadCarTerminal();
    }
}
