package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;
import com.microchip.mplab.logger.MPLABLogger;
import java.util.logging.Level;

public class MDBdebug {
    boolean XLOG = false;  // used for setting Xlogging
    boolean MOATB = false; // for testing purposes, false = local, true = using moatb
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount, connectResult, BP0,  BP1 = 0;
    long startTime, secondsCounter, BP0result, BP1result = 0;
    static String device1 = "dsPIC33EV256GM006"; // TODO update to a valid device and elf
    static String device1DEBUGimage = "./hexandelf/33EV256GM006-FPtst.X.debug.elf";
    static String device1breakpointName = "breakpoint1"; // on line 84 of X project
     
    public static void main(String[] args) throws InterruptedException, MException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");

        if (m.XLOG){
            String XLogFileName = "./MPLABXLog_Finest.xml";
            System.out.println("Enabling MPLAB X Logging, Log file located at: "+ XLogFileName);
            MPLABLogger.setLogFileName(XLogFileName);
            MPLABLogger.setLogLevel(Level.FINEST);
        }
        
        arbCount = Integer.parseInt(args[0], 10);
        while(m.failCount == 0){ // just keep running until there are issues
            System.out.println("\nRunning cycle "+(++m.debCount)+" and there have been "+m.failCount+" failures thus far");
            m.runDebugger(device1, device1DEBUGimage);
        }

        System.out.println(m.failCount+" Failures total");
        System.exit(0);
    }
        
    private void runDebugger(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as DEBUGGER----------------");
            Debugger d = new Debugger(device, ToolType.ICD3, Debugger.SessionType.DEBUGGER);
            BP1result = 0;
            d.connect();
            d.erase();
            d.loadFile(image);
            d.program();
            System.out.println("Running for a few seconds");
            d.run();

            startTime = System.currentTimeMillis();
            secondsCounter = 0;
            while(d.isRunning() && secondsCounter <= 3){
                if ((System.currentTimeMillis() - startTime) >= (secondsCounter*1000)){
                    System.out.print(+(++secondsCounter)+"s.  ");
                }
            }
            d.halt();
            System.out.println("\nRan for 3 sec.. Now halting & setting breakpoint in tmr5.c line 154, PC @ 0x556");
            
            BP0 = d.setBP(0x556); // line 184 in tmr5.c
            System.out.println("BP0: "+BP0);
            d.run();
            d.sleep(3000); // small delay, otherwsie we go right past
            System.out.println("Running until the BP is encountered");
            while (d.isRunning());
            BP0result = d.getPC();
            System.out.println("\nHALTED @ line: "+d.getFileAndLineFromAddress(BP0result)+" or 0x"+Integer.toHexString((int)BP0result)+" or decimal: "+BP0result);
            if (BP0result <= 0x552 || BP0result >= 0x55a) // skid value of 4 on EV devices
                System.out.println("Successfully ran and halted @0x556 +-4 --- PASS"); 
            else{
                System.out.println("Either did not program or did not run -- FAIL"); 
                failCount++;
            }
            d.clearBP(0);
            
            System.out.println("\nSetting new BP at line 121 in tmr5.c, PC@ 0x454");
            BP1 = d.setBP(0x454); // set another breakpoint, line 121 in tmr5.c
            System.out.println("BP1: "+BP1);
            d.run();
            d.sleep(3000); // small delay, otherwsie we go right past
            System.out.println("Running until the BP is encountered");
            while (d.isRunning());
            BP1result = d.getPC();
            System.out.println("\nHALTED @ line: "+d.getFileAndLineFromAddress(BP1result)+" or 0x"+Integer.toHexString((int)BP1result)+" or decimal: "+BP1result);
            if (BP1result <= 0x450 || BP1result >= 0x458) // skid value of 4 on EV devices
                System.out.println("Successfully ran and halted @0x454 +-4 --- PASS"); 
            else{
                System.out.println("Either did not program or did not run -- FAIL"); 
                failCount++;
            }
            
            d.clearBP(1);
            d.disconnect();
            d.destroy();
            
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
    }
}