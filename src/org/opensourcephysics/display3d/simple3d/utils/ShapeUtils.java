package org.opensourcephysics.display3d.simple3d.utils;


public class ShapeUtils {
	
	//Variables
	static final protected double TO_RADIANS = Math.PI/180.0;
	static final protected double[] vectorx = {1.0, 0.0, 0.0}, // Standard vertical Cylinder
	                                  vectory = {0.0, 1.0, 0.0}, vectorz = {0.0, 0.0, 1.0};
	
	//Static varibles for tetrahedron
	private static final double sqrt3 = Math.sqrt(3.0);
    private static final double height = Math.sqrt(6.0)/3.0f;
    private static final double xcenter = sqrt3/6.0f;
    private static final double zcenter = height/3.0f;
	
	
	//Cylinder
	static public double[][][] createStandardCylinder(int nr, int nu, int nz, double angle1, double angle2, boolean top, boolean bottom, boolean left, boolean right) {
	    int totalN = nu*nz;
	    if(bottom) {
	      totalN += nr*nu;
	    }
	    if(top) {
	      totalN += nr*nu;
	    }
	    if(Math.abs(angle2-angle1)<360) {
	      if(left) {
	        totalN += nr*nz;
	      }
	      if(right) {
	        totalN += nr*nz;
	      }
	    }
	    double[][][] data = new double[totalN][4][3];
	    // Compute sines and cosines
	    double[] cosu = new double[nu+1], sinu = new double[nu+1];
	    for(int u = 0;u<=nu;u++) {     // compute sines and cosines
	      double angle = ((nu-u)*angle1+u*angle2)*TO_RADIANS/nu;
	      cosu[u] = Math.cos(angle)/2; // The /2 is because the element is centered
	      sinu[u] = Math.sin(angle)/2;
	    }
	    // Now compute the tiles
	    int tile = 0;
	    double[] center = new double[] {-vectorz[0]/2, -vectorz[1]/2, -vectorz[2]/2};
	    {                                     // Tiles along the z axis
	      double aux = 1.0/nz;
	      for(int j = 0;j<nz;j++) {
	        for(int u = 0;u<nu;u++, tile++) { // This ordering is important for the computations below (see ref)
	          for(int k = 0;k<3;k++) {
	            data[tile][0][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+j*aux*vectorz[k];
	            data[tile][1][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+j*aux*vectorz[k];
	            data[tile][2][k] = center[k]+cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k]+(j+1)*aux*vectorz[k];
	            data[tile][3][k] = center[k]+cosu[u]*vectorx[k]+sinu[u]*vectory[k]+(j+1)*aux*vectorz[k];
	          }
	        }
	      }
	    }
	    if(bottom) {                                                      // Tiles at bottom
	      //int ref = 0;                                                    // not used
	      for(int u = 0;u<nu;u++) {
	        for(int i = 0;i<nr;i++, tile++) {
	          for(int k = 0;k<3;k++) {
	            data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
	            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
	            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
	            data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
	          }
	        }
	      }
	    }
	    if(top) { // Tiles at top
	      int ref = nu*(nz-1);
	      center[0] = vectorz[0];
	      center[1] = vectorz[1];
	      center[2] = vectorz[2]-0.5;
	      for(int u = 0;u<nu;u++) {
	        for(int i = 0;i<nr;i++, tile++) {
	          for(int k = 0;k<3;k++) {
	            data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
	            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
	            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
	            data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
	          }
	        }
	      }
	    }
	    if(Math.abs(angle2-angle1)<360) { // No need to close left or right if the Cylinder is 'round' enough
	      center[0] = -vectorz[0]/2;
	      center[1] = -vectorz[1]/2;
	      center[2] = -vectorz[2]/2;
	      if(right) { // Tiles at right
	        double aux = 1.0/nz;
	        for(int j = 0;j<nz;j++) {
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+j*aux*vectorz[k];
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+j*aux*vectorz[k];
	              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
	              data[tile][3][k] = ((nr-i)*center[k]+i*data[0][0][k])/nr+(j+1)*aux*vectorz[k];
	            }
	          }
	        }
	      }
	      if(left) { // Tiles at left
	        double aux = 1.0/nz;
	        int ref = nu-1;
	        for(int j = 0;j<nz;j++) {
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+j*aux*vectorz[k];
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+j*aux*vectorz[k];
	              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
	              data[tile][3][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr+(j+1)*aux*vectorz[k];
	            }
	          }
	        }
	      }
	    }
	    return data;
	 }
	
	//Cone
	static public double[][][] createStandardCone(int nr, int nu, int nz, double angle1, double angle2, boolean top, boolean bottom, boolean left, boolean right, double height) {
		    int totalN = nu*nz;
		    if(bottom) {
		      totalN += nr*nu;
		    }
		    if(!Double.isNaN(height)&& top) {
		      totalN += nr*nu;
		    }
		    if(Math.abs(angle2-angle1)<360) {
		      if(left) {
		        totalN += nr*nz;
		      }
		      if(right) {
		        totalN += nr*nz;
		      }
		    }
		    double[][][] data = new double[totalN][4][3];
		    // Compute sines and cosines
		    double[] cosu = new double[nu+1], sinu = new double[nu+1];
		    for(int u = 0;u<=nu;u++) {     // compute sines and cosines
		      double angle = ((nu-u)*angle1+u*angle2)*TO_RADIANS/nu;
		      cosu[u] = Math.cos(angle)/2; // The /2 is because the element is centered
		      sinu[u] = Math.sin(angle)/2;
		    }
		    // Now compute the tiles
		    int tile = 0;
		    double[] center = new double[] {-vectorz[0]/2, -vectorz[1]/2, -vectorz[2]/2};
		    {                                     // Tiles along the z axis
		      double N;
		      if(Double.isNaN(height)) {
		        N = nz;
		      } else if(height==0) {
		        N = Integer.MAX_VALUE;
		      } else {
		        N = nz/height;
		      }
		      double aux = 1.0/N;
		      for(int j = 0;j<nz;j++) {
		        for(int u = 0;u<nu;u++, tile++) { // This ordering is important for the computations below (see ref)
		          for(int k = 0;k<3;k++) {
		            data[tile][0][k] = center[k]+(cosu[u]*vectorx[k]+sinu[u]*vectory[k])*(N-j)/N+j*aux*vectorz[k];
		            data[tile][1][k] = center[k]+(cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*(N-j)/N+j*aux*vectorz[k];
		            data[tile][2][k] = center[k]+(cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*(N-j-1)/N+(j+1)*aux*vectorz[k];
		            data[tile][3][k] = center[k]+(cosu[u]*vectorx[k]+sinu[u]*vectory[k])*(N-j-1)/N+(j+1)*aux*vectorz[k];
		          }
		        }
		      }
		    }
		    if(bottom) {                                                      // Tiles at bottom
		      //int ref = 0;                                                    // not used
		      for(int u = 0;u<nu;u++) {
		        for(int i = 0;i<nr;i++, tile++) {
		          for(int k = 0;k<3;k++) {
		            data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
		            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
		            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
		            data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
		          }
		        }
		      }
		    }
		    if(!Double.isNaN(height)&&top) { // Tiles at top
		      int ref = nu*(nz-1);
		      center[0] = vectorz[0];
		      center[1] = vectorz[1];
		      if(Double.isNaN(height)) {
		        center[2] = vectorz[2]-0.5;
		      } else {
		        center[2] = height*vectorz[2]-0.5;
		      }
		      for(int u = 0;u<nu;u++) {
		        for(int i = 0;i<nr;i++, tile++) {
		          for(int k = 0;k<3;k++) {
		            data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
		            data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
		            data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
		            data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
		          }
		        }
		      }
		    }
		    if(Math.abs(angle2-angle1)<360) { // No need to close left or right if the Cylinder is 'round' enough
		      center[0] = -vectorz[0]/2;
		      center[1] = -vectorz[1]/2;
		      center[2] = -vectorz[2]/2;
		      if(right) { // Tiles at right
		        int ref = 0;
		        double N;
		        double[] nextCenter = new double[3];
		        if(Double.isNaN(height)) {
		          N = nz;
		        } else if(height==0) {
		          N = Integer.MAX_VALUE;
		        } else {
		          N = nz/height;
		        }
		        double aux = 1.0/N;
		        for(int j = 0;j<nz;j++, ref += nu) {
		          center[0] = j*aux*vectorz[0];
		          center[1] = j*aux*vectorz[1];
		          center[2] = j*aux*vectorz[2]-0.5;
		          nextCenter[0] = (j+1)*aux*vectorz[0];
		          nextCenter[1] = (j+1)*aux*vectorz[1];
		          nextCenter[2] = (j+1)*aux*vectorz[2]-0.5;
		          for(int i = 0;i<nr;i++, tile++) {
		            for(int k = 0;k<3;k++) {
		              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][0][k])/nr;
		              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][0][k])/nr;
		              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][3][k])/nr;
		              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][3][k])/nr;
		            }
		          }
		        }
		      }
		      if(left) { // Tiles at left
		        int ref = nu-1;
		        double N;
		        double[] nextCenter = new double[3];
		        if(Double.isNaN(height)) {
		          N = nz;
		        } else if(height==0) {
		          N = Integer.MAX_VALUE;
		        } else {
		          N = nz/height;
		        }
		        double aux = 1.0/N;
		        for(int j = 0;j<nz;j++, ref += nu) {
		          center[0] = j*aux*vectorz[0];
		          center[1] = j*aux*vectorz[1];
		          center[2] = j*aux*vectorz[2]-0.5;
		          nextCenter[0] = (j+1)*aux*vectorz[0];
		          nextCenter[1] = (j+1)*aux*vectorz[1];
		          nextCenter[2] = (j+1)*aux*vectorz[2]-0.5;
		          for(int i = 0;i<nr;i++, tile++) {
		            for(int k = 0;k<3;k++) {
		              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr;
		              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr;
		              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][2][k])/nr;
		              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][2][k])/nr;
		            }
		          }
		        }
		      }
		    }
		   return data;
	}
	
	
	//Ellipsoid
	static public double[][][] createStandardEllipsoid(int nr, int nu, int nv, double angleu1, double angleu2, double anglev1, double anglev2, boolean top, boolean bottom, boolean left, boolean right) {
	    int totalN = nu*nv;
	    if(Math.abs(anglev2-anglev1)<180) {
	      if(bottom) {
	        totalN += nr*nu;
	      }
	      if(top) {
	        totalN += nr*nu;
	      }
	    }
	    if(Math.abs(angleu2-angleu1)<360) {
	      if(left) {
	        totalN += nr*nv;
	      }
	      if(right) {
	        totalN += nr*nv;
	      }
	    }
	    double[][][] data = new double[totalN][4][3];
	    // Compute sines and cosines
	    double[] cosu = new double[nu+1], sinu = new double[nu+1];
	    double[] cosv = new double[nv+1], sinv = new double[nv+1];
	    for(int u = 0;u<=nu;u++) {
	      double angle = ((nu-u)*angleu1+u*angleu2)*TO_RADIANS/nu;
	      cosu[u] = Math.cos(angle);
	      sinu[u] = Math.sin(angle);
	    }
	    for(int v = 0;v<=nv;v++) {
	      double angle = ((nv-v)*anglev1+v*anglev2)*TO_RADIANS/nv;
	      cosv[v] = Math.cos(angle)/2; // /2 because the size is the diameter
	      sinv[v] = Math.sin(angle)/2;
	    }
	    // Now compute the tiles
	    int tile = 0;
	    double[] center = new double[] {0, 0, 0};
	    {                                     // Tiles along the z axis
	      for(int v = 0;v<nv;v++) {
	        for(int u = 0;u<nu;u++, tile++) { // This ordering is important for the computations below (see ref)
	          for(int k = 0;k<3;k++) {
	            data[tile][0][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
	            data[tile][1][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v]+sinv[v]*vectorz[k];
	            data[tile][2][k] = (cosu[u+1]*vectorx[k]+sinu[u+1]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
	            data[tile][3][k] = (cosu[u]*vectorx[k]+sinu[u]*vectory[k])*cosv[v+1]+sinv[v+1]*vectorz[k];
	          }
	        }
	      }
	    }
	    // Note : the computations below are valid only for the given vectorx, vectory and vectorz
	    if(Math.abs(anglev2-anglev1)<180) { // No need to close top or bottom is the sphere is 'round' enough
	      if(bottom) {                                                      // Tiles at bottom
	        center[2] = sinv[0];
	        // int ref=0; // not used
	        for(int u = 0;u<nu;u++) {
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[u][0][k])/nr; // should be ref+u
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[u][0][k])/nr; // should be ref+u
	              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[u][1][k])/nr; // should be ref+u
	              data[tile][3][k] = ((nr-i)*center[k]+i*data[u][1][k])/nr; // should be ref+u
	            }
	          }
	        }
	      }
	      if(top) { // Tiles at top
	        center[2] = sinv[nv];
	        int ref = nu*(nv-1);
	        for(int u = 0;u<nu;u++) {
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref+u][3][k])/nr;
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][3][k])/nr;
	              data[tile][2][k] = ((nr-i-1)*center[k]+(i+1)*data[ref+u][2][k])/nr;
	              data[tile][3][k] = ((nr-i)*center[k]+i*data[ref+u][2][k])/nr;
	            }
	          }
	        }
	      }
	    }
	    if(Math.abs(angleu2-angleu1)<360) { // No need to close left or right if the sphere is 'round' enough
	      // System.out.println ("Computing lateral tiles");
	      double[] nextCenter = new double[] {0, 0, 0};
	      if(right) { // Tiles at right
	        int ref = 0;
	        for(int j = 0;j<nv;j++, ref += nu) {
	          center[2] = sinv[j];
	          nextCenter[2] = sinv[j+1];
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][0][k])/nr;
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][0][k])/nr;
	              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][3][k])/nr;
	              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][3][k])/nr;
	            }
	          }
	        }
	      }
	      if(left) { // Tiles at left
	        int ref = nu-1;
	        for(int j = 0;j<nv;j++, ref += nu) {
	          center[2] = sinv[j];
	          nextCenter[2] = sinv[j+1];
	          for(int i = 0;i<nr;i++, tile++) {
	            for(int k = 0;k<3;k++) {
	              data[tile][0][k] = ((nr-i)*center[k]+i*data[ref][1][k])/nr;
	              data[tile][1][k] = ((nr-i-1)*center[k]+(i+1)*data[ref][1][k])/nr;
	              data[tile][2][k] = ((nr-i-1)*nextCenter[k]+(i+1)*data[ref][2][k])/nr;
	              data[tile][3][k] = ((nr-i)*nextCenter[k]+i*data[ref][2][k])/nr;
	            }
	          }
	        }
	      }
	    }
	    return data;
	}
	
	//Tetrahedron
	static public double[][][] createStandardTetrahedron(boolean top, boolean bottom, double height) {
	    
		int totalN = 4;  //number of tiles
		int pointsN = 4; //number of points
	    if(!Double.isNaN(height)) {
	    	pointsN +=2;
	    	if(top && bottom)          totalN  = 5;
	    	else if(!top && bottom)    totalN  = 4;
	    	else if(!top && !bottom)   totalN  = 3;
	    	else 					   totalN  = 4;
	    }
	    if(!bottom && Double.isNaN(height)){
	    	totalN = 3; 
	    }
	    double[][][] data = new double[totalN][4][3];
	    double[][] points = new double[pointsN][3];
	    
	    //Base points
	    points[0][0]=xcenter; points[0][1]=0.5f; points[0][2]= -zcenter;        //p1
	    points[1][0]=xcenter; points[1][1]=-0.5f; points[1][2]= -zcenter;       //p2
	    points[2][0]=-xcenter*2.0f; points[2][1]=0.0f;  points[2][2]= -zcenter; //p3
	    
	    if(Double.isNaN(height)){
	    	points[3][0]=0.0f; points[3][1]=0.0f; points[3][2]= ShapeUtils.height -zcenter; //p4
	    	if(bottom){
	    		int[] serie = {0,1,3,3, 0,3,2,2, 1,2,3,3, 0,2,1,1};
	    		/*p1, p2, p4, p4   // front face
        	  	p1, p4, p3, p3 	   // left, back face
        	  	p2, p3, p4, p4 	   // right, back face
        	  	p1, p3, p2, p2	   // bottom face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    	else{
	    		int[] serie = {0,1,3,3, 0,3,2,2, 1,2,3,3};
	    		/*p1, p2, p4, p4	// front face
        	  	p1, p4, p3, p3		// left, back face
        	  	p2, p3, p4, p4		// right, back face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    }
	    if(!Double.isNaN(height)){
	    	points[3][0]=xcenter*(1-height); points[3][1]=0.5f - 0.5f*height;  points[3][2]= ShapeUtils.height*height -zcenter; //p4
	    	points[4][0]=xcenter*(1-height);  points[4][1]=-0.5f + 0.5f*height; points[4][2]= ShapeUtils.height*height -zcenter; //p5
	    	points[5][0]=-xcenter*2.0f*(1-height);  points[5][1]=0.0f; points[5][2]= ShapeUtils.height*height -zcenter; //p6
	    	if(top && bottom){
	    		int[] serie = {0,3,4,1, 2,5,3,0, 1,4,5,2, 0,1,2,2, 3,5,4,4};
	    		/*p1, p4, p5, p2,    // front face
        	  	p3, p6, p4, p1,      // left face
        	  	p2, p5, p6, p3,      // right face
        	  	p1, p2, p3,	p3,      // bottom face
	    	  	p4, p6, p5,	p5,	    // top face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    	if(!top && bottom){
	    		int[] serie = {0,3,4,1, 2,5,3,0, 1,4,5,2, 0,1,2,2};
	    		/*p1, p4, p5, p2,    // front face
        	  	p3, p6, p4, p1,      // left face
        	  	p2, p5, p6, p3,      // right face
        	  	p1, p2, p3,	p3,      // bottom face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    	if(!top && !bottom){
	    		int[] serie = {0,3,4,1, 2,5,3,0, 1,4,5,2};
	    		/*p1, p4, p5, p2,    // front face
        	  	p3, p6, p4, p1,      // left face
        	  	p2, p5, p6, p3,      // right face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    	if(top && !bottom){
	    		int[] serie = {0,3,4,1, 2,5,3,0, 1,4,5,2, 3,5,4,4};
	    		/*p1, p4, p5, p2,    // front face
        	  	p3, p6, p4, p1,      // left face
        	  	p2, p5, p6, p3,      // right face
	    	  	p4, p6, p5,	p5,	    // top face*/
	    		for(int i=0;i<totalN;i++){
	    			for (int j=0; j<3; j++){
	    				data[i][0][j] = points[serie[i*4]][j];
	    				data[i][1][j] = points[serie[i*4+1]][j];
	    				data[i][2][j] = points[serie[i*4+2]][j];
	    				data[i][3][j] = points[serie[i*4+3]][j];
	    			}
	    		}
	    	}
	    }
	   return data;
	}
}
