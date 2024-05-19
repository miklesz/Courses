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
import java.util.StringTokenizer;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type HomogeneousTextureType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class TextureBrowsingTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String REGULARITY  = "Regularity";
   private static final String R_IRREGULAR = "irregular";
   private static final String R_SLIGHTLY  = "slightlyRegular";
   private static final String R_REGULAR   = "regular";
   private static final String R_HIGHLY    = "highlyRegular";
   private static final String DIRECTION   = "Direction";
   private static final String D_NO        = "noDirectionality";
   private static final String D_0         = "0Degree";
   private static final String D_30        = "30Degree";
   private static final String D_60        = "60Degree";
   private static final String D_90        = "90Degree";
   private static final String D_120       = "120Degree";
   private static final String D_150       = "150Degree";
   private static final String SCALE       = "Scale";
   private static final String S_FINE      = "fine";
   private static final String S_MEDIUM    = "medium";
   private static final String S_COARSE    = "coarse";
   private static final String S_VERY      = "veryCoarse";
   /* constants */
   private static final String[] BREG = { R_IRREGULAR, R_SLIGHTLY, R_REGULAR, R_HIGHLY }; //values of regularity: index - binary value, BREG[index] - XML value (string)
   private static final String[] BDIR = { D_NO, D_0, D_30, D_60, D_90, D_120, D_150 }; //values of direction: index - binary value, BDIR[index] - XML value (string)
   private static final String[] BSCALE = { S_FINE, S_MEDIUM, S_COARSE, S_VERY }; //values of scale: index - binary value, BSCALE[index] - XML value (string)

   private int numOfComponents, regularity, direction1, scale1, direction2, scale2;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("TextureBrowsing: startElement: "+qualified);

      if (isAtRootLevel())
      {
         numOfComponents = 0;
         currentElement = null;
      }
      else if (localName.equals(REGULARITY))
         currentElement = REGULARITY;
      else if (localName.equals(DIRECTION))
      {
         numOfComponents++;
         currentElement = DIRECTION;
      }
      else if (localName.equals(SCALE))
         currentElement = SCALE;
   }

   public void characters(char[] chars, int index, int length) throws SAXException
   {
      String str = new String(chars, index, length);

      if (DEBUG) System.out.println("TextureBrowsing: characters: "+str);

      if (currentElement == REGULARITY)
      {
         for (int i = 0; i < BREG.length; i++)
            if (str.equals(BREG[i]))
            {
               regularity = i;
               break;
            }
         currentElement = null;
      }
      else if (currentElement == DIRECTION)
      {
         for (int i = 0; i < BDIR.length; i++)
            if (str.equals(BDIR[i]))
            {
               if (numOfComponents == 1)
                  direction1 = i;
               else //if (numOfComponents == 2)
                  direction2 = i;
               break;
            }
         currentElement = null;
      }
      else if (currentElement == SCALE)
      {
         for (int i = 0; i < BSCALE.length; i++)
            if (str.equals(BSCALE[i]))
            {
               if (numOfComponents == 1)
                  scale1 = i;
               else //if (numOfComponents == 2)
                  scale2 = i;
            }
         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if(currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeBoolean(numOfComponents == 2);  //NumOfComponentsFlag
      currentChunkWriterUsed.writeByte(regularity, 2);            //Regularity
      currentChunkWriterUsed.writeByte(direction1, 3);            //Direction[1]
      currentChunkWriterUsed.writeByte(scale1, 2);                //Scale[1]
      if (numOfComponents == 2)
      {
         currentChunkWriterUsed.writeByte(direction2, 3);         //Direction[2]
         currentChunkWriterUsed.writeByte(scale2, 2);             //Scale[2]
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         boolean noc = dis.readBoolean();                                                       //NumOfComponentsFlag
         w.write("<"+ns+REGULARITY+">"+BREG[dis.readByte(2)]+"</"+ns+REGULARITY+">\n"); //Regularity
         w.write("<"+ns+DIRECTION+">"+BDIR[dis.readByte(3)]+"</"+ns+DIRECTION+">\n");   //Direction[1]
         w.write("<"+ns+SCALE+">"+BSCALE[dis.readByte(2)]+"</"+ns+SCALE+">\n");         //Scale[1]
         if (noc)
         {
            w.write("<"+ns+DIRECTION+">"+BDIR[dis.readByte(3)]+"</"+ns+DIRECTION+">\n");//Direction[2]
            w.write("<"+ns+SCALE+">"+BSCALE[dis.readByte(2)]+"</"+ns+SCALE+">\n");      //Scale[2]
         }//if noc
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("TextureBrowsing: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+TEXTURE_BROWSING, null);
   }
}


