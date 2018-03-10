import java.util.*;

public class FileTable {

   private Vector table;         // the actual entity of this file table
   private Directory dir;        // the root directory 
   private boolean debug;

	public FileTable( Directory directory, boolean dbg ) { // constructor
		debug = dbg;
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

	  
		//preinitialize the inumber and inode	  
		short iNumber = -1;
		Inode inode = null;

		//get the inumber associated with the filename
		iNumber = ( filename.equals( "/" ) ? 0 : dir.namei( filename ) );
		
		if(debug) System.err.println("**** FileTable falloc: iNumber: " + iNumber);
	  
		if(iNumber < 0){
			
			iNumber = dir.ialloc(filename);
			
		}
		
		//create a new inode, which reads from disk to create itself (so doesn't really create a new one)
		inode = new Inode( iNumber );
		
		if(debug) System.err.println("**** FileTable falloc: iNumber: " + iNumber + " new inode length: " + inode.length);
	  
		FileTableEntry e = new FileTableEntry( inode, iNumber, mode );
		
		if(debug) System.err.println("**** FileTable falloc: new filetableentry: " + e);
	  
		//if the file table entry isn't in the table, add it
		if(!table.contains(e)){
		  
			if(debug) System.err.println("**** FileTable falloc: e wasn't in table, adding.");
			table.addElement( e );
		  
		}
		  
		//increase the number of references to the inode, write it to disk,
		//and then return the file table entry
		inode.count++;
		inode.toDisk( iNumber );
		
		if(debug) System.err.println("**** FileTable falloc: " + e);
		
		return e;
	  
	  
	  /*
		short iNumber = -1;
		Inode inode = null;
	  
		while( true ) {
		  
			//get the inumber for the filename
			iNumber = ( filename.equals( "/" ) ? 0 : dir.namei( filename ) );
		  
			//if it's not the root directory
			if( iNumber >= 0 ) {
			  
				//create a new inode for the file
				inode = new Inode( iNumber );
			  
				if( mode.equals( "r" ) ) {
				  
					if ( inode.flag == 2 ) break; //read: no need to wait
				  
					else if ( inode.flag == 3 ) //write: wait for a write to exit
						try { wait() ; } catch( InterruptedException e ) {}
						
					else if( inode.flag == 4 ){
						iNumber = -1; // no more open
						return null;
					}
					
				} else if ( mode.equals( "w" ) ) {
				  
					if ( inode.flag == 2 ) break; //read: no need to wait
				  
					else if ( inode.flag == 3 ) //write: wait for a write to exit
						try { wait() ; } catch( InterruptedException e ) {}
						
					else if( inode.flag == 4 ){ //to be deleted flag
						iNumber = -1; // no more open
						return null;
					}
					  
				} //TODO: other modes? "w+" and "a" ?
			}
		}
		  
		inode.count++;
		inode.toDisk( iNumber );
		FileTableEntry e = new FileTableEntry( inode, iNumber, mode );
		table.addElement( e );
		return e;
		
		*/
		
		/*
		
		short iNumber = -1;
        Inode inode = null;

        while (true) {
            //get inode number from file
            iNumber = filename.equals("/") ? 0 : dir.namei(filename);

            // check if file exists
            if (iNumber >= 0) {
                inode = new Inode(iNumber);
                // if mode is read
                if (mode.equals("r")) {
                    //if file is being written to
                    if (inode.flag != 0 && inode.flag != 1 ) {
                        //wait until writing is done
                        try {
                            wait();
                        }
                        catch (InterruptedException e) { }
                        continue;
                    }
                    //set flag to read
                    inode.flag = 1;
                    break;
                }

                if (inode.flag != 0 && inode.flag != 3)
                {
                    if(inode.flag == 1 || inode.flag==2)
                    {
                        inode.flag = (short)(inode.flag + 3);
                        inode.toDisk(iNumber);
                    }

                    //wait until writing is done

                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e) { }
                    continue;
                }
                inode.flag = 2;
                break;
            }
            //if mode is read then return a null
            if (mode.equals("r")) return null;

            //create file
            iNumber = dir.ialloc(filename);
            inode = new Inode();
            //set flag to write
            inode.flag = 2;
            break;
        }

        //write inode to disk
        inode.count++;
        inode.toDisk(iNumber);
        //create filetable entry and return
        FileTableEntry ftEnt = new FileTableEntry(inode, iNumber, mode);
        table.addElement(ftEnt);
        return ftEnt;
		
		*/
	  
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
			  inode.flag = 0;
			  inode.count--;
			  
			  if(debug) System.err.println("**** FileTable: ffree: inode number: " + iNumber + " length: " + inode.length);
			  
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
