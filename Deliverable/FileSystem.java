/*CSS 430 Operating Systems Final Project

Description:
Manages all calls from kernel regarding the filesystem and
its management

@author Daniel Grimm
@author Kevin Ulrich
@author Preston Mar
*/

public class FileSystem {

	//class variables/objects
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;
	private boolean debug;
	
	public FileSystem( int diskBlocks ) {
		
		superblock = new SuperBlock( diskBlocks );
		directory = new Directory ( superblock.totalInodes );
		filetable = new FileTable( directory );
		
		//read the "/" file from diskBlocks
		FileTableEntry dirEnt = open( "/", "r" );
		int dirSize = fsize( dirEnt );
		if(dirSize > 0 ) {
			
			// the directory has some data.
			byte[] dirData = new byte[dirSize];
			read( dirEnt, dirData );
			directory.bytes2directory( dirData );
		}
		
		close( dirEnt );
	}
	
	//	write all to disk?
	void sync() {
		
		FileTableEntry ftEnt = open("/","w");
		byte[] theDirec = directory.directory2bytes();
		write(ftEnt, theDirec);
		close(ftEnt);
		superblock.sync();
		
	}
	
	/*
	*	Format the max number of inodes
	*	file number = # inodes
	*/
	boolean format( int files ) {

		superblock.format(files);
		
		//create the directory and filetable objects
		directory = new Directory(files);
		filetable = new FileTable(directory);
		
		return true;	
	}
	
	FileTableEntry open( String filename, String mode ) {
		
		FileTableEntry ftEnt = filetable.falloc( filename, mode );
		
		if( mode.equals( "w" )) {
			if( deallocAllBlocks( ftEnt ) == false )
				return null;
		}
		
		return ftEnt;
	}
	
	/*
	*	Closes the file ftEnt
	*	commits all file transacitons on the file
	*	removes from user file descriptior table of calling thread TCB
	*
	*	returns true in success
	*/
	boolean close( FileTableEntry ftEnt ) {
		
		if(ftEnt == null) return false;
        synchronized (ftEnt) {
            ftEnt.count--;
            if(ftEnt.count > 0) return true;
            return filetable.ffree(ftEnt);
        }
	}
	
	/*
	*	Reads up to buffer.length bytes from the file starting at the seek pointer
	*	returns the number of bytes read in
	*	returns -1 in fail
	*/
	int read( FileTableEntry ftEnt, byte[] buffer ) {
		
		//make sure its the right mode
		if(ftEnt.mode == "r" || ftEnt.mode == "w+"){
			
			int readin = 0;
			int buflen = buffer.length;

			synchronized(ftEnt){
				while(buflen > 0 && ftEnt.seekPtr < fsize(ftEnt)){
					
					//get block to start reading from
					int theBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
					
					if(theBlock == -1){
						//does not exists or failure
						break;
					}
					
					//fill up one block in array
					byte[] aBlock = new byte[512];
					SysLib.rawread(theBlock, aBlock);

					//find where the seekpoint is in relation to the block
					int startPoint = ftEnt.seekPtr % 512;
					
					//calc bytes read in from the block
					int readBytes = 512 - startPoint;
					
					//calc total bytes left in inode
					int bytesLeft = fsize(ftEnt) - ftEnt.seekPtr;

					int bytesReadIn = 0;

					//determine how many bytes were read in
					if(readBytes <= buflen){
						
						if(readBytes <= bytesLeft){
							
							bytesReadIn = readBytes;
							
						}else{
							
							bytesReadIn = bytesLeft;
							
						}
					}else{
						
						bytesReadIn = Math.min(buflen, bytesLeft);
					}

					//copy block buffer to output buffer
					System.arraycopy(aBlock, startPoint, buffer, readin, bytesReadIn);

					//update values
					ftEnt.seekPtr += bytesReadIn;
					readin += bytesReadIn;
					buflen -= bytesReadIn;

				}
				
				return readin;
			}
			
		}else{
			
			//bad input
			return -1;
			
		}		
		
	}
	
