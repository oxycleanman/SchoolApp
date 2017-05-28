/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schoolattendance;

import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author root
 */
public class SchoolAttendance {
    
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
        lcd.write(0, "   Select an   ");
        lcd.write(1, "     Option     ");
        String correctCode = "1";
        String enteredCode = "";
        Integer currentMenuEntry = 0;
        
        //Future variable to hold keypad button when used for code selection
        Future<Integer> buttonPressed = null;        
        
        while (true) {           
            
            
//            dataPin.low();
//            clockPin.high();
//            clockPin.low();
//            dataPin.high();
//            clockPin.high();
//            clockPin.low();
//            dataPin.low();
//            clockPin.high();
//            clockPin.low();
//            dataPin.high();
//            clockPin.high();
//            clockPin.low();
//            dataPin.low();
//            clockPin.high();
//            clockPin.low();
//            dataPin.high();
//            clockPin.high();
//            clockPin.low();
//            dataPin.low();
//            clockPin.high();
//            clockPin.low();
//            dataPin.high();
//            clockPin.high();
//            clockPin.low();
//            latchPin.high();
//            latchPin.low();          
            
            
//            lcd.clear(1);
//            dateTime = LocalDateTime.now();
//            lcd.write(1, dateTime.format(DateTimeFormatter.ofPattern("    hh:mm:ss    ")));
//            Thread.sleep(1000);
            if (upButton.isLow()) {
                if (currentMenuEntry + 1 < menuItems.size()) {
                    currentMenuEntry++;
                    lcd.clear();
                    lcd.write(0, "<              >");
                    lcd.write(1, menuItems.get(currentMenuEntry));
                } else {
                    currentMenuEntry = 0;
                    lcd.clear();
                    lcd.write(0, "<              >");
                    lcd.write(1, menuItems.get(currentMenuEntry));
                }
                while (upButton.isLow()) {
                    Thread.sleep(100);
                }
            }
            if (downButton.isLow()) {
                if (currentMenuEntry - 1 >= 0) {
                    currentMenuEntry--;
                    lcd.clear();
                    lcd.write(0, "<              >");
                    lcd.write(1, menuItems.get(currentMenuEntry));
                } else {
                    currentMenuEntry = menuItems.size() - 1;
                    lcd.clear();
                    lcd.write(0, "<              >");
                    lcd.write(1, menuItems.get(currentMenuEntry));
                }
                while (downButton.isLow()) {
                    Thread.sleep(100);
                }
            }
            if (selectButton.isLow()) {
                if (menuItems.get(currentMenuEntry).equals("Code Entry")) {
                    lcd.clear();
                    lcd.write(1, "Enter Code...");
                    buttonPressed = threadPool.submit(new KeypadReader(keypadTopRow, keypad2ndRow, keypad3rdRow, keypadBottomRow, keypadLeftColumn, keypadMiddleColumn, keypadRightColumn));
                } else if (menuItems.get(currentMenuEntry).equals("Name Selection")) {
                    lcd.clear();
                    lcd.write(" Select User... ");
                    if (buttonPressed != null) {
                        buttonPressed.cancel(true);
                    }
                } else {
                    lcd.clear();
                    lcd.write("Exiting...");
                    Thread.sleep(1000);
                    lcd.clear();
                    backlight.low();
                    gpio.shutdown();
                    System.exit(0);
                }
                while (selectButton.isLow()) {
                    Thread.sleep(100);
                }
            }
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
                        lcd.write(0, "  Successfully  ");
                        lcd.write(1, "    unlocked    ");
                        buzzer.low();
                        Thread.sleep(5000);
                        lcd.clear();
                        backlight.low();
                        gpio.shutdown();
                        System.exit(0);
                    } else {
                        lcd.clear();
                        lcd.write(0, " Incorrect Code ");
                        lcd.write(1, "  Resetting...  ");
                        buzzer.low();
                        Thread.sleep(3000);
                        lcd.clear();
                        lcd.write(1, "  Enter Code... ");
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
