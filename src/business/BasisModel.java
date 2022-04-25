package business;

import javafx.collections.*;

import java.io.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;

import business.db.DbAktionen;
import business.emu.ThreadTimer;

public final class BasisModel {
	
	private static BasisModel basisModel;
	private ThreadTimer threadTimer = null;
	
	public static BasisModel getInstance(){
		if (basisModel == null){
			basisModel = new BasisModel();
		}
		return basisModel;
	}
	
	private BasisModel(){		
	}
	
	private DbAktionen dbAktionen = new DbAktionen();
	
	// wird zukuenftig noch instanziiert
	private ObservableList<Messreihe> messreihen = null; 
	
	public Messung[] leseMessungenAusDb(int messreihenId)
		throws ClassNotFoundException, SQLException{
		Messung[] ergebnis = null;
		this.dbAktionen.connectDb();
		ergebnis = this.dbAktionen.leseMessungen(messreihenId);
		this.dbAktionen.closeDb();
		return ergebnis;
	} 
	
	public void speichereMessungInDb(int messreihenId, Messung messung)
		throws ClassNotFoundException, SQLException{
		this.dbAktionen.connectDb();
		this.dbAktionen.fuegeMessungEin(messreihenId, messung);
		this.dbAktionen.closeDb();
	} 
	
	public void leseMessreihenInklusiveMessungenAusDb()
		throws ClassNotFoundException, SQLException{
		this.dbAktionen.connectDb();
		Messreihe[] messreihenAusDb 
		    = this.dbAktionen.leseMessreihenInklusiveMessungen(); 
		this.dbAktionen.closeDb();
		int anzahl = this.messreihen.size();
		for(int i = 0; i < anzahl; i++){
		    this.messreihen.remove(0);
		}
		for(int i = 0; i < messreihenAusDb.length; i++){
			this.messreihen.add(messreihenAusDb[i]);
		} 
	}
		  
	public void speichereMessreiheInDb(Messreihe messreihe)
	  	throws ClassNotFoundException, SQLException{
	  	this.dbAktionen.connectDb();
	  	this.dbAktionen.fuegeMessreiheEin(messreihe);
	  	this.dbAktionen.closeDb();
	  	this.messreihen.add(messreihe);
	} 
	
  	public String getDaten(){
    	return "in getDaten";
	}
  	
  	public void starteMessreihe(int messreihenId, int laufendeNummer) {
  		this.threadTimer = new ThreadTimer(messreihenId, laufendeNummer);
  		this.threadTimer.starteMessreihe();
  	}
  	
  	public void stoppeMessreihe() {
  		this.threadTimer.stoppeMessreihe();
  	}
 }
