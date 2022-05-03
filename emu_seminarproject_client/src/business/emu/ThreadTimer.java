package business.emu;

import java.sql.SQLException;

import business.BasisModel;
import business.Messung;
import net.sf.yad2xx.FTDIException;

public class ThreadTimer extends Thread {
	private EmuCheckConnection ecc;
	private int zeitIntervall;
	private int messreihenId;
	private String messgroesse;
	private static int laufendeNummer; // static nur, falls laufendenNummer instanzübergreifend verwendet werden soll
										// (laufendenNummer + Messgroesse sind Primaerschluessel!
	private boolean istAufnahmeGestart = false;
	private BasisModel basisModel;

	public ThreadTimer(int messreihenId, int zeitIntervall, String messgroesse) {
		this.basisModel = BasisModel.getInstance();
		this.messreihenId = messreihenId;
		this.zeitIntervall = zeitIntervall;
		this.messgroesse = messgroesse;
		laufendeNummer = 1;
	}

	public void starteMessreihe() {
		this.istAufnahmeGestart = true;
		this.start();
	}

	public void stoppeMessreihe() {
		this.istAufnahmeGestart = false;
	}

	private void speichereMessungInDb(int messreihenId, Messung messung) throws ClassNotFoundException, SQLException {
		this.basisModel.speichereMessungInDb(messreihenId, messung);
	}

	public void run() {
		while (this.istAufnahmeGestart) {
			Messung messung;
			try {
				EmuCheckConnection ecc = new EmuCheckConnection();
				ecc.connect();
				Thread.sleep(1000);
				ecc.sendProgrammingMode();
				Thread.sleep(1000);

				if (this.messgroesse.contains("Leistung")) {
					ecc.sendRequest(MESSWERT.Leistung);
				} else if (this.messgroesse.contains("Scheinleistung")) {
					ecc.sendRequest(MESSWERT.Scheinleistung);
				} else if (this.messgroesse.contains("Induktive Blindleistung")) {
					ecc.sendRequest(MESSWERT.Induktive_Blindleistung);
				} else if (this.messgroesse.contains("Kapazitive Blindleistung")) {
					ecc.sendRequest(MESSWERT.Kapazitive_Blindleistung);
				} else if (this.messgroesse.contains("Arbeit")) {
					ecc.sendRequest(MESSWERT.Arbeit);
				} else if (this.messgroesse.contains("Strom")) {
					ecc.sendRequest(MESSWERT.Strom);
				} else if (this.messgroesse.contains("Spannung")) {
					ecc.sendRequest(MESSWERT.Spannung);
				} else {
					ecc.sendRequest(MESSWERT.Leistung);
				}
				Thread.sleep(1000);
				ecc.disconnect();

				messung = new Messung(laufendeNummer, ecc.gibErgebnisAus());
				laufendeNummer++;

				this.speichereMessungInDb(this.messreihenId, messung);

				sleep(this.zeitIntervall * 1000); // Mal 1000 wegen ms

			} catch (FTDIException ftdiExc) {
				System.out.println("FTDIException");
				ftdiExc.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
