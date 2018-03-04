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
				while(buflen > 0 && )
			}
		}else{
			//bad input
			return -1;
		}		
	}
	
	int write( FileTableEntry ftEnt, byte[] buffer ) {
		
		//TODO: implement
		
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

		//TODO: implement
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
	
	int seek( FileTableEntry ftEnt, int offset, int whence ) {
		
		//TODO: implement
		
	}
}
