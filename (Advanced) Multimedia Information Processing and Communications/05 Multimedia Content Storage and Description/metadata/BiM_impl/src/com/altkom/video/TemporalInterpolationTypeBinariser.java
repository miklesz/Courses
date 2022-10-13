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
import java.util.ArrayList;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type TemporalInterpolationType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class TemporalInterpolationTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String WHOLE_INTERVAL     = "WholeInterval";
   private static final String M_DUR              = "MediaDuration";
   private static final String M_INCR_DUR         = "MediaIncrDuration";
   private static final String KEY_TIME_PT        = "KeyTimePoint";
   private static final String M_TIME_PT          = "MediaTimePoint";
   private static final String M_REL_TIME_PT      = "MediaRelTimePoint";
   private static final String M_REL_INCR_TIME_PT = "MediaRelIncrTimePoint";
   private static final String I_FUNCTIONS        = "InterpolationFunctions";
   private static final String KEY_VALUE          = "KeyValue";
   private static final String TYPE               = "type";
   private static final String START_PT           = "startPoint";
   private static final String FIRST_ORDER        = "firstOrder";
   private static final String SECOND_ORDER       = "secondOrder";
   private static final String NOT_DETERMINED     = "notDetermined";
   private static final String PARAM              = "param";
   /* binary values */
   private static final int TPT_M_TIME_PT          = 0; //values of KeyTimePointDataType in binary format
   private static final int TPT_M_REL_TIME_PT      = 1;
   private static final int TPT_M_REL_INCR_TIME_PT = 2;
   private static final int FT_START_PT       = 0; //values of function type in binary format
   private static final int FT_FIRST_ORDER    = 1;
   private static final int FT_SECOND_ORDER   = 2;
   private static final int FT_NOT_DETERMINED = 3;
   /* constants and defaults */
   private static final int   FT_DEFAULT    = FT_FIRST_ORDER;
   private static final float PARAM_DEFAULT = 0.0f;
   private static final int   MAX_DIMENSION = 15;
   private static final int   DEF_REPR      = 32;

   private boolean constIntervalFlag, durType;
   private int timePtType;
   BasicMedia mediaDuration;
   ArrayList mediaTimePoints;
   BasicMedia currTP;
   private int dimension;
   ArrayList[] functions;
   boolean[] defFunc; //determines if in corresponding functions(key values) use only defaultFunction
   KeyValue currentKV;
   //quantization
   int xRepr, yRepr;
   float[] xQuantTable, yQuantTable;

   private String currentElement;

   /* constants for quantization */
   private static final int PIXEL   = 0;
   private static final int HEIGHT  = 1;
   private static final int WIDTH   = 2;
   private static final int WID_HEI = 3;

   public void enableQuantization(int xrepr, int yrepr, int units, int xSize, int ySize)
   {
      xRepr = xrepr;
      yRepr = yrepr;
      float maxx, maxy;
      switch (units)
      {
         case HEIGHT:  maxx = xSize/((float)ySize); maxy = 1F;                   break;
         case WIDTH:   maxx = 1F;                   maxy = ySize/((float)xSize); break;
         case WID_HEI: maxx = 1F;                   maxy = 1F;                   break;
         //defualt is PIXEL
         default:      
		    maxx = (xSize <= 0) ? 1 << xRepr : xSize; //when xSize is not specified use max number reprezented on xRepr bits
		    maxy = (ySize <= 0) ? 1 << yRepr : ySize; //when ySize is not specified use max number reprezented on yRepr bits
			break;
      }
      xQuantTable = getQuantizationTable(0, maxx, xRepr);
      yQuantTable = getQuantizationTable(0, maxy, yRepr);
   }

   public void disableQuantization()
   {
      xQuantTable = yQuantTable = null;
   }

   public int getXRepr()
   {
      return xRepr;
   }

   public int getYRepr()
   {
      return yRepr;
   }

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("TemporalInterpolation: startElement: "+qualified);

      if (isAtRootLevel())
      {
         dimension = 0;
         functions = new ArrayList[MAX_DIMENSION];
         defFunc = new boolean[MAX_DIMENSION];
         mediaTimePoints = null;
         currentElement = null;
      }
      else if (localName.equals(WHOLE_INTERVAL))
         constIntervalFlag = true;
      else if (localName.equals(M_DUR))
      {
         durType = false;
         mediaDuration = new MediaDurationType();
         currentElement = M_DUR;
      }
      else if (localName.equals(M_INCR_DUR))
      {
         durType = true;
         MediaIncrDurationType md = new MediaIncrDurationType();
         md.setTimeUnit(attrs.getValue(MediaIncrDurationType.TIME_UNIT));
         mediaDuration = md;
         currentElement = M_INCR_DUR;
      }
      else if (localName.equals(KEY_TIME_PT))
      {
         constIntervalFlag = false;
         mediaTimePoints = new ArrayList();
      }
      else if (localName.equals(M_TIME_PT))
      {
         timePtType = TPT_M_TIME_PT;
         mediaTimePoints.add(currTP = new MediaTimePointType());
         currentElement = M_TIME_PT;
      }
      else if (localName.equals(M_REL_TIME_PT))
      {
         timePtType = TPT_M_REL_TIME_PT;
         MediaRelTimePointType mtp = new MediaRelTimePointType();
         mtp.setTimeBase(attrs.getValue(MediaRelTimePointType.TIME_BASE));
         mediaTimePoints.add(currTP = mtp);
         currentElement = M_REL_TIME_PT;
      }
      else if (localName.equals(M_REL_INCR_TIME_PT))
      {
         timePtType = TPT_M_REL_INCR_TIME_PT;
         MediaRelIncrTimePointType mtp = new MediaRelIncrTimePointType();
         mtp.setTimeUnit(attrs.getValue(MediaRelIncrTimePointType.TIME_UNIT));
         mtp.setTimeBase(attrs.getValue(MediaRelIncrTimePointType.TIME_BASE));
         mediaTimePoints.add(currTP = mtp);
         currentElement = M_REL_INCR_TIME_PT;
      }
      else if (localName.equals(I_FUNCTIONS))
      {
         dimension++;
         functions[dimension-1] = new ArrayList();
         defFunc[dimension-1] = true;
      }
      else if (localName.equals(KEY_VALUE))
      {
         currentKV = new KeyValue();
         String attrval = attrs.getValue(TYPE);
         if (attrval == null)
            currentKV.type = FT_DEFAULT;
         else if (attrval.equals(START_PT))
            currentKV.type = FT_START_PT;
         else if (attrval.equals(FIRST_ORDER))
            currentKV.type = FT_FIRST_ORDER;
         else if (attrval.equals(SECOND_ORDER))
            currentKV.type = FT_SECOND_ORDER;
         else //if (attrval.equals(NOT_DETERMINED))
            currentKV.type = FT_NOT_DETERMINED;
         attrval = attrs.getValue(PARAM);
         currentKV.param = (attrval == null) ? PARAM_DEFAULT  : Float.parseFloat(attrval.trim());

         functions[dimension-1].add(currentKV);
         if (currentKV.type > FT_FIRST_ORDER)
            defFunc[dimension-1] = false; //not default function used
         currentElement = KEY_VALUE;
      }
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == M_DUR)
      {
         mediaDuration.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == M_INCR_DUR)
      {
         mediaDuration.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == M_TIME_PT)
      {
         currTP.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == M_REL_TIME_PT)
      {
         currTP.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == M_REL_INCR_TIME_PT)
      {
         currTP.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == KEY_VALUE)
      {
         currentKV.value = Float.parseFloat(new String(cdata, index, length).trim());
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
      if (currentChunkWriterUsed == null)
         return;

      try
      {
         int nokp = functions[0].size();
         currentChunkWriterUsed.writeInt(nokp, 16);                                       //numOfKeyPoints
         currentChunkWriterUsed.writeBoolean(constIntervalFlag);                          //ConstantTimeInterval
         boolean quantFlag = (xQuantTable != null);
         currentChunkWriterUsed.writeBoolean(quantFlag);                                  //QuantizationFlag
         if (constIntervalFlag)
         {
            currentChunkWriterUsed.writeBoolean(durType);                                 //WholeIntervalDataType
            mediaDuration.writeInto(currentChunkWriterUsed);                              //MediaDuration, MediaIncrDuration
         }//if constIntervalFlag
         else
         {
            currentChunkWriterUsed.writeByte(timePtType, 2);                              //KeyTimePointDataType
            for (int i = 0; i < nokp; i++)
               ((BasicMedia)mediaTimePoints.get(i)).writeInto(currentChunkWriterUsed);    //MediaTimePoint, MediaRelTimePoint, MediaRelIncrTimePoint
         }//else
         currentChunkWriterUsed.writeByte(dimension, 4);                                  //Dimension
         float[] xqt = null, yqt = null;
         for (int d = 0; d < dimension; d++)
         {
            currentChunkWriterUsed.writeBoolean(defFunc[d]);                              //DefaultFunction
            for (int i = 0; i < nokp; i++)
            {
               KeyValue kv = (KeyValue)functions[d].get(i);
               if (!defFunc[d])
               {
                  currentChunkWriterUsed.writeByte(kv.type, 2);                           //type
                  if (kv.type == FT_SECOND_ORDER)
                     currentChunkWriterUsed.writeFloat(kv.param);                         //param
               }
               if (!quantFlag)
                  currentChunkWriterUsed.writeFloat(kv.value);                            //KeyValue
               else
               {
                  //save quantized value
                  if (d == 0)                            //QuantizedKeyValue for X
                     currentChunkWriterUsed.writeInt(getQuantizedIdx(xQuantTable, kv.value), xRepr);
                  else if (d == 1)                       //QuantizedKeyValue for Y
                     currentChunkWriterUsed.writeInt(getQuantizedIdx(yQuantTable, kv.value), yRepr);
               }
            }//if defFunc
         }//for dimension
      }
      catch (java.io.IOException x)
      {
         System.out.println("TemporalInterpolation: Can't binarize descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         int nokp = dis.readInt(16);                                                         //NumOfKeyPoints
         boolean constIntFl = dis.readBoolean();                                             //ConstantTimeInterval
         boolean quantFlag = dis.readBoolean();                                              //QuantizationFlag
         if (constIntFl)
         {
            w.write("<"+ns+WHOLE_INTERVAL+">\n");
            if (!dis.readBoolean())                                                          //WholeIntervalDataType
               new MediaDurationType().decode(dis, w, ns+M_DUR, null);                   //MediaDuration
            else
               new MediaIncrDurationType().decode(dis, w, ns+M_INCR_DUR, null);          //MediaIncrDuration
            w.write("</"+ns+WHOLE_INTERVAL+">\n");
         }
         else
         {
            w.write("<"+ns+KEY_TIME_PT+">\n");
            int tpt = dis.readByte(2);                                                       //KeyTimePointDataType
            BasicMedia mtp;
            if (tpt == TPT_M_TIME_PT)
            {
               mtp = new MediaTimePointType();
               for (int i = 0; i < nokp; i++)
                  mtp.decode(dis, w, ns+M_TIME_PT, null);                                //MediaTimePoint
            }
            else if (tpt == TPT_M_REL_TIME_PT)
            {
               mtp = new MediaRelTimePointType();
               for (int i = 0; i < nokp; i++)
                  mtp.decode(dis, w, ns+M_REL_TIME_PT, null);                            //MediaRelTimePoint
            }
            else //if (tpt == TPT_M_REL_INCR_TIME_PT)
            {
               mtp = new MediaRelIncrTimePointType();
               for (int i = 0; i < nokp; i++)
                  mtp.decode(dis, w, ns+M_REL_INCR_TIME_PT, null);                       //MediaRelIncrTimePoint
            }
            w.write("</"+ns+KEY_TIME_PT+">\n");
         }
         int dim = dis.readByte(4);                                                          //Dimension
         for (int d = 0; d < dim; d++)
         {
            w.write("<"+ns+I_FUNCTIONS+">\n");
            boolean df = dis.readBoolean();                                                  //DefaultFunction
            for (int i = 0; i < nokp; i++)
            {
               int type = FT_DEFAULT;
               float param = PARAM_DEFAULT;
               if (!df)
               {
                  type = dis.readByte(2);                                                //type
                  param = PARAM_DEFAULT;
                  if (type == FT_SECOND_ORDER)
                     param = dis.readFloat();                                                //param
               }
               float val;
               if (!quantFlag)
                  val = dis.readFloat();                                               //KeyValue
               else
               {
                  //read quantized key value
                  if (d == 0)                            //QuantizedKeyValue for X
                  {
                     int idx = dis.readInt(xRepr);
                     val = getDequantizedValue(xQuantTable, idx);
                  }
                  else //if (d == 1)                       //QuantizedKeyValue for Y
                  {
                     int idx = dis.readInt(yRepr);
                     val = getDequantizedValue(yQuantTable, idx);
                  }
               }
               w.write("<"+ns+KEY_VALUE);
               if (type != FT_DEFAULT)
               {
                  String typestr = "";
                  switch (type)
                  {
                     case 0: typestr = START_PT;       break;
                     case 1: typestr = FIRST_ORDER;    break;
                     case 2: typestr = SECOND_ORDER;   break;
                     case 3: typestr = NOT_DETERMINED; break;
                  }
                  w.write(" "+TYPE+"=\""+typestr+"\"");
               }
               if (param != PARAM_DEFAULT)
                  w.write(" "+PARAM+"=\""+param+"\"");
               w.write(">"+val+"</"+ns+KEY_VALUE+">\n");
            }
            w.write("</"+ns+I_FUNCTIONS+">\n");
         }//for functions
         w.write("</"+name+">\n");
      }
      catch (Exception e)
      {
         System.out.println("TemporalInterpolation: Unable to decode bitstream");
         e.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+TEMPORAL_INTERPOLATION, null);
   }

   /* utility methods for quantization */
   /**
    * Generates quantization table. Generated table length equals to 'number of ranges' + 1.
    * ('number of ranges' = 1 << noOfBits).
    * quantTable[0] = min, quantTable['number of ranges' = quantTables.length-1] = max,
    * quantTable[1] .. quantTable['number of ranges'-1 = quantTable.length-2] - tresholds/
    */
   float[] getQuantizationTable(float min, float max, int noOfBits)
   {
      int noOfRanges = (1 << noOfBits);
      float[] quantTable = new float[noOfRanges+1];
      float step = (max-min)/noOfRanges;
      quantTable[0] = min;
      quantTable[quantTable.length-1] = max;
      for (int i = 1; i < quantTable.length-1; i++)
         quantTable[i] = quantTable[i-1]+step;

      if (DEBUG)
      {
         System.out.println("TemporalInterpolation: getQuantizationTable: min: "+min+", max: "+max);
         System.out.println("[");
         for (int i = 0; i < quantTable.length; i++)
            System.out.print(quantTable[i]+"; ");
         System.out.println("]");
      }

      return quantTable;
   }

   int getQuantizedIdx(float[] quantTable, float val)
   {
      for (int i = 1; i < quantTable.length; i++)
         if (val < quantTable[i])
            return i-1;

      return quantTable.length-1; //val is greater thane quantTable[quantTable.length-1]
   }

   float getDequantizedValue(float[] quantTable, int idx)
   {
      return quantTable[idx];
   }
}

/**
 * This class is used for holding parameters (type, param, value) of KeyValue or RestrictedKeyValue
 * elements.
 */
class KeyValue
{
   protected int type;
   protected float value, param;

   public String toString()
   {
      return "KeyValue: type: "+type+", param: "+param+", value: "+value; //for DEBUG purposes
   }
}