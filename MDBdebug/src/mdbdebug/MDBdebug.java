package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;
import com.microchip.mplab.crownkingx.xPIC;
import com.microchip.mplab.mdbcore.assemblies.Assembly;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import moatbclient.Main;

public class MDBdebug {
    boolean MOATB = true; // for testing purposes, false = local, true = using moatb
    String sHost = "192.168.1.2"; // IP of moatb
    String sMB = "MTI006"; // hardcoding to mti6 since that is what spawned this test
    String sPort = "3"; // ICSP port # on moatb. 3 = ICD3
    String sFreq = "10"; // Hardocding moatb oscillator to 10 MHz
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount, connectResult = 0;
    int breakpointOffset = 2;//amount of expected skid for breakpoints. I believe 16bit is 4
    long startTime, secondsCounter, BPresult = 0;
    static String device1 = "PIC16F18313"; // TODO update to a vali device and elf
    static String device1PROGimage = "./hexandelf/16F1828-blnkRB7-pin10.X.production.hex";
    static String device1DEBUGimage = "./hexandelf/16F18313-donor.X.debug.elf";
    static String device1breakpointName = "breakpoint1";
     
    public static void main(String[] args) throws InterruptedException, MException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");

        arbCount = Integer.parseInt(args[0], 10);
        for (int temp = 1; temp <= arbCount; temp++){
            System.out.println("\nRunning cycle "+temp+" of "+arbCount+" and there have been "+m.failCount+" failures thus far");
            if (m.MOATB)
                m.connectResult = m.connectDeviceMOATB(device1);
            
            switch (m.connectResult){
                case 0: //success
                    System.out.println("Success");
                    m.runDebugger(device1, device1DEBUGimage);
                    break;
                case 1: //General failure... or board is busy, this is tricky.
                    System.out.println("General failure...");
                    break;
                case 2: //Not found on any MOATB
                    System.out.println("Not Found on any MOATB");
                    break;
                case 3://Found on different MOATB
                    System.out.println("Found On different MOATB.");
                    break;
            }
        }

        System.out.println(m.failCount+" Failures total");
        System.exit(0);
    }
    
    private int connectDeviceMOATB(String sDevice) throws MException{
        try {
            String arg_string = "/host "+this.sHost+" /connect_device "+sDevice+" "+this.sMB+":"+this.sPort+" 3300 10 M";
            String[] args = arg_string.split(" ");            
            //pipe output of client.main to a variable to parse
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(byteStream, true));
            Main.main(args);           
            String capturedText = byteStream.toString();      
            
            // Resets System.out back to the way it was.
            System.setOut(
            new PrintStream(
            new BufferedOutputStream(
            new FileOutputStream(
            java.io.FileDescriptor.out), 128), true));
            //-----------------------------------------
            System.out.println(arg_string);
            System.out.println(capturedText);
            if (capturedText.contains("Operation success")){
                return 0;
            }else if (capturedText.contains("Device not found")){
                return 2;                
            }else if (capturedText.contains("Device is on a different MOATB")){
                return 3;
            }else{
                return 1;
            }
        } catch (Throwable e1) {
            e1.printStackTrace();
            return 1;
        }
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