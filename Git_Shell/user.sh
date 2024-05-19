#!/bin/sh
echo "Jak masz na imię?"
read USER_NAME
echo "Witaj $USER_NAME"
echo "Utworzę plik o nazwie ${USER_NAME}_plik"
touch "${USER_NAME}_plik"
