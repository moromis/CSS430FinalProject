/**SuperBlock.java

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
	private boolean debug;
	
	public SuperBlock( int diskSize, boolean dbg ) {
		
		debug = dbg;
		
		//read the superblock from disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread( 0, superBlock );
		totalBlocks = SysLib.bytes2int( superBlock, 0 );
		totalInodes = SysLib.bytes2int( superBlock, 4 );
		freeList = SysLib.bytes2int( superBlock, 8 );
		
		if( totalBlocks == diskSize && totalInodes > 0 && freeList >= 2 ) {
			// disk contents are valid
			if(debug) System.err.println("superblock is all good! no formatting needed!");
			return;
		} else {
			//need to format disk
			totalBlocks = diskSize;
			format( defaultInodeBlocks );
		}
	}
	
	void format(int files) {
		
		
		//set the total number of inodes in the superblock (equal to files passed in)
		totalInodes = files;
		
		//figure out the first free block
		short firstFreeBlock;
		
		if(files % 16 == 0){
			firstFreeBlock = (short)(Math.ceil((32 * files) / 512) + 1);
		}else{
			firstFreeBlock = (short)(Math.ceil((32 * files) / 512) + 2);
		}
		
		//set the first free block pointer in the superblock object
		freeList = firstFreeBlock;
		
		//sync the superblock
		sync();
		
		//inode blocks
		for(short i = 0; i < files; i++){
			
			//create files number of inodes
			Inode inode = new Inode();
			inode.flag = 0;
			inode.toDisk(i);
		}
		
		//free blocks
		for(short i = firstFreeBlock; i < files; i++){
			
			//create an empty block
			byte[] data = new byte[512];
			
			//put a pointer to the next free block at the beginning of each block
			SysLib.short2bytes( (short)(i + 1), data, 0 );
			
			//write the block to disk
			SysLib.rawwrite(i, data);
		}
		
		//write -1 to the end of the list to indicate that there aren't any more free blocks after this
		byte endBlock[] = new byte[512];
		SysLib.int2bytes(-1, endBlock, 0);
		SysLib.rawwrite(this.totalBlocks - 1, endBlock);
		
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
		
		//get the head of the freelist
		int diskBlock = freeList;
		
		if(debug) System.err.println("**** SuperBlock: getFreeBlock: freelist was: " + freeList);
		
        if(diskBlock != -1) {
			
			//create a byte array to read in a block
            byte[] block = new byte[512];
			
			//read in the block
            SysLib.rawread(diskBlock, block);
			
			//check the next free block to see what its next free block is, and
			//set the head of the freelist to that
            freeList = (int) SysLib.bytes2short(block, 0);
        }
		
		if(debug) System.err.println("**** SuperBlock: getFreeBlock: freelist is now: " + freeList);
		if(debug) System.err.println("**** SuperBlock: getFreeBlock: returning: " + diskBlock);
		
		
        return diskBlock;
		
		//return the first free block
		// int blockToReturn = freeList;
		// freeList++;
		// return blockToReturn;
		
	}
	
	boolean returnBlock( int blockNumber ) {
		
		if(debug) System.err.println("**** SuperBlock: returnBlock: initially freeList: " + freeList);
		
		if(blockNumber >= 0 && blockNumber < totalBlocks){
			
			//create a byte array to write to
            byte[] block = new byte[512];
			SysLib.short2bytes((short)freeList, block, 0);
			SysLib.rawwrite(blockNumber, block);
			this.freeList = blockNumber;
			return true;
		
		}
		
		return false;
		
	}

}
