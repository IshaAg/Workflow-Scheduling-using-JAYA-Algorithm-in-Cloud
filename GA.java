/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Cloudlet2;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Vm3;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * two datacenters with one host each and
 * run two cloudlets on them.
 */
public class GA {

	/** The cloudlet list. */
	private static List<Cloudlet2> cloudletList;

	/** The vmlist. */
	private static List<Vm3> vmlist;
	public static int n=10;
	public static int p=3;
	public static double g_fitness1,g_fitness2;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting GA...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the GridSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			//Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm3>();

			//VM description
			int vmid = 0;
			int mips = 250;
			long size = 10000; //image size (MB)
			int ram = 512; //vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; //number of cpus
			String vmm = "Xen"; //VMM name
			double procost[] = new double[3];
			//create two VMs
			double PP1[][] = {{0.00, 0.17, 0.21},	//PC x PC Communiction cost
					 {0.17, 0.00, 0.22},
					 {0.21, 0.22, 0.00}};
			for(int i=0;i<p;i++)
			{
				procost = PP1[i].clone();
				Vm3 vm1 = new Vm3(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared(), procost);
				vmid++;
				vmlist.add(vm1);
			}

			//submit vm list to the broker
			broker.submitVmList(vmlist);


			//Fifth step: Create two Cloudlets
			cloudletList = new ArrayList<Cloudlet2>();

			//Cloudlet properties
			int id = 0;
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			double taskaccess[] = new double[3];
			double taskdata[] = new double[2];
			double TP1[][] = {{1.23 ,1.12, 1.25},  //Task x PC
                    {1.27 ,0.17, 1.28},
                    {0.13 ,1.11, 2.11},
                    {1.26 ,1.12, 0.14},
                    {1.89 ,1.14, 1.22},
                    {1.27 ,0.47, 1.28},
                    {0.13 ,1.11, 1.11},
                    {1.26 ,1.62, 0.14},
                    {1.13 ,1.12, 1.25},
                    {1.89 ,1.14, 0.42}};
			double data1[][] = {{30,30}, {10,10},{10,10},{10,10},{30,60},{30,50},{30,20},{70,60},{30,40},{30,60}};
			for(int i=0;i<10;i++)
			{
				taskaccess = TP1[i].clone();
				taskdata = data1[i].clone();
				Cloudlet2 cloudlet1 = new Cloudlet2(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel,taskaccess,taskdata);
				cloudlet1.setUserId(brokerId);
				//add the cloudlets to the list
				cloudletList.add(cloudlet1);
				id++;

			}
						
		

			//submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);
			
			//creation of TP,PP,data from cloudlets and vmlist
			
			double TP[][] = new double[n][p];
			double PP[][] = new double[p][p];
			double data[][] = new double[n][2];
			int i;
			for(i=0;i<n;i++)
			{
				TP[i] = cloudletList.get(i).accesscost;
				data[i] = cloudletList.get(i).datasize;
			}
			
			
			for(i=0;i<p;i++)
			{
				PP[i] = vmlist.get(i).commcost;
			}
			
			
			long timestart = System.currentTimeMillis();
			double task_cost[][] = new double[n][p];
			double task_cost2[][] = new double[n][p];
			int x[] = new int[n];
			int x2[] = new int[n];
			//int curr_allot[] = new int [5];
			

			//random allotments
			for(i=0;i<n;i++) 			//random allotment of processors for each task
			{
				//x[i] = i%3;
				Random randomno = new Random();
				x[i] = randomno.nextInt(p);
				x2[i] = randomno.nextInt(p);
			}
			task_cost = calculate_costs(TP,PP, data,x);
			task_cost2 = calculate_costs(TP,PP, data,x2);	
			
			int join[] = new int[2*n];
			//iterations
			int iter = 10000;
			double exe_cost_final=0.0;
			int j;
			
