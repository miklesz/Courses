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

package com.expway.util;

import java.lang.Exception;
import java.io.IOException;

import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Iterator;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.Chunk;

import com.expway.tools.io.BitToBitDataInputStream;

// Gère les paths sous la forme de .../Type.Element.Num/... 
// Notons qu'on ne peut pas gérer les arbres avec cette classe !

public class Path extends Stack {
    // Modes d'encodage
    public final static int TEXTUAL_NAVIGATION_PATH=1;
    public final static int SIEMENS_NAVIGATION_PATH=2;

    // Relativité de l'encodage
    public final static int ABSOLUTE_ROOT_PATH_MODE=1;
    public final static int ABSOLUTE_TOP_PATH_MODE=2;
    public final static int RELATIVE_PATH_MODE=3;

    int iCurrentNumberOfChildren=0;
    boolean bIsAbsolute=true;
    static boolean bNavigationPathLibrayLoaded=false;

    static {
        try {
            System.loadLibrary("navigation_path");
            bNavigationPathLibrayLoaded=true;
        } catch(Error e) { 
        }
    }
    
    // Méthodes gèrant automatiquement l'index
    // Créé un nouveau noeud fils (Type,Element)
    public void goToChild(String sChildTypeName,String sChildElementName) {
        iCurrentNumberOfChildren++;    
        
        PathNode pChild=new PathNode(sChildTypeName,sChildElementName,iCurrentNumberOfChildren);
        push(pChild);
        
        iCurrentNumberOfChildren=0;
    }

    // Créé un nouveau noeud fils (Element) où TypeName=ElementName
    public void goToChild(String sChildName) {
        goToChild(sChildName,sChildName);
    }

    // Test si le path est vide
    public boolean isEmpty() {
        return size()==0;
    }

    // Remonte au père CAD ENLEVE le dernier noeud du path !
    public void goToFather() throws PathException  {
        try {
            PathNode pLast=(PathNode)pop();
            iCurrentNumberOfChildren=pLast.iIndex;
        } catch (EmptyStackException e) { throw new PathException(); }
    }

    // ATTENTION !
    // Gestion de l'index non automatique !! d'où le terme writeX

    // Crée un nouveau noeud parent et l'ECRIT dans la StringPath !
    public void writeFather() throws PathException {       
        if (getLastNode().bIsAttribute) throw new PathException("already a final attribute"); 

        PathNode pChild=new PathNode();
        push(pChild);
    }

    public void writeElement(String type, String element, int index) throws PathException {
        if (getLastNode().bIsAttribute) throw new PathException("already a final attribute"); 

        PathNode pChild=new PathNode(type,element,index);
        push(pChild);
    }

    public void writeAttribute(String type,String element) throws PathException {       
        if (getLastNode().bIsAttribute) throw new PathException("already a final attribute"); 

        PathNode pChild=new PathNode(type,element);
        push(pChild);
    }   
    
    public PathNode getLastNode() throws PathException {
        try {
            PathNode pLast=(PathNode)peek();
            return pLast;
        } catch (EmptyStackException e) { throw new PathException("no more node"); }      
    }

    public String getLastType() throws PathException {        
        return new String(getLastNode().sType);       
    }

    public String getLastElement() throws PathException {        
        return new String(getLastNode().sElement);       
    }

    public void setAbsoluteness(boolean b) {
        bIsAbsolute=b;
    }

    // Test
    static public void main(String st[]) {
        Path path=new Path();
        try {
        System.out.println("Test path");
        System.out.println(">"+path);
        path.goToChild("VideoEditing");
        System.out.println(">"+path);
        path.goToChild("Shot");
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToChild("Shot");
        System.out.println(">"+path);
        path.goToChild("Mounk");
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToChild("Shot");
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToChild("Sbaal");
        System.out.println(">"+path);
        path.goToChild("MounkType","Mounk");
        System.out.println(">"+path);
        path.goToChild("SbaalType","Sbaal");
        System.out.println(">"+path);
        path.goToChild("MounkType","Mounko");
        System.out.println(">"+path);
        Path np=new Path(path.encodeIntoString());
        System.out.println("!"+np);
        System.out.println("n:"+path.getFirstPathNode()+" p:"+path.getTailPath()+ " pp:"+path.getTailPath().getTailPath());
        path.goToFather();
        System.out.println(">"+path);
        path.goToChild("Mounk");
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.writeFather();
        System.out.println(">"+path);
        path.writeElement("aa","bt",8);
        path.writeElement("afa","ebt",7);
        path.writeElement("affa","kbt",28);
        path.writeAttribute("vgu","uut");             
        System.out.println(">"+path);
        //path.writeFather();
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        path.goToFather();
        System.out.println(">"+path);
        anotherTest();
        testSiemens();
        } catch(PathException e) {
            System.out.println("path err: "+e);
        }
    }

