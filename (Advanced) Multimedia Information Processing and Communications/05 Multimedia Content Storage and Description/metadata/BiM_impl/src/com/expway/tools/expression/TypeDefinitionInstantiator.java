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

package com.expway.tools.expression;

import java.io.*;
import java.util.*;
import com.expway.tools.automata.*;
import com.expway.util.URIRegistry;

public class TypeDefinitionInstantiator {
    static private boolean DEBUG = false;

    // Des constantes 

    static final public int UNKNOWN   = -1;
    static final public int CHOICE    = 1;
    static final public int ALL       = 2;
    static final public int SEQ       = 3;
    static final public int ATTR      = 4;
    static final public int ANY       = 5;
    static final public int XSITYPE   = 6;
    static final public int ABSTRACT  = 7;
    static final public int SUPERTYPEOF = 8;
    static final public int EXTENSION   = 9;
    static final public int RESTRICTION = 10;
    static final public int MIXED = 11;

    static final public String[] groupNames = {"NONE","CHOICE","ALL","SEQ","ATTR","ANY","XSITYPE","ABSTRACT","SUPERTYPEOF","EXTENSION","RESTRICTION","MIXED"};

    // Des variables

    SetOfDefinitions setOfDefinitions = null;

    // Constructeur

    //      public TypeDefinitionInstantiator(){
    //          setOfDefinitions=null;
    //      }

    public TypeDefinitionInstantiator(SetOfDefinitions s){
        setOfDefinitions=s;
    }
    
    // Support des namespaces
    String targetNamespace=null;
    URIRegistry localURIRegistry  = null;

	
    private GroupNode                     theCurrentGroup        = null;

    private ElementDefinition             theCurrentElement            = null;

    private ComplexComplexTypeDefinition  theCurrentComplexType        = null;
    private SimpleComplexTypeDefinition   theCurrentSimpleComplexType  = null;
    private SimpleTypeDefinition          theCurrentSimpleType         = null;

    private boolean inSimpleType         = false; // simple or simplecomplex
    private boolean inFacetDefinition    = false; 
    private boolean inXsiTypeDefinition  = false;
    private boolean inSuperTypeOfDefinition  = false;
    private boolean inExtension   = false;
    private boolean inRestriction = false;
    private boolean inNamespacesDefinition = false;
    private boolean inPropertiesDefinition = false;
    private boolean inAbstractDefinition = false; 
    private boolean inMixedDefinition = false; 
    private String  theCurrentFacetName  = null;

    //

    final public void clearContext(){
        theCurrentElement = null;
        theCurrentSimpleType = null;
        theCurrentSimpleComplexType = null;
        theCurrentComplexType = null;
        theCurrentGroup = null;
        inFacetDefinition = false;
        inSimpleType = false;
    }

    final public boolean hasContext(){
        return 
            !(theCurrentElement == null && 
              theCurrentSimpleType == null && theCurrentComplexType == null && 
              theCurrentGroup == null && inFacetDefinition == false && inNamespacesDefinition==false &&
              inSimpleType == false && inPropertiesDefinition==false);
    }

    final public void printContext(){
        System.out.println("  parsing context :");
        System.out.println("    inSimple                     = " + inSimpleType);
        System.out.println("    inFacetDefinition            = " + inFacetDefinition);
        System.out.println("    theCurrentGroup              = " + theCurrentGroup);
        System.out.println("    theCurrentElement            = " + theCurrentElement);
        System.out.println("    theCurrentComplexType        = " + theCurrentComplexType);
        System.out.println("    theCurrentSimpleType         = " + theCurrentSimpleType);
        System.out.println("    theCurrentSimpleComplexType  = " + theCurrentSimpleComplexType);
        System.out.println("    inSuperTypeOfDefinition      = " + inSuperTypeOfDefinition);
        System.out.println("      inExtension                = " + inExtension);
        System.out.println("      inRestriction              = " + inRestriction);
        System.out.println("");
    }

    //

