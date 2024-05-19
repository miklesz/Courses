clc;
clear ;
im1 = imread('1.jpg');
im2 = imread('2.jpg'); 

x = im2bw(im1);
y = im2bw(im2);
%z = rgb2gray (im2);

sum_result = and(x,y);
%d = imcomplement (y);
figure(1);

