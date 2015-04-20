package com.fqueue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import com.fqueue.form.CallDisplayScreen;
import com.rubean.rcms.cash.FatalExceptionEvent;
import com.rubean.rcms.cash.FatalExceptionListener;
import com.rubean.rcms.cash.FatalInitException;

public class QueueCentral implements PropertyChangeListener {
	private class SocketHost extends Thread {
		public void run(){
			int portNumber = 9999;
			try {  
				/* establish server socket */
				ServerSocket s = new ServerSocket(portNumber); //Port number to bind
				System.out.println ("echoServer is waiting for connection ....");   

				/* wait for a client connection and if it comes, accepts it */
				while (running){
					Socket incoming = s.accept(); 

					BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
					PrintWriter out = new PrintWriter(incoming.getOutputStream(), true /* autoFlush */);


					//Reading input here ...
					boolean done = false;
					while (true) {  
						String line = in.readLine();
						if (line == null) done = true;
						else {  
							//							out.println("Echo from server : received: " + line);

							System.out.println ("Echo: " + line);
							String[] splitted = line.split(";");
							if ("Call".equals(splitted[0])){
								setIpAddress(splitted[1]);
								setMessage(splitted[2]);
								adjustNomorAntrian(splitted[0]);
								Thread.sleep(5000);
								if (CallDisplayScreen.getCalledQueueNo()!=0){
									out.println("CalledNo"+CallDisplayScreen.getCalledQueueNo());
									out.println("CallOk");	
								}else{
									out.println("CallNoQueue0");
								}

							} else if ("klinikAnak".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikAnakOK");
							} else if ("klinikBedah".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikBedahOK");
							} else if ("klinikObsgyn".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikObsgynOK");
							} else if ("klinikPenyakitDalam".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikPenyakitDalamOK");
							} else if ("klinikSyaraf".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikSyarafOK");
							} else if ("klinikTHTMata".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("klinikTHTMataOK");
							} else if ("Finish".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								Thread.sleep(3000);
								out.println("FinishOk");
							} else if ("UpdateQueueDisplay".equals(splitted[0])){
								setIpAddress(splitted[1]);
								adjustNomorAntrian(splitted[0]);
								out.println("UpdateQueueDisplayOk");
							}
							if (line.trim().equals("BYE"))
								break;
						}
					}

					//When done, closing the socket....

					incoming.close();
					System.out.println("Ready for next connection");
				}
			}
			catch (IOException e)
			{  
				System.err.println ("A communication Error occured: " + 
						e.getClass().getName() + ":  " + e.getMessage());
				e.printStackTrace();
				running = false;

			}
			catch (Exception e)
			{  
				e.printStackTrace();
				running = false;
			}
		}

		private SocketHost()
		{
			setDaemon(true);
			start();
		}

	}


	public QueueCentral()
	{
		propertyChangeSupport = new PropertyChangeSupport(this);
		fatalExceptionListeners = new ArrayList();
	}

	public void addFatalExceptionListener(FatalExceptionListener fatalexceptionlistener)
	{
		synchronized(fatalExceptionListeners)
		{
			fatalExceptionListeners.add(fatalexceptionlistener);
		}
	}

	public void removeFatalExceptionListener(FatalExceptionListener fatalexceptionlistener)
	{
		synchronized(fatalExceptionListeners)
		{
			fatalExceptionListeners.remove(fatalexceptionlistener);
		}
	}

	public void fatalException(Throwable throwable)
	{
		synchronized(fatalExceptionListeners)
		{
			for(Iterator iterator = fatalExceptionListeners.iterator(); iterator.hasNext(); ((FatalExceptionListener)iterator.next()).fatalExceptionOccurred(new FatalExceptionEvent(this, throwable)));
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener propertychangelistener)
	{
		propertyChangeSupport.addPropertyChangeListener(propertychangelistener);
	}

	public void removePropertyChangeListener(PropertyChangeListener propertychangelistener)
	{
		propertyChangeSupport.removePropertyChangeListener(propertychangelistener);
	}

	public void propertyChange(PropertyChangeEvent propertychangeevent)
	{
		if(propertychangeevent.getPropertyName().equals("queueEmpty")){

		}
	}


	private void readSystemProperties()
	throws FatalInitException
	{
		//        if(System.getProperty("offline.dir") == null)
		////            throw new FatalInitException("");
		//        offlineDir = new File(System.getProperty("offline.dir"));
		//        try
		//        {
		//            serverPollingInterval = Long.parseLong(System.getProperty("server.polling.interval"));
		//        }
		//        catch(Exception exception) { }
	}


	public String getIpAddress() {
		return ipAddress;
	}

	public synchronized void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getNomorAntrian() {
		return nomorAntrian;
	}
	private synchronized void setNomorAntrian(String nomorAntrian) {
		this.nomorAntrian = nomorAntrian;
	}
	public String getNomorMeja() {
		return nomorMeja;
	}

	public String getProcessNo() {
		return processNo;
	}

	public synchronized void setProcessNo(String processNo) {
		this.processNo = processNo;
	}

	public String getQueueIndex() {
		return queueIndex;
	}

	public synchronized void setQueueIndex(String queueIndex) {
		this.queueIndex = queueIndex;
	}

	private synchronized void setNomorMeja(String nomorMeja) {
		this.nomorMeja = nomorMeja;
	}

	private synchronized void adjustNomorAntrian(String callMsg){
		propertyChangeSupport.firePropertyChange(callMsg, null, null);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTimeFinish() {
		return timeFinish;
	}

	public void setTimeFinish(String timeFinish) {
		this.timeFinish = timeFinish;
	}

	public String getTimeServe() {
		return timeServe;
	}

	public void setTimeServe(String timeServe) {
		this.timeServe = timeServe;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public void init()
	throws FatalInitException
	{
		readSystemProperties();
		SocketHost host = new SocketHost();
	}

	public int getCalledQueueNo() {
		return calledQueueNo;
	}

	public void setCalledQueueNo(int calledQueueNo) {
		this.calledQueueNo = calledQueueNo;
	}

	public static QueueCentral getInstance()
	{
		if(instance == null)
			instance = new QueueCentral();
		return instance;
	}

	private static QueueCentral instance;
	private PropertyChangeSupport propertyChangeSupport;
	private ArrayList fatalExceptionListeners;

	private boolean running = true;
	private String nomorAntrian = "6";
	private String nomorMeja = "1";
	private String ipAddress = "10.87.19.164";
	private String queueIndex = "1";
	private String processNo = "0";
	private String time = null;
	private String timeServe = null;
	private String timeFinish = null;
	private String message = null;
	private int calledQueueNo;


}