			for(i=0;i<iter;i++)
			{		
				
				//System.out.println(x[0] + " " +x[1] + " " +x[2] + " "+ x[3] + " " + x[4]);
				//exe_cost_final = iterations(x,x2,TP,PP,data);
				
				join = iterations(x,x2,TP,PP,data);	
				for(j=0;j<n;j++)
					x[j] = join[j];
				for(j=n;j<2*n;j++)
					x2[j-n] = join[j];
				//System.out.println(g_fitness1);
			}	
			System.out.println("---------------------------------------------------------");
			//long timeend = System.currentTimeMillis();
			for(i=0;i<n;i++)
				System.out.print(x[i]+" ");
			System.out.println(g_fitness1);
			//System.out.println(timeend-timestart);
		

		

			long timeend1 = System.currentTimeMillis();
			
			//BINDING OF FINAL MAPPING
			
			for(i=0;i<n;i++)
			{
				broker.bindCloudletToVm(i,x[i]);
			}
			
			//System.out.println(g_fitness1);
			System.out.println(timeend1-timestart);

			//bind the cloudlets to the vms. This way, the broker
			// will submit the bound cloudlets only to the specific VM
			//broker.bindCloudletToVm(cloudlet1.getCloudletId(),vm1.getId());
			
			// Sixth step: Starts the simulation
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

        	printCloudletList(newList);