    static void anotherTest() throws PathException {
        String paths[]= {
            "/Mpeg7MainType#Mpeg7Main#1/",
            "/Mpeg7MainType#Mpeg7Main#1/ContentDescriptionType#ContentDescription#1/",
            "/Mpeg7MainType#Mpeg7Main#1/ContentEntityDescriptionType#ContentDescription#2/",
            "/",
            "/Mpeg7MainType#Mpeg7Main#1/ContentEntityDescriptionType#ContentDescription#2/",
            "/ImageType#AudioVisualContent#1/",
            "/Mpeg7MainType#Mpeg7Main#1/@string#version",
            "",
            "@string#copyright",
            "##/",
            "##/@string#version",
            "##/ContentDescriptionType#ContentDescription#1/" };

        for (int i=0;i<paths.length;i++) {
            Path path=new Path(paths[i]);
            String sPath=path.toString();
            boolean equality=sPath.equals(paths[i]);
            System.out.println("path "+i+" equality="+equality);
            if (!equality) {
                System.out.println("original >"+paths[i]);
                System.out.println("original <"+sPath);
            }
        }
    }

    static void testSiemens() {
        String schema="d:\\dev\\refsw\\ulrich\\schema3.xsd";
        String description="d:\\dev\\refsw\\ulrich\\monster3.xml";
        String previous="/";
        String current="/";

        Path p=new Path();

        byte [] bArray=p.encode_path(schema,previous,current,description,(byte)1);
        String out=p.decode_path(previous,current,bArray,(byte)1);

        System.out.println("out:"+out);
    }

    public Path() {
        super();
    }

    
    public Path(String sPath) throws PathException {
        super();
        decodeFromString(sPath);
    }

    public PathNode getFirstPathNode() {
        PathNode pnFirst=null;
        try {
            pnFirst=(PathNode)get(0);
        } catch (Exception e) {}

        return pnFirst;
    }

    // Copie du path de tête
    // @@ discutable, pas bô
    public Path getTailPath() {
        // @@ BEUARK !
        if (size()<=1) return null;

        Path pTail=new Path();
        boolean first=true;
        
        for (Iterator i=listIterator();i.hasNext();) {
            PathNode pn=(PathNode)i.next();
            if (first==false) pTail.add(pn);
            first=false;
        }

        return pTail;
    }

    // Conversion des PathNodes en string
    public String encodeIntoString() {
        String sPath=new String();
        
        // Absolu ?
        if (bIsAbsolute==true) sPath+="/";

        for (Iterator i=listIterator(); i.hasNext();) {
            PathNode p=(PathNode)i.next();
            sPath+=p.toString();            
            if (!p.bIsAttribute) sPath+="/";
        }

        return sPath;
    }

    public void decodeFromString(String sPath) throws PathException {
        bIsAbsolute=false;

        if (sPath==null) throw new PathException("null input string");
        if (sPath.length()==0) return;
        
        // Absolu ?
        if (sPath.charAt(0)=='/') {
            bIsAbsolute=true; 
            sPath=sPath.substring(1);
        }
        
        int iIndex;
        do {
            // Cherche le premier caractère /
            iIndex=sPath.indexOf('/');

            if (iIndex!=-1) {
                String sPathNode=sPath.substring(0,iIndex);
                String sPathTail=sPath.substring(iIndex+1);                 
                PathNode pNode=new PathNode(sPathNode);
                push(pNode);
                sPath=sPathTail;   
            } else if (sPath.length()!=0) {
                PathNode pNode=new PathNode(sPath);
                push(pNode);
            }
        } while (iIndex!=-1);        
    }
    
