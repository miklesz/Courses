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
public class HomogeneousTextureTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String AVERAGE    = "Average";
   private static final String STD_DEV    = "StandardDeviation";
   private static final String ENERGY     = "Energy";
   private static final String ENERGY_DEV = "EnergyDeviation";
   /* constants */
   private static final int NO_OF_ENERGY = 30;

   private int average, stdDev;
   private int[] energy, energyDev;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println ("HomogeneousTexture: startElement: "+qualified);

      if (isAtRootLevel())
      {
         average = stdDev = 0;
         energy = energyDev = null;
         currentElement = null;
      }
      else if (localName.equals(AVERAGE))
         currentElement = AVERAGE;
      else if (localName.equals(STD_DEV))
         currentElement = STD_DEV;
      else if (localName.equals(ENERGY))
      {
         energy = new int[NO_OF_ENERGY];
         currentElement = ENERGY;
      }
      else if (localName.equals(ENERGY_DEV))
      {
         energyDev = new int[NO_OF_ENERGY];
         currentElement = ENERGY_DEV;
      }
   }

   public void characters(char[] chars, int index, int length) throws SAXException
   {
      String str = new String(chars, index, length);

      if (DEBUG) System.out.println("HomogeneousTexture: characters: "+str);

      if (currentElement == AVERAGE)
      {
         average = Integer.parseInt(str.trim());
         currentElement = null;
      }
      else if (currentElement == STD_DEV)
      {
         stdDev = Integer.parseInt(str.trim());
         currentElement = null;
      }
      else if (currentElement == ENERGY)
      {
         StringTokenizer st = new StringTokenizer(str);
         for (int i = 0; i < energy.length; i++)
            energy[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
      else if (currentElement == ENERGY_DEV)
      {
         StringTokenizer st = new StringTokenizer(str);
         for ( int i =0 ; i < energyDev.length ; i++)
            energyDev[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement (nameSpace, localName, qualified);

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeBoolean(energyDev != null);  //EnergyDeviationFlag
      currentChunkWriterUsed.writeInt(average, 8);             //Average
      currentChunkWriterUsed.writeInt(stdDev, 8);              //StandardDeviation
      for (int i = 0 ; i < energy.length; i++)
         currentChunkWriterUsed.writeInt(energy[i], 8);        //Energy[i]
      if (energyDev != null)
         for (int i = 0 ; i < energyDev.length; i++)
            currentChunkWriterUsed.writeInt(energyDev[i], 8);  //EnergyDeviation[i]
   }


   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         boolean edf = dis.readBoolean();                                           //EnergyDeviationFlag
         w.write("<"+ns+AVERAGE+">"+dis.readInt(8)+"</"+ns+AVERAGE+">\n");  //Average
         w.write("<"+ns+STD_DEV+">"+dis.readInt(8)+"</"+ns+STD_DEV+">\n");  //StandardDeviation
         w.write("<"+ns+ENERGY+">");
         for (int i = 0; i < NO_OF_ENERGY; i++)
            w.write(dis.readInt(8)+" ");                                            //Energy[i]
         w.write("</"+ns+ENERGY+">\n");
         if (edf)
         {
            w.write("<"+ns+ENERGY_DEV+">");
            for (int i = 0; i < NO_OF_ENERGY; i++)                                  //EenrgyDeviation[i]
               w.write(dis.readInt(8)+" ");
            w.write("</"+ns+ENERGY_DEV+">\n");
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("HomogeneousTexture: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+HOMOGENEOUS_TEXTURE, null);
   }
}


