package aufgabe_1;

import java.io.*;
import java.net.*;

public class Server extends Thread{

//private static String KEY;
private static DatagramSocket SERVERSOCKET;

	//Das Programm welches der Server in einer Schleife abspielt
	public static void main(String args[]) {
		try {
			String[] demand = new String[2]; //Array in dem auf Slot 0 der Befehl "Save" oder "GET" steht und auf Slot 1 die Nachricht
			while (true) {
				byte[] byteMessage = new byte[1024]; //Hier wird die eingegebene Nachricht in byte abgespeichert
				DatagramPacket receivePacket = new DatagramPacket(byteMessage, byteMessage.length); //Ein neues Datenpaket wird erstellt mit der leeren byte message
				SERVERSOCKET.receive(receivePacket); //Speichert die gesendete Date in das leere Datenpaket
				String message = new String(receivePacket.getData()); //Holt die message aus dem Datenpaket
				demand = message.split(" ", 2); //Teilt die Message und legt den Befehl (GET, SAVE) auf Slot 0 und die Nachricht auf Slot 1
				System.out.println("RECEIVED: " + message); //Gibt die empfangene Nachricht einmal aus
				switch(demand[0]) { //Chekt ob auf Slot 1 SAVE oder GET steht und ruft entweder die Methode zum speichern oder zum schreiben auf
				case "SAVE":
					saveData(receivePacket, byteMessage);
					break;
				case "GET":
					getData(receivePacket, byteMessage);
					break;
				}
			}
		}
		catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} 
		catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	//Konstruktor	
	public Server(int portnumber) throws SocketException {
		SERVERSOCKET = new DatagramSocket(portnumber); //Die Eingegebene Portnummer wird dem Serversocket übergeben
		File theDirectory = new File(getDirectory()); //Speichert einen Pfad auf dem die Dateien abgelegt werden sollen
		if(!theDirectory.exists()) { //Prüft ob schon ein Ordner existiert
			theDirectory.mkdirs(); //Legt einen neuen Ordner an
		}
	}

	//Run Methode	
	public void run() {
		String[] args = new String[1];
		System.out.println("Server wird gestartet."); //Gibt die start Nachricht aus
		main(args); //Startet den Thread
	}

	
		
		//Speichert die Nachricht in einem txt Dokument
		private static void saveData(DatagramPacket receivedPacket, byte[] aReceiveData) {
			PrintWriter writer;
			String message = new String(receivedPacket.getData(), 0, receivedPacket.getLength()); //Erstellt einen String aus dem Byte code (offset= "der Startpunkt")
			message = message.substring(0, message.length() - 1); //Schneidet das Ausrufezeichen am Ende ab
			String[] demand = message.split(" ", 2); //Aufteilung in Befehl (GET, SAVE) und die tatsächliche Nachricht
			String KEY = createKey(demand[1]); //Generiert einen Schlüssel und legt ihn in der globalen Variable ab
			try {
				writer = new PrintWriter(new FileWriter(createFile(getDirectory(), KEY))); // hier wird die .txt Datei erstellt.
				writer.write(demand[1]); //Nachricht wird in Datei geschrieben.
				writer.flush(); //Stream leeren
				writer.close(); //Stream beenden
				sendData(receivedPacket, KEY); //ServerSocket wird mitgegeben, damit der Schluessel an den Client zurueckgesendet werden kann.	
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		//konstruiert einen Md5-Hashwert aus dem mitgegebenen String und gibt ihn als String zurueck
		private static String createKey(String input) {
			   try {
			        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			        byte[] array = md.digest(input.getBytes());
			        StringBuffer stringbuffer = new StringBuffer();
			        for (int i = 0; i < array.length; ++i) {
			          stringbuffer.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			       	}
			        return stringbuffer.toString();
			    } 
				catch (java.security.NoSuchAlgorithmException e) {
			    }
			    return null;
		}
		
		//Sendet den Inhalt der Nachricht aus dem eingegebenen KEY
		private static void getData(DatagramPacket receivedPacket, byte[] aReceiveData) throws IOException {
			String messageContent = new String(receivedPacket.getData(),0,receivedPacket.getLength()); //Speichert den Inhalt aus der Nachricht
			String[] aKey = messageContent.split(" ", 2); //Trennt zwischen Befehl (GET) und dem tatsächlichen Key
			sendData(receivedPacket, readData(aKey[1], getDirectory())); // Das Datenpaket und der Nachrichten Inhalt werden zum Senden übergeben
		}
			
		//Liest mit dem übergebenen Pfad und dem Schlüssel die Nachricht aus
		private static String readData(String aKey, String dataPath) throws IOException {
			File[] folder = (new File(dataPath)).listFiles();
			String dataToRead = "FAILED Das File existiert nicht.";
			if(folder.length != 0) {
				for (int i = 0; i < folder.length; i++) {
					if(folder[i].getName().equals(aKey+".txt")) {
						FileReader input = new FileReader(folder[i]);
						char[] array = new char[75];
						input.read(array);
						input.close();
						String buffer = "OK "+Character.toString(array[0]);
						for (int j = 1; j < array.length; j++) {
							buffer = buffer + Character.toString(array[j]);
						}
						dataToRead = buffer;
					}
				}
			}
		    return dataToRead;
		}
		
		//sendet die übergebene Nachricht an den Absender des übergebenen Packet
		private static void sendData(DatagramPacket receivedPacket, String message) {
			InetAddress IPAddress = receivedPacket.getAddress(); //Liest die Adresse des Empfangenen Paket aus
			int port = receivedPacket.getPort(); // Liest den Port des Empfangenen Paket aus
			DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), IPAddress, port); // Erstellt ein neues Paket mit den daten:(Nachricht in Bytes, Anzahl der Bytes, Adressse, Port)
			try {
				SERVERSOCKET.send(sendPacket); //Sendet das paket an Client
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		
		private static String getDirectory() {
			String userPathWithSlash = System.getProperty("user.dir"); 
			String userPath = userPathWithSlash.replace("\\", "_");
			String[] cutOff = userPath.split("_");
			userPath = cutOff[0] + "\\" + cutOff[1] + "\\" + cutOff[2];
			String filePath = userPath + "\\Desktop\\messages\\AUFGABE_1"; // eigenen Pfad des Users herausfinden
			return filePath;
		}
		
		private static File createFile(String aFilePath, String KEY) {
			File file = new File(aFilePath + "\\" + KEY + ".txt"); // Datei wird am festgelegten Pfad abgespeichert.
			return file;
		} 
}
