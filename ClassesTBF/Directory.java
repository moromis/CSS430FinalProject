/*CSS 430 Operating Systems Final Project

Description:
This class handles the directory system for ThreadOS.

@author Daniel Grimm
@author Kevin Ulrich
@author Preston Mar
*/

public class Directory {
	
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private int maxInumber = 0;
   private int fileCounter = 0;
   private FileTable ft;

	public Directory( int maxInum ) { // directory constructor
   
		maxInumber = maxInum;
   
		fsize = new int[maxInumber];     // maxInumber = max files
	  
		for ( int i = 0; i < maxInumber; i++ ) 
			fsize[i] = 0;                 // all file size initialized to 0
	 
		fnames = new char[maxInumber][maxChars];
		String root = "/";                // entry(inode) 0 is "/"
		fsize[0] = root.length( );        // fsize[0] is the size of "/".
		root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
	  
		fileCounter++;
	}

   /** This method reads in a byte array of data from the disk
   and changes it into a directory.
   @param data : The directory from the disk.
   @return void*/
   public void bytes2directory( byte[] data ) {

      //retrieves all of the file sizes
      for (int i = 0; i < maxInumber; i++) {
         fsize[i] = SysLib.bytes2int(data, i * 4);
      }

      int index = 0;
      //retrieves the character array
      for (int i = 0; i < maxInumber; i++) {
         for (int j = 0; j < maxChars; j++) {
            fnames[i][j] = (char) fsize[(maxInumber * 4) + (index++)];
         }
      }

      fileCounter = SysLib.bytes2int(data, ((maxInumber * 4) + (maxInumber * maxChars) + 1));
   }

   /** This method writes the directory information into a byte array to be
   stored on the disk.
   @return byte[] : The directory as stored in a byte array.*/
   public byte[] directory2bytes( ) {

      byte data[] = new byte[((maxInumber * 4) + (maxInumber * maxChars) + 4)];

      //Loads all of the file sizes into the byte array
      for (int i = 0; i < maxInumber; i++) {
         SysLib.int2bytes(fsize[i], data, i * 4);
      }

      int index = 0;
      //loads all of the characters into the byte array
      for (int i = 0; i < maxInumber; i++) {
         for (int j = 0; j < maxChars; j++) {
            data[maxInumber + index++] = (byte) fnames[i][j];
         }
      }
      
      SysLib.int2bytes(fileCounter, data, ((maxInumber * 4) + (maxInumber * maxChars) + 1));

      return data;
   }

	/** This method allocates an inode for the new file.
	@param filename : The name of the new file.
	@return short : The address of the inode.*/
	public short ialloc( String filename ) {
	   
		// filename is the one of a file to be created.
		// allocates a new inode number for this filename
	  
		short inodeNum = (short)fileCounter;
		fileCounter++;
	  
		fsize[inodeNum] = filename.length();
		filename.getChars( 0, fsize[inodeNum], fnames[inodeNum], 0 );
		Inode newInode = new Inode(inodeNum);
		newInode.flag = 1;
		newInode.toDisk(inodeNum);

		return inodeNum;
	}

   /** This method frees up an inode using the supplied inode number.
   @param iNumber : The inode number to be freed up.
   @return boolean : False if the operation was not successful, otherwise true.*/
   public boolean ifree( short iNumber ) {
	 
        if(fsize[iNumber] > 0) {
			
            fsize[iNumber] = 0;
            return true;
			
        }
		
		return false;
   }

   /** This method returns the inode number that corresponds with the file name.
   @param filename : The name of the file.
   @return short : The inode number.*/
   public short namei( String filename ) {
	   
	   
      if (filename.length() > maxChars) {
         return -1;
      }
	  
      //get the filname as an array of characters
      //assumes that file names are very different from each other
      short index = 0;
      boolean found = false;
      char[] charFilename = filename.toCharArray();

      //loop over the entire array of filenames
      for (short i = 0; i < maxInumber; i++) {
		  
		 if(fsize[i] != 0){ 
			 for (short j = 0; j < filename.length(); j++) {

				//if the characters are not the same, skip this row
				if (charFilename[j] != fnames[i][j]) {
				   found = false;
				   break;

				} else { //the characters are the same so possibly a match

				   found = true;
				}
			 }
		 
		 }
         
         //found the filename so return the inode number
         if (found) {
            return (short)index;
         }

         //Search the next row
         index++;
      }

	  //return -1 if no inode is associated with the filename - can happen
      return -1;
   }
}
