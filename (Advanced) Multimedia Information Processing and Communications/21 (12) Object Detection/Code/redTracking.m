% info abou computer camera
% a = imaqhwinfo;
% [camera_name, camera_id, format] = getCameraInfo();

clc;
clear all;
close all;

vid = videoinput('winvideo',1,'YUY2_320x240');

%set properties of the video
set(vid, 'FramesPerTrigger', Inf);
set(vid, 'ReturnedColorspace', 'rgb')
vid.FrameGrabInterval = 5;

start(vid)

while(vid.FramesAcquired<=200)  %stop after 200 frames
    
    data = getsnapshot(vid); %snapshot of the current frame
    
    % Now to track red objects in real time
    % we have to subtract the red component 
    % from the grayscale image to extract the red components in the image.
    diff_im = imsubtract(data(:,:,1), rgb2gray(data));      %data(:,:,1) - red data(:,:,2) - green data(:,:,3) - blue
    %median filter to filter out noise
    diff_im = medfilt2(diff_im, [3 3]);
    %convert the resulting grayscale image into a binary image.
    diff_im = im2bw(diff_im,0.18);
    % Remove all those pixels less than 300px
    diff_im = bwareaopen(diff_im,300);
    
    % Label all the connected components in the image.
    bw = bwlabel(diff_im, 8);
    
    % Here we do the image blob analysis.
    % We get a set of properties for each labeled region.
    stats = regionprops(bw, 'BoundingBox', 'Centroid');
    
    % Display the image
    imshow(data)
    
    % Display the image as black and white in which only the color parts
    % are white and background is black
    %imshow(diff_im)
    
    hold on
    
    %This is a loop to bound the red objects in a rectangular box.
    
    centroids = zeros(length(stats), 2);
    
    for object = 1:length(stats)
        bb = stats(object).BoundingBox;
        bc = stats(object).Centroid;
        
        %probka = regionprops(logical(bw(:, :, object), 'area', 'centroid'));
        
        %srobka = regionprops(logical(bw(:,:, k)), 'area', 'centroid');
        %sroba jest tylko przykladem=> tego ma tu nie byc
        
        %area_vector = [s.Area];
        %[tmp, idx] = max(area_vector);
        %centroids(object,:) = probka(idx(1)).Centroid;
        
        rectangle('Position',bb,'EdgeColor','r','LineWidth',2)
        plot(bc(1),bc(2), '-m+')
        a=text(bc(1)+15,bc(2), strcat('X:', num2str(round(bc(1))), 'Y:', num2str(round(bc(2)))));
        set(a, 'FontName', 'Arial', 'FontWeight', 'bold', 'FontSize', 12, 'Color', 'yellow');
        
    end
    

    
       % figure(2);
       % subplot(2, 1, 1)
       % plot(1:length(stats), centroids(:,1)), ylabel('x')
       % subplot(2, 1, 2)
       % plot(1:length(stats), centroids(:, 2)), ylabel('y')
       % xlabel('time (s)')
  
    
    hold off
end

stop(vid);

flushdata(vid); %remove image data from memory


