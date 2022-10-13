I = imread('lorem.jpg'); %przyk³adowy obraz w odcieniach szaroœci
progi=[0.12,0.18]; %procentowe ni¿szego i wy¿szego progu
I2=edge(I, 'Canny');        
%I2=edge(I, 'Canny',progi); %aktywuj, jeœli chcesz ustawiaæ w³asne progi
imshowpair(I,I2,'montage');