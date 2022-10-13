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

package com.expway.tools.io;

import java.io.UTFDataFormatException;
import java.io.OutputStream;
import java.io.IOException;

import com.expway.tools.utils.MethodsBag;

import com.expway.binarisation.CodingParameters;

import java.io.*;

/**
 * A BitToBitChunkWriter lets an application write primitive 
 * Java data types to an chunk in a portable way. */

public class ChunkWriter extends Chunk {
    /**
     * The number of bytes written to the data chunk so far. 
     * If this counter overflows, it will be wrapped to Integer.MAX_VALUE.
     */
    private Chunk firstChunk = null;
    private Chunk currentChunk = null;

    /**
     *  Adds a new chunk in the chain.
     */
    protected void addChunk(Chunk c) {
        currentChunk.setPrevious(c);
        currentChunk = c;
    }

    /**
     *  Finish the current element of the chain. A new chunk of type MemoryChunk MUST be added in
     *  order to reuse the ChunkWriter.
     */
    protected void finishChain() {
        ((MemoryChunk)currentChunk).setLastByte(outputByteBuffer>>(8-significantBits),significantBits);
        significantBits=0;
        outputByteBuffer=0;
    }

    /** the significant bits in the output byte buffer not yet written to the chunk*/
    public int   significantBits  = 0;
    
    /** the ouput byte buffer : not yet written to the chunk */
    public int   outputByteBuffer = 0;

    /** constructor */
    public ChunkWriter() {
        firstChunk = currentChunk = new MemoryChunk(new byte[10]);
    }

    public ChunkWriter(Chunk bcw) {
        firstChunk = currentChunk = bcw;
    }
    
