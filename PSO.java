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
public class PSO {

	/** The cloudlet list. */
	private static List<Cloudlet2> cloudletList;

	/** The vmlist. */
	private static List<Vm3> vmlist;
	public static int n=10;
	public static int p=3;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample4...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 10;   // number of cloud users
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
			double v[] = new double[n];
			int pbest[] = new  int[n];
			int gbest=0;
			double task_cost[][] = new double[n][p];
			
			int x[] = new int[n];
			//int curr_allot[] = new int [5];
			
			for(i=0;i<n;i++) 			//random allotment of processors for each task
			{
				x[i] = i%3;
				pbest[i] = x[i];
			}
			task_cost = calculate_costs(TP,PP, data,x);	
			double mini = 9999999999999.0;
			for(i=0;i<n;i++)
			{
				if (mini > task_cost[i][pbest[i]])
				{
					mini = task_cost[i][pbest[i]];
					gbest = i; 
					//System.out.println("gbest : "+gbest);
				}
			}

			for(i=0;i<n;i++)
			{
				Random r = new Random();
				double r1 = r.nextDouble();
				v[i] = r1;
			}
			int iter = 10000;
			double exe_cost_final=0.0;
			for(i=0;i<iter;i++)
			{
				//System.out.println("gbest in while: "+gbest);
				//System.out.println(i);		
				exe_cost_final = iterations(v, pbest, gbest, x,TP,PP,data);
				
			}	
			long timeend = System.currentTimeMillis();
			for(i=0;i<n;i++)
				System.out.print(pbest[i] + " ");
			
			//BINDING OF FINAL MAPPING
			
			for(i=0;i<n;i++)
			{
				broker.bindCloudletToVm(i,pbest[i]);
			}
			
			System.out.println(exe_cost_final);
			System.out.println(timeend-timestart);

			//bind the cloudlets to the vms. This way, the broker
			// will submit the bound cloudlets only to the specific VM
			//broker.bindCloudletToVm(cloudlet1.getCloudletId(),vm1.getId());
			
			// Sixth step: Starts the simulation
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

        	printCloudletList(newList);

			Log.printLine("CloudSimExample4 finished!");
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
	
	public static double iterations(double v[], int pbest[], int gbest, int x[],double TP[][],double PP[][],double data[][])
	{
		//System.out.println("inside");
		double [][] task_cost=new double[n][3];
		task_cost=calculate_costs(TP,PP,data,x);

		int i;
		int[] temp= new int[n];
		double prev_cost = fitness(x, task_cost);
		for(i=0;i<n;i++)
		{
			v[i] = velocity(v[i], pbest[i], gbest, x[i]);
			int tmp = x[i] + (int)Math.round(v[i]);
			if (tmp>2 || tmp<0)
			{
				Random randomno = new Random();
				temp[i] = randomno.nextInt(3);
			}
			else
			{
				temp[i] = tmp;
			}

		}
		task_cost=calculate_costs(TP,PP,data,temp);	
		double min = 99999999.0;
		//System.out.println("hello111");
		double curr_cost = fitness(temp, task_cost);
		
		//System.out.println("Cost1 : "+curr_cost + "Prev cost :" + prev_cost );
		//compare
		if (curr_cost < prev_cost)
		{
			for(i=0;i<n;i++)
			{
				x[i]=temp[i];
				pbest[i] = x[i];
				//System.out.println("pbest : "+pbest[i]);
			}
			//System.out.println("Cost : "+curr_cost + "Prev cost :" + prev_cost );
			for(i=0;i<n;i++)
			{	
				if (min > task_cost[i][pbest[i]])
				{
					//System.out.println("hello66666");
					min = task_cost[i][pbest[i]];
					gbest = i; 
				}
			}
		}
		else
		{
			return prev_cost;
		}
		return curr_cost;
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

	public static double velocity(double v, double pbest, double gbest, double x)
	{
		Random r = new Random();
		//Random r2 = new Random();
		double r1 = r.nextDouble();
		double r2 = r.nextDouble();
		double inertia = 1.2;
		//System.out.println(r1);
		//System.out.println(r2);
		double new_v =  inertia*v + 2.0*r1*(pbest-x) + 2.0*r2*(gbest-x);
		return new_v;
	}

	public static double[][] calculate_costs(double TP[][], double PP[][], double data[][],int x[])
	{
		//sum of every task on each PC - exe cost of a PC
		
		double exec[] = new double[p];
		
		int i,j;
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
