/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schoolattendance;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author root
 */
public class SchoolAttendance {
    private static final String APPLICATION_NAME =
        "Google Sheets API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = Quickstart.class.getResourceAsStream("client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    public static ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Application");
        //Configure Menu Items
        List<String> menuItems = new ArrayList<>();
        menuItems.add("Code Entry");
        menuItems.add("Name Selection");
        menuItems.add("Turn Off");
        
        
        final GpioController gpio = GpioFactory.getInstance();
        
        //Setup LCD display
        GpioLcdDisplay lcd = new GpioLcdDisplay(2, 16, RaspiPin.GPIO_01, RaspiPin.GPIO_04, RaspiPin.GPIO_26, RaspiPin.GPIO_27, RaspiPin.GPIO_28, RaspiPin.GPIO_29);
        GpioPinDigitalOutput backlight = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
        
        //Setup Keypad
        GpioPinDigitalOutput keypadTopRow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
        GpioPinDigitalOutput keypad2ndRow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, PinState.HIGH);
        GpioPinDigitalOutput keypad3rdRow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.HIGH);
        GpioPinDigitalOutput keypadBottomRow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, PinState.HIGH);
        GpioPinDigitalInput keypadLeftColumn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_UP);
        GpioPinDigitalInput keypadMiddleColumn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
        GpioPinDigitalInput keypadRightColumn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_UP);
        
        //Setup Buzzer
        GpioPinDigitalOutput buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, PinState.LOW);
        
        //Up and Down buttons
        GpioPinDigitalInput upButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, PinPullResistance.PULL_UP);
        GpioPinDigitalInput downButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_UP);

        //Select Button
        GpioPinDigitalInput selectButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_21, PinPullResistance.PULL_UP);
        
//        //Setup Register Pins
//        GpioPinDigitalOutput dataPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_31, PinState.LOW);
//        GpioPinDigitalOutput clockPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW);
//        GpioPinDigitalOutput latchPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_30, PinState.LOW);
        
        //Turn on LCD Screen and display inital output
        backlight.high();
        lcd.clear();
        lcd.setCursorPosition(0, 0);
        lcd.write(0, "     Welcome    ");
        lcd.write(1, "Please sign in..");
        String correctCode = "1";
        String enteredCode = "";
        Integer currentMenuEntry = 0;
        
        //Future variable to hold keypad button when used for code selection
        Future<Integer> buttonPressed = null;        
        buttonPressed = threadPool.submit(new KeypadReader(keypadTopRow, keypad2ndRow, keypad3rdRow, keypadBottomRow, keypadLeftColumn, keypadMiddleColumn, keypadRightColumn));
        /** Application name. */
        
        System.out.println("About to try google api");
        try {
            // Build a new authorized API client service.
            Sheets service = getSheetsService();

            // Prints the names and majors of students in a sample spreadsheet:
            // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
            String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            String range = "Class Data!A2:E";
            ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) {
                System.out.println("No data found.");
            } else {
              System.out.println("Name, Major");
              for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s, %s\n", row.get(0), row.get(4));
              }
            }
        } catch (Exception e) {
            System.out.println("There was an error: " + e.getMessage());
            e.printStackTrace();
        }
        while (true) {               
            
//            lcd.clear(1);
//            dateTime = LocalDateTime.now();
//            lcd.write(1, dateTime.format(DateTimeFormatter.ofPattern("    hh:mm:ss    ")));
//            Thread.sleep(1000);
//            if (upButton.isLow()) {
//                if (currentMenuEntry + 1 < menuItems.size()) {
//                    currentMenuEntry++;
//                    lcd.clear();
//                    lcd.write(0, "<              >");
//                    lcd.write(1, menuItems.get(currentMenuEntry));
//                } else {
//                    currentMenuEntry = 0;
//                    lcd.clear();
//                    lcd.write(0, "<              >");
//                    lcd.write(1, menuItems.get(currentMenuEntry));
//                }
//                while (upButton.isLow()) {
//                    Thread.sleep(100);
//                }
//            }
//            if (downButton.isLow()) {
//                if (currentMenuEntry - 1 >= 0) {
//                    currentMenuEntry--;
//                    lcd.clear();
//                    lcd.write(0, "<              >");
//                    lcd.write(1, menuItems.get(currentMenuEntry));
//                } else {
//                    currentMenuEntry = menuItems.size() - 1;
//                    lcd.clear();
//                    lcd.write(0, "<              >");
//                    lcd.write(1, menuItems.get(currentMenuEntry));
//                }
//                while (downButton.isLow()) {
//                    Thread.sleep(100);
//                }
//            }
//            if (selectButton.isLow()) {
//                if (menuItems.get(currentMenuEntry).equals("Code Entry")) {
//                    lcd.clear();
//                    lcd.write(1, "Enter Code...");
//                    buttonPressed = threadPool.submit(new KeypadReader(keypadTopRow, keypad2ndRow, keypad3rdRow, keypadBottomRow, keypadLeftColumn, keypadMiddleColumn, keypadRightColumn));
//                } else if (menuItems.get(currentMenuEntry).equals("Name Selection")) {
//                    lcd.clear();
//                    lcd.write(" Select User... ");
//                    if (buttonPressed != null) {
//                        buttonPressed.cancel(true);
//                    }
//                } else {
//                    lcd.clear();
//                    lcd.write("Exiting...");
//                    Thread.sleep(1000);
//                    lcd.clear();
//                    backlight.low();
//                    gpio.shutdown();
//                    System.exit(0);
//                }
//                while (selectButton.isLow()) {
//                    Thread.sleep(100);
//                }
//            }
            if (buttonPressed != null && !buttonPressed.isCancelled() && buttonPressed.isDone()) {
                Integer buttonValue = buttonPressed.get();
                buzzer.high();
                if (buttonValue == 10) {
                    lcd.clear(1);
                    lcd.write(1, "Exiting...");
                    buzzer.low();
                    Thread.sleep(1000);
                    lcd.clear();
                    backlight.low();
                    
                    gpio.shutdown();
                    System.exit(0);
                } else if (buttonValue == 11) {
                    if (enteredCode.equals(correctCode)) {
                        lcd.clear();
                        lcd.write(0, "      Hello     ");
                        lcd.write(1, "      Chris     ");
                        buzzer.low();
                        Thread.sleep(5000);
                        lcd.clear();
                        backlight.low();
                        gpio.shutdown();
                        System.exit(0);
                    } else {
                        lcd.clear();
                        lcd.write(0, "  Unrecognized  ");
                        lcd.write(1, "  Resetting...  ");
                        buzzer.low();
                        Thread.sleep(3000);
                        lcd.clear();
                        lcd.setCursorPosition(0, 0);
                        lcd.write(0, "     Welcome    ");
                        lcd.write(1, "Please sign in..");
                        enteredCode = "";
                    }
                } else {
                    lcd.clear(1);
                    lcd.write(1, "Entered: " + (enteredCode += buttonValue.toString()));
                    buzzer.low();
                }
                while (true) {
                    if (keypadLeftColumn.isHigh() && keypadMiddleColumn.isHigh() && keypadRightColumn.isHigh()) {
                        buttonPressed = threadPool.submit(new KeypadReader(keypadTopRow, keypad2ndRow, keypad3rdRow, keypadBottomRow, keypadLeftColumn, keypadMiddleColumn, keypadRightColumn));
                        break;
                    }
                }
            }
        }
        
    }
    
}
