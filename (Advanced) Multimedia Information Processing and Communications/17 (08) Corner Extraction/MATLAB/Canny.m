I = imread('lorem.jpg'); %przyk�adowy obraz w odcieniach szaro�ci
progi=[0.12,0.18]; %procentowe ni�szego i wy�szego progu
I2=edge(I, 'Canny');        
%I2=edge(I, 'Canny',progi); %aktywuj, je�li chcesz ustawia� w�asne progi
imshowpair(I,I2,'montage');