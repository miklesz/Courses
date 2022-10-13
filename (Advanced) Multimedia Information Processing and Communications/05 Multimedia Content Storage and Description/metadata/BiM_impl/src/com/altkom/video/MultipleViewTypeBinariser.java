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
   An object of this class can binarise an element of the type MultipleViewType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class MultipleViewTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String FIXED_VIEWS = "fixedViewsFlag";
   private static final String IS_VISIBLE  = "IsVisible";
   private static final String DESCRIPTOR  = "Descriptor";
   private static final String DESCR_TYPE  = "xsi:type";
   /* constants */
   private static final int MAX_DESCRIPTORS = 16;

   int dID;
   boolean fixedFlag;
   boolean[] isVisible;
   Binariser[] descriptors;

   String currentElement;
   int numOfViews;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("MultipleView: startElement "+qualified);

      if (isAtRootLevel())
      {
         dID = 0; //0 is forbidden for descriptor ID in binary format
         String str = attrs.getValue(FIXED_VIEWS);
         fixedFlag = ((str != null) && str.equals("true"));
         isVisible = new boolean[MAX_DESCRIPTORS];
         descriptors = new Binariser[MAX_DESCRIPTORS];
         currentElement = null;
         numOfViews = 0;
      }
      else if (localName.equals(IS_VISIBLE))
      {
         numOfViews++;
         currentElement = IS_VISIBLE;
      }
      else if (localName.equals(DESCRIPTOR))
      {
         currentElement = DESCRIPTOR;
         if (numOfViews == 1) //firs occurance of element DESCRIPTOR, numOfViews was set in IS_VISIBLE
         {
            String str = attrs.getValue(DESCR_TYPE);
            if (DEBUG) System.out.println("MultipleView: descriptor: "+str);
            for (int i = 0; i < DESCRIPTOR_NAMES.length; i++)
               if (str.equals(DESCRIPTOR_NAMES[i]+"Type"))
               {
                  dID = i;
                  break;
               }
         }//if currIdx==0
         try
         {
            descriptors[numOfViews-1] = (Binariser)(Class.forName("com.altkom.video."+
                                                            DESCRIPTOR_NAMES[dID]+ "TypeBinariser")
                                                   .newInstance());
         }
         catch (Exception x)
         {
            if (DEBUG) System.out.println("MultipleView: error creating binariser: "+DESCRIPTOR_NAMES[dID]);
            if (DEBUG) x.printStackTrace();

            throw new SAXException("MultipleView: error creating binariser: "+DESCRIPTOR_NAMES[dID]);
         }
         descriptors[numOfViews-1].takeDescriptor(null);
         descriptors[numOfViews-1].startElement(nameSpace, localName, qualified, attrs);
      }
      else if (currentElement == DESCRIPTOR)
         descriptors[numOfViews-1].startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == IS_VISIBLE)
      {
         String str = new String(cdata, index, length);
         isVisible[numOfViews-1] = str.equals("true");
         currentElement = null;
      }
      else if (currentElement == DESCRIPTOR)
         descriptors[numOfViews-1].characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == DESCRIPTOR)
      {
         descriptors[numOfViews-1].endElement(nameSpace, localName, qualified);
         if (descriptors[numOfViews-1].isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      if (DEBUG) System.out.println("MultipleView: finish");

      currentChunkWriterUsed.writeInt(dID, 8);              //DescriptorID
      currentChunkWriterUsed.writeBoolean(fixedFlag);       //fixedViewsFlag
      currentChunkWriterUsed.writeByte(numOfViews-1, 4);    //NumOfViews, writes numOfViews-1 because binary 0000 means 1 .... 1111 means 16
      for (int i = 0; i < numOfViews; i++)
      {
         currentChunkWriterUsed.writeBoolean(isVisible[i]); //IsVisible[i]
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
         int id = dis.readInt(8);                                                               //DescriptorID
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+FIXED_VIEWS+"=\""+
                  dis.readBoolean()+"\">\n");                                                   //fixedViewsFlag
         int nov = dis.readByte(4)+1;                                                           //NumOfViews, +1 because of binary coding format
         Binariser subBin = (Binariser)(Class.forName("com.altkom.video."+DESCRIPTOR_NAMES[id]+
                                                         "TypeBinariser").newInstance());
         for (int i = 0; i < nov; i++)
         {
            w.write("<"+ns+IS_VISIBLE+">"+(dis.readBoolean() ? "true" : "false")+           //IsVisible[i]
                        "</"+ns+IS_VISIBLE+">\n");
                                                                                                //Descriptor[i]
            subBin.decode(dis, w, ns+DESCRIPTOR, DESCR_TYPE+"=\""+DESCRIPTOR_NAMES[id]+"Type\"");
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("MultipleView: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+MULTIPLE_VIEW, null);
   }
}