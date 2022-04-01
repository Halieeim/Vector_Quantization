# Vector_Quantization
This is my implementation of vector Quantization which is an algorithm that works on compressing images

> ## Methodology Of Compression


>> 1- Turn your image into pixels in gray-level.


>> 2- Split these pixel into vectors/blocks of pixels with a dividable dimensions according to the dimensions of the image.


>> 3- Get the average vector by calculating the summation of each element in the same position in other vectors and divide on the number of elements.


>> 4- Generate two vectors from the average vector:
      The first will be generated by increasing its elements by 1
      The second will be generated by decreasing its elements by 1


>> 5- Distribute the vectors of the image according two these two vectors, to make these two vectors represent the other vectors.


>> 6- Repeat the steps 4&5 till you get no change in the average vectors, Then you got the codebook vectors


>> 7- Give each vector a code, and the length of the code will depend on the size of codebook for example if the size of codebook is 8 the lenght of each vector's code will be 3.


>> 8-Replace the vectors of the image with the code that represents them and store it in a text file.


> ## Methodology Of Decompression
>> Now we have the codebook and the average vectors of the codebook, we know the length of the code form the number of average vectors and because of that we can split the codebook in the text file to separate the code of each vector.


>> 1- Read the codebook text file and split the code of each vector.


>> 2- Match the code to its corresponding average vector.


>> 3- generate the whole image again by collecting these average vector, now you have the pixels of the image.


>> 4- Convert these pixels into image again.


Now you got the image again but of course it is not like the original image.
