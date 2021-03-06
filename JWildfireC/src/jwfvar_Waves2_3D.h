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

#include "jwf_Variation.h"

class Waves2_3DFunc: public Variation {
public:
	Waves2_3DFunc() {
		freq = 2.0;
		scale = 1.0;
		initParameterNames(2, "freq", "scale");
	}

	const char* getName() const {
		return "waves2_3D";
	}

	void setParameter(char *pName, JWF_FLOAT pValue) {
		if (strcmp(pName, "freq") == 0) {
			freq = pValue;
		}
		else if (strcmp(pName, "scale") == 0) {
			scale = pValue;
		}
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
		JWF_FLOAT avgxy = (pAffineTP->x + pAffineTP->y) / 2.0;
		pVarTP->x += pAmount * (pAffineTP->x + scale * JWF_SIN(pAffineTP->y * freq));
		pVarTP->y += pAmount * (pAffineTP->y + scale * JWF_SIN(pAffineTP->x * freq));
		pVarTP->z += pAmount * (pAffineTP->z + scale * JWF_SIN(avgxy * freq));
	}

	Waves2_3DFunc* makeCopy() {
		return new Waves2_3DFunc(*this);
	}

private:
	JWF_FLOAT freq;
	JWF_FLOAT scale;
};