			Log.printLine("GA finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 10000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList.add(new Pe(1, new PeProvisionerSimple(mips)));
		
		//4. Create Host with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048*4; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;


		//in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
		//is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerSpaceShared(peList)
    			)
    		); // This is our first machine
		hostId++;
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerSpaceShared(peList)
    			)
    		);
		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	       DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
						indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}
	
	

	
	public static int[] iterations(int x[], int x2[],double TP[][], double PP[][], double data[][])
	{
		return crossover(x, x2,TP,PP,data);
	}
	
	public static int[] crossover(int x[], int x2[],double TP[][], double PP[][], double data[][])
	{
		Random randomno = new Random();
		int split = randomno.nextInt(n);
		int x_temp[] = x.clone();
		int x2_temp[] = x2.clone();
		double task_costs[][] = new double[n][p];
		double task_costs2[][] = new double[n][p];
		double task_costs_temp[][] = new double[n][p];
		double task_costs2_temp[][] = new double[n][p];


		while(split==0 || split==n-1)
			split = randomno.nextInt(n);

		for(int i=split;i<n;i++)
		{
			int temp = x_temp[i];
			x_temp[i] = x2_temp[i];
			x2_temp[i] = temp;
		}
		task_costs = calculate_costs(TP, PP, data, x);
		task_costs2 = calculate_costs(TP, PP, data, x2);
		task_costs_temp = calculate_costs(TP, PP, data, x_temp);
		task_costs2_temp = calculate_costs(TP, PP, data, x2_temp);

		double fit[] = new double[4];
		fit[0] = fitness(x, task_costs);
		fit[1] = fitness(x2, task_costs2);
		fit[2] = fitness(x_temp, task_costs_temp);
		fit[3] = fitness(x2_temp, task_costs2_temp);
		//System.out.println(fit[0] + " " +fit[1] + " " +fit[2] + " "+ fit[3] + " " );
		
		double min = 9999999.0;
		int index_1=0;
		int index_2=0;
		for(int i=0;i<4;i++)
		{
			if(min > fit[i])
			{
				min = fit[i];
				index_1 = i;
			}
		}

		double fitness1 = fit[index_1];
		g_fitness1 = fit[index_1];
		min = 9999999.0;
		for(int i=0;i<4;i++)
		{
			if(min > fit[i] && i!=index_1)
			{
				min = fit[i];
				index_2 = i;
			}
		}
		//System.out.println("index 1 : " + index_1 + " index 2 : "+ index_2);
		int p1[] = new int[n];
		int p2[] = new int[n];
		if(index_1==0)
			p1 = x.clone();
		else if(index_1==1)
			p1 = x2.clone();
		else if(index_1==2)
			p1 = x_temp.clone();
		else if(index_1==3)
			p1 = x2_temp.clone();

		//mutation

		double fitness2=0.0;

		if(index_2==0)
		{
			p2=mutation(x,fit[0],TP, PP, data);

			//p2  = x.clone();
		}
		else if(index_2==1)
		{
			p2 =mutation(x2, fit[1],TP, PP, data);
			//p2 = x2.clone();
		}	
		else if(index_2==2)
		{
			p2=mutation(x_temp,fit[2],TP, PP, data);
			//p2 = x_temp.clone();
		}	
		else if(index_2==3)
		{
			p2=mutation(x2_temp,fit[3],TP, PP, data);
			//p2 = x2_temp.clone();
		}	

		x = p1.clone();
		x2 = p2.clone();
		//System.out.println("fitness : "+ fitness2);
		if(fitness1 < g_fitness2)
		{
			x = p1.clone();
			x2 = p2.clone();
			//return fitness1;
		}
		else
		{
			x = p2.clone();
			x2 = p1.clone();
			//return fitness2;
		}
		//System.out.println("update : " + x[0] + " " +x[1] + " " +x[2] + " "+ x[3] + " " + x[4]);
		int join[] = new int[2*n];
		for(int i=0;i<n;i++)
			join[i] = x[i];
		for(int i=n;i<2*n;i++)
			join[i] = x2[i-n];
		return join;

	}

	public static int[] mutation(int x[], double fitness,double TP[][], double PP[][], double data[][])
	{
		Random randomno = new Random();
		int swap1 = randomno.nextInt(n);
		int swap2 = randomno.nextInt(n);

		while(swap1 == swap2)
		{
			swap1 = randomno.nextInt(n);
		    swap2 = randomno.nextInt(n);
		}
			
		int x_temp[] = new int[n];
		for(int i=0;i<n;i++)
			x_temp[i]=x[i];

		int temp = x_temp[swap1];
		x_temp[swap1] = x_temp[swap2];
		x_temp[swap2] = temp;

		double task_cost_temp[][] = new double[n][p];
		task_cost_temp = calculate_costs(TP,PP,data, x_temp);
		double fitness_temp = fitness(x_temp, task_cost_temp); 

		if(fitness_temp < fitness)
		{
			for(int i=0;i<n;i++)
			x[i]=x_temp[i];
		}

		if(fitness_temp < fitness)
		{
			g_fitness2 = fitness_temp;
			
		}	
		else
		{
			g_fitness2 = fitness;
		
		}
		return x; 

	}

	public static double fitness(int x[], double task_cost[][])
	{
		double total_cost = 0.0;
		int i=0;
		for(i=0;i<n;i++)
		{
			//System.out.println("x[i] : "+x[i]);
			total_cost+=task_cost[i][x[i]];
		}
		return total_cost;
	}

	public static double[][] calculate_costs(double TP[][], double PP[][], double data[][],int x[])
	{
		
		int i=0,j;
		//sum of every task on each PC - exe cost of a PC
		
		double exec[] = new double[p];
		
		
		double sum=0.0;
		for(i=0;i<n;i++)	//execution cost Cexe
		{
			
			exec[x[i]]+=TP[i][x[i]];
		}
		//for(i=0;i<3;i++)
		//	System.out.println("Exec cost: "+exec[i]);

		//Communication cost

		double comm_cost[] = new double[p];

		for(i=0;i<p;i++)
		{
			sum=0.0;
			for(j=0;j<p;j++)
			{
				sum+=PP[i][j];
			}
			comm_cost[i] = sum;
		}

		//Cost of every task on
		double task_cost[][] = new double[n][p];
		for(i=0;i<n;i++)
		{
			for(j=0;j<p;j++)
			{
				task_cost[i][j] = data[i][0]*comm_cost[j] + exec[j];
				//System.out.println(task_cost[i][j]);
			}
		}
		return task_cost;
	}


}