    /**
     * Writes the specified byte (the low eight bits of the argument 
     * <code>b</code>) to the underlying Chunk.*/
    public synchronized void write(int b)  {
        if (DEBUG) System.out.println(" #### write  " + b);
        outWrite(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to the underlying Chunk */
    public synchronized void write(byte b[], int off, int len) {
        outWrite(b, off, len);
    }

    /**
     * Writes a <code>boolean</code> to the underlying Chunk as 
     * a 1-byte value. The value <code>true</code> is written out as the 
     * value <code>(byte)1</code>; the value <code>false</code> is 
     * written out as the value <code>(byte)0</code>. */
    public final void writeBoolean(boolean v) {
        if (DEBUG) System.out.println(" #### write Boolean " + v);

        writeByte(v ? 1 : 0,1);
        //@@@@writeByte(v ? 1 : 0);
    }

    /**
     * Writes out a <code>byte</code> to the underlying chunk as 
     * a 1-byte value. */
    public final void writeByte(int v) {
        if (DEBUG) System.out.println(" #### write Byte " + v);
        outWrite(v);
    }

    /**
     * Writes out the nth low bits to the underlying chunk */
    public final void writeByte(int b,int nbits) {
        if (CodingParameters.bAligned) {
            writeByte(b);
        } else {
        
            if (nbits <= 0) return;
            if (closed)
                throw new RuntimeException("Chunk is closed");

            if (DEBUG) System.out.println("     .... write Byte " + b + "[" + nbits + "]   -> " + com.expway.tools.io.BitToBitDataInputStream.getBinaryString(b));

            //System.out.println("add " + b + "(" + nbits +" bits)");
            if (nbits == 8) // cas particulier
                {
                    outWrite(b);
                }
            else if (significantBits == 0)  // cas particulier
                {
                    //System.out.println("RIEN DANS LE BUFFER");
                    outputByteBuffer = (b << (8 - nbits))&0xFF;
                    significantBits = nbits;
                    //System.out.println("   newBF = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                    //System.out.println("   newSB =  " + significantBits);
                }
            else 
                {
                    //System.out.println("BUFFER REMPLI");
                
                    //System.out.println("   oldSB =  " + significantBits);
                    //System.out.println("   oldBf = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                    //System.out.println("  bToAdd = " + BitToBitDataInputStream.getBinaryString(b)); 
                    // on ne garde que les bits signifiants du nouvel octet
                    b =(byte)( b & ~masks[8 - nbits]);
            
                    // combien de bits prend on dans le nouvel octet
                    int decalage = 8 - significantBits - nbits;
                    //System.out.println("       decalage = " + decalage);

                    if (decalage < 0) // b n'est pas passe entierement il en reste un bout dans le buffer
                        {

                            decalage = -decalage;
                            // on decale
                            // on rajoute au buffer de sortie
                            //System.out.println(" bToAdd  = " + BitToBitDataInputStream.getBinaryString(b)); 
                            outputByteBuffer = outputByteBuffer | (b >>> decalage);
                            // on ne garde que les bits signifiants du nouvel octet
                            //System.out.println("      W  = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                            // on ecrit dans le vrai flux
                            effectiveWrite(outputByteBuffer);
                            ////System.out.println("   out = " + outputByteBuffer);
                            // on remet les bits qui restent
                            outputByteBuffer = (b << 8 - decalage)&0xFF;
                            //System.out.println("   newbf = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                            // on reaffecte le nbe de bits signifiants
                            significantBits = decalage;
                            //System.out.println("   newSB =  " + significantBits);

                        } else if (decalage == 0) {

                            outputByteBuffer = outputByteBuffer | b;
                            effectiveWrite(outputByteBuffer);
                            ////System.out.println("   out = " + outputByteBuffer); 
                            significantBits = 0;
                            //System.out.println("  c- SB=" + significantBits);
                
                        } else {
                
                            outputByteBuffer = outputByteBuffer | (b << decalage);
                            significantBits = 8 - decalage;
                            //System.out.println("  d- SB=" + significantBits);
                        }
                }
        }
    }

    /**
     * Writes a <code>short</code> to the underlying chunk as two
     * bytes, high byte first. */
    public final void writeShort(int v) {
        if (DEBUG) System.out.println(" #### write short " + v);
        outWrite((v >>> 8) & 0xFF);
        outWrite((v >>> 0) & 0xFF);
    }
    
    /**
     * Writes a <code>char</code> to the underlying chunk as a 
     * 2-byte value, high byte first. */
    public final void writeChar(int v) {
        if (DEBUG) System.out.println(" #### write char " + v);
        outWrite((v >>> 8) & 0xFF);
        outWrite((v >>> 0) & 0xFF);
    }

    /**
     * Writes an <code>int</code> to the underlying chunk as four
     * bytes, high byte first. */ 
    public final void writeInt(int v) {
        if (DEBUG) System.out.println(" #### write int " + v);
        outWrite((v >>> 24) & 0xFF);
        outWrite((v >>> 16) & 0xFF);
        outWrite((v >>>  8) & 0xFF);
        outWrite((v >>>  0) & 0xFF);
        if (DEBUG) System.out.println(" #### write int end " + v);
    }

    /**
     * Writes an <code>int</code> to the underlying chunk as nbits bits
     * , high byte first. */ 
    public final void writeInt(int v, int nbits) {
        if (DEBUG) System.out.println("\n #### write int " + v + " " + nbits + " bits");

        int piecenum      = nbits / 8;
        int lastPieceBits = nbits % 8;


        writeByte(v >>> (piecenum)*8,lastPieceBits);

        while (piecenum!=0){
            writeByte(v >>> (piecenum-1)*8);
            piecenum --;
        }
        if (DEBUG) System.out.println("\n #### write int end " + v + " " + nbits + " bits");
    }

    /**
     * Writes a <code>long</code> to the underlying chunk as eight
     * bytes, high byte first.*/
    public final void writeLong(long v) {
        if (DEBUG) System.out.println(" #### write long " + v);
        outWrite((int)(v >>> 56) & 0xFF);
        outWrite((int)(v >>> 48) & 0xFF);
        outWrite((int)(v >>> 40) & 0xFF);
        outWrite((int)(v >>> 32) & 0xFF);
        outWrite((int)(v >>> 24) & 0xFF);
        outWrite((int)(v >>> 16) & 0xFF);
        outWrite((int)(v >>>  8) & 0xFF);
        outWrite((int)(v >>>  0) & 0xFF);
    }

    /**
     * Writes a <code>long</code> to the underlying chunk as eight
     * bytes, high byte first.*/
    public final void writeLong(long v,int nbits) {
        if (DEBUG) System.out.println("\n #### write int " + v + " " + nbits + " bits");

        int piecenum      = nbits / 8;
        int lastPieceBits = nbits % 8;

        //System.out.println("piecenum = " + piecenum);
        //System.out.println("lastPieceBits = " + lastPieceBits);

        writeByte((int)(v >>> (piecenum)*8),lastPieceBits);

        while (piecenum!=0){
            writeByte((int)(v >>> (piecenum-1)*8));
            piecenum --;
        }
    }

    /**
     * Writes a <code>long</code> to the underlying chunk as eight
     * bytes, high byte first.*/
    public final int writeInfiniteLong(long v) {
        return writeInfiniteLong(v,CodingConstants.DEFAULTPIECESIZE);
    }

 /* OLD VERSION entrelacée 
    public final int writeInfiniteLong(long v,int piecesize) {
    if (DEBUG) System.out.println(" #### write infinite long " + v + " by pieces of " + piecesize + " bits");

    int sizeinbits = 0;
    System.out.println("CODAGE INFINI de " + v + " " + com.expway.tools.io.BitToBitDataInputStream.getBinaryString((int)v));

    // recherche la partie utile de l'entier (dans t)
    int t=63;
    while ( ((v>>>t) & 0x01) == 0 && t!=0) t--;
    System.out.println("t = " + t);

    // calcul du bout qui contient l'octet
    int piecenum = (t / piecesize);
    System.out.println("piecenum = " + piecenum);

    while (piecenum!=0){
    writeBoolean(true); // continue
    sizeinbits++;
    System.out.println("true");

    writeByte( ((int)(v >>> (piecenum*piecesize))) & ~masks[8-piecesize],piecesize);
    sizeinbits+=piecesize;
    System.out.println(com.expway.tools.io.BitToBitDataInputStream.getBinaryString(((int)(v >>> (piecenum*piecesize))) & ~masks[8-piecesize] ));
    piecenum--;
    }
    writeBoolean(false); // continue
    sizeinbits++;
    System.out.println("false");

    writeByte( (int)(v & ~masks[8-piecesize]),piecesize);
    sizeinbits+=piecesize;
    System.out.println(com.expway.tools.io.BitToBitDataInputStream.getBinaryString((int)(v & ~masks[8-piecesize])));
    System.out.println("==> SIZE IN BITS = " + sizeinbits);
    return sizeinbits;
    }
 */

    public final int writeInfiniteLong(long v,int piecesize) {          
        if (v<0) throw new RuntimeException("Can't encode negative values for infinite integers");

        // +1 car getCodingLength est getCodingLength(int iNonInclusif)      
        int sizedat=MethodsBag.getCodingLength(v+1);
        int nbblocks=(int)Math.ceil(((double)(sizedat))/((double)piecesize));
        
        if (v==0) nbblocks=1;
        
        sizedat=nbblocks*piecesize;

        // First part ; structure ; -1 car on suppose qu'il y a toujours au moins 1 block
        // Ainsi, 0dec =>  00000
        //        1dec =>  00001
        //       15dec =>  01111
        //       16dec =>  1000010000
        for (int i=0;i<nbblocks-1;i++) writeBoolean(true);
        writeBoolean(false);

        // Second part ; data
        writeLong(v,sizedat);
        
        if (DEBUG) System.out.println("v="+v+" nbblocks="+nbblocks+" sizedat="+sizedat);
        
        return nbblocks+sizedat;
    }

    /**
     * Converts the float argument to an <code>int</code> using the 
     * <code>floatToIntBits</code> method in class <code>Float</code>, 
     * and then writes that <code>int</code> value to the underlying 
     * chunk as a 4-byte quantity, high byte first. */ 
    public final void writeFloat(float v) {
        if (DEBUG) System.out.println(" #### write float " + v);
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Converts the double argument to a <code>long</code> using the 
     * <code>doubleToLongBits</code> method in class <code>Double</code>, 
     * and then writes that <code>long</code> value to the underlying 
     * chunk as an 8-byte quantity, high byte first. */ 
    public final void writeDouble(double v) {
        if (DEBUG) System.out.println(" #### write double " + v);
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes out the string to the underlying chunk as a 
     * sequence of bytes. Each character in the string is written out, in 
     * sequence, by discarding its high eight bits. */ 
    public final void writeBytes(String s) {
        if (DEBUG) System.out.println(" #### write bytes " + s);
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            outWrite((byte)s.charAt(i));
        }
    }

    /**
     * Writes a string to the underlying chunk as a sequence of 
     * characters. Each character is written to the data chunk as 
     * if by the <code>writeChar</code> method. */ 
    public final void writeChars(String s) {
        if (DEBUG) System.out.println(" #### write chars " + s);
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            outWrite((v >>> 8) & 0xFF);
            outWrite((v >>> 0) & 0xFF);
        }
    }

    
    /**
     * Writes a string to the underlying chunk using UTF-8 
     * encoding in a machine-independent manner. 
     * <p>
     * First, two bytes are written to the chunk as if by the 
     * <code>writeShort</code> method giving the number of bytes to 
     * follow. This value is the number of bytes actually written out, 
     * not the length of the string. Following the length, each character 
     * of the string is output, in sequence, using the UTF-8 encoding 
     * for the character. */
    public final void writeUTF(String str) throws UTFDataFormatException {
        if (str == null || str.equals("")){
            writeInfiniteLong(0);
            return;
        } 

        if (DEBUG) System.out.println(" #### write utf " + str);
        
        int strlen = str.length();
        int utflen = 0;
        int charMin = Integer.MAX_VALUE, charMax = Integer.MIN_VALUE;
        boolean compressedMode = true;

        if (CodingConstants.ALLOWSUTFCOMPRESSEDMODE != true)
            compressedMode = false;

        for (int i = 0 ; i < strlen ; i++) {
            int c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                if (charMin > c) charMin = c;
                if (charMax < c) charMax = c;
                utflen++;
            } else if (c > 0x07FF) {
                compressedMode = false;
                utflen += 3;
            } else {
                compressedMode = false;
                utflen += 2;
            }
        }
        
        /*
          if (utflen > 65535)
          throw new UTFDataFormatException();		  
          outWrite((utflen >>> 8) & 0xFF);
          outWrite((utflen >>> 0) & 0xFF);
        */
        
        writeInfiniteLong(utflen);
        
        if (compressedMode == true){
            writeBoolean(true);
            writeByte(charMin);
            int codingLength = (int)Math.ceil(Math.log((double)(charMax-charMin)+1)/Math.log(2.0));
            writeByte(codingLength,3);

            /*System.out.println("UTF compressed mode = ok : string = "+str);
              System.out.println("charMin      = " + charMin);
              System.out.println("charMax      = " + charMax);
              System.out.println("codingLength = " + codingLength);*/

            for (int i = 0 ; i < strlen ; i++) {
                int c = str.charAt(i);
                writeByte(c-charMin,codingLength);
            }
        } else {
            if (CodingConstants.ALLOWSUTFCOMPRESSEDMODE)
                writeBoolean(false);
            for (int i = 0 ; i < strlen ; i++) {
                int c = str.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    outWrite(c);
                } else if (c > 0x07FF) {
                    outWrite(0xE0 | ((c >> 12) & 0x0F));
                    outWrite(0x80 | ((c >>  6) & 0x3F));
                    outWrite(0x80 | ((c >>  0) & 0x3F));
                } else {
                    outWrite(0xC0 | ((c >>  6) & 0x1F));
                    outWrite(0x80 | ((c >>  0) & 0x3F));
                }
            }
        }
    }

