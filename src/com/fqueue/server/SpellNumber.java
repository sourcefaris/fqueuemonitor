package com.fqueue.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import com.fqueue.common.DataManager;


public class SpellNumber {
	
	private static DataManager man = new DataManager();
	
	private static String baca(int x)
	{
		String[] bil = {"","Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"};
		if (x < 12)
			return "" + bil[x];
		else if (x < 20)
			return baca(x - 10) + "Belas";
		else if (x < 100)
			return baca(x / 10) + "Puluh" + baca(x % 10);
		else if (x < 200)
			return "Seratus" + baca(x - 100);
		else if (x < 1000)
			return baca(x / 100) + "Ratus" + baca(x % 100);
		else if (x < 2000)
			return "Seribu" + baca(x - 1000);
		else if (x < 1000000)
			return baca(x / 1000) + "Ribu" + baca(x % 1000);
		else if (x < 1000000000)
			return baca(x / 1000000) + "Juta" + baca(x % 1000000);
		return null;
	}
	
	public synchronized static void PlaySound(String number, String counter){
        try{  
        	int num = Integer.parseInt(number);
        	String number1;
        	String number2;
            List l = new ArrayList();
            AudioInputStream stream=null;
            DataLine.Info info=null;
            Clip clip=null;
//            String voicePath = man.getVoicePath();
            String voicePath = man.getParameter(man.voicePathCode);
            l.add(voicePath + "NomorAntrian.wav");
            
            if (num > 100 && num%100!=0){
            	int num1;
            	int num2;
            	num1 = num/100;
            	num2 = num%100;
            	number1 = String.valueOf(num1*100); 
            	number2 = String.valueOf(num2); 
            	l.add(voicePath + number1 + ".wav");
            	l.add(voicePath + number2 + ".wav");
            } else {
            	l.add(voicePath + number + ".wav");
            }             
            
            l.add(voicePath + "DimohonKeCounter.wav");
            l.add(voicePath + counter+ ".wav");
            
            for (int i=0; i<l.size(); i++){
            	stream = AudioSystem.getAudioInputStream(new File((String) l.get(i)));
//                AudioFormat format = stream.getFormat();
                info = new DataLine.Info(Clip.class, stream.getFormat());
                clip = (Clip) AudioSystem.getLine(info);
                                
                clip.open(stream);
                stream.close();
                clip.start();
                clip.drain();
//                clip.close();
//                System.out.println("PlaySound : "+clip.getMicrosecondLength()+" : "+l.get(i));
//                Thread.sleep(clip.getMicrosecondLength()/1000);
                Thread.sleep(clip.getMicrosecondLength()/800);
                clip.close();
            }
//            clip.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
	public static void main(String[] args){		
		
//		PlaySound(baca(27), baca(2));
		for (int i=198;i<250;i++){
			System.out.println("no "+i);
			PlaySound(new Integer(i).toString(), "1");	
		}
		
		
	}
	
	
}