    final public void switchParserToWordMode(StreamTokenizer st){
        st.resetSyntax();
        st.wordChars((int)'a',(int)'z');
        st.wordChars((int)'A',(int)'Z');
        st.wordChars((int)'1',(int)'9');
        st.wordChars((int)'0',(int)'0');
        st.wordChars((int)'&',(int)'&');
        st.wordChars((int)':',(int)':');
        st.wordChars((int)'_',(int)'_');
        st.wordChars((int)'.',(int)'.');
        st.wordChars((int)'-',(int)'-');
        st.wordChars((int)'/',(int)'/');
        st.wordChars((int)'~',(int)'~');

        // Séparateur namespace/nom pour les RWSnames
        st.wordChars((int)'"',(int)'"');
    }

    final public void switchParserToNumberMode(StreamTokenizer st){
        st.parseNumbers();
    }

    final public void parseRawSchema(File f) throws IOException {
        FileReader fr=new FileReader(f);        
        parseRawSchema(fr);
        fr.close();
    }
    
    final public void parseRawSchema(Reader r) throws IOException {
        StreamTokenizer st = new StreamTokenizer(r);

        clearContext();
        switchParserToWordMode(st);

        st.nextToken();

        while(st.ttype!=StreamTokenizer.TT_EOF){
            
            if (st.ttype == StreamTokenizer.TT_WORD)
                {
                    String token = st.sval;
   
                    if (hasContext()) // nous ne sommes pas dans une definition 
                        {
                            // le contexte courant = definition de facette
                            if (inFacetDefinition) 
                                {
                                                                    
                                    newFacetValue(transcodeToken(token));
                                } 
                            else if (inXsiTypeDefinition)
                                {
                                    newXsiTypeValue(transcodeToken(token));
                                }
                            else if (inSuperTypeOfDefinition) {
                                newSuperTypeOfValue(transcodeToken(token));
                            }
                            else if (inNamespacesDefinition) {
                                // Pas de transcode token ici !
                                // Les URI débutent et terminent toutes par "
                                newNamespace(token);
                            } else if (inPropertiesDefinition) {
                                newProperty(token);                               
                            } else {
                                    if (DEBUG) System.out.println(" " + st.nval + " " + st.sval + " " + (char)st.ttype);
                                    
                                    // recherche du min et du max s'il sont la
                                    // les occurrences sous la forme [xx,yy] avec * pour infini
                                    Occurrence occ = new Occurrence();
                                    parseOccurrence(st,occ);
                                    
                                    // recherche du content Model
                                    String contentModel = null;
                                    //System.out.println("          Detection contentmodel ??");
                                    st.nextToken();
                                    if (st.ttype != StreamTokenizer.TT_WORD && (char)st.ttype == '{') // il s'agit d'un content model
                                        {
                                            
                                            st.nextToken(); // le contentmodel
                                            contentModel = st.sval; 
                                            //System.out.println("          Content Model Detecte = "+contentModel);
                                            st.nextToken(); // le crochet fermant
                                        }
                                    else
                                        st.pushBack(); // le prochain nextToken donnera la suite
                                    
                                    addItemToCurrentGroup(transcodeToken(token),transcodeToken(contentModel),occ.min,occ.max);
                                }
                        }
                    else // pas de contexte mais un mot quand meme = definition d'un element
                        {
                            // token = nom de l'élément
                            String contentModel = null;
                            //System.out.println("          Detection contentmodel ??");
                            st.nextToken();        // le {
                            st.nextToken();        // le contentmodel
                            contentModel = st.sval; 
                            st.nextToken();        // le crochet fermant
                            newElement(transcodeToken(token),transcodeToken(contentModel));
                        }
                }
            else 
                {
                    //System.out.println("TTYPE = " + (char)st.ttype);
                    if ((char)st.ttype == '(') // ouverture d'un nouveau groupe
                        {
                            st.nextToken();

                            if (st.sval.equals("TARGETNAMESPACE")) {
                                startNamespacesDefinition();
                            } else if (st.sval.equals("PROPERTIES")) {
                                startPropertiesDefinition();
                            }
                            else {
                                
                                if (!inSimpleType) // on est dans un complex, par défaut
                                    {
                                        int grouptype = -1;
                                   
                                        if (st.sval.equals("CHOICE"))
                                            grouptype = CHOICE;
                                        else if (st.sval.equals("ALL"))
                                            grouptype = ALL;
                                        else if (st.sval.equals("SEQ"))
                                            grouptype = SEQ;
                                        else if (st.sval.equals("ATTR"))
                                            grouptype = ATTR;
                                        else if (st.sval.equals("ANY"))
                                            grouptype = ANY;
                                        else if (st.sval.equals("XSITYPE"))
                                            grouptype = XSITYPE;
                                        else if (st.sval.equals("ABSTRACT"))
                                            grouptype = ABSTRACT;
                                        else if (st.sval.equals("MIXED"))
                                            grouptype = MIXED;
                                        else if (st.sval.equals("EXTENSION"))
                                            grouptype = EXTENSION;
                                        else if (st.sval.equals("RESTRICTION"))
                                            grouptype = RESTRICTION;

                                        // les occurrences sous la forme [xx,yy] avec * pour infini
                                        Occurrence occ = new Occurrence();
                                        parseOccurrence(st,occ);

                                        newGroup(grouptype,occ.min,occ.max);
                                    }
                                else // inSimpleType==true 
                                    {
                                        // ajout d'une facette ou bien d'attributs

                                        int grouptype = -1;
                                        int min=1,max=1;
                                    
                                        if (st.sval.equals("ATTR"))
                                            grouptype = ATTR;
                                        else if (st.sval.equals("XSITYPE"))
                                            grouptype = XSITYPE;
                                        //else if (st.sval.equals("ABSTRACT"))
                                        //    grouptype = ABSTRACT;
                                        else if (st.sval.equals("EXTENSION"))
                                            grouptype = EXTENSION;
                                        else if (st.sval.equals("RESTRICTION"))
                                            grouptype = RESTRICTION;
                                        
                                        if (grouptype == -1) // si c'est rien de connu c'est une facette
                                            newFacet(transcodeToken(st.sval),st);
                                        else
                                            newGroup(grouptype,min,max);
                                    }
                            }
                        }
                    else if ((char)st.ttype == ')') // fermeture
                        {
                            if (inFacetDefinition)
                                endCurrentFacet(st);
                            else if (inXsiTypeDefinition)
                                endCurrentXsiType();
                            else if (inSuperTypeOfDefinition)
                                endCurrentSuperTypeOfDefinition();
                            else if (inAbstractDefinition)
                                endCurrentAbstractDefinition();
                            else if (inMixedDefinition)
                                endCurrentMixedDefinition();
                            else if (inNamespacesDefinition)
                                endNamespacesDefinition();
                            else if (inPropertiesDefinition)
                                endPropertiesDefinition();
                            else 
                                endCurrentGroup();
                        }
                    else if ((char)st.ttype == '|') // fermeture
                        {
                            endDefinition();
                        }
                    else if ((char)st.ttype == '{') // ouverture complex
                        {
                            st.nextToken();
                            String s = st.sval;
                            newContentModel(transcodeToken(s)); 
                            st.nextToken();
                        }
                    else if ((char)st.ttype == '<') // ouverture simple
                        {
                            st.nextToken();
                            String s = st.sval;
                            //System.out.println("                " + st.nval + " " + st.sval + " " + (char)st.ttype);
                            st.nextToken();// le >
                            //System.out.println("                " + st.nval + " " + st.sval + " " + (char)st.ttype);
                            st.nextToken();
                            //System.out.println("                " + st.nval + " " + st.sval + " " + (char)st.ttype);
                            String ss = st.sval;
                            
                            newDataType(transcodeToken(s),transcodeToken(ss));
                        }
                    else if ((char)st.ttype == '[') // ouverture simple complex
                        {
                            st.nextToken();
                            String s = st.sval;
                            st.nextToken();// le >
                            st.nextToken();
                            String ss = st.sval;
                            newSimpleComplexType(transcodeToken(s),transcodeToken(ss)); 
                        }
        }
            st.nextToken();
        }
    }