	/*
	*	writes contents of buffer to ftEnt starting at seekPoint
	*	returns number of bytes written
	*/
	int write( FileTableEntry ftEnt, byte[] buffer ) {
		
		/**/
		
		if(ftEnt.mode == "w" || ftEnt.mode == "w+" || ftEnt.mode == "a"){
			
			synchronized(ftEnt){
				
				int written = 0;
				int buflen = buffer.length;
				
				while(buflen > 0){
					
					//figure out what block to write to in the inode based on the seek pointer
					int theBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
					
					// if block does not exist, create it
					if(theBlock == -1){
						
						// get the next free block
						short newBlock = (short)superblock.getFreeBlock();
						
						
						//put the block in the inode
						if(ftEnt.inode.setIndexBlock(newBlock) == -2){ //if we get -2 back then we only assigned the indirect block, need to give another block to the inode
						
							newBlock = (short)superblock.getFreeBlock();
							
							if(ftEnt.inode.setIndexBlock(newBlock) == -1)
								return -1;
								
								
						}
							
						theBlock = newBlock;
					}
					
					//create a buffer
					byte[] aBlock = new byte[512];
					SysLib.rawread(theBlock, aBlock);
					
					//decide where to start writing
					int startPoint = ftEnt.seekPtr % 512;
					int bytesToWrite = 512 - startPoint;

					//calc the ammount of bytes written
					//either buflen if end of buffer, or bytesToWrite
					int bytesWritten = Math.min(bytesToWrite, buflen);
					
					//copy buffer to aBlock
					System.arraycopy(buffer, written, aBlock, startPoint, bytesWritten);
					
					//write the block to the disk at theBlock block index
					SysLib.rawwrite(theBlock, aBlock);
					ftEnt.seekPtr += bytesWritten;
					written += bytesWritten;
					buflen -= bytesWritten;

					//deal with reaching end of inode
					if(ftEnt.seekPtr > ftEnt.inode.length){
						
						//update length if it grows
						ftEnt.inode.length = ftEnt.seekPtr;
						
					}					
				}
				
				//save to disk
				ftEnt.inode.toDisk(ftEnt.iNumber);
				return written;
			}
			
		}else{
			
			//fail
			return -1;
			
		}
		
	}
	
	/*
	*	resturns the size in bytes of the file indicated by ftEnt
	*/
	int fsize( FileTableEntry ftEnt ) {
		
		synchronized(ftEnt){
			return ftEnt.inode.length;
		}
		
	}
	
	private boolean deallocAllBlocks( FileTableEntry ftEnt ) {
		
		if(ftEnt != null){
			
			ftEnt.seekPtr = 0;
			for(int i = 0; i < ftEnt.inode.direct.length; i++){
				
				//return all of the filetableentry's direct blocks
				superblock.returnBlock(ftEnt.inode.direct[i]);
				
			}
			
			//erase all the inode properties
			ftEnt.inode.length = 0;
			ftEnt.inode.count = 0;
			ftEnt.inode.flag = 0;

			for(int i = 0; i < 11; i++){
				
				//reset all the inode directs in the filetableentry object
				//(notice we don't write these changes to disk)
				ftEnt.inode.direct[i] = -1;
				
			}
			
			//all went smoothly
			return true;
		}
		else{
			
			//the filetableentry was null
			return false;
			
		}
		
	}
	
	/*
	*	Delete the file from the disk
	*	get entry and remove from directory
	*/
	boolean delete( String filename ) {
		
		//open to dealloc blocks - this updates the inode
		FileTableEntry entry = open(filename, "w");
		short info = entry.iNumber;
		
		//close the entry and return if it closes or not
		return close(entry) && directory.ifree(info);
		
	}
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	/*
	* updates ftEnt.seekPtr
	*/
	int seek( FileTableEntry ftEnt, int offset, int whence ) {
		
		synchronized(ftEnt){
			switch (whence){
				case SEEK_SET:
					ftEnt.seekPtr = offset;
					break;
				case SEEK_CUR:
					ftEnt.seekPtr += offset;
					break;
				case SEEK_END:
					ftEnt.seekPtr = fsize(ftEnt) + offset;
					break;

			}
			return ftEnt.seekPtr;
		}
		
	}
}