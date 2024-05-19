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

package com.expway.tools.compression;

import java.io.*;
import java.util.TreeSet;

import com.expway.tools.automata.*;
import com.expway.tools.expression.*;
import com.expway.tools.compression.*;
import com.expway.tools.io.Chunk;
import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.utils.*;

import com.expway.binarisation.CodingParameters;
import com.expway.binarisation.CodingContext;

import com.expway.util.URIRegistry;

// ============================================================
// COMPLEX TYPE

abstract public class ComplexTypeInstance extends TypeInstance  {

    CompressionFiniteStateAutomata myAttributeFSA = null;
    DecompressionFiniteStateAutomata myAttributeDecodingFSA = null;
    CodingContext codingContext=null;
 
    // TODO Accesseur
    private boolean attributeFinished = false;
    public boolean isAttributeCodingFinished(){return attributeFinished;}
    public void setAttributeCodingFinished(){ attributeFinished = true;}

    private boolean attributeDecodingFinished = false;
    public boolean isAttributeDecodingFinished(){return attributeDecodingFinished;}
    public void setAttributeDecodingFinished(){ attributeDecodingFinished = true;}
    protected void withdrawSetAttributeDecodingFinished(){ attributeDecodingFinished = false;}

    private boolean bHasBeenSkipped = false;

    public void setCodingContext(CodingContext c) {
        codingContext=c;
    }

    public CodingContext getCodingContext() {
        return codingContext;
    }

    //

    public ComplexTypeInstance(TypeDefinition td){
        super(td); 
    }

    /**
     *  Give the coding of this instance, using the contextual information.
     *  If this coding should be not the top level coding chunk, the bIsNotTopLevelChunk should be true, false otherwise.
     */
    public Chunk getCodingWithContext(boolean bIsNotTopLevelChunk) {
        ChunkWriter cw=new ChunkWriter();
        ChunkWriter body=new ChunkWriter();
    
        if (isPartialInstantiated() && bIsNotTopLevelChunk) {  
            encodeSubTypeInto(CodingParameters.PARTIAL_INSTANTIATED_TYPE,body);
        } else {
            // Ecriture du corps normal
            if (getCoding()!=null) {
                getCoding().writeYourselfInto(body);
            }
        }

        if (codingContext!=null) {
            codingContext.setBodyForLengthValue(body);
            Chunk contextHeader=codingContext.writeContext();
            contextHeader.writeYourselfInto(cw);
        } else {
            System.out.println("codingcontext null ? type="+getTypeName()+" this="+this);
        }

        body.writeYourselfInto(cw);

        return cw;
    }

    // ============================================================
    // ENCODING

    public void startEncoding() throws ParsingException {
        super.startEncoding();

        // La compatibilité
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple() )
            { 
                // check if there is a subtyping, and if so if it is possible. Do not encode it, because of compatible coding.
                checkSubtypeInfo();
                
                try {
                    myAttributeFSA    = (CompressionFiniteStateAutomata) 
                        ((ComplexTypeDefinition)myDefinition).newCompatibleAttributesFSA(getCodingContext());
                } catch(DefinitionException de){
                    throw new ParsingException("Compatible Definition couldn't be generated");
                }

                if (ENCODEDEBUG) System.out.println("     attributeFSA = " + (myAttributeFSA!=null?"available":"none") + " for " + this);
                if (myAttributeFSA != null)
                    if (ENCODEDEBUG) System.out.println("        - CompatibleCoding chosen");

            } 
        else 
            {
                // check if there is a subtyping, and if so if it is possible and encode it !
                encodeTypeInfo();

                myAttributeFSA    = (CompressionFiniteStateAutomata)((ComplexTypeDefinition)myDefinition).newAttributesFSA();
                if (ENCODEDEBUG) System.out.println("     attributeFSA = " + (myAttributeFSA!=null?"available":"none") + " for " + this);
            }
        
        // Reset de l'automate

