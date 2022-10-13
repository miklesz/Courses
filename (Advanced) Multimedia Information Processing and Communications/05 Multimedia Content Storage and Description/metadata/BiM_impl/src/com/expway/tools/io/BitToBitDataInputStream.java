/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
development of the MPEG-7 Systems (ISO/IEC 15938-1) standard. 

This software module is an implementation of a part of one or more 
MPEG-7 Systems (ISO/IEC 15938-1) tools as specified by the 
MPEG-7 Systems (ISO/IEC 15938-1) standard. 

ISO/IEC gives users of the MPEG-7 Systems (ISO/IEC 15938-1) free license
to this software module or modifications thereof for use in hardware or
software products claiming conformance to the MPEG-7 Systems 
(ISO/IEC 15938-1). 

Those intending to use this software module in hardware or software 
products are advised that its use may infringe existing patents.

The original developer of this software module and his/her company, the
subsequent editors and their companies, and ISO/IEC have no liability
for use of this software module or modifications thereof in an
implementation. 

Copyright is not released for non MPEG-7 Systems (ISO/IEC 15938-1) 
conforming products. 

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

/*
 * @(#)DataInputStream.java	1.46 98/06/29
 *
 * Copyright 1994-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package com.expway.tools.io;

import java.io.*;

/**
 * A data input stream lets an application read primitive Java data
 * types from an underlying input stream in a machine-independent
 * way. An application uses a data output stream to write data that
 * can later be read by a data input stream.
 * <p>
 * Data input streams and data output streams represent Unicode
 * strings in a format that is a slight modification of UTF-8. (For
 * more information, see X/Open Company Ltd., "File System Safe
 * UCS Transformation Format (FSS_UTF)", X/Open Preliminary
 * Specification, Document Number: P316. This information also
 * appears in ISO/IEC 10646, Annex P.)
 * <p>
 * All characters in the range <code>'&#92;u0001'</code> to
 * <code>'&#92;u007F'</code> are represented by a single byte:
 * <center><table border="3">
 *   <tr><td><i>0</i></td>  <td>bits 0-7</td></tr>
 * </table></center>
 * <p>
 * The null character <code>'&#92;u0000'</code> and characters in the
 * range <code>'&#92;u0080'</code> to <code>'&#92;u07FF'</code> are
 * represented by a pair of bytes:
 * <center><table border="3">
 *   <tr><td>1</td>  <td>1</td>  <td>0</td>  <td>bits 6-10</td></tr>
 *   <tr><td>1</td>  <td>0</td>  <td colspan=2>bits 0-5</td></tr>
 * </table></center><br>
 * Characters in the range <code>'&#92;u0800'</code> to
 * <code>'&#92;uFFFF'</code> are represented by three bytes:
 * <center><table border="3">
 *   <tr><td>1</td>  <td>1</td>  <td>1</td>  <td>0</td>  <td>bits 12-15</td</tr>
 *   <tr><td>1</td>  <td>0</td>  <td colspan=3>bits 6-11</td></tr>
 *   <tr><td>1</td>  <td>0</td>  <td colspan=3>bits 0-5</td></tr>
 * </table></center>
 * <p>
 * The two differences between this format and the
 * "standard" UTF-8 format are the following:
 * <ul>
 * <li>The null byte <code>'&#92;u0000'</code> is encoded in 2-byte format
 *     rather than 1-byte, so that the encoded strings never have
 *     embedded nulls.
 * <li>Only the 1-byte, 2-byte, and 3-byte formats are used.
 * </ul>
 *
 * @author  Arthur van Hoff
 * @version 1.46, 06/29/98
 * @see     java.io.DataOutputStream
 * @since   JDK1.0
 */
public class BitToBitDataInputStream extends FilterInputStream implements DataInput {

    private static final boolean DEBUG = false;
    private static final boolean CONCISEDEBUG = false;

    /**
     * Creates a <code>FilterInputStream</code>
     * and saves its  argument, the input stream
     * <code>in</code>, for later use. An internal
     *
     * @param  in   the input stream.
     */
    public BitToBitDataInputStream(InputStream in) {
        super(in);
    }

    /**
     * See the general contract of the <code>read</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end
     *             of the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[]) throws IOException {
        return inRead(b, 0, b.length);
    }
    
    /**
     * See the general contract of the <code>read</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end
     *             of the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[], int off, int len) throws IOException {
        return inRead(b, off, len);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
        InputStream in = this.in;
        int n = 0;
        while (n < len) {
            int count = inRead(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    /**
     * See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException   if an I/O error occurs.
     */
    public final int skipBytes(int n) throws IOException {
        InputStream in = this.in;
        int total = 0;
        int cur = 0;
        
        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        
        return total;
    }
    
