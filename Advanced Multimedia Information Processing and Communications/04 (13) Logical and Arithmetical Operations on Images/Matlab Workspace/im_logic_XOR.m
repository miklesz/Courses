clc;
clear ;

im1 = imread('rose1.jpg'); % reading image
im2 = imread('rose2.jpg'); % reading image


im1_bw = im2bw(im1);       % converting RGB image to black and white *
im2_bw = im2bw(im2);      % converting RGB image to black and white

Operation_xor_result = xor(im1_bw,im2_bw); % performing OR logic on the images


figure('Name','Logic operation "XOR"','NumberTitle','on')
    
 subplot(4,4,1),
 imshow(im1);            %Display the image 
 title('image 1');
 
 subplot(4,4,2),
 imshow(im2);            %Display the image
 title('image 2');
 
 subplot(4,4,3),
 imshow(im1_bw);            %Display the image
 title('image1 B&W');
 
 subplot(4,4,4),
 imshow(im2_bw);            %Display the image
 title('image2 B&W');
 
 
 subplot(2,2,[3,4]),
 imshow(Operation_xor_result); %Display the result
 title('Logic Operation : XOR')
 



