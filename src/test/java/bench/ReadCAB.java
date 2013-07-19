package bench;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import net.sf.jcablib.CabEntry;
import net.sf.jcablib.CabFile;
import net.sf.jcablib.CabFileInputStream;
import net.sf.jcablib.CabFolder;

public class ReadCAB {

	public static void main(String[] args) throws Exception {

		String source = "/tmp/oracle/jre-6u34-windows-i586.cab";
		String target = "./target/result.zip";

		CabFile cabFile = new CabFile(source);

		CabEntry[] entryList = cabFile.getEntries();
		for (CabEntry entry : entryList) {
			System.out.println("entry=" + entry.getName());
		}

		CabEntry entry = cabFile.getEntry("core.zip");
		CabFolder cabFolder = entry.getCabFolder();

		CabFileInputStream input = new CabFileInputStream(cabFolder);

		OutputStream output = new FileOutputStream(target);
		
		IOUtils.copy(input, output);

	}

}
