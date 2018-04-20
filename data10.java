package org.cloudbus.cloudsim.examples;

// 10 tasks and 3 processors
public class data10 {

	public static double TP1[][] ={{1.23 ,1.12, 1.25},  //Task x PC
            {1.27 ,0.17, 1.28},
            {0.13 ,1.11, 2.11},
            {1.26 ,1.12, 0.14},
            {1.89 ,1.14, 1.22},
            {1.27 ,0.47, 1.28},
            {0.13 ,1.11, 1.11},
            {1.26 ,1.62, 0.14},
            {1.13 ,1.12, 1.25},
            {1.89 ,1.14, 0.42}};
	public static double PP1[][] = {{0.00, 0.17, 0.21},	//PC x PC Communiction cost
			 {0.17, 0.00, 0.22},
			 {0.21, 0.22, 0.00}};
	
	public static void main(String args [])
	{
		System.out.println(TP1[2][2]);
	}
	
}
