I0 = imread( 'Birds.bmp');   
I0grey = im2double(rgb2gray(I0));  
I= I0grey;
figure, imshow(I), title('Przed filtracj¹');

[~, threshold] = edge(I, 'sobel');
fudgeFactor = .5;
BWs = edge(I,'sobel', threshold * fudgeFactor);
figure, imshow(BWs), title('Po filtracji:');

