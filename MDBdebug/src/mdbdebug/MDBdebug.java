package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;

public class MDBdebug {
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount, usingSWBP = 0;
    int breakpointOffset = 0;//amount of expected skid. I belive 16bit is 4
    long seconds, BPexpected, BPresult = 0;
    long BP_at_PC = 0x7F6;
    static String device = "PIC16F1828";
    static String PROGimage = "./hexandelf/16F1828-blnkRB7-pin10.X.production.hex";
    static String DEBUGimage = "./hexandelf/16F1828-blnkRB7-pin10.X.debug.elf";
    static String breakpointName = "breakpoint1";
    
    public static void main(String[] args) throws InterruptedException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");
        arbCount = Integer.parseInt(args[0], 10);
        for (int temp = 1; temp <= arbCount; temp++){
            System.out.println("\nRunning cycle "+temp+" of "+arbCount+" and there have been "+m.failCount+" failures thus far");
            if (temp % 2 == 0) //just an easy way to alternate btw prog/debug
                m.runProgrammer();
            else
                m.runDebugger();
        }
        System.out.println(m.failCount+" Failures total");
        System.exit(0);
    }
    
    private void runProgrammer() throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            Debugger p = new Debugger(device, ToolType.ICD3, Debugger.SessionType.PROGRAMMER);
            p.connect();
            p.erase();
            p.program(PROGimage);

            System.out.println("Running for a few seconds. LED should be blinking");
            seconds = System.currentTimeMillis(); //reset timer
            while(System.currentTimeMillis() - seconds <= 2200){} //lets give it ~2 sec

            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
    }

    private void runDebugger() throws InterruptedException{
        try {
            System.out.println("\n---------------Connecting as DEBUGGER----------------");
            Debugger d = new Debugger(device, ToolType.ICD3, Debugger.SessionType.DEBUGGER);
            d.connect();
            d.erase();
            d.program(DEBUGimage);//MUST BE AN ELF FILE
           // BPexpected = d.getSymbolAddress(breakpointName);//doesnt seems to work for some 8-bit
            BPexpected = BP_at_PC;
            System.out.println("Setting breakpoint, expected location: 0x"+Integer.toHexString((int)BPexpected)+" or decimal: "+BPexpected);
            d.EnableSWBreakpoints();
            d.setSWBP(BP_at_PC);//there are 2 version of setBP, one for symbols (string), one for address (long)
            System.out.println("Running to breakpoint. LED should be lit");
            d.run();

            seconds = System.currentTimeMillis(); //reset timer
            secondsCount = 0;
            while(d.isRunning()){
                if (System.currentTimeMillis() - seconds >= 1000){
                    secondsCount++; 
                    seconds = System.currentTimeMillis(); //reset timer
                    System.out.print(secondsCount+" Sec ");
                }
                if (secondsCount >= 5){ //lets give it 5 seconds to hit the breakpoint
                    System.out.println("\nFailed to halt via BP in 5 sec, halting manually");
                    d.halt();
                }
            }

            BPresult = d.getPC();
            System.out.println("\nHALTED @ line: "+d.getFileAndLineFromAddress(BPresult)+" or 0x"+Integer.toHexString((int)BPresult)+" or decimal: "+BPresult);
            if (BPresult == (BPexpected + breakpointOffset))
                System.out.println("Halted at expected location --------- PASS");
            else{
                System.out.println("Failed to halt at expected location --------- FAIL");
                System.out.println("Is the skid-offset set correctly? dsPIC - 4, 32MZ - 1, cooper - 1, SWBP - 0");
                failCount++;
            }

           // d.DisableSWBreakpoints();
            d.disconnect();
            d.destroy();
            System.out.println("DONE   - Debug run count: "+(++debCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
    }
}