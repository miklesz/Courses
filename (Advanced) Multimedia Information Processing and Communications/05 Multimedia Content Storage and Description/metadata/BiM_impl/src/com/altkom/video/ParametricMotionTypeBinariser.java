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
   An object of this class can binarise an element of the type ParametricMotionType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class ParametricMotionTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String MOT_MODEL  = "motionModel";
   private static final String TRANSL     = "translational";
   private static final String ROTATION   = "rotationOrScaling";
   private static final String AFFINE     = "affine";
   private static final String PERSPEC    = "perspective";
   private static final String QUADRATIC  = "quadratic";
   private static final String COORD_REF  = "CoordRef";
   private static final String REF        = "ref";
   private static final String SPATIAL    = "spatialRef";
   private static final String COORD_DEF  = "CoordDef";
   private static final String ORIGIN_X   = "originX";
   private static final String ORIGIN_Y   = "originY";
   private static final String DURATION   = "MediaDuration";
   private static final String PARAMS     = "Parameters";
   /* binary values */
   private static final int MM_TRANSL    = 0; //values of motion model in binary format
   private static final int MM_ROTATION  = 1;
   private static final int MM_AFFINE    = 2;
   private static final int MM_PERSPEC   = 3;
   private static final int MM_QUADRATIC = 4;
   private static final int NOP_TRANSL    =  2; //number of parameters in model
   private static final int NOP_ROTATION  =  4;
   private static final int NOP_AFFINE    =  6;
   private static final int NOP_PERSPEC   =  8;
   private static final int NOP_QUADRATIC = 12;

   int model, noOfParams;
   boolean coordRefFlag, spatRefFlag;
   String ref;
   float origX, origY;
   float[] params;
   MediaIncrDurationType media;
   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("ParametricMotion: startElement: "+qualified);

      if (isAtRootLevel())
      {
         String attr = attrs.getValue(MOT_MODEL);
         if (attr.equals(TRANSL))
         {
            model = MM_TRANSL;
            noOfParams = NOP_TRANSL;
         }
         else if (attr.equals(ROTATION))
         {
            model = MM_ROTATION;
            noOfParams = NOP_ROTATION;
         }
         else if (attr.equals(AFFINE))
         {
            model = MM_AFFINE;
            noOfParams = NOP_AFFINE;
         }
         else if (attr.equals(PERSPEC))
         {
            model = MM_PERSPEC;
            noOfParams = NOP_PERSPEC;
         }
         else //if (attr.equals(QUADRATIC))
         {
            model = MM_QUADRATIC;
            noOfParams = NOP_QUADRATIC;
         }
         params = new float[noOfParams];
         currentElement = null;
      }
      else if (localName.equals(COORD_REF))
      {
         coordRefFlag = true;
         ref = attrs.getValue(REF);
         spatRefFlag = attrs.getValue(SPATIAL).equals("true");
      }
      else if (localName.equals(COORD_DEF))
      {
         coordRefFlag = false;
         origX = Float.parseFloat(attrs.getValue(ORIGIN_X).trim());
         origY = Float.parseFloat(attrs.getValue(ORIGIN_Y).trim());
      }
      else if (localName.equals(DURATION))
      {
         media = new MediaIncrDurationType();
         media.setTimeUnit(attrs.getValue(MediaIncrDurationType.TIME_UNIT));
         currentElement = DURATION;
      }
      else if (localName.equals(PARAMS))
         currentElement = PARAMS;
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == DURATION)
      {
         media.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == PARAMS)
      {
         StringTokenizer strtok = new StringTokenizer(new String(cdata, index, length));
         for (int i = 0; i < noOfParams; i++)
            params[i] = Float.parseFloat(strtok.nextToken());
         currentElement = null;
      }
   }

   public void endElement (String nameSpace, String localName, String qualified)
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

      try
      {
         currentChunkWriterUsed.writeByte(model, 3);           //motionModel
         currentChunkWriterUsed.writeBoolean(coordRefFlag);    //CoordFlag
         if (coordRefFlag)
         {
            currentChunkWriterUsed.writeUTF(ref);              //ref
            currentChunkWriterUsed.writeBoolean(spatRefFlag);  //spatialRef
         }
         else
         {
            currentChunkWriterUsed.writeFloat(origX);          //originX
            currentChunkWriterUsed.writeFloat(origY);          //originY
         }
         media.writeInto(currentChunkWriterUsed);              //MediaDuration
         for (int i = 0; i < noOfParams; i++)
            currentChunkWriterUsed.writeFloat(params[i]);      //Params[i]
      }
      catch (java.io.IOException x)
      {
         System.out.println("ParametricMotion: Can't binarise descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         int mod = dis.readByte(3);                                              //motionModel
         String mms = "";
         int nop = 0;
         switch (mod)
         {
            case MM_TRANSL:    mms = TRANSL;    nop = NOP_TRANSL;    break;
            case MM_ROTATION:  mms = ROTATION;  nop = NOP_ROTATION;  break;
            case MM_AFFINE:    mms = AFFINE;    nop = NOP_AFFINE;    break;
            case MM_PERSPEC:   mms = PERSPEC;   nop = NOP_PERSPEC;   break;
            case MM_QUADRATIC: mms = QUADRATIC; nop = NOP_QUADRATIC; break;
         }
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+MOT_MODEL+"=\""+mms+"\">\n");
         if (dis.readBoolean())                                                  //CoordFlag
         {
            String str = dis.readUTF();                                          //ref
            w.write("<"+ns+COORD_REF+" "+REF+"=\""+str+"\" "+SPATIAL+"=\""+
                        dis.readBoolean()+"\" />\n");                            //spatialRef
         }
         else
         {
            w.write("<"+ns+COORD_DEF+" "+ORIGIN_X+"=\""+dis.readFloat()+     //originX
               "\" "+ORIGIN_Y+"=\""+dis.readFloat()+"\" />\n");                  //originY
         }
         new MediaIncrDurationType().decode(dis, w, ns+DURATION, null);      //MediaDuration
         w.write("<"+ns+PARAMS+">");
         for (int i = 0; i < nop; i++)
            w.write(dis.readFloat()+" ");                                        //Params[i]
         w.write("</"+ns+PARAMS+">\n");
         w.write("</"+name+">\n");
      }
      catch (Exception e)
      {
         System.out.println("MotionActivity: Unable to decode bitstream");
         e.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+PARAMETRIC_MOTION, null);
   }
}