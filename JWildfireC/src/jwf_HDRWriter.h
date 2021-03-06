/*
 JWildfireC - an external C-based fractal-flame-renderer for JWildfire 
 Copyright (C) 2012 Andreas Maschke

 This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
 General Public License as published by the Free Software Foundation; either version 2.1 of the 
 License, or (at your option) any later version.
 
 This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License along with this software; 
 if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
#ifndef __JWF_HDR_WRITER_H__
#define __JWF_HDR_WRITER_H__

class HDRWriter {
public:
	void writeImage(char *pFilename, SimpleHDRImage *pImg) {
		FILE *fp;
		//open file for output
		fp = fopen(pFilename, "wb");
		if (!fp) {
			fprintf(stderr, "Unable to open file '%s'\n", pFilename);
			exit(-1);
		}
		//write the header file
		//image format
		fprintf(fp, "#?RGBE\n");
		//comments
		fprintf(fp, "FORMAT=32-bit_rle_rgbe\n\n");
		//image size
		fprintf(fp, "-Y %d +X %d\n", pImg->imageHeight, pImg->imageWidth);
		// pixel data
		for (int i = 0; i < pImg->imageHeight; i++) {
			for (int j = 0; j < pImg->imageWidth; j++) {
        int rgbe = pImg->getRGBEValue(j, i);
        int r=rgbe >> 24;
        int g=rgbe >> 16;
        int b=rgbe >> 8;
        int e=rgbe;
				fwrite(&r, 1, 1, fp);
				fwrite(&g, 1, 1, fp);
				fwrite(&b, 1, 1, fp);
				fwrite(&e, 1, 1, fp);
			}
		}
		fflush(fp);
		fclose(fp);
	}

};









#endif // __JWF_HDR_WRITER_H__
