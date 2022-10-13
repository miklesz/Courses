%clearing 
clc
clear all
close all   


%loading image and using radius filter 
A = imread('mountains.png');
radius = 1;
A1 = fspecial('disk', radius);
A2 = imfilter(A,A1,'replicate');
radius = 10;
A10 = fspecial('disk', radius);
A20 = imfilter(A,A10,'replicate');
subplot(131);imshow(A);title('original');
subplot(132);imshow(A2);title('radius=1');
subplot(133);imshow(A20);title('radius=10');



