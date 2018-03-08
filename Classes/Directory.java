/**Directory.java

This class handles the directory system for ThreadOS.

@author Daniel Grimm
@author Kevin Ulrich
@author Preston Mar*/

public class Directory {
	
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private int fileCounter = 0;

   public Directory( int maxInumber ) { // directory constructor
   
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
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]
	  
	  //use SysLib.bytes2int and using the format created in directory2bytes
	  //read in fsize, fnames, and fileCounter
   }

   /** This method writes the directory information into a byte array to be
   stored on the disk.
   @return byte[] : The directory as stored in a byte array.*/
   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
	  
	  //called by filesystem - likely in sync
	  
	  //use SysLib.int2bytes and store fsize, fnames, and fileCounter in a byte array, and then return that array
      return new byte[0];
   }

   /** This method allocates an inode for the new file.
   @param filename : The name of the new file.
   @return short : The address of the inode.*/
   public short ialloc( String filename ) {
	   
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
	  
	  fsize[fileCounter] = filename.length();
	  filename.getChars( 0, fsize[fileCounter], fnames[fileCounter], 0 );
	  
	  //TODO: allocate an inode number? how to do?
	  //from slides: create new inode: check if there's a free inode and assign it to the file,
	  //return inode number, otherwise return error (-1)
     return -1;
   }

   /** This method frees up an inode using the supplied inode number.
   @param iNumber : The inode number to be freed up.
   @return boolean : False if the operation was not successful, otherwise true.*/
   public boolean ifree( short iNumber ) {
	   
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
	  
	  if(iNumber == 0) return false; //don't ever free the root directory
	  
	  String thefilename = "";	//TODO get the file name
	  SysLib.delete(thefilename);
     return false;
   }

   /** This method returns the inode number that corresponds with the file name.
   @param filename : The name of the file.
   @return short : The inode number.*/
   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
	  
	  //return -1 if no inode is associated with the filename - can happen
      return -1;
   }
}