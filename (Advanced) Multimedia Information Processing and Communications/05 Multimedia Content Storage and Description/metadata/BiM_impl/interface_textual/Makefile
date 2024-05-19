SSHELL=/bin/sh
###############################################################################
##
## This software module was originally developed by
##
## Munich University of Technology, Stephan Herrmann
## (contributing organizations names)
##
## in the course of develpment of the MPEG-7 Experimentation Model.
##
## This software module is an implementation of a part of one or more MPEG-7
## Experimentation Model tools as specified by the MPEG-7 Requirements.
##
## ISO#IEC gives users of MPEG-7 free license to this software module or
## modifications thereof for use in hardware or software products claiming
## conformance to MPEG-7.
##
## Those intending to use this software module in hardware or software products
## are advised that its use may infringe existing patents. The original
## developer of this software module and his#her company, the subsequent
## editors and their companies, and ISO#IEC have no liability for use of this
## software module or modifications thereof in an implementation.
##
## Copyright is not released for non MPEG-7 conforming products. The
## organizations named above retain full right to use the code for their own
## purpose, assign or donate the code to a third party and inhibit
## third parties the from using the code for non MPEG-7 conforming products.
##
## Copyright (c) 1998-1999.
##
## This notice must be included in all copies or derivative works.
##
## Makefile
###############################################################################

DEBUG = y

## SWITCH SPECIFIC DEFINES ###################################################
ifeq ($(DEBUG),y)
	CDEFS += -DDEBUG
	CFLAGS += -g -Wall
endif

### XML ######################################################################
XML=../../../../xml
lXML= -lxerces-c1_6_0
IXML= -I$(XML)/include
LXML = -L$(XML)/lib $(lXML)
DXML =

### navigation path ##########################################################
NP=../navigation_path
lNP= -lnavigation_path
INP = -I$(NP)/include
LNP = -L$(NP)/lib $(lNP)
DNP =

EXTRA_INCLUDE_DIR += $(IXML) $(INP)
EXTRA_LIBS += $(LXML) $(LNP)
EXTRA_CDEFS += $(DXML) $(DNP)
EXTRA_CFLAGS +=

## COMMANDS ##################################################################
CCC=g++
LINK = g++
INDENT = indent -br -i2 -l80 -lc80
ifeq ($(CHECKER),PURIFY)
	LINK = purify  -best-effort -g++ $(LINK)
##	LINK = purify -suppress-filenames="libX11.so.4" $(CCC)
##	CHECKCMD = purify $(CCC)
endif
ifeq ($(CHECKER),INSURE)
	CC = insure
	CCC = insure
	LINK = insure
endif

AR = ar -rs

INSTALL = /bin/cp
RM = /bin/rm -f
CHMOD = chmod
UMASK= 644
TAR = gtar
CVS = cvs -d :pserver:cvs@cvshost.krdl.org.sg:/home/mpeg7-xm
MAKEDEPEND = makedepend

### Generate Flags #########################################################
CFLAGS += $(CDEFS) $(EXTRA_INCLUDE_DIR) $(EXTRA_CFLAGS) $(EXTRA_CDEFS)
CCFLAGS = $(CFLAGS)
LFLAGS += $(EXTRA_LIBS)

## DEPENDENCIES ############################################################

IFOBJS=interface_textual.o namespaces.o

interface_textual: $(IFOBJS) $(NP)/lib/libnavigation_path.so
	$(LINK) -o interface_textual $(IFOBJS) $(LFLAGS)
#	$(INSTALL) interface_textual ../../../

../../lib/libnavigation_path.so:
	cd $(NP); make

clean:
	$(RM) interface_textual
	$(RM) *.o
	cd $(NP); make clean

.cpp.o:
	$(CCC) $(CCFLAGS) -c -o $@ $<