    final private void parseOccurrence(StreamTokenizer st,Occurrence o) throws IOException {
        st.nextToken(); 
        if (st.ttype != StreamTokenizer.TT_WORD && (char)st.ttype == '[') // il s'agit d'une occurence
            {
                switchParserToNumberMode(st);
                st.nextToken(); // le min
                o.min = (int)st.nval;
                st.nextToken(); // la virgule
                st.nextToken(); // le max
                if (st.ttype != StreamTokenizer.TT_NUMBER) // c'est pas un nombre => l'etoile 
                    o.max = OccurrenceNode.UNBOUNDED;
                else
                    o.max = (int)st.nval;
                //System.out.println("          occurence min=" + o.min + " max=" + o.max);
                st.nextToken(); // le crochet fermant
                switchParserToWordMode(st);
            }
        else 
            st.pushBack(); // le prochain nextToken donnera la suite
    }

    // ------------------------------------------------------------

    
    final private void newElement(String name,String type){
        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("New element definition : " + name + " of type " + type);
        if (DEBUG) printContext();
        clearContext();
        theCurrentElement = new ElementDefinition(name,type);
    }
    
    final private void newDataType(String name,String primitive){
        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("New simple type definition : " + name + " primitive : " + primitive);
        if (DEBUG) printContext();
        clearContext();
        theCurrentSimpleType = SimpleTypeDefinition.newSimpleTypeDefinition(name,primitive);
        inSimpleType = true;
    }

