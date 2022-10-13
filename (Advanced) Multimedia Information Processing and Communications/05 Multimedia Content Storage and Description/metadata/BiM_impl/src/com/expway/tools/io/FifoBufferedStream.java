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

import java.io.*;

/**
 *  This is a circular fixed-sized fifo object. It can provide a InputStream and a OutputStream object
 *  by using the getInputStream() and the getOutputStream() methods. The InputStream object supports
 *  the mark() and reset() methods. 
 */
public class FifoBufferedStream {
    private int [] circularbuffer;
    private int readindex;
    private int writeindex;
    private int buffersize;
    private boolean lastisread;
    private int markedreadindex;

    /**
     *  Constructs a fixed-size size bytes fifo buffer.
     */
    public FifoBufferedStream(int size) {
        circularbuffer=new int[size];
        buffersize=size;
        readindex=0;
        writeindex=0;
        lastisread=true;
    }

    private InputStream myInputStream = new InputStream () {
            /**
             *  Reads a byte in the fifo. Throws an IOException if the fifo is empty.
             */
            public int read() throws IOException {
                if (isEmpty()) throw new IOException("Fifo is empty");
                else {
                    int value=(int)circularbuffer[readindex];
                    readindex=(readindex+1)%buffersize;
                    lastisread=true;           
                    return value;
                }
            }

            /**
             *  Supports the mark go back mechanism. No write() calls should be done until a
             *  possible upcoming reset().
             */
            public void mark(int size) {
                markedreadindex=readindex;
            }

            /**
             *  Supports the reset go back mechanism. Meaningful only if no write() calls have be done
             *  since the last mark() call.
             */
            public void reset() {
                readindex=markedreadindex;
            }

            /**
             *  Marking and reset mechanisms are supported.
             */
            public boolean markSupported() {
                return true;
            }
        };
   
    private OutputStream myOutputStream = new OutputStream () {
            /**
             *  Writes a byte in the fifo. Throws an IOException if the fifo is full.
             */
            public void write(int b) throws IOException {        
                if (isFull()) throw new IOException("Fifo is full");
                else {
                    circularbuffer[writeindex]=b&0xff;
                    writeindex=(writeindex+1)%buffersize;          
                    lastisread=false;
                }
            }

            /**
             *  Writes a byte array in the fifo. Throws an IOException if the fifo is full.
             */
            public void write(byte []b) throws IOException {
                for (int i=0;i<b.length;i++) write(b[i]);
            }
        };

    /**
     *  Gets the underlying InputStream object of this fifo stream. The mark() and reset() methods
     *  are partially supported. This means that the state of the fifo after a reset() call is
     *  relevant if no write operations have been done since the last mark() call.
     */
    public InputStream getInputStream() {
        return myInputStream;
    }

    /**
     *  Gets the underlying OutputStream object of this fifo stream.
     */
    public OutputStream getOutputStream() {
        return myOutputStream;
    }

    /**
     *  Returns true if the fifo is full.
     */ 
    public boolean isFull() {
        return (readindex==writeindex && lastisread==false);
    }

    /**
     *  Returns true if the fifo is empty.
     */ 
    public boolean isEmpty() {
        return (readindex==writeindex && lastisread==true);
    }
    
} 
