/**
 * 
 */
package at.ticketline.rest;

import java.text.SimpleDateFormat;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import at.ticketline.entity.Adresse;
import at.ticketline.entity.Auffuehrung;
import at.ticketline.entity.Kategorie;
import at.ticketline.entity.Ort;
import at.ticketline.entity.Platz;
import at.ticketline.entity.Reihe;
import at.ticketline.entity.Saal;
import at.ticketline.entity.Transaktion;
import at.ticketline.entity.Veranstaltung;

/**
 * Creates JSON objects out of other objects. To avoid deadlock circles, this
 * helper class will only read data from the given object directly and will
 * never try to transform other objects (contained in Sets etc).
 * 
 * @author Florian Westreicher aka meredrica
 * 
 */
public class JSONHelper {

	/**
	 * Transforms the given {@link Auffuehrung} into a JSONObject.
	 * 
	 * @param a
	 *            The Auffuehrung to transform.
	 * @return The resulting JSONObject.
	 * @throws JSONException
	 *             if data could not be put into the json object.
	 */
	public static JSONObject fromAuffuehrung(final Auffuehrung a) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("id", a.getId());
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		toReturn.put("date", sdf.format(a.getDatumuhrzeit()));
		toReturn.put("hinweis", a.getHinweis());
		toReturn.put("storniert", a.isStorniert());
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		toReturn.put("datum", dateFormat.format(a.getDatumuhrzeit()));
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		toReturn.put("uhrzeit", timeFormat.format(a.getDatumuhrzeit()));
		toReturn.put("preiskategorie", a.getPreis());
		return toReturn;
	}
	
	public static JSONObject fromReihe(final Reihe r) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("anzplaetze", r.getAnzplaetze());
		toReturn.put("bezeichnung", r.getBezeichnung());
		toReturn.put("id", r.getId());
		toReturn.put("startplatz", r.getStartplatz());
		toReturn.put("sitzplatz", r.isSitzplatz());
		return toReturn;
	}
	
	public static JSONObject fromTransaktion(final Transaktion t) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("datumuhrzeit", t.getDatumuhrzeit());
		if (t.getKunde() != null)
			toReturn.put("kundeid",t.getKunde().getId());
		if (t.getMitarbeiter() != null)
			toReturn.put("mitarbeiterid",t.getMitarbeiter().getId());
		toReturn.put("reservierungsnr",t.getReservierungsnr());
		toReturn.put("status",t.getStatus());
		toReturn.put("version",t.getVersion());
		toReturn.put("id", t.getId());
		toReturn.put("zahlungsart", t.getZahlungsart());
		return toReturn;
		
	}
	
	public static JSONObject fromPlatz(final Platz p) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("id", p.getId());
		toReturn.put("nummer", p.getNummer());
		toReturn.put("status", p.getStatus());
		return toReturn;
	}
	
	public static JSONObject fromVeranstaltung(final Veranstaltung v) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("id", v.getId());
		toReturn.put("bezeichnung", v.getBezeichnung());
		toReturn.put("dauer", v.getDauer());
		return toReturn;
	}
	
	public static JSONObject fromSaal(final Saal s) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("bezeichnung", s.getBezeichnung());
		toReturn.put("id", s.getId());
		toReturn.put("kostenprotag", s.getKostenprotag());
		return toReturn;
	}
	
	public static JSONObject fromOrt(final Ort o) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("besitzer", o.getBesitzer());
		toReturn.put("bezeichnung", o.getBezeichnung());
		toReturn.put("id", o.getId());
		toReturn.put("oeffnungszeiten", o.getOeffnungszeiten());
		toReturn.put("telnr", o.getTelnr());
		toReturn.put("kiosk", o.isKiosk());
		toReturn.put("verkaufsstelle", o.isVerkaufsstelle());
		toReturn.put("adresse", fromAdresse(o.getAdresse()));
		return toReturn;
	}
	
	public static JSONObject fromAdresse(final Adresse a) throws JSONException {
		JSONObject toReturn = new JSONObject();
		toReturn.put("land", a.getLand());
		toReturn.put("ort", a.getOrt());
		toReturn.put("plz", a.getPlz());
		toReturn.put("strasse", a.getStrasse());
		return toReturn;
	}
	
	/*
	 * Copy paste template
	
	public static JSONObject from(final ) throws JSONException {
		JSONObject toReturn = new JSONObject();
		
		return toReturn;
	}
	
	*/
}
