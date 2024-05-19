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

package com.expway.tools.codec;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.io.PendingChunk;
import com.expway.tools.io.FifoBufferedStream;
import com.expway.tools.io.BitOutputStream;

import com.expway.tools.expression.SimpleTypeDefinition;

import java.io.*;
import com.jcraft.jzlib.*;

public class ZLibCodec implements Codec {
    final static int BUFFERSIZE=50000;
    final static boolean DEBUG=false;

    PendingChunk myPendingChunk;

    InputStream fifoIn=null;
    OutputStream fifoOut=null;
    FifoBufferedStream fifo=null;

    int err;
    ZStream d_stream,c_stream;
    byte[] outBuffer;

    public static int FIFOSIZE=1000;

    public ZLibCodec() {
        reset();
    }

    public void reset() {
        outBuffer=new byte[BUFFERSIZE];
        d_stream=new ZStream();
        c_stream=new ZStream();
        err=d_stream.inflateInit();
        err=c_stream.deflateInit(JZlib.Z_BEST_COMPRESSION);
        myPendingChunk=null;
        fifo=new FifoBufferedStream(FIFOSIZE);
        fifoOut=fifo.getOutputStream();
        fifoIn=fifo.getInputStream();
    }

    /**
     *  Compresses and flushes the input inBuffer buffer with ZLib. 
     */
    private byte[] readAndCompressBytes(byte [] inBuffer) {
        c_stream.next_in=inBuffer;
        c_stream.next_in_index=0;
        c_stream.avail_in=inBuffer.length;

        c_stream.next_out=outBuffer;
        c_stream.next_out_index=0;
        c_stream.avail_out=BUFFERSIZE;

        err=c_stream.deflate(JZlib.Z_SYNC_FLUSH);
        
        int outlenght=BUFFERSIZE-c_stream.avail_out;
        byte out [] =new byte[outlenght];      

        System.arraycopy(outBuffer,0,out,0,outlenght);

        return out;
    }

    /**
     *  Decompress the input inBuffer buffer with ZLib. 
     */
    private byte[] readAndDecompressBytes(byte [] inBuffer) {
        d_stream.next_in=inBuffer;
        d_stream.next_in_index=0;
        d_stream.avail_in=inBuffer.length;

        d_stream.next_out=outBuffer;
        d_stream.next_out_index=0;
        d_stream.avail_out=BUFFERSIZE;

        err=d_stream.inflate(JZlib.Z_SYNC_FLUSH);

        int outlenght=BUFFERSIZE-d_stream.avail_out;
        byte out [] =new byte[outlenght];
 
        System.arraycopy(outBuffer,0,out,0,outlenght);
      
        return out;
    }

    public void encodeItInto(String value, ChunkWriter cw,SimpleTypeDefinition base) {
        try {
            if (DEBUG) System.out.println("encoding value="+value);
           
            ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
            DataOutputStream dataByteOut=new DataOutputStream(byteOut);
            // encode in UTF 8
            byte [] bvalue= value.getBytes("UTF-8");
            dataByteOut.write(bvalue);
            // write the end char
            dataByteOut.write((char)0);
            ByteArrayInputStream byteIn=new ByteArrayInputStream(byteOut.toByteArray());
            
            // As soon as the leaf is not completely encoded...
            while (byteIn.available()>0) {
                // Writes it into the fifo
                if (!fifo.isFull()) fifoOut.write(byteIn.read());
                // If the fifo is full, flushes it
                else  {
                    // Flushes the fifo
                    flush();
                }
            }

            if (myPendingChunk==null) {
                myPendingChunk=new PendingChunk();
                if (DEBUG) System.out.println("creating new myPendingChunk="+myPendingChunk);
                cw.write(myPendingChunk);
            }

        } catch(Exception e) {
            System.out.println("Exception e="+e);
            e.printStackTrace();
        }
    }
    
