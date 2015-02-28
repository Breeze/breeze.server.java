package com.breeze.testutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

	public static void write(Object obj, String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(obj);
			out.close();
			fileOut.close();
			System.out.println("Serializer: wrote to " + path);
		} catch (Exception ex) {
			throw new RuntimeException(path, ex);
		}

	}

	public static Object read(String path) {
		try {
			System.out.println("Serializer: reading from " + path);
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object obj = in.readObject();
			in.close();
			fileIn.close();
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException(path, ex);
		}
	}
	
	public static void writeString(String str, String path) {
		try {
			FileWriter writer = new FileWriter(path);
			writer.write(str);
			writer.close();
			System.out.println("Serializer: wrote to " + path);
		} catch (Exception ex) {
			throw new RuntimeException(path, ex);
		}
	}
	
	public static String readString(String path) {
		try {
			System.out.println("Serializer: reading from " + path);
			File file = new File(path);
			char[] buf = new char[(int) file.length()];
			FileReader reader = new FileReader(file);
			reader.read(buf);
			reader.close();
			return new String(buf);
		} catch (Exception ex) {
			throw new RuntimeException(path, ex);
		}
	}
}
