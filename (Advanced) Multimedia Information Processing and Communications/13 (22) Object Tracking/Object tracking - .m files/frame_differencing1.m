%FRAME DIFFERING EXAMPLE 1
clc
clear all
close all

VideoReader('sun.avi');
mmfileinfo('sun.avi');
obj=VideoReader('sun.avi');
vidFrames=read(obj);

numFrames=get(obj, 'numberOfFrames');
%getting the individual frames
for k=1:numFrames
    mov(k).cdata=vidFrames(:,:,:,k);
    mov(k).colormap=[];
end

figure(1), movie(mov,1,obj.FrameRate),title('Original movie');
figure(2),montage(vidFrames(:,:,:,10:15));
%%end of part1--------------------------------------------

%changing images from rgb to grey
for k = numFrames:-1:1
grey(:, :, k) = rgb2gray(vidFrames(:, :, :, k));
end
figure(3),imshow(grey(:,:,10));

%computing the difference between each frame
for k = numFrames-1:-1:1
diff(:, :, k) = imabsdiff(grey(:, :, k), grey(:, :, k+1));
end
figure(4),imshow(diff(:, :, 1), []);

%difference between 20 frames, just for visibility - delete later
for k = numFrames-20:-1:1
diff2(:, :, k) = imabsdiff(grey(:, :, k), grey(:, :, k+20));
end
figure(5),imshow(diff2(:, :, 1), []);
%%end of part 2------------------------------------------------

%creating a black and white image in which only the changing parts are
%white and the background is black
for k = numFrames-1:-1:1
    bw(:,:,k)= im2bw(diff(:,:,k),graythresh(diff(:,:,k)));
end
figure(6),imshow(bw(:, :, 1));

%deleting the small area that are not connected to the actual shape of moon
bw2 = bwareaopen(bw, 8, 8);

figure(7), imshow(bw2(:,:,1));
%%end of part 3-----------------------------------------------------

%calculating the avarage location of the changing object in each frame
for k= numFrames-1:-1:1
    s = regionprops(logical(bw2(:,:,k)), 'centroid');
    centroids = [s.Centroid];
    xavg = mean(centroids(:,1:2:end));
    yavg = mean(centroids(:,2:2:end));
    position(:,k)=[xavg,yavg];
end

%ploting it
figure(8), subplot (2,1,1); title('X');
plot([1:numFrames-1], position(1,:)), ylabel('position x');
subplot (2,1,2); title('Y');
plot([1:numFrames-1], position(2,:)), ylabel('position y');
%%end of part 4--------------------------------------------------

%displaying the movie with the assumed position of the object
for k=1:length(position)
    I=mov(k).cdata;
    xpos=int32(position(1,k));
    ypos=int32(position(2,k));
    I(ypos-5:ypos+5,xpos-5:xpos+5,1:2)=255;
    mov(k).cdata=I;
end;
figure(9), movie(mov,1), title ('position');
%end of code
