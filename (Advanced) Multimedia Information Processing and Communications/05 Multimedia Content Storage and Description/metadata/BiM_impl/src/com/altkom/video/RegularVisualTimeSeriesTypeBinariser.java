/***********************************************************************
This software module was originally developed by
Andrzej Buchowicz (Altkom Akademia SA), Grzegorz Galinski (Altkom Akademia SA)
Marcin Gawlik (Altkom Akademia SA), Jaroslaw Zuk (Altkom Akademia SA) and
Wladyslaw Skarbek (Altkom Akademia SA) in the course of
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

Altkom Akademia SA retains full right to use the code for his/her own purpose,
assign or donate the code to a third party and to inhibit third parties
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming
products.

This copyright notice must be included in all copies or derivative works.

Copyright Altkom Akademia SA © 2001.
************************************************************************/

package com.altkom.video;

import java.io.Writer;
import java.io.StringWriter;
import java.util.Vector;
import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class RegularVisualTimeSeriesTypeBinariser extends Binariser {
  private static final String OFFSET_ATTR = "offset";
  private static final String TIME_INCR   = "TimeIncr";
  private static final String DESCRIPTOR  = "Descriptor";
  private static final String DESCR_TYPE  = "xsi:type";

  int descrId;
  Vector descriptorChunks;
  MediaDurationType timeIncr, offset;
  String currentElement;
  Binariser subBinariser;
  int maxDescrLength;

  private boolean randomAccessFlag=false;      // how should be set this flag ???
  private int numBits;


  public RegularVisualTimeSeriesTypeBinariser() {
  }

  public void setRandomAccessFlag(boolean randomAccess) {
   randomAccessFlag = randomAccess;
  }
  public boolean getRandomAccessFlag() {
   return randomAccessFlag;
  }

  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);
    if (DEBUG) System.out.println("RegularVisualTimeSeries: " + localName);
    if(isAtRootLevel()){
      maxDescrLength=0;
      descrId=-1;
      currentElement=localName;
      //descriptorList=new Vector();
      descriptorChunks=new Vector();
      String offstr = attrs.getValue(OFFSET_ATTR);
      if (offstr != null)
         offset=new MediaDurationType(offstr);
      timeIncr = null;
      subBinariser = null;
    }else{
      if(localName.equals(TIME_INCR)){
        currentElement=TIME_INCR;
      }

      if(localName.equals(DESCRIPTOR)){
        String typeAttr=attrs.getValue(DESCR_TYPE);
        if(typeAttr==null){
          throw new SAXException("Undefined descriptor type");
        }

        try{
          subBinariser=(Binariser)(Class.forName(
                  "com.altkom.video."+ typeAttr + "Binariser").newInstance());
        }catch (Exception x){
          throw new SAXException("RegularTimeSeries: error creating binariser: "+ typeAttr);
        }

        if(descrId==-1){        // first descriptor in the TimeSeries, descrId not already set
          for (int i = 0; i < DESCRIPTOR_NAMES.length; i++){
            if (typeAttr.equals(DESCRIPTOR_NAMES[i]+"Type"))
            {
              descrId = i;
              break;
            }
          }
        }
        //descriptorList.add(subBinariser);
        subBinariser.takeDescriptor(null);
      }

      if(subBinariser!=null){
        subBinariser.startElement(nameSpace, localName, qualified, attrs);
      }

    }
  }

  public void characters (char [] chars, int index, int length) throws SAXException {
    if(currentElement.equals(TIME_INCR)){
      timeIncr=new MediaDurationType(new String(chars, index, length));
      currentElement="";    // to avoid second reading of the TimeIncr
    }

    if(subBinariser!=null){
      subBinariser.characters(chars,index,length);
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    if(subBinariser!=null){
      subBinariser.endElement(nameSpace, localName, qualified);
      if(subBinariser.isFinished()){
        ChunkWriter cw = new ChunkWriter();
        subBinariser.takeDescriptor(cw);
        subBinariser.finish();
        descriptorChunks.add(cw);
        int descrLength=(int)((cw.sizeInBits()+7)/8); //size in bytes is needed
        if (descrLength>maxDescrLength)
          maxDescrLength=descrLength;
        subBinariser=null;
      }
    }

    if(isFinished()){
      finish();
    }
  }


  public void finish() {
    if((currentChunkWriterUsed == null)){
      return;
    }

    try
    {
       // write DescriptorID
       currentChunkWriterUsed.writeByte(descrId);

       // write NumOfDescriptors
       //currentChunkWriterUsed.writeLong(descriptorList.size(), 32);
       currentChunkWriterUsed.writeLong(descriptorChunks.size(), 32);

       // IsRandomAccess
       currentChunkWriterUsed.writeBoolean(randomAccessFlag);

       // DescriptorLength
       if(randomAccessFlag){
         currentChunkWriterUsed.writeInt(maxDescrLength, 16);
       }

       // TimeIncr
       ChunkWriter ticw = new ChunkWriter();
       timeIncr.writeInto(ticw);
       ticw.writeYourselfInto(currentChunkWriterUsed);
       if (DEBUG) System.out.println("RegularTimeSeries: TimeIncr written ("+ticw.sizeInBits()+")");

       // IsOffset
       ChunkWriter offcw = null;
       if(offset==null){
         currentChunkWriterUsed.writeBoolean(false);
       }else{
         currentChunkWriterUsed.writeBoolean(true);
         offcw = new ChunkWriter();
         offset.writeInto(offcw);
         offcw.writeYourselfInto(currentChunkWriterUsed);
         if (DEBUG) System.out.println("RegularTimeSeries: Offset written ("+offcw.sizeInBits()+")");
       }

       if(randomAccessFlag){   // calculate the number stuffing bits
         int numBits=1 + (int)ticw.sizeInBits()+   // number of written bitts not equal to the multiplicity of 8
                     1 + ((offset != null) ? (int)offcw.sizeInBits() : 0);    // 1 for IsRandomAccess and IsOffset
         int numStuffingBits=((1+numBits/8)*8 - numBits)%8;
         if (numStuffingBits>0)
            currentChunkWriterUsed.writeByte(0xFF,numStuffingBits);
       }

       //Enumeration enum=descriptorList.elements();
       Enumeration chunkEnum=descriptorChunks.elements();
       //while(enum.hasMoreElements()){
       while(chunkEnum.hasMoreElements()){
         //Binariser binariser=(Binariser)(enum.nextElement());
         //binariser.takeDescriptor(currentChunkWriterUsed);
         //binariser.finish();
         ChunkWriter cw=(ChunkWriter)(chunkEnum.nextElement());
         cw.writeYourselfInto(currentChunkWriterUsed);

         if(randomAccessFlag){   // calculate the number stuffing bits
           int numStuffingBits=(int)(8*maxDescrLength-cw.sizeInBits());
           if (numStuffingBits > 0)
           {
             for(int i=0; i<numStuffingBits/32; i++){
               currentChunkWriterUsed.writeInt(0xFFFFFFFF);
             }
             currentChunkWriterUsed.writeInt(0xFFFFFFFF, numStuffingBits%32);
           }
         }
       }
    }
    catch (java.io.IOException x)
    {
      System.out.println("RegularVisualTimeSeries: Can't finish descriptor.");
      x.printStackTrace();
    }
  }


  public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs) {
    if (DEBUG) System.out.println("RegularVisualTimeSeries: decode");
    long before = dis.getReadedBits();
    try{
      String ns = getNameSpace(name);
      int descrId=dis.readInt(8);
      String descrName=Binariser.DESCRIPTOR_NAMES[descrId];
      long numDescr=dis.readLong(32);
      boolean israndom=dis.readBoolean();
      int descrLength = 0;
      if(israndom){
        descrLength=dis.readInt(16);
      }

      StringWriter tiwr = new StringWriter();
      long bef = dis.getReadedBits();
      (new MediaDurationType()).readContentFrom(dis, tiwr);
      int tisize = (int)(dis.getReadedBits()-bef);
      String timeIncr=tiwr.toString();
      if (DEBUG) System.out.println("RegularTimeSeries: timeIncr read ("+tisize+")");

      boolean isOffset=dis.readBoolean();
      String offset = null;
      int offsize = 0;
      if(isOffset){
         StringWriter owr = new StringWriter();
         bef = dis.getReadedBits();
         (new MediaDurationType()).readContentFrom(dis, owr);
         offsize=(int)(dis.getReadedBits()-bef);
         offset=owr.toString();
         if (DEBUG) System.out.println("RegularTimeSeries: offset read ("+offsize+")");
      }

      if (israndom)
      {
         int numBits=1+tisize+   // number of written bitts not equal to the multiplicity of 8
                     1+((offset != null) ? offsize : 0);
         int numStuffingBits=((1+numBits/8)*8 - numBits)%8;
         dis.readByte(numStuffingBits);
      }

      w.write("\n<" + name );
      if (offset != null)
        w.write(" " + OFFSET_ATTR + "=\"" + offset + "\"");
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(">\n" );

      w.write("<" + ns+TIME_INCR + ">\n");
      w.write("  " + timeIncr);
      w.write("</" + ns+TIME_INCR + ">\n");

      Binariser subBinariser=(Binariser)(Class.forName(
                  "com.altkom.video."+ descrName + "TypeBinariser").newInstance());
      for(long i=0; i<numDescr; i++){
        int dlen = subBinariser.decode(dis, w, ns+DESCRIPTOR, DESCR_TYPE + "=\"" + descrName + "Type\"");
        if(israndom){   // calculate the number stuffing bits
           int numStuffingBits=8*descrLength-dlen;
           if (numStuffingBits > 0)
           {
             for(int k=0; k<numStuffingBits/32; k++){
               dis.readInt();
             }
             dis.readInt(numStuffingBits%32);
           }
        }
      }

      w.write("</" + name + ">\n" );
    }catch (Exception e){
      System.out.println("RegularVisualTimeSeries: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }


  public int decode(BitToBitDataInputStream dis, Writer w) {
    return decode(dis, w, DNS+REGULAR_VISUAL_TIME_SERIES, null);
  }
}