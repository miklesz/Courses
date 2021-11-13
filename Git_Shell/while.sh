#!/bin/sh
INPUT_STRING=cześć
while [ "$INPUT_STRING" != "żegnaj" ]
do
  echo "Wpisz coś (żegnaj, żeby wyjść)"
  read INPUT_STRING
  echo "Wpisano: $INPUT_STRING"
done