        if (myAttributeFSA!=null){
            myAttributeFSA.resetMode();
            myAttributeFSA.setApplicationContext(new CompressionContext());
            try {
                myAttributeFSA.reset(new CompressionActivityToken());
            }catch(AutomataException ae){
                throw new ParsingException("error during attributes automata initialization");
            }
        }
    }

    public TypeEncoder encodeAttribute(String name) throws ParsingException {
        
        if (ENCODEDEBUG) System.out.println("");
        if (ENCODEDEBUG) System.out.println("  - encodeAttribute " + name + " of " + this);

        if (isAttributeCodingFinished())
            throw new ParsingException("no more attributes expected");

        if (myAttributeFSA == null)
            throw new ParsingException("no attributes expected");
            
        try {
            myAttributeFSA.consume(name);
        }catch(AutomataException ae){
            //throw new ParsingException("Error during parsing : " + ae.getMessage());
            throw new ParsingException("Unexpected attribute "+name+" in "+this);
        }
        
        return ((CompressionContext)myAttributeFSA.getApplicationContext()).getCurrentTypeInstance();
    }
    
    public void endAttributeEncoding() throws ParsingException {
        if (ENCODEDEBUG) System.out.println("---> EndAttributeCoding of " + this);
        
        // verifier que tous les attributs attendus
        if (myAttributeFSA!=null){
            try{
                // generation du chunk
                CompressionActivityToken cat = (CompressionActivityToken)myAttributeFSA.end();
                if (ENCODEDEBUG) System.out.println("");
                if (ENCODEDEBUG) System.out.println("         Creation du chunk attributs = " + cat);

                if (ENCODEDEBUG) System.out.println("");

                if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple())
                    {
                        if (getCompressionActivityToken()==null)       
                            setCompressionActivityToken(cat);
                        else 
                            getCompressionActivityToken().append(cat);
                    } 
                else 
                    {
                        if (getCoding()==null)
                            setCoding(cat.generateChunk());
                        else // un type encoding a deja été réalisé
                            {
                                ChunkWriter result = new ChunkWriter();
                                Chunk c = cat.generateChunk();
                                getCoding().writeYourselfInto(result);
                                c.writeYourselfInto(result);
                                setCoding(result);
                            }
                        //System.out.println("bits of attributes structure of " + this + " " + cat.getBitsOfStructure());
                        //System.out.println("CTbos1 =" + getBitsOfStructure());
                        addBitsOfStructure(cat.getBitsOfStructure());
                        //System.out.println("CTbos2 =" + getBitsOfStructure());
                    }
                // liberation
                ((ComplexTypeDefinition)myDefinition).releaseAttributesFSA(myAttributeFSA);
                myAttributeFSA = null;
                
            }catch(AutomataException ae){
                throw new ParsingException("Error while coding attributes : " + ae.getMessage() + " in " + this);
            }
        }
        
        setAttributeCodingFinished();
    }
    

    // ------------------------------------------------------------
    // ENCODE TYPE

    private void checkSubtypeInfo() throws ParsingException {

        if (ENCODEDEBUG)  System.out.println("       Checking subtype " + xsiType);

        // Création du nom compact
        //String cName=getCodingContext().getURIRegistry().getCompactName(xsiType);
        String cName = xsiType;
        
        // on teste si le sous typage est valide
        // Si le xsi:type n'est pas égal au type lui-même
        if (cName!= null && !cName.equals(getTypeName())){
            if (myDefinition.isSuperTypeOf(cName)) {                             
                // On choisit le bon TypeDefinitions selon l'URI
                changeTypeDefinition(getCodingContext().getSetOfDefinitions().getTypeDefinition(cName));
            }           
            else                                 
                throw new ParsingException("Subtyping not authorized, " 
                                           + cName + " is not a subtype of " 
                                           + getTypeName() + " or is an abstract type");
        }        
    }

    private void encodeTypeInfo() throws ParsingException {

        if (ENCODEDEBUG)  System.out.println("       Encoding subtype " + xsiType);
        
        // Création du nom compact
        // String cName=getCodingContext().getURIRegistry().getCompactName(xsiType);
        String cName = xsiType;
   
        // On teste si le sous typage est valide dans le cas ou le xsi:type n'est pas égal au type lui-même
        if (cName!=null && !cName.equals(getTypeName())){
            if (!myDefinition.isSuperTypeOf(cName)) {                             
                throw new ParsingException("Subtyping not authorized, " 
                                           + cName + " is not a subtype of " 
                                           + getTypeName() + " or is an abstract type");
            }
        }
        
        // on encode le type sur la longueur voulue
        encodeSubType(cName);
        
        // On choisit le bon TypeDefinitions selon l'URI, et on change le type de ce complexTypeInstance
        if (cName != null && !cName.equals(getTypeName()))
            changeTypeDefinition(getCodingContext().getSetOfDefinitions().getTypeDefinition(cName));
    }

    private void encodeSubType(String s) {
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple() ){
            if (ENCODEDEBUG) System.out.println("subtype not encoded, will be encoded into compatible pieces");
        } else {
            if (getCoding()!=null)
                throw new RuntimeException("Encoding type alors qu'il y a deja quelque chose ????");
            ChunkWriter result = new ChunkWriter();
            encodeSubTypeInto(s,result);
            setCoding(result);
        }
    }

    /** 
     *  Compute the TypeCast chunk. The type s should be a subtype of this type or a 
     *  PARTIAL_INSTANTIATED_TYPE or null if the type is not subtyped.
     */
    public void encodeSubTypeInto(String s,ChunkWriter cw){
        //System.out.println("========================================");
        //System.out.println("encodeSubType " + s + " of " + getName());        
        // @@COMPAT ici on devrait gerer la notion de version
        int iNumberOfSubtypes;
        int iSubtypeIndex=-1;
        boolean typeCast=false;
        
        if (CodingParameters.bAllowsCompatibility) iNumberOfSubtypes = myDefinition.getNumberOfSubtypes(true);
        else iNumberOfSubtypes = myDefinition.getNumberOfSubtypes(false);

        if (CodingParameters.bAllowsPartialInstantiation) iNumberOfSubtypes++;

        if (s!=null) {
             if (CodingParameters.bAllowsCompatibility) iSubtypeIndex = myDefinition.getSubtypeIndex(s,true);
             else iSubtypeIndex = myDefinition.getSubtypeIndex(s,false);

            // Shift if the AllowsPartialInstantiation is activated to
            // make the index room 0 left for the PARTIAL_INSTANTIATED_TYPE
            if (CodingParameters.bAllowsPartialInstantiation) {
                iSubtypeIndex++;
                if (s.equals(CodingParameters.PARTIAL_INSTANTIATED_TYPE)) iSubtypeIndex=0; 
            }

            // If the name of the subtype is not the name of this type
            if (!s.equals(myDefinition.getName())) {
                typeCast=true;          
            }
        }
        
        if (iNumberOfSubtypes>0) {
            cw.writeBoolean(typeCast);
            if (typeCast) {
                if (iSubtypeIndex==-1) throw new RuntimeException("Cannot encode TypeCast chunk : subtype="+s+" this="+this);
                cw.writeInt(iSubtypeIndex,MethodsBag.getCodingLength(iNumberOfSubtypes));
            }
        }
    }

	
    private StringWriter myContent = null;

    private String xsiType = null;
    public void setXsiType(String s) {xsiType = s;}

    //

    public void startDecoding(BitToBitDataInputStream dis) throws DecodingException {
        super.startDecoding(dis);

        // est on en mode "compatibilité" ?
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) {

            myContent = new StringWriter(); // nothing to do : reinitCompatibleDecoding has been called by complexType Instance

            // @@COMPAT on repart comme si de rien n'etait : ne peut on bénéficier de la connaissance sur le type attendu ?
            myDefinition = null;
            // on attend une entete de compatibilité, qu'on lit et qui nous retourne le type
            TypeDefinition newTD = readCompatibilityHeader(dis);
            // on initialise les automates
            reinitCompatibleDecoding(dis,newTD);
            // on met à jour le type du complex type décodé
            changeTypeDefinition(newTD);

        } else {

            // Lecture du xsitype s'il y  lieu
            try { 
                if (CodingParameters.bAllowsSubtyping) decodeTypeInfo(dis); 
            }
            catch(IOException ioe) {
                throw new DecodingException(ioe.getMessage());
            }
        
            // Automata generation
            DecompressionFiniteStateAutomata fsa = null;
            fsa = (DecompressionFiniteStateAutomata) ((ComplexTypeDefinition)myDefinition).newDecompressionAttributesFSA();
            
            if (fsa!=null)
                {
                    myAttributeDecodingFSA = fsa;
                    myAttributeDecodingFSA.resetMode();
                    myAttributeDecodingFSA.setApplicationContext(new DecompressionContext(dis));
                    try {
                        myAttributeDecodingFSA.reset(new DecompressionActivityToken());
                    }catch(AutomataException ae){
                        throw new RuntimeException("Error during content model automata initialization");
                    }
                }
        }
        
        // Dans tous les cas on écrit le nom de l'élément
        // @@COMPAT en cas de substitution group, l'élément ne devra être affiché que plus tard
        if (!STUPIDPARSING){
            try {
                if (getElementName() != null) {
                    //System.out.println(" io> Write Start Element " + getElementName());
                    String elementName=getElementName();

                    getElementWriter().write("\n<"+elementName);

                    // Double xsi:type bug
                    // if (xsiType!=null)
                    //    getElementWriter().write(" xsi:type=\""+xsiType+"\"");

                }
            } catch(IOException ioe){
                throw new DecodingException(ioe.getMessage());
            }
        }

    }
    
    //

    public TypeEncoder decode(BitToBitDataInputStream dis) throws DecodingException {

        if (isAttributeDecodingFinished()) return null;

        if (myAttributeDecodingFSA == null) {
            // no attributes expected 
            setAttributeDecodingFinished();
            return null;
        }
        
        TypeEncoder te = null;
        while (te == null){
                try {
                    if (myAttributeDecodingFSA.consume()) {
                        // no more attributes
                        setAttributeDecodingFinished();
                        return null;
                    }
                
                    // get the attribute type encoder (simple type) returned by the automata
                    te = ((DecompressionContext)myAttributeDecodingFSA.getApplicationContext()).getCurrentTypeInstance();
                    // get the attribute name triggered by the automata
                    String attribute = ((DecompressionContext)myAttributeDecodingFSA.getApplicationContext()).getCurrentElementName();

                    if (te!=null) {
                        if (attribute!=null) {
                            TypeDefinitions tdsa=getCodingContext().getSetOfDefinitions().getDefinitionsFromCompactName(attribute);
                            if (!tdsa.isAttributeFormQualified()) attribute=URIRegistry.getWithoutPrefix(attribute);
                            ((SimpleTypeInstance)te).setAttributeName(attribute);
                        } else
                            ((SimpleTypeInstance)te).setAttributeName("UNKNOWN_ATTRIBUTE_NAME");

                        // attributes are written in myWriter
                        ((TypeInstance)te).setElementWriter(getElementWriter());
                    }

                } catch(AutomataException ae){
                    System.out.println("error : " + ae.getMessage());
                    ae.printStackTrace();
                    throw new DecodingException();
                }
            }
            return te;
    }
    
    //

    public void endDecoding() throws DecodingException {

        super.endDecoding();

        if (!STUPIDPARSING){
            try {
                if (!isEmpty()){
                    String elementName=getElementName();
                    
                    if (this instanceof SimpleComplexTypeInstance)
                        getElementWriter().write("</"+elementName+">");
                    else
                        getElementWriter().write("\n</"+elementName+">");
                }
                
            } catch(IOException ioe){
                throw new DecodingException(ioe.getMessage());
            }
        }
    }

    protected void outputEndAttributeCoding(){
        if (DECODEDEBUG) System.out.println("No more attributes for " + this + " : closing '>'");

        if (bTypeChanged && !STUPIDPARSING) {
            String elementName=myDefinition.getName();
            try{getElementWriter().write(" xsi:type=\""+elementName+"\"");}catch(IOException ioe){}
        }

        if (bHasBeenSkipped && !STUPIDPARSING)   
            try{getElementWriter().write(" cc:skipped=\"true\"");}catch(IOException ioe){}
        
        if (isEmpty()){
            if (!STUPIDPARSING)   try{getElementWriter().write("/>");}catch(IOException ioe){}
        } else {
            if (!STUPIDPARSING)   try{getElementWriter().write(">");}catch(IOException ioe){}
        }
    }

    protected void outputContent(){
        if (myContent != null)
            try { getElementWriter().write(myContent.getBuffer().toString()); } catch(IOException ioe){}
    }
    
    protected void write(String s) throws IOException {
        if (myContent != null) {
            myContent.write(s);
            System.out.println("write " + s + " of type " + this + " in " + myContent);
        } else {
            getElementWriter().write(s);
            System.out.println("write " + s + " of type " + this + " in " + getElementWriter());
        }
    }
    
    protected Writer getContentWriter(){
        if (myContent != null)
            return myContent;
        else
            return getElementWriter();
    }

    // ------------------------------------------------------------
    // DECODE TYPE

    // Decodage du champs type info, renvoie true si le type ici est non-instantié
    private void decodeTypeInfo(BitToBitDataInputStream dis) throws IOException {
        if (DECODEDEBUG) System.out.println("Type info decoding");
        
        // recupere le nom du sous type effectif
        String subtype = decodeSubType(dis);           
        // s'il y en a bien un
        if (subtype != null){
            // est ce une instantiation partielle
            if (subtype.equals(CodingParameters.PARTIAL_INSTANTIATED_TYPE)) {
                setPartialInstantiated(true);
            } else {
                // Change le type effectif de ce type decode (ce complex type)
                changeTypeDefinition(getCodingContext().getSetOfDefinitions().getTypeDefinition(subtype));
                setXsiType(subtype);
            }
        }
    }

    /**
     * Decode the TypeCast chunk
     */
    public String decodeSubType(BitToBitDataInputStream dis) throws IOException {
        int iNumberOfSubtypes;
        int iSubtypeIndex;
        boolean typeCast=false;
        
        if (CodingParameters.bAllowsCompatibility) {
            iNumberOfSubtypes = myDefinition.getNumberOfSubtypes(true);
        } else {
            iNumberOfSubtypes = myDefinition.getNumberOfSubtypes(false);
        }

        // Shift if the AllowsPartialInstantiation is activated to
        // make the index room 0 left for the PARTIAL_INSTANTIATED_TYPE
        if (CodingParameters.bAllowsPartialInstantiation) {
            iNumberOfSubtypes++;
        }

        if (iNumberOfSubtypes>0) {
            typeCast=dis.readBoolean();
        } else return null;

        // Type casting ?
        if (typeCast) {
            iSubtypeIndex=dis.readInt(MethodsBag.getCodingLength(iNumberOfSubtypes));
        }
        else return null;

        if (CodingParameters.bAllowsPartialInstantiation) {
            if (iSubtypeIndex==0) return CodingParameters.PARTIAL_INSTANTIATED_TYPE;
            else iSubtypeIndex--;
        }

        TypeDefinition subType;

        if (CodingParameters.bAllowsCompatibility) subType=myDefinition.getSubtype(iSubtypeIndex,true);
        else subType=myDefinition.getSubtype(iSubtypeIndex,false);

        if (subType==null) throw new RuntimeException("Cannot decode TypeCast chunk : iSubtypeIndex="+iSubtypeIndex+" this="+this);

        return subType.getName();
    }

    /** 
        Lit une entete de compatibilite
        @return null si le décodage a été skippé 
    */
    
    protected TypeDefinition readCompatibilityHeader(BitToBitDataInputStream dis) throws DecodingException  {
        try {
            if (ENCODEDEBUG) System.out.println(">>>>>>> Start Compatibility header reading");
            
            if (testTypeLength(dis)) return null; // no more PCH

            int type = -1;
            // Décodage de la version
            int version = dis.readInt(getCodingContext().getURIRegistry().getVersionDecodingSize());
            // si la version est inconnue on la saute
            if (!getCodingContext().getURIRegistry().knowsVersion(version)){
                skip(dis);
                if (DECODEDEBUG) System.out.println("  skip performed unknown schema");
                if (ENCODEDEBUG) System.out.println(">>>>>>>");  
                return null; // on a finit le decodage de ce complex type
            }
            // recupere l'URI de la version
            String versionURI = getCodingContext().getURIRegistry().getVersionURI(version);
            if (DECODEDEBUG) System.out.println("      Version read = " + versionURI);

			
            if (myDefinition != null) 
                {
                    type = dis.readInt(myDefinition.getSubtypeEncodingLength(versionURI,getCodingContext().getSetOfDefinitions()));
                    TypeDefinition tdf = myDefinition.getSubtypeDefinition(type,versionURI,getCodingContext().getSetOfDefinitions());
                    if (DECODEDEBUG) System.out.println("       Subtype of " + myDefinition + " found = " + tdf);
                    if (ENCODEDEBUG) System.out.println(">>>>>>>");  
                    return tdf;
                } 
            else // on a deja encodé un type, on va maintenant coder la différence  
                {
                    // on va chercher la table des definitions des sous-types possibles
                    TreeSet tds = getCodingContext().getSetOfDefinitions().getDefinitions(versionURI).typeDefinitionNames();
                    // on lit le type
                    type = dis.readInt(MethodsBag.getCodingLength(tds.size()));
                    // recupere la definition associée 
                    TypeDefinition tdf = getCodingContext().getSetOfDefinitions().getTypeDefinition((String)MethodsBag.getObjectAt(type,tds));
                    if (DECODEDEBUG) System.out.println("         Subtype of " + myDefinition + " found = " + tdf);
                    if (ENCODEDEBUG) System.out.println(">>>>>>>");  
                    return tdf;
                }
        } catch(IOException ioe){
            throw new DecodingException(ioe.getMessage());
        }
    }

    /**  reinitialise les automates en conséquence */
    protected void reinitCompatibleDecoding(BitToBitDataInputStream dis, TypeDefinition newType) throws DecodingException {

        try {
            DecompressionFiniteStateAutomata fsa = null;
            fsa = (DecompressionFiniteStateAutomata) 
                ((ComplexTypeDefinition)newType).newCompatibleDecompressionAttributesFSA(getCodingContext(),myDefinition);
        
            if (fsa!=null)
                {
                    myAttributeDecodingFSA = fsa;
                    myAttributeDecodingFSA.resetMode();
                    myAttributeDecodingFSA.setApplicationContext(new DecompressionContext(dis));
                    try {
                        myAttributeDecodingFSA.reset(new DecompressionActivityToken());
                    }catch(AutomataException ae){
                        throw new RuntimeException("Error during content model automata initialization");
                    }
                }
        } catch (DefinitionException de){
            de.printStackTrace();
            throw new DecodingException(de.getMessage());
        }
    }

    // ------------------------------------------------------------

    /** test if the type has been read in the stream (based on length information) */
    public boolean testTypeLength(BitToBitDataInputStream bis) throws DecodingException {
        int bitsToSkip=getBitsToSkip(bis);
        //System.out.println("BITSTOSKIP=="+bitsToSkip);
        return (bitsToSkip == 0);
    }
    
    public void skip(BitToBitDataInputStream bis) throws DecodingException {
        bHasBeenSkipped = true;
        int bitsToSkip = getBitsToSkip(bis);
        if (bitsToSkip < 0)
            throw new DecodingException("try to skip an element while it has been already decoded");
        
        try {                  
            bis.skip(bitsToSkip);
        } catch(IOException e) {
            throw new DecodingException("IOException while skipping :"+e);
        }
    }
    

    private int getBitsToSkip(BitToBitDataInputStream bis) throws DecodingException {
        if (codingContext!=null) {
            int startIndex=codingContext.getStartIndex();
            int instantaneousIndex=bis.getReadedBits();
            if (codingContext.isSkipping()) {
                int skipLength=codingContext.getLength();
                int skipBits=instantaneousIndex-startIndex;
                int bitsToSkip=skipLength-instantaneousIndex+startIndex;
                return bitsToSkip;
            } else throw new DecodingException("This context is not skippable !");
        } else throw new DecodingException("No context in this ComplexType ?");
    }

}



