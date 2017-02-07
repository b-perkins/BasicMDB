package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;
import com.microchip.mplab.mdbcore.assemblies.Assembly;

public class MDBdebug {
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount = 0;
    int breakpointOffset = 2;//amount of expected skid for breakpoints. I believe 16bit is 4
    long startTime, secondsCounter, BPresult = 0;
    static String device1 = "PIC16F18313"; // TODO update to a vali device and elf
    static String device1PROGimage = "./hexandelf/16F1828-blnkRB7-pin10.X.production.hex";
    static String device1DEBUGimage = "./hexandelf/16F18313-donor.X.debug.elf";
    static String device1breakpointName = "breakpoint1";
     
    public static void main(String[] args) throws InterruptedException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");

        arbCount = Integer.parseInt(args[0], 10);
        for (int temp = 1; temp <= arbCount; temp++){
            System.out.println("\nRunning cycle "+temp+" of "+arbCount+" and there have been "+m.failCount+" failures thus far");
            m.runDebugger(device1, device1DEBUGimage);
        }
        System.out.println(m.failCount+" Failures total");
        System.exit(0);
    }
    
    private void runDebugger(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as DEBUGGER----------------");
            Debugger d = new Debugger(device, ToolType.ICD3, Debugger.SessionType.DEBUGGER);
            //Assembly assembly = d.getToolAssembly();
            //assembly.GetToolProperties().setProperty("programoptions.pgmspeed", "Max");
            // AssemblyFactory  assemblyFactory = Lookup.getDefault().lookup(AssemblyFactory.class);
            // Assembly assembly = assemblyFactory.Create(device);
            //System.out.println("Using tool settings: "+d.getToolProperties());
            BPresult = 0;
            d.connect();
            d.erase();
            d.loadFile(image);
            d.program();
            System.out.println("Running");
            // d.setBP(0x3CB0);
            d.run();

            startTime = System.currentTimeMillis();
            secondsCounter = 0;
            while(d.isRunning()){
                if (System.currentTimeMillis() - startTime >= 1000){
                    secondsCount++; 
                    secondsCounter = System.currentTimeMillis(); //reset timer
                    System.out.print(secondsCounter+" Sec ");
                }
                if (secondsCount >= 3){ //lets give it 3 seconds to run
                    System.out.println("\nRan for 3 sec, halting & exiting");
                    d.halt();
                }
            }

            BPresult = d.getPC();
            System.out.println("\nHALTED @ line: "+d.getFileAndLineFromAddress(BPresult)+" or 0x"+Integer.toHexString((int)BPresult)+" or decimal: "+BPresult);
            if (BPresult > 0)
                System.out.println("Successfully ran and halted --- PASS"); 
            else{
                System.out.println("Either did not program or did not run -- FAIL"); 
                failCount++;
            }
            d.disconnect();
            d.destroy();
            
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
    }
}