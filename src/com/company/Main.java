package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


class Vector{
    int width, height;
    int[][] pixels;
    int vectorSize = width * height;
    AverageVector avg;
    String code;

    public Vector(int width, int height){
        this.width = width;
        this.height = height;
        this.pixels = new int[height][width];
    }

}

class AverageVector extends Vector {
    int width, height;
    double[][] pixels;
    int vectorSize = width * height;

    public AverageVector(int width, int height){
        super(width,height);
        this.width = width;
        this.height = height;
        this.pixels = new double[height][width];
    }

}

class CodeBook{
    public static ArrayList<AverageVector> averageVectors;
    public static String[] code;
}


class Compression{

    String[] compressedImage;

    public int getImgHeightOrWidth(BufferedImage img, String name, String option){
        try {           // READING THE IMAGE...
            File imagefile = new File(name);
            img = ImageIO.read(imagefile);
            if (option.equals("w")){
                int width = img.getWidth();
                return width;
            }
            else if (option.equals("h")){
                int height = img.getHeight();
                return height;
            }
            System.out.println("Readed Successfully");
        }catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    public int[][] readImage(String name, BufferedImage img){

        try {           // READING THE IMAGE...
            File imagefile = new File(name);
            img = ImageIO.read(imagefile);
            System.out.println("Image is Readed Successfully.");
        }catch (IOException e){
            e.printStackTrace();
        }
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] pixels = new int[height][width];
        Raster raster = img.getData();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y][x] = raster.getSample(x,y,0);
            }
        }
        return pixels;
    }

    public ArrayList<Vector> split(int[][] pixels, int vectorWidth, int vectorHeight, int imgWidth, int imgHeight) throws ArrayIndexOutOfBoundsException{
        ArrayList<Vector> splittedVectors = new ArrayList<>();
        int vectorSize = vectorWidth * vectorHeight;
        int totalSizeofPixels = imgWidth * imgHeight;
        int numOfVectors = totalSizeofPixels/vectorSize;
        while (splittedVectors.size() < numOfVectors) {
            for (int c = 0; c < imgHeight; c += vectorHeight) {
                for (int k = 0; k < imgWidth; k += vectorWidth) {
                    Vector v = new Vector(vectorWidth, vectorHeight);
                    for (int j = 0; j < vectorHeight; j++) {
                        for (int i = 0; i < vectorWidth; i++) {
                            v.pixels[j][i] = pixels[c + j][k + i];
                        }
                    }
                    splittedVectors.add(v);
                }
            }
        }
        return splittedVectors;
    }

    public Vector sumOfSplittedVectors(ArrayList<Vector> splittedVectors, int width, int height){
        Vector vec = new Vector(width, height);
        for (int i = 0; i < vec.height; i++) {
            for (int j = 0; j < vec.width; j++) {
                vec.pixels[i][j] = 0;                   // filling the vector with zeros.
            }
        }

        for (int k = 0; k < splittedVectors.size(); k++) {
            for (int i = 0; i < splittedVectors.get(k).height; i++) {
                for (int j = 0; j < splittedVectors.get(k).width; j++) {
                    vec.pixels[i][j] += splittedVectors.get(k).pixels[i][j];
                }
            }
        }
        return vec;
    }

    public AverageVector getAverageVector(ArrayList<Vector> splittedVectors, int width, int height) {
        AverageVector averageVector = new AverageVector(width,height);
        Vector vectorSum = sumOfSplittedVectors(splittedVectors, width, height);
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                averageVector.pixels[i][j] = (Double.valueOf(vectorSum.pixels[i][j]) / Double.valueOf(splittedVectors.size()));
            }
        }
        return averageVector;
    }

    public double getMax(AverageVector vec,int width, int height){
        vec = new AverageVector(width,height);
        double max = 0.0;
        for (int i = 0; i < vec.height; i++){
            for (int j = 0; j < vec.width; j++){
                double temp = vec.pixels[i][j];
                if (max < temp){
                    max = temp;
                }
            }
        }
        return max;
    }

    public double getDifferences(Vector splittedVectors, AverageVector avgVectors, int width, int height){
        double diff = 0.0;
        for (int h = 0; h < height; h++){
            for (int w = 0; w < width; w++){
                double value = splittedVectors.pixels[h][w] - avgVectors.pixels[h][w];
                diff += Math.pow(value,2);
            }
        }
        return diff;
    }

    public void compare(ArrayList<Vector> splittedVectors, ArrayList<AverageVector> avgVectors, int width, int height){
        int idx = 0;
        for (int i = 0; i < splittedVectors.size(); i++){
            double mindiff = 10000000.0;
            for (int j = 0; j < avgVectors.size(); j++){
                double d = getDifferences(splittedVectors.get(i),avgVectors.get(j),width,height);
                if (mindiff > d){
                    mindiff = d;
                    idx = j;
                }
            }
            splittedVectors.get(i).avg = avgVectors.get(idx);
        }
    }

    public ArrayList<AverageVector> getAvgVectorForNewSplitted(ArrayList<AverageVector> avg, ArrayList<Vector> splittedVectors, int width, int height){
        ArrayList<AverageVector> a = new ArrayList<>();
        for (int i = 0; i < avg.size(); i++){
            ArrayList<Vector> newsplitted = new ArrayList<>();
            for (int j = 0; j < splittedVectors.size(); j++){
                if (splittedVectors.get(j).avg == avg.get(i)){
                    newsplitted.add(splittedVectors.get(j));
                }
            }
            if (newsplitted.size() == 0){
                a.add(avg.get(i));
            }
            else {
                a.add(getAverageVector(newsplitted, width, height));
            }
        }
        return a;
    }

    public ArrayList<AverageVector> sortAvgs(ArrayList<AverageVector> avg, int width, int height){
        double min = getMax(avg.get(0), width, height);
        for (int i = 0; i < avg.size(); i++) {
            double temp = getMax(avg.get(i), width, height);
            if (min > temp){
                min = temp;
                avg.set(i, avg.get(i-1));
            }
        }
        return avg;
    }

    public void compress(int codeBookSize, AverageVector avg, ArrayList<Vector> splittedVectors, int width, int height){
        ArrayList<AverageVector> vec = new ArrayList<>();
        int count = 0;
        int pixLength = 0;
        avg = getAverageVector(splittedVectors,width,height);
        vec.add(avg);
        ArrayList<AverageVector> previous = vec;
        while (vec.size() < codeBookSize) {
            vec = new ArrayList<>();
            for (int k = 0; k < previous.size(); k++) {
                AverageVector v1 = new AverageVector(width,height);
                AverageVector v2 = new AverageVector(width,height);
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if ((int) previous.get(k).pixels[i][j] < previous.get(k).pixels[i][j]) {
                            v1.pixels[i][j] = (int) previous.get(k).pixels[i][j];
                            v2.pixels[i][j] = (int) previous.get(k).pixels[i][j] + 1;
                        } else if ((int) previous.get(k).pixels[i][j] == previous.get(k).pixels[i][j]) {
                            v1.pixels[i][j] = (int) previous.get(k).pixels[i][j] - 1;
                            v2.pixels[i][j] = (int) previous.get(k).pixels[i][j] + 1;
                        }
                    }
                }
                vec.add(v1);
                vec.add(v2);
            }
            compare(splittedVectors, vec, width, height);
            vec = getAvgVectorForNewSplitted(vec, splittedVectors, width, height);
            previous = vec;
        }

        ArrayList<AverageVector> newAvg = new ArrayList<>();
        int loop_limit = 100;
        while (loop_limit > 0){
            // checking changes
            compare(splittedVectors, previous, width, height);
            newAvg = getAvgVectorForNewSplitted(previous,splittedVectors,width,height);

            for (int i = 0; i < newAvg.size(); i++) {
                for (int j = 0; j < previous.size(); j++) {
                    for (int h = 0; h < height; h++) {
                        for (int w = 0; w < width; w++) {
                            if (newAvg.get(i).pixels[h][w] == previous.get(j).pixels[h][w]) {
                                count++;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < newAvg.size(); i++) {
                pixLength += newAvg.get(i).vectorSize;
            }
            if (count == pixLength && newAvg.size() == previous.size() && newAvg.size() == codeBookSize) {
                break;                          // No changes then get out of the loop.
            } else if (count < pixLength) {     // there are changes then update the previous ArrayList.
                previous = newAvg;
            }
            loop_limit--;
        }
        compare(splittedVectors, newAvg, width, height);
        CodeBook.averageVectors = sortAvgs(newAvg,width,height);
        CodeBook.code = new String[codeBookSize];
        int codeSize = Integer.toBinaryString(codeBookSize).length();
        for (int i = 0; i < codeBookSize; i++){
            String value = Integer.toBinaryString(i);
            for (int j = 0; value.length() < codeSize - 1; j++) {
                value = "0" + value;
            }
            CodeBook.code[i] = value;
        }

        try{
            BufferedWriter f = new BufferedWriter(new FileWriter("codebook.txt"));
            for (int i = 0; i < CodeBook.code.length; i++){
                String text = Arrays.deepToString(CodeBook.averageVectors.get(i).pixels) + "," + CodeBook.code[i];
                f.write(text);
                f.newLine();
            }
            System.out.println("codebook is written successfully");
            f.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        // compresse img           REPLACE ALL SPLITTED VECTORS WITH AVERAGE VECTORS CODE IN CODE BOOK
        for (int i = 0; i < splittedVectors.size(); i++){
            for (int j = 0; j < codeBookSize; j++){
                if (splittedVectors.get(i).avg == CodeBook.averageVectors.get(j)) {
                    splittedVectors.get(i).code = CodeBook.code[j];
                    break;
                }
            }
        }

        this.compressedImage = new String[splittedVectors.size()];
        try{
            FileWriter file = new FileWriter("compressedImg.txt");
            ArrayList<String> text = new ArrayList<>();
            for (int i = 0; i < splittedVectors.size(); i++){
                file.write(splittedVectors.get(i).code);
                this.compressedImage[i] = splittedVectors.get(i).code;
            }

            System.out.println("compressed image is written successfully.");
            file.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class Decompression{

    public void writeImage(double[][] pixels, String outputFilePath, int imgHeight, int imgWidth) {
        File fileout = new File(outputFilePath);
        BufferedImage image2 = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                double pixel = pixels[y][x];
                pixel = pixel + ((int) pixel << 8 ) + ((int) pixel << 16);
                image2.setRGB(x, y, (int)pixel);
            }
        }
        try {
            ImageIO.write(image2, "png", fileout);
            System.out.println("Image is Written Successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void decompress(int width, int height, Compression c, int imageWidth, int imageHeight){
        ArrayList<AverageVector> v = new ArrayList<>();

        for (int i = 0; i < c.compressedImage.length; i++){
            AverageVector vec = new AverageVector(width, height);
            for (int j = 0; j < CodeBook.code.length; j++){
                if (c.compressedImage[i] == CodeBook.code[j]){
                    vec = CodeBook.averageVectors.get(j);
                    break;
                }
            }
            if (vec.pixels[0].length > 0) {
                v.add(vec);
            }
        }

        AverageVector oneVector = new AverageVector(imageWidth,imageHeight);        //THE VECTOR THAT REPRESENT THE WHOLE IMAGE
        int k = 0;
        while (k < v.size()){
            for (int i = 0; i < imageHeight; i += height) {
                for (int j = 0; j < imageWidth; j += width) {
                    for (int h = 0; h < height; h++) {
                        for (int w = 0; w < width; w++) {
                            oneVector.pixels[i+h][j+w] = v.get(k).pixels[h][w];
                        }
                    }
                    if (k < v.size()){
                        k++;
                    }
                }
            }
            break;
        }
        double[][]recPixels = oneVector.pixels;
        writeImage(recPixels,"decompression.png",imageHeight,imageWidth);
    }
}


class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("What is the Image path?");
        String imageName = input.next();
        System.out.println("What is vector size?{vectorheight vectorwidth}");
        int vectorheight = Integer.parseInt(input.next());
        int vectorwidth = Integer.parseInt(input.next());
        System.out.println("What is code book size?");
        int codebookSize = input.nextInt();

        Compression c = new Compression();

        BufferedImage img = null;
        int imgWidth = c.getImgHeightOrWidth(img,imageName,"w");
        int imgHeight = c.getImgHeightOrWidth(img,imageName,"h");
        int[][] pixels = c.readImage(imageName, img);

        try{
            ArrayList<Vector> vecs = c.split(pixels,vectorwidth,vectorwidth,imgWidth,imgHeight);
            AverageVector avg = c.getAverageVector(vecs,vectorwidth,vectorheight);
            c.compress(codebookSize, avg, vecs, vectorwidth, vectorheight);

            Decompression d = new Decompression();
            d.decompress(vectorwidth,vectorheight,c,imgWidth,imgHeight);
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("This Vector Dimensions are NOT APPROPRIATE To this image, So please change them and try again!!!");
        }
    }
}
