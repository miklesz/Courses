%clearing 
clc
clear all
close all   

%loading image and using noise on image to show how filter works 

A = imread('meadow.png');
noise = imnoise(A,'salt & pepper',0.2);

% We need to use filter for each channel separately 
r = medfilt2(noise(:, :, 1), [3 3]);
g = medfilt2(noise(:, :, 2), [3 3]);
b = medfilt2(noise(:, :, 3), [3 3]);

% We need to reconstruct the image from separated channels
A1 = cat(3, r, g, b);

figure
subplot(131);imshow(A);title('original'); 
subplot(132);imshow(noise);title('with noise');
subplot(133);imshow(A1);title('after median filter');

