# Microsoft Developer Studio Project File - Name="navigation_path" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** NICHT BEARBEITEN **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=navigation_path - Win32 Debug
!MESSAGE Dies ist kein gültiges Makefile. Zum Erstellen dieses Projekts mit NMAKE
!MESSAGE verwenden Sie den Befehl "Makefile exportieren" und führen Sie den Befehl
!MESSAGE 
!MESSAGE NMAKE /f "navigation_path.mak".
!MESSAGE 
!MESSAGE Sie können beim Ausführen von NMAKE eine Konfiguration angeben
!MESSAGE durch Definieren des Makros CFG in der Befehlszeile. Zum Beispiel:
!MESSAGE 
!MESSAGE NMAKE /f "navigation_path.mak" CFG="navigation_path - Win32 Debug"
!MESSAGE 
!MESSAGE Für die Konfiguration stehen zur Auswahl:
!MESSAGE 
!MESSAGE "navigation_path - Win32 Release" (basierend auf  "Win32 (x86) Dynamic-Link Library")
!MESSAGE "navigation_path - Win32 Debug" (basierend auf  "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "navigation_path - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "NAVIGATION_PATH_EXPORTS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /I "../../../../xml/src" /I "../xml/src" /I "include" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "NAVIGATION_PATH_EXPORTS" /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x407 /d "NDEBUG"
# ADD RSC /l 0x407 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386
# ADD LINK32 xerces-c_1.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386 /out:"../../../lib/navigation_path.dll" /libpath:"..\..\..\..\xml\lib" /libpath:"..\xml\lib"

!ELSEIF  "$(CFG)" == "navigation_path - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "NAVIGATION_PATH_EXPORTS" /YX /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /I "../../../../xml/src" /I "../xml/src" /I "include" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "NAVIGATION_PATH_EXPORTS" /YX /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x407 /d "_DEBUG"
# ADD RSC /l 0x407 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 xerces-c_1.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /out:"../../../lib/navigation_path.dll" /pdbtype:sept /libpath:"..\..\..\..\xml\lib" /libpath:"..\xml\lib"

!ENDIF 

# Begin Target

# Name "navigation_path - Win32 Release"
# Name "navigation_path - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\alphabetical_order.cpp
# End Source File
# Begin Source File

SOURCE=.\bitstream.cpp
# End Source File
# Begin Source File

SOURCE=.\build_TBCs.cpp
# End Source File
# Begin Source File

SOURCE=.\codec_multi.cpp
# End Source File
# Begin Source File

SOURCE=.\decoder.cpp
# End Source File
# Begin Source File

SOURCE=.\encoder.cpp
# End Source File
# Begin Source File

SOURCE=.\namespaces.cpp
# End Source File
# Begin Source File

SOURCE=.\navigation_path.cpp
# End Source File
# Begin Source File

SOURCE=.\node_list.cpp
# End Source File
# Begin Source File

SOURCE=.\parse_file.cpp
# End Source File
# Begin Source File

SOURCE=.\path_operations.cpp
# End Source File
# Begin Source File

SOURCE=.\polymorphism.cpp
# End Source File
# Begin Source File

SOURCE=.\position_code.cpp
# End Source File
# Begin Source File

SOURCE=.\SubstGrp.cpp
# End Source File
# Begin Source File

SOURCE=.\textual_path.cpp
# End Source File
# Begin Source File

SOURCE=.\textual_path_list.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\include\alphabetical_order.h
# End Source File
# Begin Source File

SOURCE=.\include\bitstream.h
# End Source File
# Begin Source File

SOURCE=.\include\build_TBCs.h
# End Source File
# Begin Source File

SOURCE=.\include\codec.h
# End Source File
# Begin Source File

SOURCE=.\include\com_expway_util_Path.h
# End Source File
# Begin Source File

SOURCE=.\include\ErrorHandler.hpp
# End Source File
# Begin Source File

SOURCE=.\include\global_header.h
# End Source File
# Begin Source File

SOURCE=.\include\jni.h
# End Source File
# Begin Source File

SOURCE=.\include\jni_md.h
# End Source File
# Begin Source File

SOURCE=.\include\namespaces.h
# End Source File
# Begin Source File

SOURCE=.\include\navigation_path.h
# End Source File
# Begin Source File

SOURCE=.\include\node_list.h
# End Source File
# Begin Source File

SOURCE=.\include\parse_file.h
# End Source File
# Begin Source File

SOURCE=.\include\path_type.h
# End Source File
# Begin Source File

SOURCE=.\include\polymorphism.h
# End Source File
# Begin Source File

SOURCE=.\include\position_code.h
# End Source File
# Begin Source File

SOURCE=.\include\schema_type.h
# End Source File
# Begin Source File

SOURCE=.\include\SubstGrp.h
# End Source File
# Begin Source File

SOURCE=.\include\textual_path.h
# End Source File
# Begin Source File

SOURCE=.\include\textual_path_list.h
# End Source File
# Begin Source File

SOURCE=.\include\xml_element.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# End Group
# End Target
# End Project
