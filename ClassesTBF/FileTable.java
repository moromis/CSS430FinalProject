/*CSS 430 Operating Systems Final Project

Description:
Keeps a record of all files, and creates, opens, closes, and deletes
files as needed.

@author Daniel Grimm
@author Kevin Ulrich
@author Preston Mar
*/

import java.util.*;

public class FileTable {

	private Vector table;         // the actual entity of this file table
	private Directory dir;        // the root directory 

	public FileTable( Directory directory ) { // constructor
	
		table = new Vector( );     	// instantiate a file (structure) table
		dir = directory;           	// receive a reference to the Director
									// from the file system
	}                             

	// major public methods
	public synchronized FileTableEntry falloc( String filename, String mode ) {
		
		//preinitialize the inumber and inode
		short iNumber = -1;
        Inode inode = null;

        while (true) {
			
            //get inode number from file
            iNumber = filename.equals("/") ? 0 : dir.namei(filename);

            //if the file exists (an inode has been allocated for it)
            if (iNumber >= 0) {
				
				//open the inode associated with the file
                inode = new Inode(iNumber);
				
                //if mode is read
                if (mode.equals("r")) {
					
                    //if file is being written to
                    if (inode.flag != inode.UNUSED && inode.flag != inode.USED ) {
						
                        //wait until writing is done
                        try { wait(); } catch (InterruptedException e) { }
                        continue;
                    }
					
                    //set flag to used
                    inode.flag = inode.USED;
                    break;
					
                }
				
				//set the flag to read, if it isn't read
                inode.flag = inode.READ;
                break;
            }
			
            //if mode is read then return a null
            if (mode.equals("r")) return null;
			
            //create file
            iNumber = dir.ialloc(filename);
            inode = new Inode();
			
            //set flag to write
            inode.flag = inode.READ;
            break;
        }

        //increase the count of references to the inode and write the inode to disk
        inode.count++;
        inode.toDisk(iNumber);
		
        //create the filetableentry, add it to the table, and return it
        FileTableEntry ftEnt = new FileTableEntry(inode, iNumber, mode);
        table.addElement(ftEnt);
        return ftEnt;
	  
   }

   public synchronized boolean ffree( FileTableEntry e ) {

	  for(int i = 0; i < table.size(); i++){
		  
		  if(table.elementAt(i) == e){
			  
			  //decrease the number of references to the inode,
			  //since we're freeing it
			  Inode inode = e.inode;
			  short iNumber = e.iNumber;
			  inode.flag = 0;
			  inode.count--;
			  
			  //then save the inode to the disk
			  inode.toDisk( iNumber );
			  
			  //remove the file table entry from the table
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
