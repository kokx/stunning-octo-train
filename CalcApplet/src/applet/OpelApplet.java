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
            default:
                //ISOException.throwIt(ISO7816.SW_WRONG_INS) ;
        }
    }
}
