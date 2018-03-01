import java.util.*;

public class SysLib {
    public static int exec( String args[] ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.EXEC, 0, args );
    }

    public static int join( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WAIT, 0, null );
    }

    public static int boot( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.BOOT, 0, null );
    }

    public static int exit( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.EXIT, 0, null );
    }

    public static int sleep( int milliseconds ) {
	return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.SLEEP, milliseconds, null );
    }

    public static int disk( ) {
	return Kernel.interrupt( Kernel.INTERRUPT_DISK,
				 0, 0, null );
    }

    public static int cin( StringBuffer s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.READ, 0, s );
    }

    public static int cout( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WRITE, 1, s );
    }

    public static int cerr( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.WRITE, 2, s );
    }

    public static int rawread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.RAWREAD, blkNumber, b );
    }

    public static int rawwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.RAWWRITE, blkNumber, b );
    }

    public static int sync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.SYNC, 0, null );
    }

    public static int cread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CREAD, blkNumber, b );
    }

    public static int cwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CWRITE, blkNumber, b );
    }

    public static int flush( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CFLUSH, 0, null );
    }

    public static int csync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
				 Kernel.CSYNC, 0, null );
    }

    public static String[] stringToArgs( String s ) {
	StringTokenizer token = new StringTokenizer( s," " );
	String[] progArgs = new String[ token.countTokens( ) ];
	for ( int i = 0; token.hasMoreTokens( ); i++ ) {
	    progArgs[i] = token.nextToken( );
	}
	return progArgs;
    }

    public static void short2bytes( short s, byte[] b, int offset ) {
	b[offset] = (byte)( s >> 8 );
	b[offset + 1] = (byte)s;
    }

    public static short bytes2short( byte[] b, int offset ) {
	short s = 0;
        s += b[offset] & 0xff;
	s <<= 8;
        s += b[offset + 1] & 0xff;
	return s;
    }

    public static void int2bytes( int i, byte[] b, int offset ) {
	b[offset] = (byte)( i >> 24 );
	b[offset + 1] = (byte)( i >> 16 );
	b[offset + 2] = (byte)( i >> 8 );
	b[offset + 3] = (byte)i;
    }

    public static int bytes2int( byte[] b, int offset ) {
	int n = ((b[offset] & 0xff) << 24) + ((b[offset+1] & 0xff) << 16) +
	        ((b[offset+2] & 0xff) << 8) + (b[offset+3] & 0xff);
	return n;
    }

    /**This method formats Disk.java's data contents to specify
    the maximum number of files (inodes) to be created.
    @param files : The maximum number of files (inodes) in the
    file system. 
    @return int : If the allocation is successful, 0 is returned
    else -1 is returned.*/
    public static int format(int files) {
        //invalid number of files
        if (files < 1) {
            return -1;
        }
        return -1;
    }

    /**This method opens the specified file in the specified mode.
    Additionally, this file allocates a new file descriptor and returns it.
    The file descriptor will be in the range of 3 to 31.
    The mode will be one of the following:

    "r" for read only
    "w" for write only
    "w+" for read and write only
    "a" for append mode

    @param fileName : The name of the file to be opened or created.
    @param mode : The permissions for users to access the file.
    @return int : The file descriptor of the file or -1 for error.*/
    public static int open(String fileName, String mode) {
        if (!(mode.equals("r") || mode.equals("w") || mode.equals("w+") || mode.equals("a"))) {
            return -1;
        }
        return -1;
    }

    /**This method reads up to the size of buffer's bytes from the file and
    returns the number of bytes read.
    @param fd : The file to be read from.
    @param buffer : The characters read from the file.
    @return int : The number of bytes read or -1 on error.*/
    public static int read(int fd, byte[] buffer) {
        //
    }

    /**This method writes the contents of a buffer to the specified file.
    The number of bytes that were read is returned.
    @param fd : The file to be written to.
    @param buffer : The characters to be written to the file.
    @return int : The number of bytes read or -1 on error.*/
    public static int write(int fd, byte[] buffer) {
        //
    }

    /**This method sets the seek pointer in the specified file to the
    specified offset. The offset of the seek pointer is returned.
    @param fd : The file who's seek pointer is being modified.
    @param offset : The amount of offset from the whence position.
    @param whence : Where the offset starts from.
    @return int : The offset of the seek pointer.*/
    public static int seek(int fd, int offset, int whence) {
        //
    }

    /**This method closes the specified file and removes the pointers from
    the file descriptor table.
    @param fd : The file being closed.
    @return boolean : true is returned if the file successfully closes, else false
    is returned.*/
    public static int close(int fd) {
        //
    }

    /**This method deletes the specified file and frees the blocks used by it.
    @param fileName : The file to be deleted.
    @return boolean : true if the file was successfully deleted otherwise false.*/
    public static int delete(String fileName) {
        //
    }

    /**This method returns the size in bytes of the specified file.
    @param fd : The file whose size should be returned.
    @return int : The size of the file.*/
    public static int fsize(int fd) {
        //
    }

}