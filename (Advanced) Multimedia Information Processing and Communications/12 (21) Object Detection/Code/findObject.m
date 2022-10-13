clc;
clear all;
close all;

RGB = imread('jam.jpg');
objectImage = rgb2gray(RGB);

figure(1);
imshow(objectImage);
title('Image of a object');

RGB2 = imread('background.jpg');
backgroundImage = rgb2gray(RGB2);
figure(2);
imshow(backgroundImage);
title('Image of a background');

objectPoints = detectSURFFeatures(objectImage);
backgroundPoints = detectSURFFeatures(backgroundImage);

figure(3);
imshow(objectImage);
title('100 Strongest Feature Points from object Image');
hold on;
plot(selectStrongest(objectPoints, 100));

figure(4);
imshow(backgroundImage);
title('300 Strongest Feature Points from background Image');
hold on;
plot(selectStrongest(backgroundPoints, 300));

[objectFeatures, objectPoints] = extractFeatures(objectImage, objectPoints);
[backgroundFeatures, backgroundPoints] = extractFeatures(backgroundImage, backgroundPoints);

boxPairs = matchFeatures(objectFeatures, backgroundFeatures);

matchedObjectPoints = objectPoints(boxPairs(:, 1), :);
matchedBackgroundPoints = backgroundPoints(boxPairs(:, 2), :);
figure(5);
showMatchedFeatures(objectImage, backgroundImage, matchedObjectPoints, ...
    matchedBackgroundPoints, 'montage');
title('Putatively Matched Points with Others');

[tform, inlierObjectPoints, inlierBackgroundPoints] = ...
    estimateGeometricTransform(matchedObjectPoints, matchedBackgroundPoints, 'affine');

figure(6);
showMatchedFeatures(objectImage, backgroundImage, inlierObjectPoints, ...
    inlierBackgroundPoints, 'montage');
title('Matched Points without Others');

objectPolygon = [1, 1;                           % top-left
        size(objectImage, 2), 1;                 % top-right
        size(objectImage, 2), size(objectImage, 1); % bottom-right
        1, size(objectImage, 1);                 % bottom-left
        1, 1];                   % top-left again to close the polygon
    
    newObjectPolygon = transformPointsForward(tform, objectPolygon);
    
    figure(7);
imshow(backgroundImage);
hold on;
line(newObjectPolygon(:, 1), newObjectPolygon(:, 2), 'Color', 'y');
title('Detected Image');