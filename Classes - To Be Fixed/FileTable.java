import java.util.*;

public class FileTable {

   private Vector table;         // the actual entity of this file table
   private Directory dir;        // the root directory 

   public FileTable( Directory directory ) { // constructor
      table = new Vector( );     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Director
   }                             // from the file system

   // major public methods
   public synchronized FileTableEntry falloc( String filename, String mode ) {
	   
      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry

	  /*
	  
		//preinitialize the inumber and inode	  
		short iNumber = -1;
		Inode inode = null;

		//get the inumber associated with the filename
	  iNumber = ( filename.equals( "/" ) ? 0 : dir.namei( filename ) );
	  
	  if(iNumber >= 0){
		  
		  //create a new inode, which reads from disk to create itself (so doesn't really create a new one
		  inode = new Inode( iNumber );
		  
		  FileTableEntry e = new FileTableEntry( inode, iNumber, mode );
		  
		  //if the file table entry isn't in the table, add it
		  if(!table.contains(e)){
			  
			  table.addElement( e );
			  
		  }
			  
		  //increase the number of references to the inode, write it to disk,
		  //and then return the file table entry
		  inode.count++;
		  inode.toDisk( iNumber );
		  return e;

	  }else{

		return null;
	
	  }
	  
	  */
	  
	  short iNumber = -1;
	  Inode inode = null;
	  
	  while( true ) {
		  
		  //get the inumber for the filename
		  iNumber = ( filename.equals( "/" ) ? 0 : dir.namei( fname ) );
		  
		  //if it's not the root directory
		  if( iNumber >= 0 ) {
			  
			  //create a new inode for the file
			  inode = new Inode( iNumber );
			  
			  
			  if( mode.compareTo( "r" ) ) {
				  
				  if ( inode.flag == 2 )
					  break; //read: no need to wait
				  
				  else if ( inode.flag == 3 ) { //write: wait for a write to exit
				  
					try { 
						wait() ;
					} catch( InterruptedException e ) {
						
						iNumber = -1; // no more open
						return null;
					}
				  }
				  
			  } else if ( mode.compareTo( "w" ) ) {
				  
				  //TODO: just made this the same as read, what to actually do here?
				   if ( inode.flag == 2 )
					  break; //read: no need to wait
				  
				  else if ( inode.flag == 3 ) { //write: wait for a write to exit
				  
					try { 
						wait() ;
					} catch( InterruptedException e ) {
						
						iNumber = -1; // no more open
						return null;
					}
				  }
				  
			  }//TODO: other modes? "w+" and "a" ?
		  }
		  
		  inode.count++;
		  inode.toDisk( iNumber );
		  FileTableEntry e = new FileTableEntry( inode, iNumber, mode );
		  table.addElement( e );
		  return e;
	  
   }

   public synchronized boolean ffree( FileTableEntry e ) {
	   
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
	  
		

	  for(int i = 0; i < table.size(); i++){
		  if(table.elementAt(i) == e){
			  
			  //decrease the number of references to the inode,
			  //since we're freeing it, and then save the inode 
			  //to the disk
			  Inode inode = e.inode;
			  short iNumber = e.iNumber;
			  inode.count--;
			  inode.toDisk( iNumber );
			  
			  //remove the file table entry from the 
			  table.remove(i);
			  return true;
		  }
	  }
	  
	  //didn't find the file table entry
	  return false;
   }

   public synchronized boolean fempty( ) {
	   
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}
