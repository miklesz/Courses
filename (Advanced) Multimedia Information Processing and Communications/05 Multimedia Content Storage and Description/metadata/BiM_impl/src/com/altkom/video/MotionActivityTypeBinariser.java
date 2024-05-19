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
   An object of this class can binarise an element of the type MotionActivityType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class MotionActivityTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String INTENSITY   = "Intensity";
   private static final String DOM_DIR     = "DominantDirection";
   private static final String SPA_DIS_PAR = "SpatialDistributionParams";
   private static final String NSR         = "numOfShortRuns";
   private static final String NMR         = "numOfMediumRuns";
   private static final String NLR         = "numOfLongRuns";
   private static final String SPA_LOC_PAR = "SpatialLocalizationParams";
   private static final String VECTOR      = "Vector";
   private static final String VECTOR4     = VECTOR+"4";
   private static final String VECTOR16    = VECTOR+"16";
   private static final String VECTOR64    = VECTOR+"64";
   private static final String VECTOR256   = VECTOR+"256";
   private static final String TEMP_PAR    = "TemporalParams";
   /* binary values */
   private static final int SLN_4   = 0; //binary values of number of spatial localization parameters
   private static final int SLN_16  = 1;
   private static final int SLN_64  = 2;
   private static final int SLN_256 = 3;
   /* constants */
   private static final int VL_4   =   4;
   private static final int VL_16  =  16;
   private static final int VL_64  =  64;
   private static final int VL_256 = 256;
   private static final int NO_OF_TEMP_PAR = 5;

   private boolean dirFlag, spatDistrFlag, spatLocFlag, tempFlag;
   private int intensity, domDir, nsr, nmr, nlr;
   private int[] spatLocParams, tempParams;
   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("startElement "+qualified);

      if (isAtRootLevel())
      {
         dirFlag = spatDistrFlag = spatLocFlag = tempFlag = false;
         spatLocParams = tempParams = null;
         currentElement = null;
      }
      else if (localName.equals(INTENSITY))
         currentElement = INTENSITY;
      else if (localName.equals(DOM_DIR))
      {
         dirFlag = true;
         currentElement = DOM_DIR;
      }
      else if (localName.equals(SPA_DIS_PAR))
      {
         spatDistrFlag = true;
         nsr = Integer.parseInt(attrs.getValue(NSR));
         nmr = Integer.parseInt(attrs.getValue(NMR));
         nlr = Integer.parseInt(attrs.getValue(NLR));
      }
      else if (localName.equals(VECTOR4))
      {
         spatLocFlag = true;
         spatLocParams = new int[VL_4];
         currentElement = SPA_LOC_PAR;
      }
      else if (localName.equals(VECTOR16))
      {
         spatLocFlag = true;
         spatLocParams = new int[VL_16];
         currentElement = SPA_LOC_PAR;
      }
      else if (localName.equals(VECTOR64))
      {
         spatLocFlag = true;
         spatLocParams = new int[VL_64];
         currentElement = SPA_LOC_PAR;
      }
      else if (localName.equals(VECTOR256))
      {
         spatLocFlag = true;
         spatLocParams = new int[VL_256];
         currentElement = SPA_LOC_PAR;
      }
      else if (localName.equals(TEMP_PAR))
      {
         tempFlag = true;
         tempParams = new int[NO_OF_TEMP_PAR];
         currentElement = TEMP_PAR;
      }
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      String str = new String(cdata, index, length);

      if (currentElement == INTENSITY)
      {
         intensity = Integer.parseInt(str.trim());
         currentElement = null;
      }
      else if (currentElement == DOM_DIR)
      {
         domDir = Integer.parseInt(str.trim());
         currentElement = null;
      }
      else if (currentElement == SPA_LOC_PAR)
      {
         StringTokenizer strtok = new StringTokenizer(str);
         for (int i = 0; i < spatLocParams.length; i++)
            spatLocParams[i] = Integer.parseInt(strtok.nextToken());
         currentElement = null;
      }
      else if (currentElement == TEMP_PAR)
      {
         StringTokenizer strtok = new StringTokenizer(str);
         for (int i = 0; i < NO_OF_TEMP_PAR; i++)
            tempParams[i] = Integer.parseInt(strtok.nextToken());
         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeByte(intensity, 3);                         //Intensity
      currentChunkWriterUsed.writeBoolean(dirFlag);                           //DirectionFlag
      currentChunkWriterUsed.writeBoolean(spatDistrFlag);                     //SpatialDistributionFlag
      currentChunkWriterUsed.writeBoolean(spatLocFlag);                       //SpatialLocalizedDistributionFlag
      currentChunkWriterUsed.writeBoolean(tempFlag);                          //TemporalDistributionFlag
      if (dirFlag)
         currentChunkWriterUsed.writeByte(domDir, 3);                         //DominantDirection
      if (spatDistrFlag)
      {
         currentChunkWriterUsed.writeByte(nsr, 6);                            //Nsr
         currentChunkWriterUsed.writeByte(nmr, 5);                            //Nmr
         currentChunkWriterUsed.writeByte(nlr, 5);                            //Nlr
      }
      if (spatLocFlag)
      {
         switch (spatLocParams.length)
         {
            case   VL_4: currentChunkWriterUsed.writeByte(SLN_4,   2); break; //SpaLocNumber
            case  VL_16: currentChunkWriterUsed.writeByte(SLN_16,  2); break; //SpaLocNumber
            case  VL_64: currentChunkWriterUsed.writeByte(SLN_64,  2); break; //SpaLocNumber
            case VL_256: currentChunkWriterUsed.writeByte(SLN_256, 2); break; //SpaLocNumber
         }
         for (int i = 0; i < spatLocParams.length; i++)
            currentChunkWriterUsed.writeByte(spatLocParams[i], 3);            //SpatialLocalizationParameter[i]
      }
      if (tempFlag)
         for (int i = 0; i < tempParams.length; i++)
            currentChunkWriterUsed.writeByte(tempParams[i], 6);               //TemporalParameter[i]
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         w.write("<"+ns+INTENSITY+">"+dis.readByte(3)+"</"+ns+INTENSITY+">\n");   //Intensity
         boolean df = dis.readBoolean();                                                  //DirectionFlag
         boolean sdf = dis.readBoolean();                                                 //SpatialDistributionFlag
         boolean slf = dis.readBoolean();                                                 //SpatialLocalizedDistributionFlag
         boolean tf = dis.readBoolean();                                                  //TemporalDistributiionFlag
         if (df)
            w.write("<"+ns+DOM_DIR+">"+dis.readByte(3)+"</"+ns+DOM_DIR+">\n");    //DominantDirection
         if (sdf)
            w.write("<"+ns+SPA_DIS_PAR+" "+NSR+"=\""+dis.readByte(6)+"\" "+           //Nsr
               NMR+"=\""+ dis.readByte(5)+"\" "+                                          //Nmr
               NLR+"=\""+dis.readByte(5)+"\"/>\n");                                       //Nlr
         if (slf)
         {
            int nop = dis.readByte(2);                                     //SpaLocNumber
            switch (nop)
            {
               case   SLN_4: nop =   VL_4; break;
               case  SLN_16: nop =  VL_16; break;
               case  SLN_64: nop =  VL_64; break;
               case SLN_256: nop = VL_256; break;
            }
            w.write("<"+ns+SPA_LOC_PAR+"><"+ns+VECTOR+nop+">");
            for (int i = 0; i < nop; i++)
               w.write(" "+dis.readByte(3));                               //SpatialLocalizationParameter[i]
            w.write("</"+ns+VECTOR+nop+"></"+ns+SPA_LOC_PAR+">\n");
         }//if (slf)
         if (tf)
         {
            w.write("<"+ns+TEMP_PAR+">");
            for (int i = 0; i < NO_OF_TEMP_PAR; i++)
               w.write(" "+dis.readByte(6));                               //TemporalParameter[i]
            w.write("</"+ns+TEMP_PAR+">\n");
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("MotionActivity: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+MOTION_ACTIVITY, null);
   }
}