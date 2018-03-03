
public class Directory {
	
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private fileCounter = 0;

   public Directory( int maxInumber ) { // directory constructor
   
      fsizes = new int[maxInumber];     // maxInumber = max files
	  
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
	 
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
	  
	  fileCounter++;
   }

   public void bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]
	  
	  //use SysLib.bytes2int and using the format created in directory2bytes
	  //read in fsize, fnames, and fileCounter
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
	  
	  //called by filesystem - likely in sync
	  
	  //use SysLib.int2bytes and store fsize, fnames, and fileCounter in a byte array, and then return that array
   }

   public short ialloc( String filename ) {
	   
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
	  
	  fsize[fileCounter] = filename.length();
	  filename.getChars( 0, fsizes[fileCounter], fnames[fileCounter], 0 );
	  
	  //TODO: allocate an inode number? how to do?
   }

   public boolean ifree( short iNumber ) {
	   
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
	  
	  if(iNumber == 0) return false; //don't ever free the root directory
	  
	  String thefilename = "";	//TODO get the file name
	  SysLib.delete(thefilename);
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
   }
}