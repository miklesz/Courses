#include <stdio.h>
#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/nonfree/nonfree.hpp"
#include "opencv2/calib3d/calib3d.hpp"

#include <stdio.h>
#include <iostream>


using namespace cv;
using namespace std;

static void help()
{
    printf("\nThis program demonstrates using features2d detector, descriptor extractor and simple matcher\n"
            "Using the SURF desriptor:\n"
            "\n"
            "Usage:\n matcher_simple <image1> <image2>\n");
}

int main(int argc, char** argv)
{


    Mat img1 = imread("11.jpg", CV_LOAD_IMAGE_GRAYSCALE);
    Mat img2 = imread("12.jpg", CV_LOAD_IMAGE_GRAYSCALE);
    if(img1.empty() || img2.empty())
    {
        printf("Can't read one of the images\n");
        return -1;
    }

    // detecting keypoints
    SurfFeatureDetector detector;
    vector<KeyPoint> keypoints1, keypoints2;
    detector.detect(img1, keypoints1);
    detector.detect(img2, keypoints2);

    // computing descriptors
    SurfDescriptorExtractor extractor;
    Mat descriptors1, descriptors2;
    extractor.compute(img1, keypoints1, descriptors1);
    extractor.compute(img2, keypoints2, descriptors2);

    // matching descriptors
    BFMatcher matcher(NORM_L2);
    vector<DMatch> matches;
    matcher.match(descriptors1, descriptors2, matches);


    double max_dist = 0;
    double min_dist = 100;

    //-- Quick calculation of max and min distances between keypoints
    for( int i = 0; i < descriptors1.rows; i++ )
    {
        double dist = matches[i].distance;
        if( dist < min_dist ) min_dist = dist;
        if( dist > max_dist ) max_dist = dist;
    }

    printf("-- Max dist : %f \n", max_dist );
    printf("-- Min dist : %f \n", min_dist );


    vector< DMatch > good_matches;

    for( int i = 0; i < descriptors1.rows; i++ )
    {
        if( matches[i].distance < 3*min_dist )//decyduje o iloœci punktów, które zostan¹ narysowane
        {
            good_matches.push_back( matches[i]);
        }
    }
    namedWindow("matches", 1);
    Mat img_matches;
    drawMatches(img1, keypoints1, img2, keypoints2, good_matches, img_matches);

    vector<Point2f> cor1;
    vector<Point2f> cor2;

    for( int i = 0; i < good_matches.size(); i++ )
    {
        //-- Get the keypoints from the matches
        cor1.push_back( keypoints1[ good_matches[i].queryIdx ].pt );
        cor2.push_back( keypoints2[ good_matches[i].trainIdx ].pt );
    }

    Mat H = findHomography( cor1, cor2, CV_RANSAC );
    const double det=H.at<double>(0,0)*H.at<double>(1,1)-H.at<double>(1,0)*H.at<double>(0,1);
    cout<<det<<endl;

    //sprawdzanie zgodności całego obrazka
    if(abs(det)<1.2&&abs(det)>0.8)
    {
        //prawy gorny rog img1
        Point2f corner1=Point2f( img1.cols, 0 );
        //rogi img2
        vector<Point2f> corners2(4);
        corners2[0] = Point2f(0,0);
        corners2[1] = Point2f( img2.cols, 0 );
        corners2[2] = Point2f( img2.cols, img2.rows );
        corners2[3] = Point2f( 0, img2.rows );


        //zaznaczanie obrazka
        line( img_matches, (corner1 + corners2[0]), (corner1 + corners2[1]), Scalar(0, 255, 0), 4 );
        line( img_matches, (corner1 + corners2[1]), (corner1 + corners2[2]), Scalar(0, 255, 0), 4 );
        line( img_matches, (corner1 + corners2[2]), (corner1 + corners2[3]), Scalar(0, 255, 0), 4 );
        line( img_matches, (corner1 + corners2[3]), (corner1 + corners2[0]), Scalar(0, 255, 0), 4 );
    }

    imshow("matches", img_matches);
    waitKey(0);

    return 0;
}
