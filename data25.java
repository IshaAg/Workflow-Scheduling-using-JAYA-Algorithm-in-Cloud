package org.cloudbus.cloudsim.examples;

//25 tasks and 3 processors

public class data25 {

	public static double TP1[][] = {{1.23 ,1.12, 1.25},  //Task x PC
            {1.27 ,0.17, 1.28},
            {0.13 ,1.21, 2.11},
            {1.26 ,1.12, 0.14},
            {1.89 ,1.14, 1.22},
            {1.2 ,0.47, 1.2},
            {0.13 ,0.11, 1.11},
            {1.26 ,1.62, 0.14},
            {1.13 ,0.12, 1.25},
            {1.8 ,1.14, 0.42},
            {1.23 ,1.12, 1.25},  //Task x PC
            {1.2 ,0.17, 1.28},
            {0.13 ,1.11, 2.8},
            {1.26 ,1.12, 0.14},
            {1.66 ,1.1, 1.22},
            {1.27 ,1.7, 1.28},
            {0.13 ,1.11, 1.11},
            {1.6 ,1.62, 0.14},
            {1.13 ,1.12, 1.25},
            {1.9 ,0.14, 0.42},
            {0.13 ,0.11, 2.11},
            {1.6 ,1.12, 0.14},
            {1.89 ,1.14, 1.22},
            {1.0 ,1.47, 1.2},
            {0.43 ,1.11, 1.7}};
	public static double PP1[][] = {{0.00, 0.17, 0.21},	//PC x PC Communiction cost
			 {0.17, 0.00, 0.22},
			 {0.21, 0.22, 0.00}};
	
	public static void main(String args [])
	{
		System.out.println(TP1[2][2]);
	}
	
}