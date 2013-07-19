package net.sf.jcablib.test;

//
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import net.sf.jcablib.*;

/** Print all the cab information to System.out */
public
class CabInfoApp {
	public static void main(String[] args) {
		FileDialog		fd = new FileDialog(new Frame(), "Find a .cab file", FileDialog.LOAD);
		CabDump	ca;
		boolean			proceed = true;
		String			input = "";
		while(proceed) {
			fd.show();
			if(fd.getFile() != null) {
				try {
					ca = new CabDump(new File(fd.getDirectory(), fd.getFile()));
					ca.close();
					ca.reportHeader(System.out);
					ca.reportEntries(System.out);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
			System.out.println("Do another (y/n)?");
			DataInputStream dis = new DataInputStream(System.in);
			try {
				input = dis.readLine();
			} catch (Exception blah) {}
			if(input.toLowerCase().startsWith("n"))
				proceed = false;
		}
	}
}
