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
import java.util.StringTokenizer;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type Spatial2DCoordinateSystemType as of
   the ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class Spatial2DCoordinateSystemTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String ID           = "id"; //inherited from HeaderType
   private static final String X_REPR       = "xRepr";
   private static final String Y_REPR       = "yRepr";
   private static final String X_SRC_SIZE   = "xSrcSize";
   private static final String Y_SRC_SIZE   = "ySrcSize";
   private static final String UNIT         = "Unit";
   private static final String U_PIXEL      = "pixel";
   private static final String U_METER      = "meter";
   private static final String U_HEIGHT     = "pictureHeight";
   private static final String U_WIDTH      = "pictureWidth";
   private static final String U_WID_HEI    = "pictureWidthAndHeight";
   private static final String LOC_COORD    = "LocalCoordinateSystem";
   private static final String NAME         = "name";
   private static final String DATA_SET     = "dataSet";
   private static final String PIXEL        = "Pixel";
   private static final String COORD_PT     = "CoordPoint";
   private static final String CUR_PIXEL    = "CurrPixel";
   private static final String SRC_PIXEL    = "SrcPixel";
   private static final String MAP_FUNC     = "MappingFunct";

   private static final String INTEG_COORD  = "IntegratedCoordinateSystem";
   private static final String MODEL_TYPE   = "modelType";
   private static final String MT_TRANSL    = "translational";
   private static final String MT_ROTATION  = "rotationAndScaling";
   private static final String MT_AFFINE    = "affine";
   private static final String MT_PERSPECT  = "perspective";
   private static final String MT_QUADRATIC = "quadratic";
   private static final String X_ORIGIN     = "xOrigin";
   private static final String Y_ORIGIN     = "yOrigin";
   private static final String TIME_INCR    = "TimeIncr";
   private static final String MOT_PARAMS   = "MotionParams";
   /* binary values */
   private static final int BU_PIXEL   = 0;  //values of unit in binary format
   private static final int BU_METER   = 1;
   private static final int BU_HEIGHT  = 4;
   private static final int BU_WIDTH   = 5;
   private static final int BU_WID_HEI = 6;
   private static final int BMT_TRANSL    = 0; //values of model type in binary format
   private static final int BMT_ROTATION  = 1;
   private static final int BMT_AFFINE    = 2;
   private static final int BMT_PERSPECT  = 3;
   private static final int BMT_QUADRATIC = 4;
   private static final int NOP_TRANSL    =  2; //number of parameters in model
   private static final int NOP_ROTATION  =  4;
   private static final int NOP_AFFINE    =  6;
   private static final int NOP_PERSPECT  =  8;
   private static final int NOP_QUADRATIC = 12;
   /* constants and defaults */
   private static final int BU_DEFAULT = BU_PIXEL; //default value for unit
   private static final int MAX_PIXELS = 3;
   private static final int MAX_MAP_FUNC = 2;

   private String id;
   private int xRepr, yRepr, xSrcSize, ySrcSize;
   private boolean xSrcFlag, ySrcFlag, unitFlag, locCoordFlag, integCoordFlag;
   int unit;
   String name, dataSet;
   int numOfPts;
   private int[] pixelX, pixelY, srcPixelX, srcPixelY; //pixelXY are used for Pixel or CurrPixel
   private float[] coordPtX, coordPtY;
   boolean coordFlag;
   int numOfMapFunc;
   String[] mapFunc;
   int modelType, numOfPar;
   float xOrigin, yOrigin;
   ArrayList media;
   MediaIncrDurationType currDur;
   ArrayList motParams;
   float[] currentMotParams;
   int curParIdx;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("Spatial2DCoordinateSystem: startElement: "+qualified);

      if (isAtRootLevel())
      {
         id = attrs.getValue(ID);
         xRepr = Integer.parseInt(attrs.getValue(X_REPR));
         yRepr = Integer.parseInt(attrs.getValue(Y_REPR));
         String str = attrs.getValue(X_SRC_SIZE);
         xSrcFlag = (str != null);
         xSrcSize = xSrcFlag ? Integer.parseInt(str) : 0;
         str = attrs.getValue(Y_SRC_SIZE);
         ySrcFlag = (str != null);
         ySrcSize = ySrcFlag ? Integer.parseInt(str) : 0;

         unitFlag = locCoordFlag = integCoordFlag = false;
         media = null;
         motParams = null;
         currentElement = null;
      }
      else if (localName.equals(UNIT))
      {
         unitFlag = true;
         currentElement = UNIT;
      }
      else if (localName.equals(LOC_COORD))
      {
         locCoordFlag = true;
         name = attrs.getValue(NAME);
         dataSet = attrs.getValue(DATA_SET);
         numOfPts = 0;
         numOfMapFunc = 0;
         pixelX = new int[MAX_PIXELS];
         pixelY = new int[MAX_PIXELS];
         coordPtX = null;
         coordPtY = null;
         srcPixelX = null;
         srcPixelY = null;
      }
      else if (localName.equals(PIXEL))
      {
         numOfPts++;
         currentElement = PIXEL;
      }
      else if (localName.equals(COORD_PT))
      {
         if (coordPtX == null)
         {
            coordPtX = new float[MAX_PIXELS];
            coordPtY = new float[MAX_PIXELS];
         }
         coordFlag = true; 
         currentElement = COORD_PT;
      }
      else if (localName.equals(CUR_PIXEL))
      {
         numOfPts++;
         currentElement = CUR_PIXEL;
      }
      else if (localName.equals(SRC_PIXEL))
      {
         if (srcPixelX == null)
         {
            srcPixelX = new int[MAX_PIXELS];
            srcPixelY = new int[MAX_PIXELS];
         }
         coordFlag = false; 
         currentElement = SRC_PIXEL; 
      }
      else if (localName.equals(MAP_FUNC))
      {
         numOfMapFunc++;
         if (mapFunc == null)
            mapFunc = new String[MAX_MAP_FUNC];
         currentElement = MAP_FUNC;
      }
      else if (localName.equals(INTEG_COORD))
      {
         integCoordFlag = true;
         String str = attrs.getValue(MODEL_TYPE);
         if (str.equals(MT_TRANSL))
         {
            modelType = BMT_TRANSL;
            numOfPar = NOP_TRANSL;
         }
         else if (str.equals(MT_ROTATION))
         {
            modelType = BMT_ROTATION;
            numOfPar = NOP_ROTATION;
         }
         else if (str.equals(MT_AFFINE))
         {
            modelType = BMT_AFFINE;
            numOfPar = NOP_AFFINE;
         }
         else if (str.equals(MT_PERSPECT))
         {
            modelType = BMT_PERSPECT;
            numOfPar = NOP_PERSPECT;
         }
         else //if (str.equals(MT_QUADRATIC))
         {
            modelType = BMT_QUADRATIC;
            numOfPar = NOP_QUADRATIC;
         }
         xOrigin = Float.parseFloat(attrs.getValue(X_ORIGIN));
         yOrigin = Float.parseFloat(attrs.getValue(Y_ORIGIN));
         media = new ArrayList();
         motParams = new ArrayList();
      }
      else if (localName.equals(TIME_INCR))
      {
         currentMotParams = new float[numOfPar];
         curParIdx = 0;                         //currentMotParams and curParIdx initialized here, because first occurance of MotionParameters doesn't differ from second etc.
         currDur = new MediaIncrDurationType();
         currDur.setTimeUnit(attrs.getValue(MediaIncrDurationType.TIME_UNIT));
         media.add(currDur);
         currentElement = TIME_INCR;
      }
      else if (localName.equals(MOT_PARAMS))
         currentElement = MOT_PARAMS;
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == UNIT)
      {
         String str = new String(cdata, index, length);
         if (str.equals(U_PIXEL))
            unit = BU_PIXEL;
         else if (str.equals(U_METER))
            unit = BU_METER;
         else if (str.equals(U_HEIGHT))
            unit = BU_HEIGHT;
         else if (str.equals(U_WIDTH))
            unit = BU_WIDTH;
         else //if (str.equals(U_WID_HEI))
            unit = BU_WID_HEI;
         currentElement = null;
      }
      else if ((currentElement == PIXEL) || (currentElement == CUR_PIXEL))
      {
         StringTokenizer strtok = new StringTokenizer(new String(cdata, index, length));
         pixelX[numOfPts-1] = Integer.parseInt(strtok.nextToken());
         pixelY[numOfPts-1] = Integer.parseInt(strtok.nextToken());
         currentElement = null;
      }
      else if (currentElement == COORD_PT)
      {
         StringTokenizer strtok = new StringTokenizer(new String(cdata, index, length));
         coordPtX[numOfPts-1] = Float.parseFloat(strtok.nextToken());
         coordPtY[numOfPts-1] = Float.parseFloat(strtok.nextToken());
         currentElement = null;
      }
      else if (currentElement == SRC_PIXEL)
      {
         StringTokenizer strtok = new StringTokenizer(new String(cdata, index, length));
         srcPixelX[numOfPts-1] = Integer.parseInt(strtok.nextToken());
         srcPixelY[numOfPts-1] = Integer.parseInt(strtok.nextToken());
         currentElement = null;
      }
      else if (currentElement == MAP_FUNC)
      {
         mapFunc[numOfMapFunc-1] = new String(cdata, index, length);
         currentElement = null;
      }
      else if (currentElement == TIME_INCR)
      {
         currDur.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if (currentElement == MOT_PARAMS)
      {
         currentMotParams[curParIdx++] = Float.parseFloat(new String(cdata, index, length));
         if (curParIdx == numOfPar)
            motParams.add(currentMotParams); //if it is necessary, currentMotParams and curParIdx will be set in startElement: TIME_INCR
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

      try
      {
         currentChunkWriterUsed.writeUTF(id);                                             //id
         currentChunkWriterUsed.writeInt(xRepr, 8);                                       //xRepr
         currentChunkWriterUsed.writeInt(yRepr, 8);                                       //yRepr
         currentChunkWriterUsed.writeBoolean(xSrcFlag);                                   //XSrcSizeDefined
         if (xSrcFlag)
            currentChunkWriterUsed.writeInfiniteLong(xSrcSize, 4);                        //xSrcSize
         currentChunkWriterUsed.writeBoolean(ySrcFlag);                                   //YSrcSizeDefined
         if (ySrcFlag)
            currentChunkWriterUsed.writeInfiniteLong(ySrcSize, 4);                        //ySrcSize
         currentChunkWriterUsed.writeBoolean(unitFlag);                                   //unitDefined
         currentChunkWriterUsed.writeBoolean(locCoordFlag);                               //LocalCoordinatesDefined
         currentChunkWriterUsed.writeBoolean(integCoordFlag);                             //IntegratedCoordinatesDefined
         if (unitFlag)
            currentChunkWriterUsed.writeByte(unit, 3);                                    //Unit
         if (locCoordFlag)
         {
            currentChunkWriterUsed.writeInfiniteLong(name.length(), 4);                   //NameLength
            currentChunkWriterUsed.writeBytes(name);                                      //name
            currentChunkWriterUsed.writeBoolean(dataSet != null);                         //DataSetDefined
            if (dataSet != null)
            {
               currentChunkWriterUsed.writeInfiniteLong(dataSet.length(), 4);             //DataSetLength
               currentChunkWriterUsed.writeBytes(dataSet);                                //dataSet
            }
            currentChunkWriterUsed.writeBoolean(coordFlag);                               //Coord
            currentChunkWriterUsed.writeByte(numOfPts, 2);                                //NumOfPoints
            for (int i = 0; i < numOfPts; i++)
            {
               currentChunkWriterUsed.writeInt(pixelX[i], xRepr);                         //PixelX
               currentChunkWriterUsed.writeInt(pixelY[i], yRepr);                         //PixelY
               if (!coordFlag)
               {
                  currentChunkWriterUsed.writeInt(srcPixelX[i], 16);                      //PixelX --> probably should be SrcPixelX
                  currentChunkWriterUsed.writeInt(srcPixelY[i], 16);                      //PixelY --> probably should be SrcPixelY
               }//if
               else
               {
                  currentChunkWriterUsed.writeFloat(coordPtX[i]);                         //CoordPointX
                  currentChunkWriterUsed.writeFloat(coordPtY[i]);                         //CoordPointY
               }
            }//for
            currentChunkWriterUsed.writeByte(numOfMapFunc, 2);                            //NumOfMappingFuncts
            for (int i = 0; i < numOfMapFunc; i++)
            {
               currentChunkWriterUsed.writeInfiniteLong(mapFunc[i].length(), 4);          //MappingFunctLength
               currentChunkWriterUsed.writeBytes(mapFunc[i]);                             //MappingFunct
            }
         }//if locCoordFlag
         if (integCoordFlag)
         {
            currentChunkWriterUsed.writeByte(modelType, 3);                               //modelType
            currentChunkWriterUsed.writeFloat(xOrigin);                                   //xOrigin
            currentChunkWriterUsed.writeFloat(yOrigin);                                   //yOrigin
            currentChunkWriterUsed.writeInt(motParams.size(), 16);                        //NumOfMotionParamSets
            for (int i = 0; i < motParams.size(); i++)
            {
               ((MediaIncrDurationType)media.get(i)).writeInto(currentChunkWriterUsed);   //TimeIncr
               float[] params = (float[])motParams.get(i);
               for (int p = 0; p < numOfPar; p++)
                  currentChunkWriterUsed.writeFloat(params[p]);                           //MotionParams
            }
         }//if integCoordFlag
 
         //add coded Spatial2D
         addCodedCoordinate(id, xRepr,yRepr, xSrcSize, ySrcSize, unit);
      }
      catch (java.io.IOException x)
      {
         System.out.println("Spatial2DCoordinateSystem: Can't finalize descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      if (DEBUG) System.out.println("Spatial2DCoordinateSystem: decode: start");

      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         String id = dis.readUTF();                                                    //id
         int xr = dis.readInt(8);                                                      //xRepr
         int yr = dis.readInt(8);                                                      //yRepr
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+ID+"=\""+id+"\" "+X_REPR+"=\""+xr+
                     "\" "+Y_REPR+"=\""+yr+"\"");
         int xsrcs = 0, ysrcs = 0, u = 0;
         if (dis.readBoolean())                                                        //XSrcSizeDefined
         {
            xsrcs = (int)dis.readInfiniteLong(4);                                           //xSrcSize
            w.write(" "+X_SRC_SIZE+"=\""+xsrcs+"\"");
         }
         if (dis.readBoolean())                                                        //YSrcSizeDefined
         {
            ysrcs = (int)dis.readInfiniteLong(4);                                           //ySrcSize
            w.write(" "+Y_SRC_SIZE+"=\""+ysrcs+"\"");
         }
         w.write(">\n");
         boolean udef = dis.readBoolean();                                             //UnitDefined
         boolean lcdef = dis.readBoolean();                                            //LocalCoordinatesDefined
         boolean icdef = dis.readBoolean();                                            //IntegratedCoordinatesDefined
         if (udef)
         {
            u = dis.readByte(3);                                                   //Unit
            String us = "";
            switch (u)
            {
               case BU_PIXEL:   us = U_PIXEL;   break;
               case BU_METER:   us = U_METER;   break;
               case BU_HEIGHT:  us = U_HEIGHT;  break;
               case BU_WIDTH:   us = U_WIDTH;   break;
               case BU_WID_HEI: us = U_WID_HEI; break;
            }
            w.write("<"+ns+UNIT+">"+us+"</"+ns+UNIT+">\n");
         }//if udef
         if (lcdef)
         {
            int len = (int)dis.readInfiniteLong(4);                                    //NameLength
            byte[] bn = new byte[len];
            dis.read(bn);                                                              //name
            w.write("<"+ns+LOC_COORD+" "+NAME+"=\""+new String(bn)+"\"");
            if (dis.readBoolean())                                                     //DataSetDefined
            {
               len = (int)dis.readInfiniteLong(4);                                     //DataSetLength
               bn = new byte[len];
               dis.read(bn);                                                           //dataSet
               w.write(" "+DATA_SET+"=\""+new String(bn)+"\"");
            }
            w.write(">\n");
            boolean cf = dis.readBoolean();                                            //Coord
            int nopts = dis.readByte(2);                                               //NumOfPoints
            for (int i = 0; i < nopts; i++)
            {
               w.write("<"+ns+(cf ? PIXEL : CUR_PIXEL)+">"+dis.readInt(xr)+" "+                       //PixelX
                  dis.readInt(yr)+"</"+ns+(cf ? PIXEL : CUR_PIXEL)+">\n");                            //PixelY
               if (!cf)                                                 //Coord
                  w.write("<"+ns+SRC_PIXEL+">"+((dis.readInt(16)<<16)>>16)+" "+    //PixelX --> probably should be SrcPixelX; it is simsbf (signed), so it is shifted left, then right in order to multiply MSB
                     ((dis.readInt(16)<<16)>>16)+"</"+ns+SRC_PIXEL+">\n");         //PixelY --> probably should be SrcPixelY; it is simsbf (signed), so it is shifted left, then right in order to multiply MSB
               else
                  w.write("<"+ns+COORD_PT+">"+dis.readFloat()+" "+                 //CoordPointX
                     dis.readFloat()+"</"+ns+COORD_PT+">\n");                      //CoordPointY
            }
            int nomf = dis.readByte(2);                                                //NumOfMappingFuncts
            for (int i = 0; i < nomf; i++)
            {
               len = (int)dis.readInfiniteLong(4);                                     //MappingFunctLength
               bn = new byte[len];
               dis.read(bn);                                                           //MappingFunct
               w.write("<"+ns+MAP_FUNC+">"+new String(bn)+"</"+ns+MAP_FUNC+">\n");
            }
            w.write("</"+ns+LOC_COORD+">\n");
         }//if lcdef
         if (icdef)
         {
            int mt = dis.readByte(3);                                                  //modelType
            String mts = "";
            int nop = 0;
            switch (mt)
            {
               case BMT_TRANSL:    mts = MT_TRANSL;    nop = NOP_TRANSL;    break;
               case BMT_ROTATION:  mts = MT_ROTATION;  nop = NOP_ROTATION;  break;
               case BMT_AFFINE:    mts = MT_AFFINE;    nop = NOP_AFFINE;    break;
               case BMT_PERSPECT:  mts = MT_PERSPECT;  nop = NOP_PERSPECT;  break;
               case BMT_QUADRATIC: mts = MT_QUADRATIC; nop = NOP_QUADRATIC; break;
            }
            w.write("<"+ns+INTEG_COORD+" "+MODEL_TYPE+"=\""+mts+"\" "+X_ORIGIN+"=\""+
               dis.readFloat()+"\" "+Y_ORIGIN+"=\""+                                   //xOrigin
               dis.readFloat()+"\">\n");                                               //yOrigin
            int nomps = dis.readInt(16);                                               //NumOfMotionParamSets
            MediaIncrDurationType mid = new MediaIncrDurationType();
            for (int i = 0; i < nomps; i++)
            {
               mid.decode(dis, w, TIME_INCR, null);
               for (int p = 0; p < nop; p++)
                  w.write("<"+ns+MOT_PARAMS+">"+dis.readFloat()+                   //MotionParams
                              "</"+ns+MOT_PARAMS+">\n");
            }
            w.write("</"+ns+INTEG_COORD+">\n");
         }//if icdef
         w.write("</"+name+">\n");

         //add decoded srcSize
         addDecodedCoordinate(id, xr, yr, (int)xsrcs, (int)ysrcs, u);
      }
      catch (Exception x)
      {
         System.out.println("Spatial2DCoordinateSystem: Unable to decode bitstream");
         x.printStackTrace();
      }
      if (DEBUG) System.out.println("Spatial2DCoordinateSystem: decode: end");

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+SPATIAL_2D_COORDINATE_SYSTEM, null);
   }

   /**
    * This arrays and methods are for other descriptors, which needs data saved in this descriptor.
    * Actually this is xSrcSize and ySrcSize, tha are needed by RegionLocator.
    */
   private static ArrayList codedCoordinates;
   private static void addCodedCoordinate(String name, int xrepr, int yrepr, int xsrcSize, int ysrcSize, int units)
   {
      if (codedCoordinates == null)
         codedCoordinates = new ArrayList();

      codedCoordinates.add(new SpatialCoordinates(name, xrepr, yrepr, xsrcSize, ysrcSize, units));
   }
   public static int[] getCodedSrcSize(String name) throws Exception
   {
      if (codedCoordinates != null)
         for (int i = 0; i < codedCoordinates.size(); i++)
         {
            SpatialCoordinates sc = (SpatialCoordinates)codedCoordinates.get(i);
            if (name.equals(sc.name))
               return new int[] { sc.xsrcSize, sc.ysrcSize };
         }

      throw new Exception("Coded Spatial2DCoordinateSystem with name "+name+" not found!");
   }

   private static ArrayList decodedCoordinates;
   private static void addDecodedCoordinate(String name, int xrepr, int yrepr, int xsrcSize, int ysrcSize, int units)
   {
      if (decodedCoordinates == null)
         decodedCoordinates = new ArrayList();

      decodedCoordinates.add(new SpatialCoordinates(name, xrepr, yrepr, xsrcSize, ysrcSize, units));
   }
   public static int[] getDecodedSrcSize(String name) throws Exception
   {
      if (decodedCoordinates != null)
         for (int i = 0; i < decodedCoordinates.size(); i++)
         {
            SpatialCoordinates sc = (SpatialCoordinates)decodedCoordinates.get(i);
            if (name.equals(sc.name))
               return new int[] { sc.xsrcSize, sc.ysrcSize };
         }

      throw new Exception("Decoded Spatial2DCoordinateSystem with name "+name+" not found!");
   }
}
/**
 * This class is used by codedCoordinates and decodedCoordinates.
 */
class SpatialCoordinates
{
   String name;
   int xrepr, yrepr, xsrcSize, ysrcSize, units;

   SpatialCoordinates(String name, int xr, int yr, int xsize, int ysize, int un)
   {
      this.name = name;
      xrepr = xr;
      yrepr = yr;
      xsrcSize = xsize;
      ysrcSize = ysize;
      units = un;
   }
}