    /**
     *  Returns the number of bits contained in this ChunkWriter.
     *  Returns -1 if this size is still unknown.
     */
    public final long sizeInBits() {
        long nbits=0;
        Chunk c = firstChunk;

        while (c!=null){
            long sizeInBits=c.sizeInBits();
            if (sizeInBits==-1) return -1;
            else nbits+=sizeInBits;
            c=c.getPrevious();
        }

        return nbits+significantBits;
    }
    
    
    /**
     * used internally to allow bittobit output
     **/
    
    static private byte[] masks = null;
    static {
        masks = new byte[8];
        for (byte t=0;t<8;t++){
            masks[t] = (byte)(~(0xFF >>> t));
            //System.out.println(" t=" +t+" b=" + com.expway.tools.io.BitToBitDataInputStream.getBinaryString(masks[t]));
        }
    }

    private final void outWrite(int b) {
        if (DEBUG) System.out.println("     .... write Byte " + b + "[8]   -> " + 
                                      com.expway.tools.io.BitToBitDataInputStream.getBinaryString(b));
       if (closed)
            throw new RuntimeException("Chunk is closed");

        if (significantBits == 0){
            effectiveWrite(b);
        }
        else
            {
                //System.out.println("BUFFER REMPLI");
                
                //System.out.println("   oldSB =  " + significantBits);
                //System.out.println("   oldBf = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                //System.out.println("  bToAdd = " + BitToBitDataInputStream.getBinaryString(b));
                // on ne garde que les bits signifiants du nouvel octet

                // on cree l'octet
                outputByteBuffer = ((b&0xFF) >>> significantBits) | outputByteBuffer;
                // on l'envoi dans le vrai flux
                //System.out.println("      W  = " + BitToBitDataInputStream.getBinaryString(outputByteBuffer));
                effectiveWrite(outputByteBuffer);
                //System.out.println("   out = " + outputByteBuffer);
                // on met dans le buffer ce qu'il reste
                outputByteBuffer = (b << 8 - significantBits)&0xFF;
                // les significantBits sont les memes
                // significantBits = significantBits
            }
    }
    