    final private void newSimpleComplexType(String name,String primitive ){
        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("New simple complex type definition : " + name + " primitive : " + primitive);
        clearContext();
        theCurrentSimpleComplexType = new SimpleComplexTypeDefinition(name,primitive);
        inSimpleType = true;
    }

    final private void newContentModel(String name){ 
        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("New complex type definition : " + name);
        if (DEBUG) printContext();
        clearContext();
        theCurrentComplexType = new ComplexComplexTypeDefinition(name);
    }

    final private void newFacet(String name,StreamTokenizer st){ 
        if (DEBUG) System.out.println("     new Facet " + name);
        if (DEBUG) printContext();
        theCurrentFacetName = name;

        // Dans le cas de la facette enumeration, les espace ne sont plus des separateurs
        if (theCurrentFacetName.equals("enumeration")) {         
            st.wordChars(' ',' ');
            st.whitespaceChars('\"','\"');
        }

        inFacetDefinition = true;
    }

    final private void newFacetValue(String value){ 
        if (DEBUG) System.out.println("          facet value " + theCurrentFacetName + " " + value);
        if (DEBUG) printContext();

        // En mode enumeration, on retire les guillemets entourant la value
        if (theCurrentFacetName.equals("enumeration")) {
            // caractères espace entre les "" dans le RWS des énumérations
            if (value.equals(" ")) return;
        }

        if (theCurrentSimpleType != null)
            theCurrentSimpleType.setFacetValue(theCurrentFacetName,value);
        else if (theCurrentSimpleComplexType != null)
            theCurrentSimpleComplexType.setFacetValue(theCurrentFacetName,value);
    }

    final private void newXsiTypeValue(String value){ 
        if (DEBUG) System.out.println("          xsitype value " + value);
        if (DEBUG) printContext();

        throw new RuntimeException("XSITYPE metacode is not supported anymore in RWS");
        
        /*
          if (theCurrentComplexType != null)
          theCurrentComplexType.addPossibleSubType(value);
          else if (theCurrentSimpleComplexType != null)
          theCurrentSimpleComplexType.addPossibleSubType(value);
        */
    }

    final private void newSuperTypeOfValue(String value){
        if (DEBUG) System.out.println("          supertypeof value " + value);
        if (setOfDefinitions!=null) {
            if (theCurrentComplexType != null){
                theCurrentComplexType.setSuperType(value,(short)(inExtension?TypeDefinition.EXTENSION:TypeDefinition.RESTRICTION));
            }
            else if (theCurrentSimpleComplexType != null){
                theCurrentSimpleComplexType.setSuperType(value,(short)(inExtension?TypeDefinition.EXTENSION:TypeDefinition.RESTRICTION));
            }
            else if (theCurrentSimpleType != null){
                theCurrentSimpleType.setSuperType(value,(short)(inExtension?TypeDefinition.EXTENSION:TypeDefinition.RESTRICTION));
            }
        } else {
            throw new RuntimeException("new SuperTypeOf Value et pas de SetOfDefinitions présent ?!");
        }
    }
    
