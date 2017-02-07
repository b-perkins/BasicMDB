package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;

public class MDBdebug {
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount = 0;
    long startTime, BPexpected, BPresult = 0;
    static String device1 = "PIC16F1459"; // just hardcoded. clunky but simple
    static String device2 = "PIC24FJ256GB110";
    static String device3 = "PIC18F46J50";
        
    public static void main(String[] args) throws InterruptedException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");
        arbCount = Integer.parseInt(args[0], 10);
        for (int temp = 1; temp <= arbCount; temp++){
            System.out.println("\nBeginning cycle "+temp+" of "+arbCount+" and there have been "+m.failCount+" failures thus far");
            m.runProgrammer(device1); // TODO account for major fails
            m.runProgrammer(device2);
            m.runProgrammer(device3);
        }
        System.out.println(m.failCount+" Failures total");
        System.exit(0);
    }

    private void runProgrammer(String device) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            Debugger p = new Debugger(device, ToolType.PICKIT3, Debugger.SessionType.PROGRAMMER);
            p.connect();
          //p.erase();
            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
    }
}