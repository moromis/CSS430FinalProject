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
			if( deallocAllBlocks( ftEnt ) == false ) //TODO: implement deallocAllBlocks
				return null;
		}
		return ftEnt;
	}
	
	
	boolean close( FileTableEntry ftEnt ) {
		
		//TODO: implement
	
	}
	
	int read( FileTableEntry ftEnt, byte[] buffer ) {
		
		//TODO: implement
		
	}
	
	
	int write( FileTableEntry ftEnt, byte[] buffer ) {
		
		//TODO: implement
		
	}
	
	
	int fsize( FileTableEntry ftEnt ) {
		
		//TODO: implement
		
	}
	
	private boolean deallocAllBlocks( FileTableEntry ftEnt ) {

		
	}
	
	/*
	*	Delete the file from the disk
	*	get entry and remove from directory
	*/
	boolean delete( String filename ) {
		FileTableEntry entry = this.open(filename, "w");
		short info = entry.iNumber;
		return this.close(entry) && this.directory.ifree(info);
		
	}
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	
	int seek( FileTableEntry ftEnt, int offset, int whence ) {
		
		//TODO: implement
		
	}
}
