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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
   An object of this class can binarise an element of the type ScalableColorType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class ScalableColorTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String COEFF                 = "Coeff";
   private static final String NUM_OF_COEFF          = "numOfCoeff";
   private static final String NUM_OF_BITPLANES_DISC = "numOfBitplanesDiscarded";
   /* binary values */
   private static final int[] NOC = new int[] { 16, 32, 64, 128, 256 }; //binary and XML values of number of coefficient; index = binary value, NOC[index] = XML value
   private static final int[] NOBD = new int[] { 0, 1, 2, 3, 4, 6, 8 }; //binary and XML values of number of bitplanes discarded; index = binary value, NOBD[index] = XML value
   /* constants */
   private static final int SIZE_OF_BITPLANE[][] = { //size of successive bitplanes depending on the number of coefficients ([bitplane=0..7][noOfCoeff(Bin)=0..4])
      { 3,  3,  3,   3,   3},
      { 5,  5,  5,   5,   5},
      {14, 17, 17,  17,  17},
      {16, 24, 25,  25,  25},
      {16, 30, 51,  82,  91},
      {16, 31, 63, 102, 138},
      {16, 32, 64, 122, 227},
      {16, 32, 64, 128, 256}
   };
   private static final int IDX_TO_CI[] = { //CI index of successive coefficients [0..256]
        0,   4,   8,  12,  32,  36,  40,  44, 128, 132, 136, 140, 160, 164, 168, 172, //0-15
        2,   6,  10,  14,  34,  38,  42,  46, 130, 134, 138, 142, 162, 166, 170, 174, //16-31
       64,  66,  68,  70,  72,  74,  76,  78,  96,  98, 100, 102, 104, 106, 108, 110, // \
      192, 194, 196, 198, 200, 202, 204, 206, 224, 226, 228, 230, 232, 234, 236, 238, // /  32-63
       16,  18,  20,  22,  24,  26,  28,  30,  48,  50,  52,  54,  56,  58,  60,  62, // \
       80,  82,  84,  86,  88,  90,  92,  94, 112, 114, 116, 118, 120, 122, 124, 126, //  \
      144, 146, 148, 150, 152, 154, 156, 158, 176, 178, 180, 182, 184, 186, 188, 190, //  / 64-127
      208, 210, 212, 214, 216, 218, 220, 222, 240, 242, 244, 246, 248, 250, 252, 254, // /
        1,   3,   5,   7,   9,  11,  13,  15,  17,  19,  21,  23,  25,  27,  29,  31, // \
       33,  35,  37,  39,  41,  43,  45,  47,  49,  51,  53,  55,  57,  59,  61,  63, //  |
       65,  67,  69,  71,  73,  75,  77,  79,  81,  83,  85,  87,  89,  91,  93,  95, //  |
       97,  99, 101, 103, 105, 107, 109, 111, 113, 115, 117, 119, 121, 123, 125, 127, //  \
      129, 131, 133, 135, 137, 139, 141, 143, 145, 147, 149, 151, 153, 155, 157, 159, //  / 128-255
      161, 163, 165, 167, 169, 171, 173, 175, 177, 179, 181, 183, 185, 187, 189, 191, //  |
      193, 195, 197, 199, 201, 203, 205, 207, 209, 211, 213, 215, 217, 219, 221, 223, //  |
      225, 227, 229, 231, 233, 235, 237, 239, 241, 243, 245, 247, 249, 251, 253, 255  // /
   };
   private static final int CI_BN_QO[][] = { //indexing, bit allocation and quantizer offsets of coefficients [CI=0..255][0 = BN, 1 = QO]
      {8, 217}, {3, -1}, {6,  4}, {2,  0}, {8, -54}, {2, -1}, {5, -1}, {2, 0},
      {8, -71}, {2,  0}, {5, -5}, {2,  0}, {7, -27}, {2,  0}, {4,  3}, {2, 0},
      {4,  -1}, {3,  1}, {4,  1}, {3,  0}, {4,   0}, {2, -1}, {4, -1}, {2, 0},
      {3,   0}, {3,  0}, {3, -1}, {2,  0}, {2,  -1}, {1,  0}, {3, -1}, {2, 0},
      {6, -22}, {3,  1}, {6,  0}, {3,  0}, {6, -19}, {2,  0}, {4, -2}, {2, 0},
      {6, -14}, {3,  0}, {5, -2}, {2, -1}, {6,  -9}, {1,  0}, {4,  2}, {2, 0},
      {3,   0}, {3,  1}, {3, -1}, {2,  0}, {3,   0}, {2,  0}, {2,  0}, {2, 0},
      {3,  -1}, {2,  0}, {3,  0}, {2, -1}, {2,   0}, {1,  0}, {3,  1}, {2, 1},
      {4,   3}, {3,  1}, {5,  3}, {3,  0}, {4,   0}, {2, -1}, {4, -1}, {2, 0},
      {4,   2}, {3,  0}, {4,  0}, {2,  0}, {4,   0}, {2,  0}, {4,  0}, {2, 0},
      {4,   0}, {4,  2}, {4, -1}, {4, -1}, {4,  -3}, {3, -1}, {4, -3}, {2, 0},
      {4,  -1}, {4,  1}, {4, -2}, {2, -1}, {3,  -2}, {1,  0}, {4, -2}, {2, 0},
      {4,   0}, {4,  1}, {4, -3}, {4, -1}, {4,  -3}, {2,  0}, {4, -2}, {2, 0},
      {4,  -2}, {3,  1}, {4, -2}, {2,  0}, {2,   0}, {1,  0}, {4, -1}, {2, 0},
      {4,  -2}, {3,  0}, {4, -2}, {3,  0}, {2,   0}, {2,  0}, {2,  0}, {1, 0},
      {3,  -1}, {2,  0}, {2,  0}, {2,  0}, {2,   0}, {1,  0}, {2,  0}, {2, 0},
      {7, -29}, {4,  1}, {6,  0}, {3, -1}, {6, -22}, {2,  0}, {4,  0}, {2, 0},
      {6, -14}, {3,  0}, {5, -5}, {2,  0}, {6,  -8}, {2,  0}, {4,  1}, {2, 0},
      {4,   0}, {4,  2}, {4, -2}, {4, -1}, {4,  -3}, {2,  0}, {4, -3}, {2, 0},
      {4,  -1}, {4,  1}, {4, -2}, {2,  0}, {3,  -1}, {1,  0}, {4, -2}, {2, 0},
      {6, -22}, {3,  1}, {5, -2}, {3,  0}, {5, -11}, {2,  0}, {2,  0}, {1, 0},
      {6, -13}, {3,  1}, {4, -1}, {2,  0}, {5,  -6}, {1,  0}, {3,  0}, {2, 0},
      {4,  -1}, {2,  0}, {3, -1}, {2,  0}, {2,   0}, {1,  0}, {1,  0}, {1, 0},
      {3,  -1}, {2,  0}, {2,  0}, {2,  0}, {1,   0}, {1,  0}, {2,  0}, {2, 0},
      {4,  -5}, {3,  0}, {4, -4}, {3, -1}, {4,  -2}, {2,  0}, {4, -1}, {2, 0},
      {4,  -3}, {3,  0}, {4, -1}, {2,  0}, {3,  -1}, {2,  0}, {4, -1}, {2, 0},
      {4,  -1}, {3,  1}, {4, -2}, {3, -1}, {4,  -2}, {2,  0}, {4, -2}, {1, 0},
      {4,  -1}, {3,  1}, {4, -2}, {2,  0}, {3,  -1}, {1,  0}, {3, -1}, {2, 0},
      {4,  -2}, {2,  0}, {4, -1}, {2,  0}, {2,   0}, {1,  0}, {2,  0}, {1, 0},
      {4,  -1}, {3,  0}, {2,  0}, {2,  0}, {2,   0}, {1,  0}, {2,  0}, {1, 0},
      {3,  -1}, {2,  0}, {2,  0}, {2,  0}, {1,   0}, {1,  0}, {1,  0}, {1, 0},
      {3,   0}, {2,  0}, {2,  0}, {2,  0}, {1,   0}, {1,  0}, {1,  0}, {1, 0}
   };

   private int noOfCoeff, noOfCoeffBin, noOfBitplDisc, noOfBitplDiscBin; //binary values are required for indexing in SIZE_OF_BITPLANE
   private int coeff[];

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (isAtRootLevel())
      {
         noOfCoeff = Integer.parseInt(attrs.getValue(NUM_OF_COEFF).trim());
         for (int i = 0; i < NOC.length; i++)
            if (noOfCoeff == NOC[i])
            {
               noOfCoeffBin = i;
               break;
            }
         noOfBitplDisc = Integer.parseInt(attrs.getValue(NUM_OF_BITPLANES_DISC).trim());
         for (int i = 0; i < NOBD.length; i++)
            if (noOfBitplDisc == NOBD[i])
            {
               noOfBitplDiscBin = i;
               break;
            }
         coeff = new int[noOfCoeff];

         currentElement = null;
      }
      else if (localName.equals(COEFF))
         currentElement = COEFF;
   }

   public void characters(char[] chars, int index, int length) throws SAXException
   {
      if (currentElement == COEFF)
      {
         StringTokenizer strtok = new StringTokenizer(new String(chars, index, length));

         for (int i = 0; i < coeff.length; i++)
            coeff[i] = Integer.parseInt(strtok.nextToken());

         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if(isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeByte(noOfCoeffBin, 3);                      //numOfCoeff
      currentChunkWriterUsed.writeByte(noOfBitplDiscBin, 3);                  //numOfBitplanesDiscarded
      for(int i = 0; i < coeff.length; i++)
         //how coefficient sign is represented? 1(true) if coeff >= 0, 0(false) if coeff < 0
         currentChunkWriterUsed.writeBoolean(coeff[i] >= 0);                   //CoefficientSign
      for(int bp = 0; bp < 8-noOfBitplDisc; bp++) //loop for bitplanes, bp - bitplane index
      {
         int sizeOfBitplane = 0;
         int mask = (1 << (7-bp)); //1 is shifted by (7-i) not (8-i) because i takes values from 0 to 7 (i < 8-...), and for i = 0 mask = 10000000 is needed (so 1 must be shifted by 7)
         for (int c = 0; c < coeff.length; c++) //loop for coefficients, c - coeff index
            if (CI_BN_QO[IDX_TO_CI[c]][0] >= 8-bp)
            {
               int ampl = Math.abs(coeff[c]-CI_BN_QO[IDX_TO_CI[c]][1]);
               currentChunkWriterUsed.writeBoolean((ampl&mask) == mask);  //Bitplane[bp][c]
               sizeOfBitplane++;
            }
         if (DEBUG) System.out.println("finish: bitplane: "+bp+", coeffs: "+noOfCoeff+", sizeOfBitplane: "+sizeOfBitplane+", SOB: "+SIZE_OF_BITPLANE[bp][noOfCoeffBin]);
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         int nocb = dis.readByte(3);                           //numOfCoeff
         int noc = NOC[nocb];
         int nobdb = dis.readByte(3);                          //numOfBitplanesDiscarded
         int nobd = NOBD[nobdb];

         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+NUM_OF_COEFF+"=\""+noc+"\" "+
                  NUM_OF_BITPLANES_DISC+"=\""+nobd+"\">\n");
         //how coefficient sign is represented? 0(false) if coeff >= 0, 1(true) if coeff < 0
         boolean[] sign = new boolean[noc];
         for(int i = 0; i < noc; i++)
            sign[i] = dis.readBoolean();                       //CoefficientSign
         int[] coeff = new int[noc];
         for(int bp = 0; bp < 8-nobd; bp++) //loop for bitplanes
         {
            int sizeOfBitplane = 0;
            for (int c = 0; c < noc; c++)
            {
               if (CI_BN_QO[IDX_TO_CI[c]][0] >= 8-bp)
               {
                  coeff[c] = (coeff[c] << 1)+dis.readByte(1);  //Bitplane[bp][c]
                  sizeOfBitplane++;
               }
            }
            if (DEBUG) System.out.println("decode: bitplane: "+bp+", coeffs: "+noc+", sizeOfBitplane: "+sizeOfBitplane+", SOB: "+SIZE_OF_BITPLANE[bp][nocb]);
         }
         w.write("<"+ns+COEFF+">\n");
         //in order to fully decode coefficient following steps must be done:
         //1: values stored in table coeff[] must be shifted left by nobd (nobd bitplanes was not readen from bitstream - these bits must be set to 0) in order to get amplitude
         //2: amplitude (see: 1) must be multiplied by coefficient sign stored in table sign[]
         //3: coefficient offset (from CI_BN_QO) must be add to signed amplitude (result of step 2)
         for (int c = 0; c < noc; c++)
            w.write(CI_BN_QO[IDX_TO_CI[c]][1]+((sign[c] ? 1 : -1)*(coeff[c] << nobd))+" ");
         w.write("\n</"+ns+COEFF+">\n");
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("ScalableColor: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+SCALABLE_COLOR, null);
   }
}

