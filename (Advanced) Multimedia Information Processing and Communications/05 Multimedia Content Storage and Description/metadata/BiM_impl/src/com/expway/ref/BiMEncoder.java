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

import com.expway.binarisation.GeneralBinaryHandler;

public class BiMEncoder extends BiMCommandLine {   
    public static void main(String [] argv) {
        parseArgs(argv,BIM_ENCODER);

        try {
            AccessUnitParser auParser=new AccessUnitParser(xmlName,schemaName);

            if (bAccessUnitAware) {
                System.out.println("Parsing access unit "+xmlName+" ...");
                auParser.parse();
            } else {
                System.out.println("Parsing description "+xmlName+" ...");
                auParser.parse();
            }

            System.out.println("Writing binary fragment "+bimName+" ...");
            auParser.writeOutput(bimName);

            printStats(auParser);

        } catch(Exception e) {
            System.out.println("Exception :"+e);
        }
    }

    static private void printStats(AccessUnitParser auParser) {
        System.out.println("");
        System.out.println("Encoding report");
        System.out.println("---------------");
        auParser.printReport();
    }
}