    final private void newGroup(int type,int min, int max) {
        if (DEBUG) System.out.println("New group : t=" + groupNames[type] + " minOccurs=" + min + " maxOccurs="+max);
        if (DEBUG) printContext();

        GroupNode theNewGroup = null;
        if (type == SEQ)
            theNewGroup = new SEQNode();
        else if (type == CHOICE)
            theNewGroup = new CHOICENode();
        else if (type == ALL)
            theNewGroup = new ALLNode();
        else if (type == ATTR)
            theNewGroup = new ATTRNode();
        else if (type == ANY)
            theNewGroup = new ANYNode();

        if (theCurrentGroup == null){
            // cas particuliers
            if (type == ATTR){
                if (theCurrentComplexType!=null)
                    theCurrentComplexType.setDefinitionAttributesNode(new OccurrenceNode(theNewGroup,1,1));
                else if (theCurrentSimpleComplexType!=null)
                    theCurrentSimpleComplexType.setDefinitionAttributesNode(new OccurrenceNode(theNewGroup,1,1));
            }
            else if (type == XSITYPE){
                if (theCurrentComplexType != null || theCurrentSimpleComplexType != null)
                    inXsiTypeDefinition = true;
                else
                    throw new RuntimeException("Tentative XSITYPE hors de tout complexType");
            }
            else if (type == EXTENSION || type == RESTRICTION){
                if (theCurrentComplexType != null || theCurrentSimpleComplexType != null || theCurrentSimpleType != null){
                    inSuperTypeOfDefinition = true;
                    if (type == EXTENSION)
                        inExtension = true;
                    else if ( type == RESTRICTION) 
                        inRestriction = true;
                    else
                        throw new RuntimeException("Tentative SUPERTYPEOF hors de tout complexType");                
                } else
                    throw new RuntimeException("Tentative SUPERTYPEOF sans EXTENSION ou RESTRICTION");                
            }
            else if (type == ABSTRACT){
                if (theCurrentComplexType != null){
                    theCurrentComplexType.setAbstract(true);
                    inAbstractDefinition = true;
                }
                else if (theCurrentSimpleComplexType != null){
                    theCurrentSimpleComplexType.setAbstract(true);
                    inAbstractDefinition= true;
                }
                else
                    throw new RuntimeException("Tentative ABSTRACT hors de tout complexType");
            }
            else if (type == MIXED) {
                if (theCurrentComplexType != null && theCurrentComplexType instanceof ComplexComplexTypeDefinition){
                    ((ComplexComplexTypeDefinition)theCurrentComplexType).setMixed(true);
                    inMixedDefinition = true;
                }
                else throw new RuntimeException("Tentative MIXED hors de tout ComplexComplexType");
            }
            else
                theCurrentComplexType.setDefinitionContentModelNode(new OccurrenceNode(theNewGroup,min,max));
        }
        else 
            theCurrentGroup.addChild(new OccurrenceNode(theNewGroup,min,max));

        theCurrentGroup = theNewGroup;
    }

    final private void addItemToCurrentGroup(String token, String contentModel, int min, int max){
        if (DEBUG) System.out.println("Add item to current group: t=" + token + "{" + contentModel + "}" 
                                      + " min=" + min + " max="+max);
        if (DEBUG) printContext();
        theCurrentGroup.addChild(new OccurrenceNode(new ItemNode(token,contentModel),min,max));
    }
    
    final private void endCurrentGroup(){
        if (DEBUG) System.out.println("End current group");
        if (DEBUG) printContext();
        // l'occurrence node et le group node (2 parents)
        OccurrenceNode on = (OccurrenceNode)theCurrentGroup.getParent();
        theCurrentGroup = (GroupNode)on.getParent();
    }

    final private void endCurrentFacet(StreamTokenizer st){
        if (DEBUG) System.out.println("End current facet");
        if (DEBUG) printContext();

        // Dans le cas de la facette enumeration, les espace redeviennent comme avant
        if (theCurrentFacetName.equals("enumeration")) {
            switchParserToWordMode(st);
            //st.whitespaceChars(' ',' ');
            //st.wordChars('\"','\"');
        }

        theCurrentFacetName = null;
        inFacetDefinition = false;
    }

    final private void endCurrentXsiType(){
        if (DEBUG) System.out.println("End XsiType definition");
        if (DEBUG) printContext();
        inXsiTypeDefinition = false;
    }

