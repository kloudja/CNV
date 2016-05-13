
//

// StatisticsTool.java

//

// This program measures and instruments to obtain different statistics

// about Java programs.

//

// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).

// ALL RIGHTS RESERVED.

//

// Permission to use, copy, modify, and distribute this software and its

// documentation for non-commercial purposes is hereby granted provided 

// that this copyright notice appears in all copies.

// 

// This software is provided "as is".  The licensor makes no warrenties, either

// expressed or implied, about its correctness or performance.  The licensor

// shall not be liable for any damages suffered as a result of using

// and modifying this software.

import BIT.highBIT.*;

import java.io.File;

import java.util.Enumeration;

import java.util.HashMap;

import java.util.Vector;

public class MyTool

{

	private static HashMap<Worker, Integer> hash_aload_0 = new HashMap<>();

	private static HashMap<Worker, Integer> hash_getstatic = new HashMap<>();

	private static HashMap<Worker, Integer> hash_invokevirtual = new HashMap<>();

	private static HashMap<Worker, Integer> hash_ifeq = new HashMap<>();

	private static HashMap<Worker, Integer> hash_putstatic = new HashMap<>();

	private static HashMap<Worker, Integer> hash_GOTO = new HashMap<>();

	public static synchronized void aload_0(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_aload_0.containsKey(worker)) {

			hash_aload_0.put(worker, (hash_aload_0.get(worker) + 1));

		}

		else {

			hash_aload_0.put(worker, 1);

		}

	}

	public static synchronized void getstatic(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_getstatic.containsKey(worker)) {

			hash_getstatic.put(worker, (hash_getstatic.get(worker) + 1));

		}

		else {

			hash_getstatic.put(worker, 1);

		}

	}

	public static synchronized void invokevirtual(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_invokevirtual.containsKey(worker)) {

			hash_invokevirtual.put(worker, (hash_invokevirtual.get(worker) + 1));

		}

		else {

			hash_invokevirtual.put(worker, 1);

		}

	}

	public static synchronized void ifeq(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_ifeq.containsKey(worker)) {

			hash_ifeq.put(worker, (hash_ifeq.get(worker) + 1));

		}

		else {

			hash_ifeq.put(worker, 1);

		}

	}

	public static synchronized void putstatic(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_putstatic.containsKey(worker)) {

			hash_putstatic.put(worker, (hash_putstatic.get(worker) + 1));

		}

		else {

			hash_putstatic.put(worker, 1);

		}

	}

	public static synchronized void GOTO(int type)

	{

		Worker worker = (Worker) Thread.currentThread();

		if (hash_GOTO.containsKey(worker)) {

			hash_GOTO.put(worker, (hash_GOTO.get(worker) + 1));

		}

		else {

			hash_GOTO.put(worker, 1);

		}

	}

	public static void doLoadStore(ClassInfo ci)

	{

		for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();) {

			Routine routine = (Routine) e.nextElement();

			for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements();) {

				Instruction instr = (Instruction) instrs.nextElement();

				int opcode = instr.getOpcode();

				if (opcode == InstructionTable.aload_0)

					instr.addBefore("MyTool", "aload_0", new Integer(1));

				else if (opcode == InstructionTable.getstatic)

					instr.addBefore("MyTool", "getstatic", new Integer(1));

				else if (opcode == InstructionTable.invokevirtual)

					instr.addBefore("MyTool", "invokevirtual", new Integer(1));

				else if (opcode == InstructionTable.ifeq)

					instr.addBefore("MyTool", "ifeq", new Integer(1));

				else if (opcode == InstructionTable.putstatic)

					instr.addBefore("MyTool", "putstatic", new Integer(1));

				else if (opcode == InstructionTable.GOTO)

					instr.addBefore("MyTool", "GOTO", new Integer(1));

			}

		}

	}

	public static synchronized void printCost(String foo)

	{

		Worker worker = (Worker) Thread.currentThread();

		System.out.println(hash_aload_0.get(worker));

		System.out.println(hash_getstatic.get(worker));

		System.out.println(hash_invokevirtual.get(worker));

		System.out.println(hash_ifeq.get(worker));

		System.out.println(hash_putstatic.get(worker));

		System.out.println(hash_GOTO.get(worker));

		int custo = ((hash_aload_0.get(worker) * 5) + (hash_getstatic.get(worker) * 5)

				+ (hash_invokevirtual.get(worker) * 3) + (hash_ifeq.get(worker) * 3)

				+ (hash_GOTO.get(worker) * 3)) / 19;

		System.out.println("\tCOST: " + custo);

	}

	public static void main(String argv[])

	{

		File file = null;

		try {

			file = new File("/home/kloudja/Desktop/CNV/Server/IntFactorization.class");

		} catch (Exception e) {

			System.out.println("Erro ao tentar obter o ficheiro IntFactorization.class. Path errado!");

			System.err.println(e);

		}

		String in_filename = file.getAbsolutePath();

		ClassInfo ci = new ClassInfo(in_filename);

		doLoadStore(ci);

		ci.addAfter("MyTool", "printCost", "null");

		ci.write("/home/kloudja/Desktop/CNV/Server/Worker Group/IntFactorization.class");

	}

}