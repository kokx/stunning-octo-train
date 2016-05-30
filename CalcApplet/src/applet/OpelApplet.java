package applet;

import javacard.framework.*;
import javacard.framework.service.*;

/**
 * Only use this to rent Opel Corsa's!
 *
 * @author The Opel Corsa Team
 */
public class OpelApplet extends Applet implements ISO7816 {

	private final static byte[] hello = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
	private final static byte[] hai = { 'H', 'a', 'i', ' ', 'h', 'a', 'i', ' ', 'h', 'a'};
	//private byte[] carID = {(byte)0x01,(byte) 0x57,(byte) 0x3C,(byte) 0x83,(byte) 0xD9,(byte) 0x57, (byte)0x5C,(byte) 0xBB,(byte) 0xE3};
	byte[] carID = new byte[9];
	private byte[] driveMilleage = {(byte) 0x00, (byte) 0x00, (byte) 0x00};
	private byte[] carMilleage = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	
    public OpelApplet() {
        register();
    }

    public static void install(byte[] buffer, short offset, byte length)
            throws SystemException {
        new OpelApplet();
    }

    //public boolean select() {
        //return true;
    //}

    public void process(APDU apdu)
    {
        byte[] buf = apdu.getBuffer() ;

        switch(buf[ISO7816.OFFSET_INS])
        {
            case 0x40:
                Util.arrayCopy(
                        hello,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)11);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 11);
                break;
            case 0x42:
                Util.arrayCopy(
                        hai,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)10);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 10);
                break;
            // send the carID, start data and end data to the terminal,
            // to verifie if we can open the car
            case 0x44:
                Util.arrayCopy(
                        carID,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)9);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 9);
				break;
			// get the current car milleage from the terminal and acknolege that the applet did
			// get the milleage. 
			case 0x45:
				byte[] accept = {0x01};
				carMilleage[0] = buf[OFFSET_CDATA];
        		carMilleage[1] = buf[OFFSET_CDATA +1];
        		carMilleage[2] = buf[OFFSET_CDATA +2];
                Util.arrayCopy(
                        accept,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)1);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 1);
                break;
             case 0x46:
             	byte[] accept2 = {0x01};
				int test123= 
					((buf[OFFSET_CDATA ]&0xFF) << 16 | (buf[OFFSET_CDATA +1]&0xFF) <<8 |
					 (buf[OFFSET_CDATA +2]&0xFF) ) -
					  ((carMilleage[0]&0xFF) << 16 | (carMilleage[1]&0xFF) <<8 | 
					  (carMilleage[2]&0xFF) );
				int drive = ((driveMilleage[0]&0xFF) << 16 | (driveMilleage[1]&0xFF) <<8 |
					 (driveMilleage[2]&0xFF) );
				drive += test123;
             	driveMilleage = new byte[] { (byte)(drive >>> 16),(byte)(drive >>> 8), (byte) drive, accept2[0]};
             	carMilleage[0] = buf[OFFSET_CDATA];
        		carMilleage[1] = buf[OFFSET_CDATA +1];
        		carMilleage[2] = buf[OFFSET_CDATA +2];
             	Util.arrayCopy(
                        driveMilleage,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)4);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 4);
                break;
             case 0x47:
				byte[] accept3 = {0x01};
				carID[0] = buf[OFFSET_CDATA +0];
				carID[1] = buf[OFFSET_CDATA +1];
				carID[2] = buf[OFFSET_CDATA +2];
				carID[3] = buf[OFFSET_CDATA +3];
				carID[4] = buf[OFFSET_CDATA +4];
				carID[5] = buf[OFFSET_CDATA +5];
				carID[6] = buf[OFFSET_CDATA +6];
				carID[7] = buf[OFFSET_CDATA +7];
				carID[8] = buf[OFFSET_CDATA +8];
                Util.arrayCopy(
                        accept3,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)1);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 1);
                break;
              case 0x48:
              	byte[] ticketMilleage = new byte[12];
		ticketMilleage[0] = carID[0];
		ticketMilleage[1] = carID[1];
		ticketMilleage[2] = carID[2];
		ticketMilleage[3] = carID[3];
		ticketMilleage[4] = carID[4];
		ticketMilleage[5] = carID[5];
		ticketMilleage[6] = carID[6];
		ticketMilleage[7] = carID[7];
		ticketMilleage[8] = carID[8];
		ticketMilleage[9] = driveMilleage[0];
                ticketMilleage[10] = driveMilleage[1];
                ticketMilleage[11] = driveMilleage[2];
		carID = new byte[9];
                Util.arrayCopy(
                        ticketMilleage,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)12);           
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 12);
                break;
            default:
                //ISOException.throwIt(ISO7816.SW_WRONG_INS) ;
        }
    }
}
