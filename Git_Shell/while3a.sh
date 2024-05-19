#!/bin/sh
while read f
do
  case $f in
        hello)          echo angielski     ;;
        howdy)          echo amerykański   ;;
        gday)           echo australijski  ;;
        bonjour)        echo francuski     ;;
        "guten tag")    echo niemiecki     ;;
        *)              echo nieznany język: $f         ;;
   esac
done < myfile
