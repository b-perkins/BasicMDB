package mdbdebug;
import com.microchip.mdbcs.Debugger;
import com.microchip.mdbcs.Finder.ToolType;
import com.microchip.mdbcs.MException;
import com.microchip.mplab.mdbcore.assemblies.Assembly;

public class MDBdebug {
    static int arbCount = 0;
    int progCount, debCount, secondsCount, failCount = 0;
    int breakpointOffset = 2;//amount of expected skid for breakpoints. I believe 16bit is 4
    long startTime, recordedProgrammingTime,resultingTimesMAX, resultingTimesNORM, resultingTimesMIN, resultingTimesCurrentGen = 0;
    static String tool = "REALICE";
    static String device1 = "PIC18F8722";
    static String device1Hex = "hexandelf\\PIC18F8722.hex";
    static String device2 = "PIC24FJ128GA010";
    static String device2Hex = "hexandelf\\PIC24FJ128GA010.hex";
    static String device3 = "PIC32MZ2048EFH100";
    static String device3Hex = "hexandelf\\PIC32MZ2048EFH100.hex";
    static String device4 = "PIC16F1709";
    static String device4Hex = "hexandelf\\PIC16F1709.hex";
    static String device5 = "PIC24FJ256GA110";
    static String device5Hex = "hexandelf\\PIC24FJ256GA110.hex";
    static String device6 = "PIC16F1509";
    static String device6Hex = "hexandelf\\PIC16F1509.hex";
     
    public static void main(String[] args) throws InterruptedException {
        MDBdebug m = new MDBdebug();
        if (args.length != 1)
            System.out.println("Incorrect usage: arg1 => run count");

        arbCount = Integer.parseInt(args[0], 10);
        if (tool.equals("ICD4")){
            m.resultingTimesMIN = m.runProgrammerICD4MIN(device6, device6Hex); // only for ICD4 (sets programming speed)
            m.resultingTimesNORM = m.runProgrammerICD4NORM(device6, device6Hex); // only for ICD4 (sets programming speed)
            m.resultingTimesMAX = m.runProgrammerICD4MAX(device6, device6Hex); // only for ICD4 (sets programming speed)
        }
        else
            m.resultingTimesCurrentGen = m.runProgrammer(device6, device6Hex); // for everything but ICD4

        System.out.println(m.failCount+" Failures total");
        if (tool.equals("ICD4")){
            System.out.println("Results for prog speed in ms:");
            System.out.println("MAX    NORM   MIN");
            System.out.println(m.resultingTimesMAX+"  "+m.resultingTimesNORM+"  "+m.resultingTimesMIN);
        }
        else {
            System.out.println("Results for REALICE prog speed:");  
            System.out.println(m.resultingTimesCurrentGen);
        }
        System.exit(0);
    }

    private long runProgrammer(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            Debugger p = new Debugger(device, ToolType.REALICE, Debugger.SessionType.PROGRAMMER); // hardcoded to ICD3
            p.connect();
            p.erase();
            p.loadFile(image);
            startTime= System.currentTimeMillis(); //reset timer
            p.program();
            recordedProgrammingTime = System.currentTimeMillis() - startTime;
            System.out.println("\nProgramming operation took: "+recordedProgrammingTime+" ms"); // TODO convert 1000 to float before division
            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
        return recordedProgrammingTime;
    }
    
    private long runProgrammerICD4MAX(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            recordedProgrammingTime = 0;
            Debugger p = new Debugger(device, ToolType.ICD4, Debugger.SessionType.PROGRAMMER); // hardcoded to ICD4
            Assembly assembly = p.getToolAssembly();
            assembly.GetToolProperties().setProperty("programoptions.pgmspeed", "Max");
            System.out.println("Using tool settings: "+p.getToolProperties());
            p.connect();
            p.erase();
            p.loadFile(image);
            startTime= System.currentTimeMillis(); //reset timer
            p.program();
            recordedProgrammingTime = System.currentTimeMillis() - startTime;
            System.out.println("\nProgramming operation took: "+recordedProgrammingTime+" ms");
            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
        return recordedProgrammingTime;
    }
    
        private long runProgrammerICD4NORM(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            recordedProgrammingTime = 0;
            Debugger p = new Debugger(device, ToolType.ICD4, Debugger.SessionType.PROGRAMMER); // hardcoded to ICD4
            Assembly assembly = p.getToolAssembly();
            assembly.GetToolProperties().setProperty("programoptions.pgmspeed", "Med"); // Med = "normal"
            System.out.println("Using tool settings: "+p.getToolProperties());
            p.connect();
            p.erase();
            p.loadFile(image);
            startTime= System.currentTimeMillis(); //reset timer
            p.program();
            recordedProgrammingTime = System.currentTimeMillis() - startTime;
            System.out.println("\nProgramming operation took: "+recordedProgrammingTime+" ms");
            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
        return recordedProgrammingTime;
    }
        
        private long runProgrammerICD4MIN(String device, String image) throws InterruptedException {
        try {
            System.out.println("\n---------------Connecting as PROGRAMMER----------------");
            recordedProgrammingTime = 0;
            Debugger p = new Debugger(device, ToolType.ICD4, Debugger.SessionType.PROGRAMMER); // hardcoded to ICD4
            Assembly assembly = p.getToolAssembly();
            assembly.GetToolProperties().setProperty("programoptions.pgmspeed", "Min");
            System.out.println("Using tool settings: "+p.getToolProperties());
            p.connect();
            p.erase();
            p.loadFile(image);
            startTime= System.currentTimeMillis(); //reset timer
            p.program();
            recordedProgrammingTime = System.currentTimeMillis() - startTime;
            System.out.println("\nProgramming operation took: "+recordedProgrammingTime+" ms");
            p.disconnect();
            p.destroy();
            System.out.println("\nDONE   - Programmer run count: "+(++progCount));
        } catch (MException ex) {
            System.err.println("Oops we died : " + ex.getMessage());
            failCount++;
        }
        return recordedProgrammingTime;
    }
}