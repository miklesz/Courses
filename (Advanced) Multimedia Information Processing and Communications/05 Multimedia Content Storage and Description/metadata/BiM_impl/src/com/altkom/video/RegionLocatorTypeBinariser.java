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
import java.io.UTFDataFormatException;
import java.util.StringTokenizer;

import java.util.Vector;
import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
   An object of this class can binarise an element of the type RegionLocatorType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class RegionLocatorTypeBinariser  extends Binariser{
  // elements/attributes strings
  private final String COORD_REF_ELEM="CoordRef";
  private final String REF_ATTRIB="ref";
  private final String SPATIAL_REF_ATTRIB="spatialRef";
  private final String BOX_ELEM="Box";
  private final String UNLOCATED_REGION_ATTRIB="unlocatedRegion";
  private final String POLYGON_ELEM="Polygon";
  private final String COORDS_ELEM="Coords";

  // binariser state identifier
  private final int LOCATOR=0;
  private final int BOX=1;
  private final int POLYGON=2;
  private final int POLYGON_COORDS=3;
  private final int SKIP=-1;

    // local variables
  private int state;
  private boolean coordFlag;
  private String coordRef;
  private boolean spatialRef;
  private int xRepr, yRepr;
  private Vector boxList, polygonList;
  private boolean unlocated;
  private Vector boxCoords, polygonCoords;


  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=LOCATOR;
      boxList=new Vector();
      polygonList=new Vector();
      coordFlag=false;
      // ??? how to set these variables
      xRepr=0;
      yRepr=0;
    }else{
      if(localName.equals(COORD_REF_ELEM)){
        coordFlag=true;       // is it really necessary, CoordRef element MUST occur in the DDL representation
        coordRef=attrs.getValue(REF_ATTRIB);
        spatialRef=Boolean.valueOf(attrs.getValue(SPATIAL_REF_ATTRIB)).booleanValue();
      }

      if(localName.equals(BOX_ELEM)){
        unlocated=Boolean.valueOf(attrs.getValue(UNLOCATED_REGION_ATTRIB)).booleanValue();
        boxCoords=new Vector();
        state=BOX;
      }

      if(localName.equals(POLYGON_ELEM)){
        unlocated=Boolean.valueOf(attrs.getValue(UNLOCATED_REGION_ATTRIB)).booleanValue();
        polygonCoords=new Vector();
        state=POLYGON;
      }

      if(localName.equals(COORDS_ELEM)){
        state=POLYGON_COORDS;
      }
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    StringTokenizer tokenizer;

    switch(state){
      case BOX:
        tokenizer=new StringTokenizer(new String(chars, index, length));
        while(tokenizer.hasMoreTokens()){
          try{
            boxCoords.add(Integer.valueOf(tokenizer.nextToken()));
          }catch(NumberFormatException nfe){
            throw new SAXException("RegionLocator - box coordinate unrecognized");
          }
        }
        state=SKIP; // do not read any data more
        break;

      case POLYGON_COORDS:
        tokenizer=new StringTokenizer(new String(chars, index, length));
        while(tokenizer.hasMoreTokens()){
          try{
            polygonCoords.add(Integer.valueOf(tokenizer.nextToken()));
          }catch(NumberFormatException nfe){
            throw new SAXException("RegionLocator - polygon coordinate unrecognized");
          }
        }
        state=SKIP; // do not read any data more
        break;

      default:
        break;
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    if (DEBUG) System.out.println("RegionLocator: endElement: "+localName);

    if(localName.equals(BOX_ELEM)){
      boxList.add(new Box(unlocated, boxCoords));
    }

    if(localName.equals(POLYGON_ELEM)){
      polygonList.add(new Polygon(unlocated, polygonCoords));
    }

    if(isFinished()){
      finish();
    }
  }


  public void finish(){
    if((currentChunkWriterUsed == null)){
      return;
    }

    if (DEBUG) System.out.println("RegionLocator: finish");
    try
    {
       calculateXYRepr(); //calculates values of xRepr and yRepr according to the values of Pixel (in Box) or FirstVertex (in Polygon)

       // write CoordFlag
       currentChunkWriterUsed.writeBoolean(coordFlag);
       // write reference to the coordinate system
       if(coordFlag){
         currentChunkWriterUsed.writeUTF(coordRef);
         currentChunkWriterUsed.writeBoolean(spatialRef);
       }else{
         currentChunkWriterUsed.writeInt(xRepr, 8);
         currentChunkWriterUsed.writeInt(yRepr, 8);
       }
       // write ContainedLocatorTypes
       int locatorType=0;
       if(boxList.size()>0)
         locatorType |= 1;
       if(polygonList.size()>0)
         locatorType |= 2;
       currentChunkWriterUsed.writeByte(locatorType, 2);
       // write boxes
       if(boxList.size()>0){
         // NumOfBoxes
         currentChunkWriterUsed.writeInfiniteLong(boxList.size(), 4);
         Enumeration boxEnum=boxList.elements();
         while(boxEnum.hasMoreElements()){
           Box box=(Box)(boxEnum.nextElement());
           // unlocatedRegion
           currentChunkWriterUsed.writeBoolean(box.unlocated);
           // Use3P
           currentChunkWriterUsed.writeBoolean(box.getUse3P());
           // PixelX, PixelY
           for(int k=0; k< (box.getUse3P() ? 3 : 2); k++){
             currentChunkWriterUsed.writeInt(box.xCoords[k], xRepr);
             currentChunkWriterUsed.writeInt(box.yCoords[k], yRepr);
           }
         }
       }
       // write polygons
       if(polygonList.size()>0){
         // NumOfPolygons
         currentChunkWriterUsed.writeInfiniteLong(polygonList.size(),4);
         Enumeration polygonEnum=polygonList.elements();
         while(polygonEnum.hasMoreElements()){
           Polygon polygon=(Polygon)(polygonEnum.nextElement());
           // unlocatedRegion
           currentChunkWriterUsed.writeBoolean(polygon.unlocated);
           // NumOfVertices
           currentChunkWriterUsed.writeInfiniteLong(polygon.numOfVertices,4);
           // FirstVertexX, FirstVertexY
           currentChunkWriterUsed.writeInt(polygon.firstVertexX, xRepr);
           currentChunkWriterUsed.writeInt(polygon.firstVertexY, yRepr);
           // XDynamicRange, YDynamicRange
           currentChunkWriterUsed.writeByte(polygon.xDynamicRange, 4);
           currentChunkWriterUsed.writeByte(polygon.yDynamicRange, 4);

           for(int k=0; k<polygon.numOfVertices-1; k++){
             currentChunkWriterUsed.writeInt(polygon.octant[k], 3);
             int majorComponentNumBits=polygon.octant[k]>3 ? polygon.yDynamicRange : polygon.xDynamicRange;
             int minorComponentNumBits=polygon.octant[k]<=3 ? polygon.yDynamicRange : polygon.xDynamicRange;
             if (minorComponentNumBits>majorComponentNumBits)
               minorComponentNumBits= majorComponentNumBits;
             currentChunkWriterUsed.writeInt(polygon.majorComponent[k], majorComponentNumBits);
             currentChunkWriterUsed.writeInt(polygon.minorComponent[k], minorComponentNumBits);
           }
         }
       }
    }
    catch (Exception x)
    {
      System.out.println("RegionLocator: can't finish descriptor.");
      x.printStackTrace();
    }
  }

  /**
   * default decode method, writes descriptor tyoe as an element name
   */
  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+REGION_LOCATOR, null);
  }

  /**
   * general decode method
   * @param dis
   * @param w
   * @param name
   */
  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("RegionLocator: decode");

    long before = dis.getReadedBits();
    try{
      String ns = getNameSpace(name);
      w.write("\n<" + name);
      if(attrs!=null){
        w.write(" " + attrs);
      }
      w.write(">\n");

      int xRepr, yRepr;
      boolean coordFlag=dis.readBoolean();
      if(coordFlag){
        String ref=dis.readUTF();                // this reference should probably be stored somewhere ???
        boolean spatialRef=dis.readBoolean();
        w.write("<"   + ns+COORD_REF_ELEM + " " + REF_ATTRIB + "=\"" + ref +
                "\" " + SPATIAL_REF_ATTRIB + "=\""  + (spatialRef? "true" : "false") + "\"/>\n");
        //calculate xRepr and yRepr from Spatial2D - ref
        int[] srcSize = Spatial2DCoordinateSystemTypeBinariser.getDecodedSrcSize(ref);
        xRepr = yRepr = 0;
        while (srcSize[0] > 0) { srcSize[0] >>= 1; xRepr++; }
        while (srcSize[1] > 0) { srcSize[1] >>= 1; yRepr++; }
      }else{
        xRepr=dis.readInt(8);
        yRepr=dis.readInt(8);
      }
      int locatorTypes=dis.readByte(2);
      if( (locatorTypes & 1) == 1){
        int numBoxes=(int)(dis.readInfiniteLong(4));
        for(int i=0; i<numBoxes; i++){
          w.write("<" + ns+BOX_ELEM );
          boolean unlocatedRegion=dis.readBoolean();
          w.write(" " + UNLOCATED_REGION_ATTRIB + "=\""  + (unlocatedRegion? "true" : "false") + "\">\n");
          int use3P=dis.readInt(1);
          int[][] coords=new int[2][2+use3P];
          for(int k=0; k<2 + use3P; k++){
            coords[0][k]=dis.readInt(xRepr);
            coords[1][k]=dis.readInt(yRepr);
          }
          for (int k=0; k<2+use3P; k++)
            w.write(coords[0][k]+" ");
          w.write("\n");
          for (int k=0; k<2+use3P; k++)
            w.write(coords[1][k]+" ");
          w.write("\n</" + ns+BOX_ELEM + ">\n");
        }
      }
      if( (locatorTypes & 2) == 2){
        int numPolygons=(int)(dis.readInfiniteLong(4));
        for(int i=0; i<numPolygons; i++){
          w.write("<" + ns+POLYGON_ELEM );
          boolean unlocatedRegion=dis.readBoolean();
          w.write(" " + UNLOCATED_REGION_ATTRIB + "=\""  + (unlocatedRegion? "true" : "false") + "\">\n");
          int numOfVertices=(int)dis.readInfiniteLong(4);
          int firstVertexX=dis.readInt(xRepr);
          int firstVertexY=dis.readInt(yRepr);
          int xDynamicRange=dis.readByte(4);
          int yDynamicRange=dis.readByte(4);
          w.write("<" + ns+COORDS_ELEM + ">\n");
          int[][] coords=new int[2][numOfVertices-1];
          for(int k=0; k<numOfVertices-1; k++){
            int octant=dis.readByte(3);
            int majorComponentNumBits=octant>3 ? yDynamicRange : xDynamicRange;
            int minorComponentNumBits=octant<=3 ? yDynamicRange : xDynamicRange;
            if (minorComponentNumBits>majorComponentNumBits)
               minorComponentNumBits = majorComponentNumBits;
            int majorComponent=dis.readInt(majorComponentNumBits);
            int minorComponent=dis.readInt(minorComponentNumBits);
            switch (octant)
            {
               case 0:  coords[0][k] = majorComponent;
                        coords[1][k] = minorComponent;
                        break;
               case 1:  coords[0][k] = -majorComponent;
                        coords[1][k] = minorComponent;
                        break;
               case 2:  coords[0][k] = majorComponent;
                        coords[1][k] = -minorComponent;
                        break;
               case 3:  coords[0][k] = -majorComponent;
                        coords[1][k] = -minorComponent;
                        break;
               case 4:  coords[0][k] = minorComponent;
                        coords[1][k] = majorComponent;
                        break;
               case 5:  coords[0][k] = minorComponent;
                        coords[1][k] = -majorComponent;
                        break;
               case 6:  coords[0][k] = -minorComponent;
                        coords[1][k] = majorComponent;
                        break;
               case 7:  coords[0][k] = -minorComponent;
                        coords[1][k] = -majorComponent;
                        break;
            }
          }
          w.write(firstVertexX+" ");
          for (int k=0; k<numOfVertices-1; k++)
            w.write(coords[0][k]+" ");
          w.write("\n"+firstVertexY+" ");
          for (int k=0; k<numOfVertices-1; k++)
            w.write(coords[1][k]+" ");
          w.write("</" + ns+COORDS_ELEM + ">\n");
          w.write("</" + ns+POLYGON_ELEM + ">\n");
        }
      }
      w.write("\n</" + name + ">\n");
    }catch (Exception e){
      System.out.println("RegionLocator: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }

   void calculateXYRepr() throws Exception
   {
      if (coordFlag)
      {
         int[] srcSize = Spatial2DCoordinateSystemTypeBinariser.getCodedSrcSize(coordRef);
         xRepr = yRepr = 0;
         while (srcSize[0] > 0) { srcSize[0] >>= 1; xRepr++; }
         while (srcSize[1] > 0) { srcSize[1] >>= 1; yRepr++; }
      }
      else
      {
         int maxx = Integer.MIN_VALUE;
         int maxy = Integer.MIN_VALUE;
         if(boxList.size()>0)
         {
            Enumeration boxEnum=boxList.elements();
            while(boxEnum.hasMoreElements())
            {
               Box box=(Box)(boxEnum.nextElement());
               for(int k=0; k< (box.getUse3P() ? 3 : 2); k++)
               {
                  if (box.xCoords[k]>maxx)
                     maxx=box.xCoords[k];
                  if (box.yCoords[k]>maxy)
                     maxy=box.yCoords[k];
               }
            }
         }//if box
         if(polygonList.size()>0)
         {
            Enumeration polygonEnum=polygonList.elements();
            while(polygonEnum.hasMoreElements())
            {
               Polygon polygon=(Polygon)(polygonEnum.nextElement());
               if (polygon.firstVertexX>maxx)
                  maxx=polygon.firstVertexX;
               if (polygon.firstVertexY>maxy)
                  maxy=polygon.firstVertexY;
            }
         }
         xRepr = 0;
         while (maxx > 0) { maxx >>= 1; xRepr++; }
         yRepr = 0;
         while (maxy > 0) { maxy >>= 1; yRepr++; }
      }//else
      if (DEBUG) System.out.println("RegionLocator: calculateXYRepr: xRepr: "+xRepr+", yRepr: "+yRepr);
   }

  /**
   *  used to store Box element
   */
  private class Box{
    boolean unlocated;
    int xCoords[], yCoords[];
    int numPoints;

    Box(boolean unlocated, Vector coords){
      this.unlocated=unlocated;

      numPoints=coords.size()/2;
      xCoords=new int[numPoints];
      yCoords=new int[numPoints];

      for(int i=0; i<numPoints; i++){
        xCoords[i]=((Integer)(coords.elementAt(i))).intValue();
      }

      for(int i=0; i<numPoints; i++){
        yCoords[i]=((Integer)(coords.elementAt(numPoints+i))).intValue();
      }
    }

    boolean getUse3P(){
      return (numPoints==3);
    }
  }

  /**
   *  used to store Polygon element
   */
  private class Polygon{
    boolean unlocated;
    int numOfVertices;
    int firstVertexX, firstVertexY;
    int xDynamicRange, yDynamicRange;
    int octant[], majorComponent[], minorComponent[];

    Polygon(boolean unlocated, Vector coords){
      this.unlocated=unlocated;

      numOfVertices=coords.size()/2;

      firstVertexX=((Integer)(coords.get(0))).intValue();
      firstVertexY=((Integer)(coords.get(numOfVertices))).intValue();

      octant=new int[numOfVertices-1];
      majorComponent=new int[numOfVertices-1];
      minorComponent=new int[numOfVertices-1];

      int maxdx = Integer.MIN_VALUE;
      int maxdy = Integer.MIN_VALUE;
      for (int i=0; i < numOfVertices-1; i++)
      {
         int x = ((Integer)(coords.get(i+1))).intValue(); //+1 becase coords[0] and coords[numOfVertices] are stored as firstVertex
         int y = ((Integer)(coords.get(numOfVertices+i+1))).intValue();
         if ((x > y) && (y>=0))     octant[i] = 0;
         else if ((x<=-y) && (y>0)) octant[i] = 1;
         else if ((x>=-y) && (y<0)) octant[i] = 2;
         else if ((x<y) && (y<=0))  octant[i] = 3;
         else if ((x>0) && (y>=x))  octant[i] = 4;
         else if ((x>=0) && (y<-x)) octant[i] = 5;
         else if ((x<=0) && (y>-x)) octant[i] = 6;
         else if ((x<0) && (y<=x))  octant[i] = 7;
         if (x < 0) x = -x;
         if (y < 0) y = -y;
         if (octant[i] > 3)
         {
            majorComponent[i]=y;
            minorComponent[i]=x;
         }
         else
         {
            majorComponent[i]=x;
            minorComponent[i]=y;
         }
         if (x>maxdx)
            maxdx=x;
         if (y>maxdy)
            maxdy=y;
      }
      xDynamicRange = 0;
      while (maxdx>0) { maxdx>>=1; xDynamicRange++; }
      yDynamicRange = 0;
      while (maxdy>0) { maxdy>>=1; yDynamicRange++; }
    }
  }
}