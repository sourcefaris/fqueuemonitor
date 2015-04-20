package com.fqueue.form;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.fqueue.QueueCentral;
import com.fqueue.common.DataManager;
import com.fqueue.common.NumberFormater;
import com.fqueue.common.TimeCategory;
import com.fqueue.server.SpellNumber;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.effects.ImagePainter;
import com.jeta.forms.gui.form.GridView;
import com.rubean.rcms.ui.RubeanLabel;
import com.rubean.rcms.ui.RubeanPanel;

public class CallDisplayScreen extends RubeanPanel implements PropertyChangeListener{

	public CallDisplayScreen()
	{
		try
		{
			jbInit();
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	public void init()
	{
		QueueCentral queuecentral = QueueCentral.getInstance();
		queuecentral.addPropertyChangeListener(this);

		SwingUtilities.invokeLater(new Runnable() {

			public void run()
			{
				adjustStatusLabels();
			}

		});
	}

	private void adjustStatusLabels()
	{
		counterNo = man.getCounterNo(QueueCentral.getInstance().getIpAddress());
		deskNo = String.valueOf(counterNo);
		message = QueueCentral.getInstance().getMessage();
		queueNo = String.valueOf(calledQueueNo);
	}
	
	private void jbInit() throws Exception {
		man = new DataManager();
//		man.executeSqlScript();
		branchCode= man.getParameter("branchcode");
		HashMap map = man.getSumQueueBranchInfo();
		
		if(branchCode!=null)
			map.put("branchCode",branchCode);
		if(openBranchDate!=null)
			map.put("openBranchDate",openBranchDate);
		
		windowWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		
		panel = new FormPanel("com/fqueue/res/CallDisplayScreen_Kurs(1280x720).jfrm");
		gridCallDisplayScreen = (GridView) panel.getComponentByName("gridCallDisplayScreen");
		ImagePainter ip = new ImagePainter(new ImageIcon(CallDisplayScreen.class.getResource("/com/fqueue/res/Qdisplay_img_ver2.jpg")), 0, 0);	
		gridCallDisplayScreen.setBackgroundPainter(ip);	
		
		no1 = (RubeanLabel) panel.getComponentByName("no1");
		no2 = (RubeanLabel) panel.getComponentByName("no2");
		no3 = (RubeanLabel) panel.getComponentByName("no3");
		no4 = (RubeanLabel) panel.getComponentByName("no4");
		counter1 = (RubeanLabel) panel.getComponentByName("counter1");
		counter2 = (RubeanLabel) panel.getComponentByName("counter2");
		counter3 = (RubeanLabel) panel.getComponentByName("counter3");
		counter4 = (RubeanLabel) panel.getComponentByName("counter4");
		lblCounter1 = (RubeanLabel) panel.getComponentByName("lblCounter1");
		lblCounter2 = (RubeanLabel) panel.getComponentByName("lblCounter2");
		lblCounter3 = (RubeanLabel) panel.getComponentByName("lblCounter3");
		lblCounter4 = (RubeanLabel) panel.getComponentByName("lblCounter4");
		
		timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		listQueue = new ArrayList();
		
		arrayNo = new String[5];
		arrayCounter = new String[5];
		arrayWaitTime = new String[5];

		for (int i=4; i>=0; i--){
			arrayNo[i] = "0";
			arrayCounter[i] = "0";
			arrayWaitTime[i] = STR_WAIT_TIME+"00:00:00";

		}
		
		setBlinkNo(1);
		
		callThread =  new CallThread();
		callThread.start();
		if(!man.getParameter("displayScr").equalsIgnoreCase("no video")){
			moviePanel = (RubeanPanel) panel.getComponentByName("moviePanel");	
			movieThread();
		}
			
		
		
		t = new Timer(500, blinkTimeActionListener);
		this.add(panel);
	}
	
	public synchronized void propertyChange(final PropertyChangeEvent e)
	{
		SwingUtilities.invokeLater(new Runnable() {

			public void run(){	
				String serviceName = man.getServiceName(QueueCentral.getInstance().getIpAddress());
				counterNo = man.getCounterNo(QueueCentral.getInstance().getIpAddress());
				deskNo = String.valueOf(counterNo);
				qNumberBefore=man.getQueueBefore(deskNo);
				if(e.getPropertyName().equals("appServerOnline"))
					adjustStatusLabels();
				else
					if("Call".equals(e.getPropertyName())){	
						insertListQueue(serviceName,deskNo);
						updateData();
					} else if("klinikAnak".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_ANAK);
					} else if("klinikBedah".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_BEDAH);
					} else if("klinikObsgyn".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_OBSGYN);
					} else if("klinikPenyakitDalam".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_PENYAKIT_DALAM);
					} else if("klinikSyaraf".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_SYARAF);
					} else if("klinikTHTMata".equals(e.getPropertyName())){
						if (!qNumberBefore.equalsIgnoreCase("0"))
							transferLayanan(qNumberBefore,man.Q_KLINIK_THT_MATA);
					} else if("Finish".equals(e.getPropertyName())){
						updateFinishBefore(serviceName);
					} else if("UpdateQueueDisplay".equals(e.getPropertyName())){
//						callWS();
					} 
			}
		});
	}
	
	public void insertListQueue(String serviceName,String deskNo){
		setRecall(false); 
		message = QueueCentral.getInstance().getMessage();
		
		if ("finish".equalsIgnoreCase(message)){
			updateFinishBefore(serviceName);
			calledQueueNo = getNextCalledNo(serviceName);
		} else if ("noshow".equalsIgnoreCase(message)){
			calledQueueNo = getRecallNo(serviceName, deskNo);
			setRecall(true);
			int callHit = man.getCustomerCallHit(String.valueOf(calledQueueNo));
			if (callHit >= new Integer(man.getParameter(man.callhitCode)).intValue() ){
				updateNoShowBefore(serviceName);
				calledQueueNo = getNextCalledNo(serviceName);
				setRecall(false);
			}
		}
	
		if (calledQueueNo != 0){
			queueNo = String.valueOf(calledQueueNo);
			listQueue.add(new String[]{queueNo, deskNo});
			setCalledQueueNo(calledQueueNo);			
		}else 
			queueNo=String.valueOf(0);		
	}
	
	private String defineService(String service){
		String ser="";
		if(service.equals(man.Q_KASIR)||service.equals(man.Q_REGISTRASI))
			ser="'Registrasi','Kasir'";
		else if(service.equals(man.Q_KLINIK_ANAK))
			ser="'Klinik Anak'";
		else if(service.equals(man.Q_KLINIK_BEDAH))
			ser="'Klinik Bedah'";
		else if(service.equals(man.Q_KLINIK_OBSGYN))
			ser="'Klinik Obsgyn'";
		else if(service.equals(man.Q_KLINIK_PENYAKIT_DALAM))
			ser="'Penyakit Dalam'";
		else if(service.equals(man.Q_KLINIK_SYARAF))
			ser="'Klinik Syaraf'";
		else if(service.equals(man.Q_KLINIK_THT_MATA))
			ser="'Klinik THT dan Mata'";
		
		return ser;
	}
	
	/*function for call next queue depend on priority*/
	private int getNextCalledNo(String service){
		String ser=defineService(service);
		Map map=null;
		if (man.Q_KASIR.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KASIR)!=null)			
				calledQueueNo= Integer.parseInt(map.get(man.Q_KASIR).toString());
			else if(map.get(man.Q_REGISTRASI)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_REGISTRASI).toString());
			else
				calledQueueNo=0;
		}

		if (man.Q_REGISTRASI.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_REGISTRASI)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_REGISTRASI).toString());
			else if(map.get(man.Q_KASIR)!=null)			
				calledQueueNo= Integer.parseInt(map.get(man.Q_KASIR).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_ANAK.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_ANAK)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_ANAK).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_BEDAH.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_BEDAH)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_BEDAH).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_OBSGYN.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_OBSGYN)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_OBSGYN).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_PENYAKIT_DALAM.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_PENYAKIT_DALAM)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_PENYAKIT_DALAM).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_SYARAF.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_SYARAF)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_SYARAF).toString());
			else
				calledQueueNo=0;
		}
		
		if (man.Q_KLINIK_THT_MATA.equalsIgnoreCase(service)){
			map= man.getNextQueueNo(ser);
			if(map.get(man.Q_KLINIK_THT_MATA)!=null)
				calledQueueNo= Integer.parseInt(map.get(man.Q_KLINIK_THT_MATA).toString());
			else
				calledQueueNo=0;
		}
		
		
		return calledQueueNo;
	}
	
	private int getRecallNo(String service, String deskNo){
		calledQueueNo=0;
		String ser=defineService(service);
		calledQueueNo = man.getRecallQueueNo(ser, deskNo);
		
		return calledQueueNo;
	}
	
	private void setCalledNo(String qNo, String dNo) throws Exception{
		if (isRecall()){
			recallQueueNo(qNo, dNo);
			if (getBlinkNo() == 0){
				setBlinkNo(1);
				for (int i=4; i>0; i--){
					arrayNo[i] = arrayNo[i-1];
					arrayCounter[i] = arrayCounter[i-1];
					arrayWaitTime[i] = arrayWaitTime[i-1];

					if(i==1){
						arrayNo[i] = qNo;
						arrayCounter[i] = dNo;
						arrayWaitTime[i]=STR_WAIT_TIME+man.getQueueWaitTime(qNo, true);

					}
				}
			}
		} else {
			setBlinkNo(1);
			for (int i=4; i>0; i--){
				arrayNo[i] = arrayNo[i-1];
				arrayCounter[i] = arrayCounter[i-1];
				arrayWaitTime[i] = arrayWaitTime[i-1];

				if(i==1){
					arrayNo[i] = qNo;
					arrayCounter[i] = dNo;
					arrayWaitTime[i]=STR_WAIT_TIME+man.getQueueWaitTime(qNo, false);

				}
			}
		}
		for (int i=4; i>0; i--){
			no = (RubeanLabel) panel.getComponentByName("no"+i);
			counter = (RubeanLabel) panel.getComponentByName("counter"+i);
			waitTime = (RubeanLabel) panel.getComponentByName("waitTime"+i);

			((RubeanLabel) no).setText(setLeadingZero(arrayNo[i]));
			((RubeanLabel) counter).setText(arrayCounter[i]);
			((RubeanLabel) waitTime).setText(arrayWaitTime[i]);

		}
		
		t.start();
	}
	
	private String setLeadingZero(String str){
		NumberFormat formatter = new DecimalFormat("000");
		String res = formatter.format(Integer.parseInt(str));
		return res;
	}
	
	public void movieThread()
	{
				try {
					URL url = new URL("file:/" + man.getParameter(man.moviePathCode));
					MediaPanel mediaPanel = new MediaPanel(url);
					moviePanel.add(mediaPanel);
					moviePanel.setVisible(true);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
	}
	
	class CallThread extends Thread{
		public void run(){
			while (true){
				try {
					if (listQueue.size()>0){
						adjustStatusLabels();
						String spell[] = (String[]) listQueue.get(0);
						setCalledNo(spell[0],spell[1]);
						SpellNumber.PlaySound(spell[0], spell[1]);
						t.stop();
						listQueue.remove(0);
						setAllTextVisible();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
		
	public String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		time = sdf.format(cal.getTime());
		return time;
	}
		
	private ActionListener blinkTimeActionListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			count++;
			no = panel.getComponentByName("no"+getBlinkNo());
			counter = panel.getComponentByName("counter"+getBlinkNo());
			lblCounter = panel.getComponentByName("lblCounter"+getBlinkNo());
			waitTime=panel.getComponentByName("waitTime"+getBlinkNo());

			if (count % 2 == 0){
				no.setVisible(false);
				counter.setVisible(false);
				lblCounter.setVisible(false);
				waitTime.setVisible(false);

			}else{
				no.setVisible(true);
				counter.setVisible(true);
				lblCounter.setVisible(true);
				waitTime.setVisible(true);

			}
		}
	};
	
	
	class BottomTextThread extends Thread{
		public void run(){
			bottomTimer.start();
		}
	}
	
	private ActionListener bottomTextTimer = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			x--;
			bottomText.setLocation(x, 0);
			if (x<-800)	x = 1280;
		}
	};
	
	
	public void setAllTextVisible(){
		for (int i=1; i<=4; i++){
			no = panel.getComponentByName("no"+i);
			counter = panel.getComponentByName("counter"+i);
			lblCounter = panel.getComponentByName("lblCounter"+i);
			waitTime = panel.getComponentByName("waitTime"+i);
			no.setVisible(true);
			counter.setVisible(true);
			lblCounter.setVisible(true);
			waitTime.setVisible(true);
		}
	}
	
	public void recallQueueNo(String queueNo, String deskNo){
		setBlinkNo(0);
		for (int i=1; i<=4; i++){
			no = (RubeanLabel) panel.getComponentByName("no"+i);
			counter = (RubeanLabel) panel.getComponentByName("counter"+i);
			int qNo = Integer.parseInt(queueNo);
			int noText = Integer.parseInt(((RubeanLabel) no).getText());
			if (qNo == noText){
				if (deskNo.equals(((RubeanLabel) counter).getText())){
					setBlinkNo(i);
				}
			}
		}
	}
	
	//database  --------------------------------------------------------------------------
	private void updateData(){
		String date = timeFormat.format(new Date());
		int callHit = man.getCustomerCallHit(queueNo);
		String category = TimeCategory.getCategory(new Date());
		String query = null;
		if (callHit == 0){
			String args[] = {date, date, String.valueOf(callHit+1), deskNo, man.STATUS_SERVED, category, queueNo, man.STATUS_FINISH_SERVED};
			query = "UPDATE process set TIME_CALL = ?, TIME_LAST_CALL = ?, CALL_HIT = ?, COUNTER_NO = ?, STATUS = ?, " +
					"PERIOD_SERVED = ? where NO = ? " +
					"and curdate() = DATE_FORMAT(process.`TIME`,GET_FORMAT(DATE,'ISO')) " +
					"and status <> ? " +
					"and COUNTER_NO = 0 ORDER BY QUEUE_INDEX limit 1";			
			man.updateData(args, query);
		}else if (callHit>0 && callHit< new Integer(man.getParameter(man.callhitCode)).intValue() ){
			String args[] = {date, String.valueOf(callHit+1), deskNo, man.STATUS_SERVED, category, queueNo, man.STATUS_FINISH_SERVED};
			query = "UPDATE process set TIME_LAST_CALL = ?, CALL_HIT = ?, COUNTER_NO = ?, STATUS = ?, " +
					"PERIOD_SERVED = ? where NO = ? " +
					"and curdate() = DATE_FORMAT(process.`TIME`,GET_FORMAT(DATE,'ISO')) " +
					"and status <> ? ORDER BY NO, QUEUE_INDEX limit 1";
			man.updateData(args, query);
		} 
		
	}
	
	private void updateFinishBefore(String serviceName){
		String date = timeFormat.format(new Date());
		String category = TimeCategory.getCategory(new Date());
		
		String args[] = {date, man.STATUS_FINISH_SERVED, category, serviceName, man.getQueueBefore(deskNo), deskNo};
		String query = "UPDATE process set TIME_FINISH = ?, STATUS = ?, PERIOD_FINISHED = ?, CALL_BY=?  " +
						"where NO = ? and COUNTER_NO = ? and " +
						"curdate() = DATE_FORMAT(process.`TIME`,GET_FORMAT(DATE,'ISO')) ";
		man.updateData(args, query);
	}

	private void updateNoShowBefore(String serviceName){
		String date = timeFormat.format(new Date());
		String category = TimeCategory.getCategory(new Date());
		
		String args[] = {date, man.STATUS_NOSHOW, category, serviceName, man.getQueueBefore(deskNo), deskNo, man.STATUS_SERVED};
		String query = "UPDATE process SET TIME_FINISH = ?, STATUS = ?, PERIOD_FINISHED = ?,CALL_BY=?  " +
				       "WHERE NO = ? AND COUNTER_NO = ? AND " +
				       "STATUS = ? AND " +
					   "curdate() = DATE_FORMAT(process.`TIME`,GET_FORMAT(DATE,'ISO')) ";

		man.updateData(args, query);
	}
	
	/*insert to db since switch to cs is happened > saat cs send to teller*/
	private void transferLayanan(String qNo,String service){
		String date = timeFormat.format(new Date());
		String dateDefault ="0000-00-00 00:00:00";
		String category = TimeCategory.getCategory(new Date());
		String args[] = {qNo, date, dateDefault, dateDefault,dateDefault, man.START_STATUS, null, 
			     String.valueOf((man.getMaxQueueIndex()+1)), man.NOT_CALLED, 
			     category, null, null, man.getParameter(man.branchCode), man.START_COUNTER, service,null};
		String query = "INSERT INTO process VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		man.insertData(args, query);
		
	}
	
	//setter getter------------------------------------------------------------------
	public int getBlinkNo() {
		return blinkNo;
	}
	public void setBlinkNo(int blinkNo) {
		this.blinkNo = blinkNo;
	}
	public boolean isRecall() {
		return recall;
	}
	public void setRecall(boolean recall) {
		this.recall = recall;
	}
	
	private String branchCode ="099"; //default value
	private Calendar openBranchDate = Calendar.getInstance();
	String qNumberBefore="";
	Thread mThread = null;
	CallThread callThread = null;
	BottomTextThread btThread = null;
	private FormPanel panel = null;
	private RubeanLabel no1 = null;
	private RubeanLabel no2 = null;
	private RubeanLabel no3 = null;
	private RubeanLabel no4 = null;
	private RubeanLabel counter1 = null;
	private RubeanLabel counter2 = null;
	private RubeanLabel counter3 = null;
	private RubeanLabel counter4 = null;
	private RubeanLabel lblCounter1 = null;
	private RubeanLabel lblCounter2 = null;
	private RubeanLabel lblCounter3 = null;
	private RubeanLabel lblCounter4 = null;
	private RubeanLabel bottomText = null;
	private RubeanPanel moviePanel = null;
	private Component no = null;
	private Component counter = null;
	private Component lblCounter = null;
	private Component waitTime = null;

	private DataManager man = null;
	private int counterNo;
	private static int calledQueueNo;
	private int blinkNo;
	private String queueNo = null;
	private String deskNo = null;
	private String message = "";
	private SimpleDateFormat timeFormat = null;
	private List listQueue = null;
	private String arrayNo[] = null;
	private String arrayCounter[] = null;
	private String arrayWaitTime[] = null;

	private Timer t = null;
	private Timer bottomTimer = null;
	private int count = 0;
	private int x = 1280;
	private boolean recall = false;
	private int windowWidth = 1024;
	private NumberFormater numFormat= new NumberFormater();
	private String time = "00:00:00";
	private final String  STR_WAIT_TIME="Waktu Tunggu ";
	private String scrType = "" ;
	private String novideo = "";
	private GridView gridCallDisplayScreen =null;
	
	public static int getCalledQueueNo() {
		return calledQueueNo;
	}
	
	public void setCalledQueueNo(int calledQueueNo) {
		CallDisplayScreen.calledQueueNo = calledQueueNo;
	}
}
