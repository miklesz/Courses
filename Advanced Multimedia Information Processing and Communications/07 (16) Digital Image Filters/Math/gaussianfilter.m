%clearing 
clc
clear all
close all   


%loading image and using gaussian filter

A = imread('black.png');
imgd = im2double(A);   % imgd in [0,1]
W = smooth3(imgd,'gaussian');
subplot(121);imshow(A);title('original');
subplot(122);imshow(W);title('gaussian filter');
