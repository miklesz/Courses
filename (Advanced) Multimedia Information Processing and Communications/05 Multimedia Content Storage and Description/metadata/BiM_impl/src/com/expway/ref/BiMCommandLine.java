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

import com.expway.util.Path;
import com.expway.binarisation.CodingParameters;

public class BiMCommandLine {
    static String schemaName;
    static String xmlName;
    static String bimName;

    static int BIM_ENCODER=1;
    static int BIM_DECODER=2;

    static int iNavigationPathMode=Path.TEXTUAL_NAVIGATION_PATH;

    public static boolean bAccessUnitAware=false;
    public static boolean DEBUG=false;
    
    static void parseArgs(String [] argv,int type) {

        boolean isOptionsHere=false;

        if (DEBUG) System.out.println("DEBUG Mode !");

        try {
            int argc=argv.length;
            if (argc<2 || argc>4) throw new Exception();
                        
            if (argv[0].indexOf("-")!=-1) {
                isOptionsHere=true;
                if (argv[0].indexOf("a")!=-1) bAccessUnitAware=true;
                if (argv[0].indexOf("d")!=-1) bAccessUnitAware=false;
                if (argv[0].indexOf("t")!=-1) iNavigationPathMode=Path.TEXTUAL_NAVIGATION_PATH;
                if (argv[0].indexOf("b")!=-1) iNavigationPathMode=Path.SIEMENS_NAVIGATION_PATH;
                if (argv[0].indexOf("c")!=-1) CodingParameters.bAllowsCompatibility=true;
                if (argv[0].indexOf("z")!=-1) CodingParameters.bSpecificTimeDatatypes=true;
                if (argv[0].indexOf("v")!=-1) CodingParameters.bSpecificVideoCodecs=true;
                if (argv[0].indexOf("u")!=-1) CodingParameters.bUnitSize = 0;	//AM
            }

            int shift=0;
            if (isOptionsHere) shift=1;
                  
            if (type==BIM_ENCODER) {
                xmlName=new String(argv[0+shift]);
                bimName=new String(argv[1+shift]);
            } else {
                bimName=new String(argv[0+shift]);
                xmlName=new String(argv[1+shift]);                
            }

            if(CodingParameters.bUnitSize==0 && isOptionsHere){ //AM...
                Integer testInt = null;
                if (iNavigationPathMode!=Path.SIEMENS_NAVIGATION_PATH){
                    if(argc == 4)
                        CodingParameters.bUnitSize = testInt.parseInt(argv[2+shift]);
                    else if(argc == 5)
                        CodingParameters.bUnitSize = testInt.parseInt(argv[3+shift]);
                }
                else{
                    if(argc == 5)
                        CodingParameters.bUnitSize = testInt.parseInt(argv[3+shift]);
                    else if(argc == 6)
                        CodingParameters.bUnitSize = testInt.parseInt(argv[4+shift]);
                }
            }		//AM...**

            if (iNavigationPathMode==Path.SIEMENS_NAVIGATION_PATH) schemaName=new String(argv[2+shift]);

        } catch(Exception e) {
            printUsage(type);
            if (DEBUG) {
                iNavigationPathMode=Path.SIEMENS_NAVIGATION_PATH;
                bAccessUnitAware=false;
                String path="D:/dev/refsw/bimrefsoft/examples/description/";
                //String path="D:/dev/refsw/bimrefsoft/examples/compat/";
                //String path="D:/dev/refsw/bimrefsoft/examples/accessunit/";
                schemaName=path;
                //if (type==BIM_ENCODER) xmlName=path+"monster_junior.xml"; 
                //else  xmlName=path+"monster_junior_D.xml"; 
                //bimName=path+"monster_junior.bim";
                if (type==BIM_ENCODER) xmlName=path+"monsterJr4Ver3.xml";
                else  xmlName=path+"monsterJr4Ver3_D.xml"; 
                bimName=path+"monsterJr4Ver3.bim";
                CodingParameters.bAllowsCompatibility=false;
                //if (type==BIM_ENCODER) xmlName=path+"mdsExamplesClause4_7.xml";
                //else  xmlName=path+"mdsExamplesClause4_7_D.xml"; 
                //bimName=path+"mdsExamplesClause4_7.bim";
                //if (type==BIM_ENCODER) xmlName=path+"example.xml";
                //else  xmlName=path+"example_D.xml"; 
                //bimName=path+"example.bim";
                // if (type==BIM_ENCODER) xmlName=path+"visualExamples.xml";
                //else  xmlName=path+"visualExamples.xml"; 
                //bimName=path+"visualExamples.bim";
            }
            else System.exit(1);
        }
    } 

    static void printUsage(int type) {
        String name;
        if (type==BIM_ENCODER) {
            System.out.println("Usage : BiMEncoder [-abdtczvu] input_xml_filename output_bim_filename [schema_filename]");
        }
        if (type==BIM_DECODER) {
            System.out.println("Usage : BiMDecoder [-abdtczvu] input_bim_filename output_xml_filename [schema_filename]");
        }

        System.out.println("-t uses textual path (default)(only used in textual access unit mode)");
        System.out.println("-b uses binary path (schema file is required)(only used in textual access unit mode)");
        System.out.println("-d implies that the xml file is a piece of description (default)");
        System.out.println("-a implies that the xml file is a textual access unit");
        System.out.println("-c enables the f&b compatibility mode (default is disabled)");
        System.out.println("-z activates the specific time datatypes codecs");
        System.out.println("-v activates the specific video codecs");
        System.out.println("-u implies that the xml file is using unitsize feature to code itself"); //AM
        
    }
}
