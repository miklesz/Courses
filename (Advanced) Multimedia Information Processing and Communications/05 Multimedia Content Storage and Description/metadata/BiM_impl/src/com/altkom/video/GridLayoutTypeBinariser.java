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

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type GridLayoutType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class GridLayoutTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String NUM_X       = "numOfPartX";
   private static final String NUM_Y       = "numOfPartY";
   private static final String MASK        = "descriptorMask";
   private static final String DESCRIPTOR  = "Descriptor";
   private static final String DESCR_TYPE  = "xsi:type";

   int dID, numX, numY;
   boolean[] masks;
   Binariser[] descriptors;

   String currentElement;
   int currIdx;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("GridLayout: startElement "+qualified);

      if (isAtRootLevel())
      {
         dID = 0; //0 is forbidden for descriptor ID in binary format
         numX = Integer.parseInt(attrs.getValue(NUM_X));
         numY = Integer.parseInt(attrs.getValue(NUM_Y));
         String str = attrs.getValue(MASK);
         if (str == null)
            masks = null;
         else
         {
            masks = new boolean[numX*numY];
            for (int i = 0; i < masks.length; i++)
               masks[i] = (str.charAt(i) == '1');
         }
         descriptors = new Binariser[numX*numY];
         currentElement = null;
         currIdx = -1;
      }
      else if (localName.equals(DESCRIPTOR))
      {
         currIdx++;
         currentElement = DESCRIPTOR;
         if (currIdx == 0) //firs occurance of element DESCRIPTOR
         {
            String str = attrs.getValue(DESCR_TYPE);
            for (int i = 0; i < DESCRIPTOR_NAMES.length; i++)
               if (str.equals(DESCRIPTOR_NAMES[i]+"Type"))
               {
                  dID = i;
                  break;
               }
         }//if currIdx==0
         try
         {
            descriptors[currIdx] = (Binariser)(Class.forName("com.altkom.video."+
                                                            DESCRIPTOR_NAMES[dID]+ "TypeBinariser")
                                                   .newInstance());
         }
         catch (Exception x)
         {
            if (DEBUG) System.out.println("GridLayout: error creating binariser: "+DESCRIPTOR_NAMES[dID]);
            if (DEBUG) x.printStackTrace();

            throw new SAXException("GridLayout: error creating binariser: "+DESCRIPTOR_NAMES[dID]);
         }
         descriptors[currIdx].takeDescriptor(null);
         descriptors[currIdx].startElement(nameSpace, localName, qualified, attrs);
      }
      else if (currentElement == DESCRIPTOR)
         descriptors[currIdx].startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == DESCRIPTOR)
         descriptors[currIdx].characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == DESCRIPTOR)
      {
         descriptors[currIdx].endElement(nameSpace, localName, qualified);
         if (descriptors[currIdx].isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeInt(dID, 8);              //DescriptorID
      currentChunkWriterUsed.writeInt(numX, 8);             //numOfPartX
      currentChunkWriterUsed.writeInt(numY, 8);             //numOfPartY
      currentChunkWriterUsed.writeBoolean(masks != null);   //DescriptorMaskPresent
      if (masks != null)
         for (int i = 0; i < masks.length; i++)
            currentChunkWriterUsed.writeBoolean(masks[i]);  //descriptorMask
      for (int i = 0; i <= currIdx; i++)
      {
         descriptors[i].takeDescriptor(currentChunkWriterUsed);
         descriptors[i].finish();                           //Descriptor[i]
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         int id = dis.readInt(8);                        //DescriptorID
         int nox = dis.readInt(8);                       //numOfPartX
         int noy = dis.readInt(8);                       //numOfPartY
         boolean[] ms = null;
         StringBuffer sbms = null;
         if (dis.readBoolean())                          //DescriptorMaskPresent
         {
            ms = new boolean[nox*noy];
            sbms = new StringBuffer(ms.length);
            for (int i = 0; i < ms.length; i++)
            {
               ms[i] = dis.readBoolean();                //descriptorMask
               sbms.append(ms[i] ? '1' : '0');
            }
         }
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+NUM_X+"=\""+nox+"\" "+NUM_Y+
                     "=\""+noy+"\"");
         if (ms != null)
            w.write(" "+MASK+"=\""+sbms+"\"");
         w.write(">\n");
         Binariser subBin = (Binariser)(Class.forName("com.altkom.video."+DESCRIPTOR_NAMES[id]+
                                                         "TypeBinariser").newInstance());
         for (int i = 0; i < nox*noy; i++)
            if (((ms != null) && ms[i]) || (ms == null))

               subBin.decode(dis, w, ns+DESCRIPTOR,  //Descriptor[k]
                                                   DESCR_TYPE+"=\""+DESCRIPTOR_NAMES[id]+"Type\"");
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("GridLayout: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }


   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+GRID_LAYOUT, null);
   }
}