    public void decode(BitToBitDataInputStream bis,Writer w,SimpleTypeDefinition base) {
        try {
            String value=null;
            if (DEBUG) System.out.println("decode...");
            boolean catchedstring=false;
            ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
            while (!catchedstring) {
                // If fifo is not empty, tries to read a string on it
                if (!fifo.isEmpty()) {
                    int c=fifoIn.read();
                    
                    if (c==0) {
                        value=new String(byteOut.toByteArray(),"UTF-8");
                        catchedstring=true;
                    } else {
                        byteOut.write(c);
                    }

                    // If fifo is empty, feeds the fifo by reading a compressed chunk on the stream
                } else {
                    // Reads a compressed chunk and decompresses it
                    if (DEBUG) System.out.println("fifo empty = >read compressed data...");
                    int compressedLength=(int)bis.readInfiniteLong();
                    if (DEBUG) System.out.println("length="+compressedLength);
                    byte compressedBytes[]=new byte[compressedLength];
                    for (int i=0;i<compressedLength;i++) compressedBytes[i]=bis.readByte();
                    if (DEBUG) System.out.println(hexaData(compressedBytes));
                    byte decompressedBytes[]=readAndDecompressBytes(compressedBytes);
                    if (DEBUG) System.out.println(hexaData(decompressedBytes));
                    // Feeds the fifo
                    fifoOut.write(decompressedBytes);     
                }
            }

            if (DEBUG) System.out.println("readed value="+value);            

            w.write(value);
        } catch(Exception e) {
            System.out.println("Exception e="+e);
            e.printStackTrace();
        }
    }

    public void dispose() {
        flush();
    }

    public void init() {
    }

    private void flush() {
        try {
            // Flushes the fifo
            ByteArrayOutputStream bytesOutFromFifo=new ByteArrayOutputStream();
            while (!fifo.isEmpty()) {
                bytesOutFromFifo.write(fifoIn.read());
            }
            // pour afficher
            // System.out.println("LAST flushing this="+this);                    
            // System.out.println(charData(bytesOutFromFifo.toByteArray()));
                
            // Compresses the flushed bytes
            byte [] compressedBytes=readAndCompressBytes(bytesOutFromFifo.toByteArray());
            int compressedLength=compressedBytes.length;            
 
            // System.out.println("input="+bytesOutFromFifo.size()+" compressedLength="+compressedLength);              

            // Makes a new Chunk
            ChunkWriter compressedChunk=new ChunkWriter();
            compressedChunk.writeInfiniteLong(compressedLength);

            for (int i=0;i<compressedLength;i++) compressedChunk.writeByte(compressedBytes[i]);

            if (myPendingChunk!=null) {
                if (DEBUG) System.out.println("myPendingChunk="+myPendingChunk+" is released with length="+compressedLength+" data:");
                if (DEBUG) System.out.println(hexaData(compressedBytes));                                  
                myPendingChunk.setChunk(compressedChunk);
                myPendingChunk=null;
            } else {
                System.out.println("ERROr pas NORMAL de GREG");
            }
        } catch(Exception e) {
            System.out.println("ex="+e);
            e.printStackTrace();
        }
    }

    public String hexaData(byte []in) {
        String out="";

        for (int i=0;i<in.length;i++) {
            String chunk=(Integer.toHexString(in[i]&0xff)).toUpperCase();
            if (chunk.length()==1) chunk="0"+chunk;
            out+=chunk+" ";
            if ((i%16)==15) out+="\n";
        }

        return out;
    }

    public String charData(byte []in) {
        String out="";

        for (int i=0;i<in.length;i++) {
            //String chunk=(Char.toHexString(in[i]&0xff)).toUpperCase();
            //if (chunk.length()==1) chunk="0"+chunk;
            int v=in[i]&0xff;
            Character chunk;
            if (v>31 && v<128) chunk=new Character((char)v);
            else chunk=new Character('?');
             
            out+=chunk;
            if ((i%48)==47) out+="\n";
        }

        return out;
    }
}
