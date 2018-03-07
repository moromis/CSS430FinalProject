/**Filesystem.java

This class details the file system for ThreadOS

@author Daniel Grimm
@author Kevin Ulrich
@author Preston Mar*/

public class FileSystem {

	private SuperBlock superblock;
	private Directory directory;
	private FileStructureTable filetable;
	
	public FileSystem( int diskBlocks ) {
		superblock = new SuperBlock( diskBlocks );
		directory = new Directory ( superblock.totalInodes );
		filetable = new FileStructureTable( directory );
		
		//read the "/" file from diskBlocks
		FileTableEntry dirEnt = SysLib.open( "/", "r" );
		int dirSize = fsize( dirEnt );
		if(dirSize > 0 ) {
			// the directory has some data.
			byte[] dirData = new byte[dirSize];
			SysLib.read( dirEnt, dirData );
			directory.bytes2directory( dirData );
		}
		SysLib.close( dirEnt );
	}
	
	void sync() {

		//TODO: implement
	}
	
	/*
	*	Format the max number of inodes
	*	file number = # inodes
	*/
	boolean format( int files ) {
		//block 0 is superblock
		
		//inode blocks

		//first free block
		superblock.format(files);
		
	}
	
	FileTableEntry open( String filename, String mode ) {
		
		FileTableEntry ftEnt = filetable.falloc( filename, mode );
		if( mode.equals( "w" ) {
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
		//be th eonly one touching the file
		synchronized(ftEnt){
			//decrement # of users
			ftEnt.count--;
			if(ftEnt.count > 0){
				//return true if other people are using the file
				return true;
			}
		}
		//if you are the last one to use the file save everything
		return filetable.ffree(ftEnt);
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
				while(buflen > 0 && ftEnt.seekPtr < this.fsize(ftEnt)){
					//get block to start reading from
					int theblock = fnEnt.inode.findTargetBlock(ftEnt.seekPtr);
					if(theblock == -1){
						//does not exists or failure
						break;
					}
					//fill up one block in array
					byte[] ablock = new byte[512]
					SysLib.rawread(theblock, ablock)

					//find where the seekpoint is in relation to the block
					int startPoint = ftEnt.seekPtr % 512;
					//calc bytes read in from the block
					int readBytes = 512 - startPoint;
					//calc total bytes left in inode
					int bytesLeft = this.fsize(ftEnt) - ftEnt.seekPoint;

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
					System.arraycopy(ablock, startPoint, buffer, readin, bytesReadIn);

					//update values
					ftEnt.seekPtr += bytesReadIn;
					readin += bytesReadIn;
					buflen -= bytesReadIn;

				}
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
		if(ftEnt.mode == "w" || ftEnt.mode == "w+" || ftEnt.mode == "a"){
			int written = 0;
			int buflen = buffer.length;
			
			synchronized(ftEnt){
				while(buflen > 0){
					int theBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
					//if block does not exist, create it
					if(theBlock == -1){
						short newBlock = (short)this.superblock.getFreeBlock();
						//init new block
						theBlock = newBlock;
					}
					byte[] aBlock = new byte[512];
					int startPoint = ftEnt.seekPtr % 512;
					int bytesToWrite = 512 - startPoint;

					//calc the ammount of bytes written
					//either buflen if end of buffer, or bytesToWrite
					int bytesWritten = Math.min(bytesToWrite, buflen);
					//copy buffer to aBlock
					System.arraycopy(buffer, written, aBlock, startPoint, bytesWritten);
					//write to block
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
		if(ftEnt.inode.count != 1){
			//if files reference this inode
			return false;
		}else{
			//TODO
		}
	}
	
	/*
	*	Delete the file from the disk
	*	get entry and remove from directory
	*/
	boolean delete( String filename ) {
		//open to dealloc blocks - this updates the inode
		FileTableEntry entry = this.open(filename, "w");
		short info = entry.iNumber;
		//close the entry and return if it closes or not
		return this.close(entry) && this.directory.ifree(info);
		
	}
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	/*
	* updates ftEnt.seekPtr
	*/
	int seek( FileTableEntry ftEnt, int offset, int whence ) {
		synchronized(ftEnt){
			switch whence{
				case SEEK_SET:
					ftEnt.seekPtr = offset;
					break;
				case SEEK_CUR:
					ftEnt.seekPtr += offset;
					break;
				case SEEK_END:
					ftEnt.seekPtr = this.fsize(ftEnt) + offset;
					break;

			}
		}
		
	}
}
