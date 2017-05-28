/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schoolattendance;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import java.util.concurrent.Callable;

/**
 *
 * @author root
 */
public class KeypadReader implements Callable<Integer>{
    
    GpioPinDigitalOutput keypadTopRow;
    GpioPinDigitalOutput keypad2ndRow;
    GpioPinDigitalOutput keypad3rdRow;
    GpioPinDigitalOutput keypadBottomRow;
    GpioPinDigitalInput keypadLeftColumn;
    GpioPinDigitalInput keypadMiddleColumn;
    GpioPinDigitalInput keypadRightColumn;

    public KeypadReader(GpioPinDigitalOutput keypadTopRow, GpioPinDigitalOutput keypad2ndRow, GpioPinDigitalOutput keypad3rdRow, GpioPinDigitalOutput keypadBottomRow, GpioPinDigitalInput keypadLeftColumn, GpioPinDigitalInput keypadMiddleColumn, GpioPinDigitalInput keypadRightColumn) {
        this.keypadTopRow = keypadTopRow;
        this.keypad2ndRow = keypad2ndRow;
        this.keypad3rdRow = keypad3rdRow;
        this.keypadBottomRow = keypadBottomRow;
        this.keypadLeftColumn = keypadLeftColumn;
        this.keypadMiddleColumn = keypadMiddleColumn;
        this.keypadRightColumn = keypadRightColumn;
    }
        
    @Override
    public Integer call() throws Exception {
        while(true) {
            this.keypadTopRow.low();
            if (this.keypadLeftColumn.isLow() || this.keypadMiddleColumn.isLow() || this.keypadRightColumn.isLow()) {
                if (this.keypadLeftColumn.isLow()) {
                    return 1;
                } else if (this.keypadMiddleColumn.isLow()) {
                    return 2;
                } else {
                    return 3;
                }
            } else {
                this.keypadTopRow.high();
                this.keypad2ndRow.low();
                if (this.keypadLeftColumn.isLow() || this.keypadMiddleColumn.isLow() || this.keypadRightColumn.isLow()) {
                    if (this.keypadLeftColumn.isLow()) {
                        return 4;
                    } else if (this.keypadMiddleColumn.isLow()) {
                        return 5;
                    } else {
                        return 6;
                    }
                } else {
                    this.keypad2ndRow.high();
                    this.keypad3rdRow.low();
                    if (this.keypadLeftColumn.isLow() || this.keypadMiddleColumn.isLow() || this.keypadRightColumn.isLow()) {
                        if (this.keypadLeftColumn.isLow()) {
                            return 7;
                        } else if (this.keypadMiddleColumn.isLow()) {
                            return 8;
                        } else {
                            return 9;
                        }
                    } else {
                        this.keypad3rdRow.high();
                        this.keypadBottomRow.low();
                        if (this.keypadLeftColumn.isLow() || this.keypadMiddleColumn.isLow() || this.keypadRightColumn.isLow()) {
                            if (this.keypadLeftColumn.isLow()) {
                                return 10;
                            } else if (this.keypadMiddleColumn.isLow()) {
                                return 0;
                            } else {
                                return 11;
                            }
                        } else {
                            this.keypadBottomRow.high();
                        }
                    }
                }
            }
        }
    }
    
    
    
    
}