    /**
     * See the general contract of the <code>readBoolean</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final boolean readBoolean() throws IOException {
        int ch = readByte(1);
        return (ch != 0);
    }

    /**
     * See the general contract of the <code>readByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream as a signed 8-bit
     *             <code>byte</code>.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final byte readByte() throws IOException {
        int ch = inRead();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * See the general contract of the <code>readUnsignedByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream, interpreted as an
     *             unsigned 8-bit number.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     * @see         java.io.FilterInputStream#in
     */
    public final int readUnsignedByte() throws IOException {
        int ch = inRead();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * See the general contract of the <code>readShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted as a
     *             signed 16-bit number.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final short readShort() throws IOException {
        int ch1 = inRead();
        int ch2 = inRead();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readUnsignedShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted as an
     *             unsigned 16-bit integer.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final int readUnsignedShort() throws IOException {

        int ch1 = inRead();
        int ch2 = inRead();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream as a Unicode
     *             character.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final char readChar() throws IOException {
        int ch1 = inRead();
        int ch2 = inRead();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readInt</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted as an
     *             <code>int</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading four bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */

    public final int readInt() throws IOException {
        int ch1 = inRead();
        int ch2 = inRead();
        int ch3 = inRead();
        int ch4 = inRead();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public final int readInt(int nbits) throws IOException {
        int piecenum      = nbits / 8;
        int lastPieceBits = nbits % 8;

        //System.out.println("piecenum = " + piecenum);
        //System.out.println("lastPieceBits = " + lastPieceBits);

        int result = 0;
        result = result | readByte(lastPieceBits);
        while (piecenum!=0){
            //System.out.println("i result = " + getBinaryString(result));
            result = result << 8;
            result = result | (readByte() & 0xFF);
            piecenum --;
        }

        if (CONCISEDEBUG) System.out.println("  io: readInt "+nbits+" bits result = " + result);
        return result;
    }


    /**
     * See the general contract of the <code>readLong</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted as a
     *             <code>long</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading eight bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public final long readLong(int nbits) throws IOException {
        int piecenum      = nbits / 8;
        int lastPieceBits = nbits % 8;

        //System.out.println("piecenum = " + piecenum);
        //System.out.println("lastPieceBits = " + lastPieceBits);

        long result = readByte(lastPieceBits);
        while (piecenum!=0){
            //System.out.println("result = " + getBinaryString(result));
            result = result << 8;
            result = result | (readByte() & 0xFF);
            piecenum --;
        }
        //System.out.println("result = " + getBinaryString(result));
        return result;
    }

    public final long readInfiniteLong() throws IOException {
        return readInfiniteLong(CodingConstants.DEFAULTPIECESIZE);
    }

/* OLD VERSION qui entrelacait les bits de structure de l'entier long et les bits de données
   public final long readInfiniteLong(int piecesize) throws IOException {
        
   long result = 0;

   while(readBoolean()){
   System.out.println("true");
   result = result << piecesize;
   int b = readByte(piecesize);
   System.out.println(com.expway.tools.io.BitToBitDataInputStream.getBinaryString(b));
   result = result | (b & ~masks[8-piecesize]);
   }
   System.out.println("false");
   result = result << piecesize;
   int b = readByte(piecesize);
   System.out.println(com.expway.tools.io.BitToBitDataInputStream.getBinaryString(b));
   result = result | (b & ~masks[8-piecesize]);

   if (CONCISEDEBUG) System.out.println("  io: read VLC "+  result); 

   return result;
   }
*/

    public final long readInfiniteLong(int piecesize) throws IOException {
        int quart=0;
        while(readByte(1)>0)
            quart++;
        quart++;
        int bitsize=quart*piecesize;
        
        long value=readLong(bitsize);
        if (CONCISEDEBUG) System.out.println("read VLC "+  value); 

        return value;
    }


    /**
     * See the general contract of the <code>readFloat</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted as a
     *             <code>float</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading four bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.DataInputStream#readInt()
     * @see        java.lang.Float#intBitsToFloat(int)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * See the general contract of the <code>readDouble</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted as a
     *             <code>double</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading eight bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.DataInputStream#readLong()
     * @see        java.lang.Double#longBitsToDouble(long)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

        private char lineBuffer[];

    /**
     * See the general contract of the <code>readLine</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @deprecated This method does not properly convert bytes to characters.
     * As of JDK&nbsp;1.1, the preferred way to read lines of text is via the
     * <code>BufferedReader.readLine()</code> method.  Programs that use the
     * <code>DataInputStream</code> class to read lines can be converted to use
     * the <code>BufferedReader</code> class by replacing code of the form:
     * <blockquote><pre>
     *     DataInputStream d =&nbsp;new&nbsp;DataInputStream(in);
     * </pre></blockquote>
     * with:
     * <blockquote><pre>
     *     BufferedReader d
     *          =&nbsp;new&nbsp;BufferedReader(new&nbsp;InputStreamReader(in));
     * </pre></blockquote>
     *
     * @return     the next line of text from this input stream.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.BufferedReader#readLine()
     * @see        java.io.FilterInputStream#in
     */
        public final String readLine() throws IOException {
        InputStream in = this.in;
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

    loop:	while (true) {
        switch (c = inRead()) {
        case -1:
        case '\n':
            break loop;

        case '\r':
            int c2 = inRead();
            if ((c2 != '\n') && (c2 != -1)) {
                if (!(in instanceof PushbackInputStream)) {
                    in = this.in = new PushbackInputStream(in);
                }
                ((PushbackInputStream)in).unread(c2);
            }
            break loop;

        default:
            if (--room < 0) {
                buf = new char[offset + 128];
                room = buf.length - offset - 1;
                System.arraycopy(lineBuffer, 0, buf, 0, offset);
                lineBuffer = buf;
            }
            buf[offset++] = (char) c;
            break;
        }
    }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }
    

    /**
     * See the general contract of the <code>readUTF</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     a Unicode string.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.DataInputStream#readUTF(java.io.DataInput)
     */
    public final String readUTF() throws IOException {
        //        int utflen = in.readUnsignedShort();

        int utflen = (int)readInfiniteLong();

        boolean compressedMode = false;
        int charMin = 0;
        int codingLength = 8;

        if (utflen==0) return "";

        // test du compressed
        if(CodingConstants.ALLOWSUTFCOMPRESSEDMODE){
            compressedMode = readBoolean();
            if (compressedMode){
                charMin = readByte();
                codingLength = readByte(3);

                /*System.out.println("UTF compressed mode = ok");
                  System.out.println("charMin      = " + charMin);
                  System.out.println("codingLength = " + codingLength);*/

            }
        }

        char str[] = new char[utflen];
        int count = 0;
        int strlen = 0;
        while (count < utflen) {
            int c = 0;
            if (compressedMode == true){
                c = readByte(codingLength);
                c += charMin;
            } else {
                c = readUnsignedByte();
            }
            
            int char2, char3;
            switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                count++;
                str[strlen++] = (char)c;
                break;
            case 12: case 13:
                // 110x xxxx   10xx xxxx
                count += 2;
                if (count > utflen)
                    throw new UTFDataFormatException();
                char2 = readUnsignedByte();
                if ((char2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException();
                str[strlen++] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                count += 3;
                if (count > utflen)
                    throw new UTFDataFormatException();
                char2 = readUnsignedByte();
                char3 = readUnsignedByte();
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException();
                str[strlen++] = (char)(((c & 0x0F) << 12) |
                                       ((char2 & 0x3F) << 6) |
                                       ((char3 & 0x3F) << 0));
                break;
            default:
                // 10xx xxxx,  1111 xxxx
                throw new UTFDataFormatException();
            }
        }

        String s =new String(str, 0, strlen);
        
        if (CONCISEDEBUG) System.out.println("  io: read UTF " + s);
        
        return s;
    }

    /** ADDED BY CLAUDE */
    private int    significantBits  = 0;
    private int   inputByteBuffer = 0;
    static private byte[] masks = null;
    static {
        masks = new byte[8];
        for (byte t=0;t<8;t++)
            masks[t] = (byte)(~(0xFF >>> t));
    }

    final private int getNexByteGivenThisOne(int b){
        
        if (DEBUG) System.out.println("              GETNEXBYTEGIVENTHISONE");
        
        if (significantBits == 0){
            if (DEBUG) System.out.println("              result = " + b);
            return b;
        }
        else
            {
                int i = (b & 0xFF);

                if (DEBUG) System.out.println("             old inputbuf  = " + getBinaryString(inputByteBuffer));
                if (DEBUG) System.out.println("             new byte read = " + getBinaryString(i));
                
                if (DEBUG) System.out.println("                ret        = " +  getBinaryString((inputByteBuffer | (i >>> significantBits)))) ;

                int ret = (i >>> significantBits) | inputByteBuffer;
                inputByteBuffer = (i << (8-significantBits))&0xFF;
                // significantBits = significantBits;

                if (DEBUG) System.out.println("             new inputbuf  = " + getBinaryString(inputByteBuffer));
                if (DEBUG) System.out.println("            new signifbits = " + significantBits);
                if (DEBUG) System.out.println("");
                if (DEBUG) System.out.println("              result = " + ret);
                return ret;
            }

    }

    final private int inRead() throws IOException {

        //System.out.println("               inread byte");
        iReadedBytes++;
        return getNexByteGivenThisOne(in.read());
    }  
    
    final private int inRead(byte[] b, int off, int len) throws IOException {
        
        //if (DEBUG) System.out.println("               inread bytes[]");

        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        iReadedBytes+=len;
        int readbytes = in.read(b,off,len);
        if (readbytes == -1) return -1;

        for (int t=off;t<off+readbytes;t++)
            b[t] = (byte)getNexByteGivenThisOne(b[t]);

        return readbytes;
    }
   

    final public int readByte(int nbits) throws IOException {
        if (DEBUG) System.out.println("==============> readbyte de " + nbits);
        if (nbits == 0) {
            if (CONCISEDEBUG) System.out.println("  io: readByte "+nbits+" bits result = " + 0);
            return 0;
        }

        if (nbits == 8) {
            int ret= inRead() & 0xFF;
            if (CONCISEDEBUG) System.out.println("  io: readByte "+nbits+" bits result = " + ret);
            return ret;
        }
        else {

            // on a assez de bits dans le buffer
            
            if (nbits <= significantBits) 
                {
                    int ret = inputByteBuffer >>> 8-nbits;
                    inputByteBuffer = (inputByteBuffer << nbits)&0xFF;
                    significantBits = significantBits - nbits;
                    if (DEBUG) System.out.println("         - ASSEZ DE BITS dans l'input buffer");
                    if (DEBUG) System.out.println("             new inputbuf  = " + getBinaryString(inputByteBuffer));
                    if (DEBUG) System.out.println("            new signifbits = " + significantBits);
                    if (CONCISEDEBUG) System.out.println("  io: readByte "+nbits+" bits result = " + (ret&0xff));
                    return ret & 0xFF;
                }
            
            // pas assez de bits dans le buffer : on lit un nouvel octet
            iReadedBytes++;
            int i = in.read();
            
            if (DEBUG) System.out.println("             old inputbuf  = " + getBinaryString(inputByteBuffer));
            if (DEBUG) System.out.println("             new byte read = " + getBinaryString(i));
            
            if (DEBUG) System.out.println("                decalage 1 = " +  getBinaryString((inputByteBuffer | (i >>> significantBits)))) ;
            int ret = (inputByteBuffer | (i >>> significantBits)) >>> (8 - nbits);
            if (DEBUG) System.out.println("                decalage 2 = " + getBinaryString(ret));
            
            inputByteBuffer = (i << ( nbits - significantBits)) & 0xFF;
            significantBits = 8 - (nbits - significantBits);
            if (DEBUG) System.out.println("             new inputbuf  = " + getBinaryString(inputByteBuffer));
            if (DEBUG) System.out.println("            new signifbits = " + significantBits);
            if (CONCISEDEBUG) System.out.println("  io: readByte "+nbits+" bits result = " + (ret&0xff));
            return ret & 0xFF;
        }
    }

    int iReadedBytes=0;

    // Renvoie le nombre de bits lus depuis la création du BitToBitDataInputStream
    public int getReadedBits() {
        return iReadedBytes*8-significantBits;
    }

    // Skippe (saute) nbits
    public void skip(int nbits) throws IOException {
        for (int i=0;i<nbits/8;i++) { readByte(); } 
        readByte(nbits%8);
    }

    private int savedSignificantBits=0;
    private int savedInputByteBuffer=0;

    public void mark(int nbytes) {
        super.mark(nbytes);
        savedInputByteBuffer=inputByteBuffer;
        savedSignificantBits=significantBits;
    }

    public void reset() throws IOException {
        super.reset();        
        inputByteBuffer=savedSignificantBits;
        significantBits=savedInputByteBuffer;
    }

    public boolean markSupported() {
        return super.markSupported();
    } 

    static public String getBinaryString(int b){
        
        String s = "";
        
        for (int t=0;t<32;t++){
            if (t%8 == 0)
                s+=" ";
            if (((b >>> 31-t)&0x01) == 1)
                s += "1";
            else
                s += "0";
        }
        
        return s;
    }
    

}


