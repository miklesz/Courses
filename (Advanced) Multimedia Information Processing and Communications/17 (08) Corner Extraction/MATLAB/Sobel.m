I = imread('pilka.png'); %przyk�adowy obraz w odcieniach szaro�ci
[I2,I3]=imgradient(I, 'Sobel'); 
imshowpair(I,I2,'montage'); %Nat�enie kraw�dzi
figure(2);
imshowpair(I,I3,'montage'); %Kierunkowo�� kraw�dzi