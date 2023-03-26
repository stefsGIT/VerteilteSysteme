package aufgabe_1;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Thread{
    private static String[] KEY = null; //Die Schlüsselliste, die die MD5-Hashwerte der Dateien enthält, kann nur von einem der drei Server gespeichert werden, da ansonsten die Werte mehrfach vorhanden wären.
	private static int PORTNUMBER; //Alle aktiven Portnummern der Server werden hier gespeichert.
	private static DatagramSocket CLIENTSOCKET; //
	private static InetAddress IPADDRESS; //

	public Client(int portnummer) throws IOException {
		PORTNUMBER = portnummer;
		CLIENTSOCKET = new DatagramSocket(); // Client Socket wird erstellt
		IPADDRESS = InetAddress.getByName("localhost"); // Erstellt die IP-Adresse des lokalen Hosts für Netzwerkverbindungen in Java.
	}

	public void run() {
		try {
			main(null);
		} catch (Exception e) {
			e.printStackTrace();
		} //Der Code ruft die main()-Methode auf und gibt bei Auftreten von Ausnahmen den Stack-Trace aus.
	}
    public static void main(String[] args) throws IOException {
		boolean sendAgain = true;
		KEY = new String[300];
		int keySlot = 0;
		while(sendAgain) {
			System.out.println("---------------------------------------------------------------------------------------");
			System.out.println("Sie haben die Wahl zwischen zwei Optionen: Wenn Sie eine Datei senden und speichern möchten, geben Sie 'SAVE' ein, und wenn Sie eine Nachricht lesen möchten, geben Sie 'GET' ein.");
			byte[] receiveData = new byte[1024];
				switch(messageInput()) {
					case "SAVE":
						System.out.println("Sie können jetzt Ihre Nachricht eingeben, die gespeichert werden soll. Bitte achten Sie darauf, die Nachricht mit einem Satzzeichen zu beenden und sie darf maximal 80 Zeichen lang sein.");
						String input = messageInput();
						if(checkMessage(input)) {
							DatagramPacket packetToSend = new DatagramPacket(("SAVE "+input).getBytes(), ("SAVE "+input).getBytes().length, IPADDRESS, PORTNUMBER);
							CLIENTSOCKET.send(packetToSend);
							DatagramPacket getKeyfromServer = new DatagramPacket(receiveData, receiveData.length);
							CLIENTSOCKET.receive(getKeyfromServer); //Client bekommt ACK von allen Server
							System.out.println("KEY "+new String(getKeyfromServer.getData(), 0, getKeyfromServer.getLength()));
							KEY[keySlot] = new String(getKeyfromServer.getData(), 0, getKeyfromServer.getLength());
							keySlot++;
						}
						else {
							System.out.println("Die Nachricht entspricht nicht den Vorgaben!");
						}
							break;
					case "GET":
								System.out.println("Sie können einen Schlüssel aus der oben stehenden Schlüsselliste auswählen und diesen dann erneut in die Konsole eingeben.");
								for (String key : KEY) {
									if(key != null) {
									System.out.println(key);
									}
								}
								String inputKey = messageInput();
								DatagramPacket sendRequest = new DatagramPacket(("GET " + inputKey).getBytes(), ("GET " + inputKey).getBytes().length, IPADDRESS, PORTNUMBER); //hier wird einem beliebigen Server eine Nachricht zugesendet.
								CLIENTSOCKET.send(sendRequest); //An diesem Punkt wird die Nachricht versendet.
								DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
								CLIENTSOCKET.receive(receivedPacket); //Hier wird dem Client der Hashwert im DatagramPacket zugewiesen.
								if ((new String(receivedPacket.getData(), 0, receivedPacket.getLength())).startsWith("FAILED")){
									System.out.println("Es ist ein Fehler aufgetreten!");
									break;
								}
								System.out.println("Folgende Informationen sind in der angeforderten Datei enthalten:");
								System.out.println((new String(receivedPacket.getData(), 0, receivedPacket.getLength())).substring(3)); //Die ersten drei Zeichen (`OK ´) der Zeichenkette werden abgeschnitten. 
								break;
					}
			}
		CLIENTSOCKET.close();
	}

	private static String messageInput() {
		BufferedReader userResponse = new BufferedReader(new InputStreamReader(System.in));
		try {
			String input = userResponse.readLine();
			if(input.length()>80) { //Nachrichten über 80 Zeichen sind ungültig.
				System.out.println("Bitte geben Sie eine Nachricht ein, die den Anforderungen entspricht!");
				return messageInput();//Falls eine Nachricht zu lang ist, dann wird ein neuer messageInput gefordert
			}
			return input;
		} catch (IOException e) {
			e.printStackTrace();
			return "FAILED"; // Wenn ein Fehler bei Nachrichteneingabe auftritt, dann wird FAILED ausgegeben.
		}
	}

	private static Boolean checkMessage(String Message){
		if (Message.endsWith(";") || Message.endsWith(".")||Message.endsWith("?")||Message.endsWith("!")||Message.endsWith(":")){
			if (Message.length()<=80){
				return true;
			}
		}
		return false;
	}
}