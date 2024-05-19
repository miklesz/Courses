package com.siemens;

import com.expway.tools.expression.SimpleTypeDefinition;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.io.ChunkWriter;
import java.io.IOException;
import java.io.Writer;

public class BasicTimePointDataType extends SimpleTypeDefinition {
    long facet=0;
    long represent;

    public BasicTimePointDataType(String name,String primitive,long repr){
        super(name,primitive);
        represent=repr;
    }

    public void internalSetFacetValue(String name,String value){}

    public void encodeItInto(String value, ChunkWriter cw){
        System.out.println("BTPDT : encoding value="+value);	
        String pattern=new String("");
        pattern="";
        if((represent&1024)>0)
            pattern+="\\-?";
		
        if ((represent&128)>0)
            {
                pattern+="(\\d+";
                if ((represent&256)>0)
                    {
                        pattern+="(\\-\\d{2}";
                        if ((represent&512)>0)
                            {
                                pattern+="(\\-\\d{2})?";
                            }
                        pattern+=")?";
                    }
                pattern+=")?";
            }
		
        if ((represent&4)>0)
            {
                pattern+="(T\\d{2}";
                if ((represent&8)>0)
                    {
                        pattern+="(:\\d{2}";
                        if ((represent&16)>0)
                            {
                                pattern+="(:\\d{2}";
                                if ((represent&32)>0)
                                    {
                                        pattern+="(:\\d+";
                                        if ((represent&64)>0)
                                            {
                                                pattern+="(\\.\\d{2})?";
                                            }
                                        pattern+=")?";
                                    }
                                pattern+=")?";
                            }
                        pattern+=")?";
                    }
                pattern+=")?";
            }
		
        if((represent&2)>0)
            pattern+="(F\\d+)?";
		
        if((represent&1)>0)
            pattern+="((\\-|\\+)\\d{2}:\\d{2})?";
			
        //Coding of Representation Flags
		
        int repFlags=0;
        int nrOfFlags=0;
        int strIndex=0;	
        int tz_esc=0;

        TimeStreamIO bufferStream=new TimeStreamIO(256);
        int fPos=0;
        int dfPos=0;
        int expr_lngth=value.length();
		
		
        if ((represent&1024)>0) 
            { 
                nrOfFlags++;
                if ((strIndex<expr_lngth) && value.charAt(strIndex)=='-')
                    {
                        repFlags+=1;
                        strIndex++;
                    }
            }
					
        if ((represent&128)>0)
            {
                nrOfFlags++;
                //ASCII numbers have to be checked
                int chk=strIndex;
                while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                    strIndex++;
                if(chk!=strIndex && ((strIndex>=expr_lngth) || (value.charAt(strIndex)!=':')))
                    {
                        repFlags<<=1;
                        repFlags++;
                        bufferStream.writeVLInt(value.substring(chk,strIndex));
				
                        if ((represent&256)>0)
                            {
                                nrOfFlags++;
                                if ((strIndex<expr_lngth) && value.charAt(strIndex)=='-')
                                    {
                                        strIndex++;
                                        chk=strIndex;
                                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                                            strIndex++;
                                        if ((strIndex>=expr_lngth) || (value.charAt(strIndex)!=':'))	
                                            {
                                                repFlags<<=1;
                                                repFlags++;
                                                bufferStream.writeRangeInt(value.substring(chk,strIndex),1,12);
							
                                                if ((represent&512)>0)
                                                    {
                                                        nrOfFlags++;
                                                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='-')
                                                            {
                                                                strIndex++;
                                                                chk=strIndex;
                                                                while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                                                                    strIndex++;
                                                                if ((strIndex>=expr_lngth) || (value.charAt(strIndex)!=':'))
                                                                    {		
                                                                        repFlags<<=1;
                                                                        repFlags++;
                                                                        bufferStream.writeRangeInt(value.substring(chk,strIndex),1,31);
										
                                                                    }
                                                                else
                                                                    {
                                                                        tz_esc=1;			
                                                                        strIndex=chk;
                                                                        repFlags<<=1;
                                                                    }
                                                            }
                                                        else
                                                            {
                                                                repFlags<<=1;
                                                            }
							
                                                    }
					
                                            }
                                        else
                                            {
                                                tz_esc=1;
                                                strIndex=chk;
                                                repFlags<<=1;
                                            }
                                    }
                                else
                                    repFlags<<=1;
                            }
                        else if (chk==strIndex)
                            {
                                repFlags<<=1;
                            }
                        else if (chk!=strIndex && (strIndex<expr_lngth) && value.charAt(strIndex)==':')
                            {
                                tz_esc=1;
                                strIndex=chk;
                                //first "-" is part of the time zone. This is corrected in the following two lines
                                repFlags--;
                                strIndex--;
				
                                repFlags<<=1;	
                            }
                    }
            }
			
    
			

		
        if ((represent&4)>0)
            {
                nrOfFlags++;
                if ((tz_esc==0) && (strIndex<expr_lngth) && value.charAt(strIndex)=='T')
                    {
                        repFlags<<=1;
                        repFlags++;
                        bufferStream.writeRangeInt(value.substring(strIndex+1,strIndex+3),0,23);
                        strIndex+=3;
                        if ((represent&8)>0)
                            {
                                nrOfFlags++;
                                if ((strIndex<expr_lngth) && value.charAt(strIndex)==':')
                                    {
                                        repFlags<<=1;
                                        repFlags++;
                                        bufferStream.writeRangeInt(value.substring(strIndex+1,strIndex+3),0,59);
                                        strIndex+=3;
                                        if ((represent&16)>0)
                                            {
                                                nrOfFlags++;
                                                if ((strIndex<expr_lngth) && value.charAt(strIndex)==':')
                                                    {
                                                        repFlags<<=1;
                                                        repFlags++;
                                                        bufferStream.writeRangeInt(value.substring(strIndex+1,strIndex+3),0,59);
                                                        strIndex+=3;
                                                        if ((represent&32)>0)
                                                            {
                                                                nrOfFlags++;
                                                                if ((strIndex<expr_lngth) && value.charAt(strIndex)==':')
                                                                    {
                                                                        repFlags<<=1;
                                                                        repFlags++;
                                                                        strIndex++;
                                                                        fPos=strIndex;
										
                                                                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                                                                            strIndex++;
                                                                        if ((represent&64)>0)
                                                                            {
                                                                                nrOfFlags++;
                                                                                if ((strIndex<expr_lngth) && value.charAt(strIndex)=='.')
                                                                                    {
                                                                                        repFlags<<=1;
                                                                                        repFlags++;
                                                                                        dfPos=strIndex+1;
												
                                                                                        strIndex+=3;						
                                                                                    }
                                                                                else
                                                                                    repFlags<<=1;
                                                                            }
                                                                    }
                                                                else
                                                                    repFlags<<=1;
                                                            }
                                                    }
                                                else
                                                    repFlags<<=1;
                                            }
                                    }
                                else
                                    repFlags<<=1;
                            }
                    }
                else
                    repFlags<<=1;
            }

