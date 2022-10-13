I = imread('pilka.png'); %przyk³adowy obraz w odcieniach szaroœci
[I2,I3]=imgradient(I, 'Sobel'); 
imshowpair(I,I2,'montage'); %Natê¿enie krawêdzi
figure(2);
imshowpair(I,I3,'montage'); %Kierunkowoœæ krawêdzi