    // Wrapper avec le module C++ de Siemens
    private void decodeFromSiemensBinary(BitToBitDataInputStream bis,String schema,String sPreviousElement,byte bPathMode)
        throws IOException,PathException {
        int BUFFERSIZE=64;
        
        // Marquage et lecture de 64 octets
        bis.mark(BUFFERSIZE+10);
        byte array[]=new byte [BUFFERSIZE];
        for (int i=0;i<BUFFERSIZE;i++) {
            byte b;
            try {
                b=bis.readByte();
            } catch (Exception e) {
                b=0;
            }
            array[i]=b;
        }

        // Appel au module externe
        String text=decode_path(schema,sPreviousElement,array,bPathMode);
        
        // Enlève la partie % du path de retour
        int nbitsread=0;
        int iPercent=text.indexOf("%");
        String sNBits=text.substring(0,iPercent);
        nbitsread=(new Integer(sNBits)).intValue();
        String path=text.substring(iPercent+1);
        
        // Repositionnement après marquage
        bis.reset();
        for (int i=0;i<nbitsread/8;i++) { bis.readByte(); } 
        bis.readByte(nbitsread%8);
        decodeFromString(path);
    }

    // Wrapper
    private Chunk encodeIntoSiemensBinary(String schema,String sPreviousElement,String description,byte bPathMode) {
        ChunkWriter cw=new ChunkWriter();
        byte [] array=encode_path(schema,sPreviousElement,encodeIntoString(),description,bPathMode);
        int bitslength=array[0]+array[1]*256;
               
        // Ecriture des bytes
        for (int i=0;i<bitslength/8;i++) {
            cw.writeByte(array[i+2]);
        }

        // Ecriture des bits restants
        cw.writeByte(array[2+bitslength/8]>>(8-bitslength%8),bitslength%8);

        return cw;
    }

    // Encode le path sur le binary stream 
    public Chunk encodeIntoBinary(String schema,String sPrevious,String description,byte mode,int iNavigationPathMode) throws Exception {
        ChunkWriter cw=new ChunkWriter();
        switch (iNavigationPathMode) {
        case TEXTUAL_NAVIGATION_PATH:
            cw.writeUTF(encodeIntoString());
            break;
        case SIEMENS_NAVIGATION_PATH:
            if (bNavigationPathLibrayLoaded) {
                return encodeIntoSiemensBinary(schema,sPrevious,description,mode);
            } else {
                throw new PathException("NavigationPath library not loaded !");
            }
        }

        return cw;
    }

    // Decode le path sur le binary stream 
    public void decodeFromBinary(BitToBitDataInputStream bis,String schema,String sPrevious,byte mode,int iNavigationPathMode) 
        throws PathException, IOException {        
            switch (iNavigationPathMode) {
                case TEXTUAL_NAVIGATION_PATH:
                decodeFromString(bis.readUTF());
                break;
                case SIEMENS_NAVIGATION_PATH:
                    if (bNavigationPathLibrayLoaded) {
                        decodeFromSiemensBinary(bis,schema,sPrevious,mode);
                    } else {
                        throw new PathException("NavigationPath library not loaded !");
                    }
                break;
            }       
        }
    /*
    byte [] encode_path(String schema,String previous_element,String current_element,String description,byte mode){        
        byte str[]= current_element.getBytes();
        int length=str.length+1;
        byte array[]= new byte[length];      
        array[0]=(byte)(length-1);

        for (int i=1;i<length;i++) array[i]=(byte)(str[i-1]+array[i-1]);

        return array;
    }
    
    String decode_path(String previous_element,byte [] bin_path,byte mode) {
        int length=(byte)bin_path[0];
        byte str[]=new byte[length];
        
        for (int i=0;i<length;i++) { str[i]=(byte)(bin_path[i+1]-bin_path[i]); }

        String s=new String(str);
        int nbitsread=(length+1)*8;

        return nbitsread+"%"+s;
    }
    */

    native byte [] encode_path(String schema,String previous_element,String current_element,String description,byte mode);
    native String decode_path(String schema,String previous_element,byte [] bin_path,byte mode);

}
