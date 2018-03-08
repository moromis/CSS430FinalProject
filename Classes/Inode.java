public class Inode {
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   
   public short flag;                             
   // 0 = unused, 1 = used, 2 = read, 3 = write, 4 = to be deleted
   
   
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer
   
   /*
   
	length = 4 bytes
	count = 2 bytes
	flag = 2 bytes
	direct = 22 bytes (11 shorts)
	indirect = 2 bytes - note, indirect block is 512 bytes, each reference
						in the indirect block is a short, for a total of
						256 indirect blocks
	
	*/

   Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   Inode( short iNumber ) {                       // retrieving inode from disk
   
		int blockNumber = 1 + iNumber / 16;
		byte [] data = new byte[Disk.blockSize];
		SysLib.rawread( blockNumber, data );
		int offset = ( iNumber % 16) * 32;
		
		length = SysLib.bytes2int( data, offset );
		offset += 4;
		count = SysLib.bytes2short( data, offset );
		offset += 2;
		flag = SysLib.bytes2short( data, offset );
		offset += 2;
		direct = SysLib.bytes2short( data, offset );
		offset += 22;
		indirect = SysLib.bytes2short( data, offset );
		// 32 bytes in total
   }

   int toDisk( short iNumber ) {                  // save to disk as the i-th inode
   
		int blockNumber = 1 + iNumber / 16;
		byte [] data = new byte[Disk.blockSize];
		int offset = ( iNumber % 16) * 32;
		
		// fill the temp data block with the Inode data
		SysLib.int2bytes( length, data, offset );
		offset += 4;
		SysLib.short2bytes( count, data, offset );
		offset += 2;
		SysLib.short2bytes( flag, data, offset );
		offset += 2;
		SysLib.short2bytes( direct, data, offset );
		offset += 22;
		SysLib.short2bytes( indirect, data, offset );
		
		// write this Inode to the disk
		SysLib.rawrite( blockNumber, data );
   }
   
   //returns the block that was last placed in this inode, i.e. the most
   //recent one
   short getIndexBlockNumber() {
	   
	   //look for a free direct index
		for(int i = 0; i < directSize; i++){
			if(direct[i] == 0 && i != 0){
				return direct[i - 1];
			}
		}
		
		//otherwise, the direct blocks are all taken up, so
		//the latest block must be in indirect or the inode is full
		
		if(indirect != 0){
			
			//get the indirect block from memory
			byte [] data = new byte[Disk.blockSize];
			SysLib.rawread( indirect, data );
			
			//go through the indirect block
			for(int i = 0; i < Disk.blockSize; i++){
				
				//if we find the latest block here, return it
				if(data[i] == 0 && i != 0){
					return data[i - 1];
				}
			}
			
		}
		
		//if we get here then this whole inode is full or the indirect
		//pointer is unallocated or the first index is 0 (nothing in the inode)
		return -1;
	   
   }
   
   boolean setIndexBlock( short indexBlockNumber ) {
		
		//look for a free direct index
		for(int i = 0; i < directSize; i++){
			if(direct[i] == 0){
				direct[index] = indexBlockNumber;
				return true;
			}
		}
		
		//otherwise, the direct blocks are all taken up, so
		//we need to place this block in the indirect pointer
		
		if(indirect != 0){
			
			//if the indirect pointer is allocated, search it for a place to put
			//the block
			
			//get the indirect block from memory
			byte [] data = new byte[Disk.blockSize];
			SysLib.rawread( indirect, data );
			
			//go through the indirect block
			for(int i = 0; i < Disk.blockSize; i++){
				
				//if we find a free location to put the block, place it
				if(data[i] == 0){
					data[i] = indexBlockNumber;
					return true;
				}
			}
			
		}else{
			
			//otherwise just set the indirect pointer
			indirect = indexBlockNumber;
			return true;
			
		}
		
		//if we get here then this whole inode is full
		return false;
		
   }
   
   short findTargetBlock( int offset ) {
		
		//translate the offset into a block number
		int index = Math.floor(offset / Disk.blockSize);
		
		//find the corresponding block using the index
		if(index >= 0){
			
			//if the index is a direct block...
			if(index < directSize){
				
				if(direct[offset] != 0){
					
					//return the block
					return direct[offset];
					
				}else{
					
					//if the direct block is unallocated return -1
					return -1;
					
				}
				
			//otherwise if the index is an indirect block	
			}else if(index >= directSize && index < 256 + directSize){
				
				//remove 12 from the block index - for instance if we're trying
				//to find block 13, then really it's block index 1
				//in the indirect pointer block
				index -= directSize;
				
				if(indirect != 0){
					
					//get the indirect block of pointers
					byte [] data = new byte[Disk.blockSize];
					SysLib.rawread( indirect, data );
					
					//return the correct block if it's been filled
					if(data[index] != 0){
						
						//return the block
						return data[index];
						
					}else{
						
						//if the block the caller wants is unallocated, return -1
						return -1;
					}
					
				}else{
					
					//if the indirect pointer is unallocated, return -1
					return -1;
				}
				
			}else{
				
				//otherwise a block index larger than the number of blocks in
				//an inode was asked for, return -1
				return -1;
			
			}
			
		}else{
			
			//if the index is negative don't even try it bud
			return -1;
			
		}
	   
   }
   
   //TODO: what other functions are needed?
   
}