        int chk;
		
        if((represent&2)>0)
            {
                nrOfFlags++;
                if ((tz_esc==0) && (strIndex<expr_lngth) && value.charAt(strIndex)=='F')
                    {
				
                        repFlags<<=1;
                        repFlags++;
                        strIndex++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        bufferStream.writeVLInt(value.substring(chk,strIndex));
                        if (fPos!=0)
                            {
                                int subStrIndex=fPos;
                                while(value.charAt(subStrIndex)>='0' && value.charAt(subStrIndex)<='9')
                                    subStrIndex++;
                                bufferStream.writeRangeInt(value.substring(fPos,subStrIndex),0,Integer.parseInt(value.substring(chk,strIndex))-1);
                            }
                        if(dfPos!=0)
                            bufferStream.writeRangeInt(value.substring(dfPos+1,dfPos+3),0,99);	
                    }
                else
                    repFlags<<=1;
            }
			
		
        if((represent&1)>0)
            {
                nrOfFlags++;
                //todo: flags have to be reset above!!!
                if ((tz_esc==1) | ((strIndex<expr_lngth) && value.charAt(strIndex)=='-') | ((strIndex<expr_lngth) && value.charAt(strIndex)=='+'))
                    {
                        repFlags<<=1;
                        repFlags++;

                        bufferStream.writeTimeZone(value.substring(strIndex));
	
                    }
                else
                    repFlags<<=1;
            }
		

			
        cw.writeInt(repFlags,nrOfFlags);
        bufferStream.writeToCW(cw);
    }

    public void decode(BitToBitDataInputStream bis, Writer w){
        // par defaut une string
        try {

            long repFlags=0;
            String timeExpr=new String("");
		
		
            if ((represent&1024)>0) 
                { 
                    repFlags+=(bis.readBoolean())?1024:0;
                }
					
            if ((represent&128)>0)
                {
                    repFlags+=(bis.readBoolean())?128:0;
                    if ((represent&256)>0 && (repFlags&128)>0)
                        {
                            repFlags+=(bis.readBoolean())?256:0;

                            if ((represent&512)>0 && (repFlags&256)>0)
                                {
                                    repFlags+=(bis.readBoolean())?512:0;
                                }
                        }
                }
		
		
		
		
            if ((represent&4)>0)
                {
                    repFlags+=(bis.readBoolean())?4:0;
                    if ((represent&8)>0 && (repFlags&4)>0)
                        {
                            repFlags+=(bis.readBoolean())?8:0;

                            if ((represent&16)>0 && (repFlags&8)>0)
                                {
                                    repFlags+=(bis.readBoolean())?16:0;

                                    if ((represent&32)>0 && (repFlags&16)>0)
                                        {
                                            repFlags+=(bis.readBoolean())?32:0;
                                            if ((represent&64)>0 && (repFlags&32)>0)
                                                {
                                                    repFlags+=(bis.readBoolean())?64:0;
                                                }
                                        }
                                }
                        }
                }


		
            if((represent&2)>0)
                {
                    repFlags+=(bis.readBoolean())?2:0;
                }
			
		
            if((represent&1)>0)
                {
                    repFlags+=(bis.readBoolean())?1:0;
                }		
		
		
		
            long intValue;
            long intFract;
		
            if ((repFlags&1024)>0) 
                { 
                    timeExpr+="-";
                }
					
            if ((repFlags&128)>0)
                {
                    intValue=bis.readInfiniteLong();
                    timeExpr+=intValue;
                    if ((repFlags&256)>0)
                        {
                            intValue=bis.readInt(4)+1;

                            timeExpr+="-"+((intValue<10)?"0":"")+intValue;			
                            if ((repFlags&512)>0)
                                {
                                    intValue=bis.readInt(5)+1;
                                    timeExpr+="-"+((intValue<10)?"0":"")+intValue;
                                }
                        }
                }
			

            if ((repFlags&4)>0)
                {
                    intValue=bis.readInt(5);
                    timeExpr+="T"+((intValue<10)?"0":"")+intValue;
                    if ((repFlags&8)>0)
                        {
                            intValue=bis.readInt(6);

                            timeExpr+=":"+((intValue<10)?"0":"")+intValue;			
                            if ((repFlags&16)>0)
                                {
                                    intValue=bis.readInt(6);
                                    timeExpr+=":"+((intValue<10)?"0":"")+intValue;
					
                                }
                        }
                }
		
            if ((repFlags&2)>0)
                {
                    intFract=bis.readInfiniteLong();
                    intValue=intFract;
                    int nrOfBits=0;
                    while (intValue!=0)
                        {
                            nrOfBits++;
                            intValue>>=1;
                        }
		
                    if ((repFlags&32)>0)
                        {
                            intValue=bis.readInt(nrOfBits);
                            timeExpr+=":"+intValue;
                            if ((repFlags&64)>0)
                                {
                                    intValue=bis.readInt(7);
                                    timeExpr+="."+((intValue<10)?"0":"")+intValue;
                                }
                        }
                    timeExpr+="F"+intFract;
                }
		
            if ((repFlags&1)>0)
                {
                    intValue=bis.readInt(5)-15;
                    if(intValue>=0)
                        timeExpr+="+"+((intValue<10)?"0":"")+intValue;
                    else
                        timeExpr+="-"+((intValue>-10)?"0":"")+(intValue*-1);
                    intValue=bis.readInt(2)*15;
                    timeExpr+=":"+intValue;	
                }
            w.write(timeExpr);

        } catch(IOException utf){
            throw new RuntimeException("IOERROR");
        }
    }
    
}
