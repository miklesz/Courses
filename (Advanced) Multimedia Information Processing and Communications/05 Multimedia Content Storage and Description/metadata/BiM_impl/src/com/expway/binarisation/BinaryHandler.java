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

package com.expway.binarisation;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.expway.util.URIRegistry;
import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

import com.expway.tools.expression.TypeDefinitions;
import com.expway.tools.expression.TypeDefinition;
import com.expway.tools.expression.SetOfDefinitions;
import com.expway.tools.expression.ComplexTypeDefinition;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.compression.ComplexTypeInstance;
import com.expway.tools.compression.SpecificTypeInstance;
import com.expway.tools.compression.ParsingException;

import com.expway.schema.xml.XMLSchemaInstance;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * il s'agit de l'interface pour tous les autres Handler
 * un handler a soit un dictionnaire définit dans le handlerManager ou pas de dictionnaire
 */
public class BinaryHandler extends LocalHandler {
	
    
    final static boolean DEBUG=false;
    
    TreeMap attributeCodingMap = new TreeMap();
    TypeEncoder typeEncoder;
    SetOfDefinitions setOfDefinitions;
    int bitsOfStructure = 0;
    CodingContext fatherCodingContext,codingContext;
    
    //

    public BinaryHandler(TypeEncoder ate,SetOfDefinitions sd,CodingContext fatherCo){
        super();
        typeEncoder = ate;
        setOfDefinitions = sd;
        fatherCodingContext=fatherCo;
    }

    // instantiation pour un schéma fixe

    public int getBitsOfStructure(){return bitsOfStructure;}

    /**
     * Reset the object before it goes in the pool
     */
    public void reset() {
        attributeCodingMap.clear();
    }
    
    /**
     * envoyer quand le fils fini
     *
     * @param son le fils qui a fini son instantiation
     */
    public void informEnd(LocalHandler son) throws HandlerException {}
    
    /**
     * intitialize the object created by the handler
     */    
    public void init(String uri, String local, String raw, Attributes attrs) throws HandlerException {
        //System.out.println("init raw="+raw+" this="+this);
        if (DEBUG) System.out.println("bhns init: "+getCompactName(raw));
        try {            
            if (typeEncoder instanceof ComplexTypeInstance) {

                // Calcul du contexte, en fonction des attributs commencant par le prefixe cc:
                codingContext=fatherCodingContext.makeNewContext(attrs);
                ((ComplexTypeInstance)typeEncoder).setCodingContext(codingContext);

                // On récupère la valeur de "XMLSchema-Instance" (xsi):type
                String qValue=XMLSchemaInstance.processType(attrs); // null si il n'y a rien
                ((ComplexTypeInstance)typeEncoder).setXsiType(getCompactName(qValue));
            }

            // On trie les attributs par ordre alphabetique et on enlève tous
            // ceux appartenant à : 
            // - XMLSchema-Instance (xsi)
            // - cc (attribut spécial pour le codage contextuel)
            // @@@@COMPAT séparer les attributs par namespaces ??
            for (int i =0;i<attrs.getLength();i++){
                if (!XMLSchemaInstance.isReserved(attrs,i) && !CodingContext.isReserved(attrs,i)) {
                    attributeCodingMap.put(attrs.getLocalName(i),new AttributeWrapper(attrs.getQName(i),attrs.getValue(i)));
                }
            }

            typeEncoder.startEncoding(); 

            // on les encode
            for (Iterator it=attributeCodingMap.entrySet().iterator();it.hasNext();){
                Map.Entry me =(Map.Entry)it.next();
                AttributeWrapper aw = (AttributeWrapper)me.getValue();
                String tname = aw.rawName;
                if (URIRegistry.hasPrefix(tname)) tname = getCompactName(tname);
                TypeEncoder ate = typeEncoder.encodeAttribute(tname);
                ate.setValue(aw.value);
                ate.endEncoding();
            }            
        } catch (ParsingException e){
            e.setLocator(locator);
            //e.printStackTrace();
            System.out.println("[Error] During encoding of " + typeEncoder.getTypeDefinition().getName());
            
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_FATAL);
        } catch (Exception ee) {
            //ee.printStackTrace();
            System.out.println("[Error] During encoding of " + typeEncoder.getTypeDefinition().getName());
            
            throw new HandlerException(ee,ee.getMessage(),HandlerException.TYPE_FATAL);
        }
    }
    
    public LocalHandler getSonHandler(String uri, String local, String raw) throws HandlerException {
        try{
            //System.out.println("getson raw="+raw+" this="+this);

            TypeEncoder ate = null;
            
            String tname = raw;
            if (URIRegistry.hasPrefix(raw)) tname = getCompactName(tname);
            ate = typeEncoder.encodeElement(tname);
            
            if (ate == null)
                System.out.println("?????????????????");

            LocalHandler localHandler;
            if (ate instanceof SpecificTypeInstance) 
                localHandler=new SpecificHandler((SpecificTypeInstance)ate,setOfDefinitions);
            else
                localHandler=new BinaryHandler(ate,setOfDefinitions,codingContext);
            
            return localHandler;
        } catch (ParsingException e){
            e.setLocator(locator);
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_FATAL);
        } 
    }

    /**
     * inform the Handler that its instantiation has just finished
     */
    public void end() throws HandlerException {
        try {
            typeEncoder.endEncoding();
            bitsOfStructure = typeEncoder.getBitsOfStructure();
            
        } catch (ParsingException e){
            e.setLocator(locator);
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_FATAL);
        } catch (Exception ee) {
            //ee.printStackTrace();
            System.out.println("[Error] During encoding of " + typeEncoder.getTypeDefinition().getName());
            //typeEncoder.getTypeDefinition().print();
            throw new HandlerException(ee,ee.getMessage(),HandlerException.TYPE_FATAL);
        }
    }

    /**
     * get character
     */             
    public void characters(char[] ch, int start, int length) throws HandlerException {
        try{
            if (DEBUG) System.out.println("ch: "+new String(ch,start,length));
            //System.out.println("ingnoreWhiteSpace = " + typeEncoder.getTypeDefinition().ignoreWhiteSpace());
            if (typeEncoder.getTypeDefinition().ignoreWhiteSpace()){
                //System.out.println("removewhitespaceof #"+new String(ch,start,length)+"#=#"+removeWhiteSpace(ch,start,length)+"#");
                typeEncoder.setValue(removeWhiteSpace(ch,start,length));
            }
            else
                typeEncoder.setValue(new String(ch,start,length));
        } catch (ParsingException e){
            e.setLocator(locator);
            e.printStackTrace();
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_FATAL);
        }
    }

    public boolean hasPoolHoldByHandlerManager(){return false;}

    /**
     * the object created
     */
    public Object getCreation() throws HandlerException {return typeEncoder;}

    // @@OPTIM : pas de création d'un deuxième tableau
    private String removeWhiteSpace(char[] ch, int start, int length){
        char[] ch2 = new char[length];
        int i=start;
        int j=0;
        while (i<start+length){
            if (ch[i]!=' ' && ch[i]!='\n' && ch[i]!='\t'){
                ch2[j] = ch[i];
                j++;
            }
            i++;
        }
        return new String(ch2,0,j); 
    }
   
}

class AttributeWrapper {
    AttributeWrapper(String r,String v){rawName=r;value=v;}
    String rawName = null;
    String value = null;
}
