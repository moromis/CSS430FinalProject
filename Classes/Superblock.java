/**Superblock.java

This file creates a superblock that details the file format of
the disk in ThreadOS

@author Daniel Grimm
@author Preston Mar
@author Kevin Ulrich*/

public class SuperBlock {

	private final int defaultInodeBlocks = 64;
	public int totalBlocks;
	public int totalInodes;
	public int freeList;
	
	public SuperBlock( int diskSize ) {
		
		//read the superblock from disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread( 0, superBlock );
		totalBlocks = SysLib.bytes2int( superBlock, 0 );
		totalInodes = SysLib.bytes2int( superBlock, 4 );
		freeList = SysLib.bytes2int( superBlock, 8 );
		
		if( totalBlocks == diskSize && totalInodes > 0 && freeList >= 2 ) {
			// disk contents are valid
			return;
		} else {
			//need to format disk
			totalBlocks = diskSize;
			format( defaultInodeBlocks );
		}
	}
	
	void sync() {
		
		//create a byte array to represent the superblock
		byte[] superBlock = new byte[Disk.blockSize];
		
		//convert all the superblock info into bytes, put
		//them in the byte array
		SysLib.int2bytes( totalBlocks, superBlock, 0 );
		SysLib.int2bytes( totalInodes, superBlock, 4 );
		SysLib.int2bytes( freeList, superBlock, 8 );
		
		//write the superblock to sync
		SysLib.rawwrite( 0, superBlock );
		
	}
	
	int getFreeBlock() {
		
		//return the first free block
		return freeList;
		
	}
	
	void returnBlock( int blockNumber ) {
		
		//change the first free block
		freeList = blockNumber;
		
	}

}
