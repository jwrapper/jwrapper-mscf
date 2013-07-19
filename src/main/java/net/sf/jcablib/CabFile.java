package net.sf.jcablib;/** CabLib, a library for extracting MS cabinets* Copyright (C) 1999, 2002  David V. Himelright** This library is free software; you can redistribute it and/or* modify it under the terms of the GNU Library General Public* License as published by the Free Software Foundation; either* version 2 of the License, or (at your option) any later version.** This library is distributed in the hope that it will be useful,* but WITHOUT ANY WARRANTY; without even the implied warranty of* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU* Library General Public License for more details.** You should have received a copy of the GNU Library General Public* License along with this library; if not, write to the* Free Software Foundation, Inc., 59 Temple Place - Suite 330,* Boston, MA  02111-1307, USA.** David Himelright can be reached at:* <dhimelright@gmail.com> */import java.io.*;import java.util.*;import java.util.zip.*;/*** Presents cabinet files via the familar java.util.zip interface.* @author David Himelright <a href="mailto:dhimelright@gmail.com">dhimelright@gmail.com</a>*/publicclass CabFile {	    // Globals --------------------    public    CabEntry[]	entries;    CabFolder[]	folders;    File	mFile;    // Cab data -------------------    public    short   cab_version,            cab_folders,            cab_files,            cab_flags,            cab_setid,                      //cab set id            cab_icab;                       //zero-based cabinet number (huh???)    public    int     cab_signature,                  //file signature 'MSCF'            cab_header_checksum,	            cab_file_size,            cab_folder_checksum,            cab_entry_offset,               //offset of first cab entry            cab_file_checksum,            file_size;    public     boolean hasprev = false,            hasnext = false;    String  prevname,            nextname,            prevnum,            nextnum;    /**    * @param inFile A reference to a Cabinet file.    * @exception CabException When the file header is malformed.    * @exception IOException thrown by the IO classes.    */    public CabFile(File inFile)    throws IOException {        this.mFile = inFile;        readHeader();    }    /**    * @param name A path to a Cabinet file.    * @exception CabException When the file header is malformed.    * @exception IOException thrown by the IO classes.    */    public CabFile(String name)    throws IOException {        this(new File(name));    }    /**    * @exception CabException When the file header is malformed.    * @exception IOException thrown by the IO classes.    */    private synchronized void readHeader()    throws IOException {        RandomAccessFile raf = new RandomAccessFile(mFile, "r");        CabFolder crapFolder = new CabFolder(null, 0, (short)0, CabConstants.kInvalidFolder, -1);        file_size = (int)raf.length();        //read HEADER        cab_signature		= raf.readInt();        cab_header_checksum	= raf.readInt();        cab_file_size		= CabUtils.rotateInt(raf.readInt());        cab_folder_checksum	= raf.readInt();        cab_entry_offset	= CabUtils.rotateInt(raf.readInt());        cab_file_checksum	= raf.readInt();        cab_version		= CabUtils.rotateShort(raf.readShort());        cab_folders		= CabUtils.rotateShort(raf.readShort());        cab_files		= CabUtils.rotateShort(raf.readShort());        cab_flags		= CabUtils.rotateShort(raf.readShort());        cab_setid		= CabUtils.rotateShort(raf.readShort());        cab_icab		= CabUtils.rotateShort(raf.readShort());        //detect errors        if(cab_signature != CabConstants.MSCF)            throw new CabException(mFile.getName() + " has an invalid signature.");        if(cab_files < 1 || cab_folders < 1)            throw new CabException(mFile.getName() + " has less than zero files (invalid header).");        //read series data (if a cab is a part of an installer or series)        if((CabConstants.kFlagHasPrev & cab_flags) == CabConstants.kFlagHasPrev) {            this.hasprev = true;            prevname = CabUtils.readCString(raf);            prevnum = CabUtils.readCString(raf);        }        if((CabConstants.kFlagHasNext & cab_flags) == CabConstants.kFlagHasNext) {            this.hasnext = true;            nextname = CabUtils.readCString(raf);            nextnum = CabUtils.readCString(raf);        }        if((CabConstants.kFlagReserve & cab_flags) == CabConstants.kFlagReserve)                raf.seek(cab_entry_offset - (8 * cab_folders));        //read cabfolders (compressed chunks)        folders = new CabFolder[cab_folders];        for(int i=0; i<cab_folders; i++) {            folders[i] = new CabFolder(this.mFile,	//self reference                CabUtils.rotateInt(raf.readInt()),	//folder offset                CabUtils.rotateShort(raf.readShort()),	//folder data                CabUtils.rotateShort(raf.readShort()),	//folder compression method                i);					//folder number        }                //read entries        entries = new CabEntry[cab_files];        raf.seek(cab_entry_offset);        int inflated_size;        int inflated_offset;        short folder_ix;        int timestamp;//	short date;//	short time;        short attribs;		        String name;        CabFolder cabfolder;        for(int i=0; i<entries.length; i++) {            inflated_size = CabUtils.rotateInt(raf.readInt());            inflated_offset = CabUtils.rotateInt(raf.readInt());            folder_ix = CabUtils.rotateShort(raf.readShort());            timestamp = CabUtils.rotateInt(raf.readInt());//          mDate = raf.readShort();//          mTime = raf.readShort();            attribs = raf.readShort();		            name = CabUtils.readCString(raf);            //extrapolated information            if(folder_ix >= 0)                cabfolder = folders[folder_ix];            else                cabfolder = crapFolder;            entries[i] = new CabEntry(name, inflated_size, inflated_offset, timestamp,                                                                    folder_ix, attribs, cabfolder);        }        raf.close();    }    /**    * @return the name of the cabinet file    */    public String getName() {        return mFile.getName();    }    /**    * @return the file    */    public File getFile() {        return mFile;    }    /**    * Included for parity with java.util.zip.ZipFile interface.    * @return an enumeration containing references to this file's CabEntries    */    public Enumeration entries() {        Vector v = new Vector(entries.length);        v.copyInto(entries);        return v.elements();    }    /**     * @return all CabEntries in this file as an array    */    public CabEntry[] getEntries() {        return entries;    }    /**    * unique to CabFile    * @return an enumeration containing references to this file's CabFolders    */    public Enumeration folders() {        Vector v = new Vector(folders.length);        v.copyInto(folders);        return v.elements();    }    /**     * @return all CabFolders in this file as an array    */    public CabFolder[] getFolders() {        return folders;    }    /**    * Included for parity with java.util.zip.ZipFile interface.    * @param name the name of the CabEntry to match    * @return a CabEntry whose name matches the string provided    */    public CabEntry getEntry(String name) {        int i = 0;        while(true) {            if(entries[i].getName().equals(name))                return entries[i];            i++;        }    }    /**    * Included for parity with java.util.zip.ZipFile interface.    * This method does nothing at all, the RandomAccessFile is opened and closed in     * the readHeader method.    */    public void close() {}    /**    * Create a new CabFileInputStream for reading the current entry.  This is the    * slow way of doing things, but it was added to provide an interface identical    * to java.util.zip.ZipFile    * @param ce retrieve an InputStream to this entry    * @return an InputStream that returns uncompressed data    * @exception CabException thrown by CabFileInputStream constructor    * @exception IOException thrown by CabFileInputStream constructor    */    public InputStream getInputStream(CabEntry ce)    throws IOException {        return new CabFileInputStream(ce);    }    /**    * Create a new CabFileInputStream for reading the current entry.  This is the    * slow way of doing things, but it was added to provide an interface identical    * to java.util.zip.ZipFile    * @param cf retrieve an InputStream to this folder    * @return an InputStream that returns uncompressed data    * @exception CabException thrown by CabFileInputStream constructor    * @exception IOException thrown by CabFileInputStream constructor    */    public InputStream getInputStream(CabFolder cf)    throws IOException {        return new CabFileInputStream(cf);    }}