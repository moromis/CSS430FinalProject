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
   
   short getIndexBlockNumber() {
	   
	   //look for a free direct index
		for(int i = 0; i < directSize; i++){
			if(direct[i] == 0){
				return direct[i]; //TODO: or should we return i here?
			}
		}
		
		//if we get here then the direct block is full
		//TODO: iterate through the block that the indirect pointer points
		//		to, and if we find a 0 then place the block there and return true
		
		//if we get here then this whole inode is full
		return -1;
	   
   }
   
   boolean setIndexBlock( short indexBlockNumber ) {
		
		//look for a free direct index
		for(int i = 0; i < directSize; i++){
			if(direct[i] == 0){
				direct[index] = shortBlockNumber;
				directIsFull = false;
				return true;
			}
		}
		
		//otherwise, the direct blocks are all taken up, so
		//we need to place this block in the indirect pointer
		
		//TODO: iterate through the block that the indirect pointer points
		//		to, and if we find a 0 then place the block there and return true
		
		//if we get here then this whole inode is full
		return false;
		
   }
   
   short findTargetBlock( int offset ) {
		
		//translate the offset into a block number
		int index = Math.floor(offset / 512); //TODO: maybe could use a constant here? should Inode know about disk block size?
		
		//find the corresponding block using the index
		if(index >= 0){
			
			if(index < directSize){
				
				if(direct[offset] != 0)
					return direct[offset];
				else
					return -1;
				
			}else if(index >= directSize && index < 256 + directSize){
				
				//TODO: return actual block from indirect
				if(indirect != 0)
					return indirect;
				else
					return -1;
				
			}
			
			return -1;
			
		}else{
			
			return -1;
			
		}
	   
   }
   
   //TODO: what other functions are needed?
   
}