    final private void endCurrentSuperTypeOfDefinition(){
         if (DEBUG) System.out.println("End SuperTypeOf definition");
        inSuperTypeOfDefinition = false;
        inExtension = false;
        inRestriction = false;
    }

    final private void endCurrentAbstractDefinition(){
        if (DEBUG) System.out.println("End abstract definition");
        if (DEBUG) printContext();
        inAbstractDefinition = false;
    }
   
    final private void endCurrentMixedDefinition(){
        if (DEBUG) System.out.println("End mixed definition");
        if (DEBUG) printContext();
        inMixedDefinition = false;
    }
   
    final private void endDefinition(){
        if (DEBUG) System.out.println("End Definition: ");
        if (DEBUG) printContext();

        if (theCurrentComplexType != null)
            setOfDefinitions.putTypeDefinition(theCurrentComplexType);
        else if (theCurrentSimpleType != null)
            setOfDefinitions.putTypeDefinition(theCurrentSimpleType);
        else if (theCurrentSimpleComplexType != null)
            setOfDefinitions.putTypeDefinition(theCurrentSimpleComplexType);
        else if (theCurrentElement != null)
            setOfDefinitions.putElementDefinition(theCurrentElement);
        
        clearContext();
    }

    // Support des namespaces
    final private void startNamespacesDefinition() {
        if (DEBUG) System.out.println("Start Namespaces definition");
        inNamespacesDefinition = true;
        localURIRegistry=new URIRegistry();
    }

    final private void newNamespace(String uri) {
        // On retire les guillemets initial et final
        uri=uri.substring(1,uri.length()-1);
        if (DEBUG) System.out.println("new uri: "+uri);      
        
        // On ajoute la nouvelle URI dans les registres locaux et globaux
        localURIRegistry.unknownRegisterURI(uri);
        setOfDefinitions.unknownRegisterURI(uri); 
    }

    final private void endNamespacesDefinition() {
         if (DEBUG) System.out.println("end Namespaces definition");
         inNamespacesDefinition=false;
    }

    // Properties
    final private void startPropertiesDefinition() {
        if (DEBUG) System.out.println("Start Properties definition");
        inPropertiesDefinition = true;
    }

    final private void newProperty(String tok) {
        try {            
            // La première propriété que l'on lit est l'index du targetNamespace
            if (targetNamespace==null) {
                Integer iIndex=new Integer(tok);
                int index=iIndex.intValue();
                targetNamespace=localURIRegistry.getURI(index);
            }

            TypeDefinitions tds=setOfDefinitions.getDefinitions(targetNamespace);

            if (tok.equals("QE")) tds.setElementFormQualified(true);
            else if (tok.equals("UQE")) tds.setElementFormQualified(false);
            else if (tok.equals("QA")) tds.setAttributeFormQualified(true);
            else if (tok.equals("UQA")) tds.setAttributeFormQualified(false);
        } catch(Exception e) {
            System.out.println("[Warning] Exception "+e+" occured when parsing RWS properties");            
        }
    }

    final private void endPropertiesDefinition() {
         if (DEBUG) System.out.println("end Properties definition");
         inPropertiesDefinition=false;
    }


    // =========================================================================

    // Convertit les tokens RWS en CompactNames
    final private String transcodeToken(String token) {
        //System.out.println("tok:"+token);
        
        // Pas de namespaces ?
        if (localURIRegistry==null) {
            System.out.println("[Warning] No namespace has been defined for this RWS file");
            return token;
        }

        try {    
        // Cherche le dernier "
        int iLastColon=token.lastIndexOf('"');
        
        // Ce ne doit pas être un QName
        if (iLastColon==-1) return token;

        String sIndexPart=token.substring(0,iLastColon);
        String sElementPart=token.substring(iLastColon+1);
        
        Integer iIndex=new Integer(sIndexPart);
        int indexLocal=iIndex.intValue();

        String uri=localURIRegistry.getURI(indexLocal);
        String ret=setOfDefinitions.getURIRegistry().getCompactName(uri,sElementPart);

        //System.out.println("transcodeToken of " + token + " = " + ret );
        
        return ret;
       
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("invalid token:"+token+" exception:"+e);
        }
    }
    
    
   
    // ------------------------------------------------------------

    class Occurrence {
        int min = 1;
        int max = 1;
    }  
}

