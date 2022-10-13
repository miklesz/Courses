%clearing 
clc
clear all
close all   


%loading image and using sharpen filter

A = imread('squirell.png');
B = imsharpen(A);
subplot(121);imshow(A);title('original');
subplot(122);imshow(B);title('sharpen');

