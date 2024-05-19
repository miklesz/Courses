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
lXML= -lxerces-c1
IXML= -I$(XML)/include
LXML = -L$(XML)/lib $(lXML)
DXML =

EXTRA_INCLUDE_DIR += -Iinclude $(IXML)
EXTRA_LIBS += 
EXTRA_CDEFS += 
EXTRA_CFLAGS += -fPIC

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

AR = gcc -shared -o

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

NPOBJS=alphabetical_order.o bitstream.o build_TBCs.o codec_multi.o \
	decoder.o encoder.o namespaces.o navigation_path.o \
	node_list.o parse_file.o path_operations.o polymorphism.o \
	position_code.o SubstGrp.o textual_path.o textual_path_list.o

lib/libnavigation_path.so: $(NPOBJS)
	$(AR) lib/libnavigation_path.so $(NPOBJS)
	$(INSTALL) lib/libnavigation_path.so ../../../lib

clean:
	$(RM) lib/libnavigation_path.so
	$(RM) ../../../lib/libnavigation_path.so
	$(RM) *.o

.cpp.o:
	$(CCC) $(CCFLAGS) -c -o $@ $<