    private final void outWrite(byte[] b, int off, int len) {

        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int t=off;t<off+len;t++)
            outWrite(b[t]);
    }  

    /** the method which effectively write the byte to the chunk */
    private void effectiveWrite(int byteToWrite){
        
        if (DEBUG) System.out.println("             effectiveWrite " + byteToWrite + "[8]   -> " + com.expway.tools.io.BitToBitDataInputStream.getBinaryString(byteToWrite));

        if (currentChunk == null)
            throw new RuntimeException("No Chunk Available available");
        
        if (closed)
            throw new RuntimeException("Chunk is closed");
        
        if (currentChunk.isFull()){
            addChunk(new MemoryChunk(new byte[10]));
        }

        //System.out.println(" =====> write " + com.expway.tools.io.BitToBitDataInputStream.getBinaryString(byteToWrite));
        
        currentChunk.writeByte(byteToWrite);
    }

    /**
     *  Writes a PendingChunk in the chain.
     */
    public void write(PendingChunk pc) {        
        finishChain();
        addChunk(pc);
        addChunk(new MemoryChunk(new byte[10]));   
    }

    public void writeYourselfInto(ChunkWriter cw){
        if (DEBUG) System.out.println("DEBUT writeYourself : chunkwriter");
        Chunk c = firstChunk;

        //cw.finishChain();

        /*
        cw.addChunk(this);
        cw.addChunk(new MemoryChunk(new byte[10])); 
        */

        while (c!=null){
            //System.out.println("writing chunk="+c+" into cw="+this);            
            Chunk pre=c.getPrevious();
            c.writeYourselfInto(cw);
            c = pre;
        }
        

        // et enfin on code les quelques bits qui restent
        if (significantBits>0){     
            if (DEBUG) System.out.println("   write last "+ significantBits+" bits ");
            cw.writeByte(outputByteBuffer>>(8-significantBits),significantBits);
        }
         if (DEBUG) System.out.println("FIN writeYourself : chunkwriter");
    }

    public void writeYourselfInto(BitOutputStream os) throws IOException {
        if (DEBUG) System.out.println("DEBUT writeYourself in OS : chunkwriter");
        
        Chunk c = firstChunk;
        while (c!=null){
            //System.out.println("writing :"+c);            
            c.writeYourselfInto(os);
            c = c.getPrevious();
        }

        // et enfin on code le tout dernier octet
        if (significantBits>0){
            if (DEBUG) System.out.println("Le tout dernier octet");
            os.write(outputByteBuffer>>(8-significantBits),significantBits);
        }
 
        if (DEBUG) System.out.println("FIN writeYourself in OS : chunkwriter");
    }

	
}


