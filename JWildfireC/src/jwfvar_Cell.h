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

#include "jwf_Constants.h"
#include "jwf_Variation.h"

class CellFunc: public Variation {
public:
	CellFunc() {
		size = 0.6;
		initParameterNames(1, "size");
	}

	const char* getName() const {
		return "cell";
	}

	void setParameter(char *pName, JWF_FLOAT pValue) {
		if (strcmp(pName, "size") == 0) {
			size = pValue;
		}
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		JWF_FLOAT inv_cell_size = 1.0 / size;

		/* calculate input cell */
		int x = (int) JWF_FLOOR(pAffineTP->x * inv_cell_size);
		int y = (int) JWF_FLOOR(pAffineTP->y * inv_cell_size);

		/* Offset from cell origin */
		JWF_FLOAT dx = pAffineTP->x - x * size;
		JWF_FLOAT dy = pAffineTP->y - y * size;

		/* interleave cells */
		if (y >= 0) {
			if (x >= 0) {
				y *= 2;
				x *= 2;
			}
			else {
				y *= 2;
				x = -(2 * x + 1);
			}
		}
		else {
			if (x >= 0) {
				y = -(2 * y + 1);
				x *= 2;
			}
			else {
				y = -(2 * y + 1);
				x = -(2 * x + 1);
			}
		}

		pVarTP->x += pAmount * (dx + x * size);
		pVarTP->y -= pAmount * (dy + y * size);

		if (pContext->isPreserveZCoordinate) {
			pVarTP->z += pAmount * pAffineTP->z;
		}
	}

	CellFunc* makeCopy() {
		return new CellFunc(*this);
	}

private:
	JWF_FLOAT size;
};

