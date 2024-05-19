/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
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

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

package com.expway.ref;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;

import com.expway.tools.io.Chunk;
import com.expway.tools.io.ChunkWriter;
import com.expway.util.Path;
import com.expway.util.PathException;

import com.expway.binarisation.GeneralDecompressor;
import com.expway.binarisation.CodingParameters;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.io.BitOutputStream;

public class AccessUnit {
    byte bPathMode=Path.ABSOLUTE_ROOT_PATH_MODE;

    String sPreviousElement="";

    Chunk cSubtree;
    Path pPath;
    String sSchema;

    long lTotalSizeInBits=0;
    long lPathSizeInBits=0;
    long lSubtreeSizeInBits=0;
    long lHeaderSizeInBits=0;
    
    AccessUnit () {
        cSubtree=null;
        pPath=null;
        sSchema="";
    }

    AccessUnit (String schema) {
        cSubtree=null;
        pPath=null;
        sSchema=schema;
    }

    public void setPath(String s) {    
        try {
            pPath=new Path(s);
        } catch(PathException e) {
            System.out.println("Invalid path "+s+" "+e);
        }
    }

    public void setSubtreeChunk(Chunk c) {
        cSubtree=c;
    }

    public String getPathLastType() {
        if (pPath!=null) {
            try {
                return pPath.getLastType();
            } catch(PathException p) {
            }
        }
        
        return "";
    }

    public long getHeaderSizeInBits() {
        return lHeaderSizeInBits;
    }

    public void setHeaderSizeInBits(long a) {
        lHeaderSizeInBits=a;
    }

    public long getTotalSizeInBits() {
        return lTotalSizeInBits;
    }

    public long getPathSizeInBits() {
        return lPathSizeInBits;
    }

    public long getSubtreeSizeInBits() {
        return lSubtreeSizeInBits;
    }

    public void writeOutput(String outputName) throws Exception {
        ChunkWriter cw=new ChunkWriter();

        if (cSubtree==null) {
            System.out.println("[Warning] Empty payload");
        } else {
            if (BiMEncoder.bAccessUnitAware) {
                pPath.encodeIntoBinary(sSchema,sPreviousElement,"dummy",bPathMode,
                                       BiMCommandLine.iNavigationPathMode).writeYourselfInto(cw);
                lPathSizeInBits=cw.sizeInBits();
        
                cSubtree.writeYourselfInto(cw);
                lSubtreeSizeInBits=cSubtree.sizeInBits();
            } else {
                cSubtree.writeYourselfInto(cw);
                lSubtreeSizeInBits=cSubtree.sizeInBits();
            }

            // Flush all pending codecs
            CodingParameters.flushAllPendingCodecs();
        
            BitOutputStream fos = new BitOutputStream(new FileOutputStream(outputName));
            cw.writeYourselfInto(fos);
            lTotalSizeInBits=cw.sizeInBits();
            fos.close();
        }
    }

    public void readFromFragment(String sAUName,GeneralDecompressor gd,String outputName) throws Exception {
        BitToBitDataInputStream bis=new BitToBitDataInputStream(new BufferedInputStream(new FileInputStream(sAUName)));
        FileWriter fw=new FileWriter(outputName);
        if (BiMEncoder.bAccessUnitAware) {
            fw.write("<MPEG7_AccessUnit>\n");
            fw.write("<location>");
           
            try {
                pPath=new Path();
                pPath.decodeFromBinary(bis,sSchema,sPreviousElement,bPathMode,BiMCommandLine.iNavigationPathMode);       
            } catch(PathException e) {
                System.out.println("Invalid path "+e);
            }              

            fw.write(pPath.encodeIntoString());
            fw.write("</location>\n");
            fw.write("<value>\n");                     

            try {
                gd.setFirstElementName(pPath.getLastElement());
                gd.setFirstTypeName(pPath.getLastType());
            } catch(PathException e) {
            }
        }
        
        gd.decompress(bis,fw);

        if (BiMEncoder.bAccessUnitAware) {
            fw.write("</value>\n");
            fw.write("</MPEG7_AccessUnit>\n");
        }

        fw.close();
    }
}
