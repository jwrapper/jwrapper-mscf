/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jcablib;

import java.io.*;
import java.util.Date;

/**
 * static utilities class for type conversion
 * @author dvh
 */
public class CabUtils {
    
    public static final byte kNil = 0x00;
    
    /**
    * Converts DOS time to Java time (number of milliseconds since epoch)
    * XXX deprecated API
    */
    public static long dosToJavaTime(long dtime) {
	Date d = new Date((int)(((dtime >> 25) & 0x7f) + 80),
			  (int)(((dtime >> 21) & 0x0f) - 1),
			  (int)((dtime >> 16) & 0x1f),
			  (int)((dtime >> 11) & 0x1f),
			  (int)((dtime >> 5) & 0x3f),
			  (int)((dtime << 1) & 0x3e));
	return d.getTime();
    }

    /**
    * Converts Java time to DOS time. XXX deprecated API
    */
    public static long javaToDosTime(long time) {
	Date d = new Date(time);
	int year = d.getYear() + 1900;
	if (year < 1980) {
	    return (1 << 21) | (1 << 16);
	}
	return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
           d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
           d.getSeconds() >> 1;
    }
    

    /**
    * Rotate an int's byte order; switch big to little or little to big endian
    * @param
    * @return
    */
    public static int rotateInt(int i) {
        int[] bytes = new int[4];
        bytes[0] = (i >> 24) & 0x000000FF;
        bytes[1] = (i >> 8) & 0x0000FF00;
        bytes[2] = (i << 8) & 0x00FF0000;
        bytes[3] = (i << 24) & 0xFF000000;
        return bytes[0] + bytes[1] + bytes[2] + bytes[3];
    }

    /**
    * Rotate an short's byte order; switch big to little or little to big endian
    * @param s 
    * @return
    */
    public static short rotateShort(short s) {
        short[] bytes = new short[2];
        bytes[0] = (short)(s >> 8);
        bytes[1] = (short)(s << 8);
        return (short)(bytes[0] + bytes[1]);
    }

    /**
    * Converts c string to java string
    * XXX this is absolutely boneheaded code
    * @param in
    * @return 
    * @exception IOException Thrown by DataInput.
    */
    public static String readCString(DataInput in)
    throws IOException {
        int	i = 0;
        byte[]	temp = new byte[256];

        while(true) {
            temp[i] = in.readByte();
            if(temp[i] == kNil || i>255)
                break;
            i++;
        }
        return new String(temp).substring(0, i);
    }
    
    /**
     * Writes java string to c string in dataoutput.
     * @param inString
     * @param out
     * @throws java.io.IOException
     */
    public static void writeCString(String inString, DataOutput out)
    throws IOException{
        byte[]  buf;
        if(inString.length() > 255)
            buf = inString.substring(0,255).getBytes();
        else
            buf = inString.getBytes();
        out.write(buf);
        out.writeByte(kNil);
    }
    
}
