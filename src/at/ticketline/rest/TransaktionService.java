package at.ticketline.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ticketline.dao.interfaces.AuffuehrungDao;
import at.ticketline.dao.interfaces.PlatzDao;
import at.ticketline.dao.interfaces.TransaktionDao;
import at.ticketline.entity.Auffuehrung;
import at.ticketline.entity.Platz;
import at.ticketline.entity.PlatzStatus;
import at.ticketline.entity.Reihe;
import at.ticketline.entity.Transaktion;
import at.ticketline.entity.Transaktionsstatus;
import at.ticketline.entity.Zahlungsart;
import at.ticketline.log.LogFactory;
import at.ticketline.log.Logger;

import com.sun.jersey.spi.inject.Inject;

/**
 * Implements the RESTFUL service for events.
 * 
 * @author Florian Westreicher aka meredrica
 */
@Component
@Scope("request")
@Path("transaktion")
public class TransaktionService {
	
	protected Logger log = LogFactory.getLogger(TransaktionService.class);
	
	private TransaktionDao transaktionDao;
	
	private AuffuehrungDao auffuehrungDao;
	
	private PlatzDao platzDao;
	

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public JSONArray getTransaktionen() throws Exception {
    	List<Transaktion> transaktionen = this.transaktionDao.findAll();
		JSONArray toReturn = new JSONArray();

		for (Transaktion t : transaktionen) {
			JSONObject o = JSONHelper.fromTransaktion(t);
    		JSONArray a = new JSONArray();
    		for (Platz p: t.getPlaetze()) {
    			a.put(JSONHelper.fromPlatz(p));
    		}
    		o.put("plaetze", a);
    		toReturn.put(o);
    	}
    	
		return toReturn;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("id/{id}")
    @Transactional
    public JSONObject getTransaktion(@PathParam("id") Integer id) throws Exception {
    	JSONObject result = new JSONObject();

    	Transaktion t = this.transaktionDao.findById(id);
    	if (t == null) {
    		return result;
    	} else {
    		result = JSONHelper.fromTransaktion(t);
    		JSONArray a = new JSONArray();
    		for (Platz p: t.getPlaetze()) {
    			a.put(JSONHelper.fromPlatz(p));
    		}
    		result.put("plaetze", a);
    		return result;
    	}
    }
    

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("delete/{id}")
    @Transactional
    public JSONObject deleteTransaktion(@PathParam("id") Integer id) throws Exception {
    	this.transaktionDao.remove(this.transaktionDao.findById(id));
    	JSONObject toReturn = new JSONObject();
    	toReturn.put("result", "ok");
    	return toReturn;
    }
    
    @POST
    @Path("createReservierung")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public JSONObject createReservierung(JSONObject reservierung) throws Exception {
    	JSONObject result = new JSONObject();
    	JSONArray resultPlaetze = new JSONArray();
    	
    	int auffuehrungId = reservierung.getInt("auffuehrung_id");
    	Auffuehrung a = this.auffuehrungDao.findById(auffuehrungId);
    	
    	// Temporäres Array mit allen belegten Plätzen in der Form reiheId_platzNummer
    	List<String> belegtePlaetze = new ArrayList<String>();
    	for (Reihe r : a.getSaal().getReihen()) {
    		for (Platz p : r.getPlaetze()) {
    			if ((p.getStatus() == PlatzStatus.GEBUCHT) || (p.getStatus() == PlatzStatus.RESERVIERT))
    				belegtePlaetze.add(r.getId() + "_" + p.getNummer());
    		}
    	}
    	
    	JSONArray plaetze = reservierung.getJSONArray("plaetze");
    	for (int i = 0; i < plaetze.length(); i++) {
    		JSONObject p = plaetze.getJSONObject(i);
    		if (belegtePlaetze.contains(p.get("reihe") + "_" + p.get("nummer"))) {
    			resultPlaetze.put(p);
    			if (log.isDebugEnabled())
    				log.debug("Belegter Platz: Reihe " + p.get("reihe") + " Nummer " + p.get("nummer"));
    		}
    	}
    	
    	if (resultPlaetze.length() > 0) {
    		result.put("status", "FEHLGESCHLAGEN");
    		result.put("plaetze", resultPlaetze);
    		return result;
    	}
    	
    	Transaktion t = new Transaktion();
    	t.setDatumuhrzeit(new Date());
    	t.setReservierungsnr(23);
    	t.setStatus(Transaktionsstatus.RESERVIERUNG);
    	t.setZahlungsart(Zahlungsart.BAR);
    	t = this.transaktionDao.persist(t);
    	t.setReservierungsnr(t.getId());
    	
    	for (int i = 0; i < plaetze.length(); i++) {
    		JSONObject p = plaetze.getJSONObject(i);
    		
    		if (log.isDebugEnabled())
    			log.debug("Reservierung für Reihe " + p.get("reihe") + ", Nummer " + p.get("nummer"));
    		
    		Platz platz = new Platz();
    		platz.setNummer(p.getInt("nummer"));
    		platz.setStatus(PlatzStatus.RESERVIERT);
    		
    		// Transaktion
    		platz.setTransaktion(t);
    		t.getPlaetze().add(platz);
    		
    		// Auffuehrung
    		platz.setAuffuehrung(a);
    		a.getPlaetze().add(platz);
    		
    		// Reihe
    		Reihe r = null;
    		Integer reiheId;
    		try {
    			reiheId = new Integer(p.get("reihe").toString());
    		} catch (NumberFormatException nfe) {
    			throw new RuntimeException("Reihe " + p.get("reihe") + " ist keine gültige Zahl");
    		}
    		for (Reihe reihe : a.getSaal().getReihen()) {
    			if (reiheId.equals(reihe.getId())) {
    				r = reihe;
    				break;
    			}
    		}
    		if (r != null) {
    			platz.setReihe(r);
    			r.getPlaetze().add(platz);
    		} else {
    			throw new RuntimeException("Reihe " + p.get("reihe") + " nicht gefunden");
    		}
    		
    		this.platzDao.persist(platz);
    	}
    	
    	result.put("status", "ERFOLGREICH");
    	result.put("transaktion_id", t.getId());
    	return result;
    }

    // meredrica: does not work right now, i have no idea how to tell
    // tomcat to consume a json object.
//    @GET
//    @Produces("application/json")
//    @Path("create")
//    @Transactional
//    public JSONObject createTransaktion(Transaktion t) throws Exception {
//    	this.transaktionDao.persist(t);
//    	JSONObject toReturn = new JSONObject();
//    	toReturn.put("result", "ok");
//    	return toReturn;
//    }
//    
    

	@Inject("transaktionDao")
	public void setTransaktionDao(TransaktionDao transaktionDao) {
		this.transaktionDao = transaktionDao;
	}

	public TransaktionDao getTransaktionDao() {
		return this.transaktionDao;
	}

	@Inject("auffuehrungDao")
	public void setAuffuehrungDao(AuffuehrungDao auffuehrungDao) {
		this.auffuehrungDao = auffuehrungDao;
	}
	
	public AuffuehrungDao getAuffuehrungDao() {
		return auffuehrungDao;
	}

	public PlatzDao getPlatzDao() {
		return platzDao;
	}

	@Inject("platzDao")
	public void setPlatzDao(PlatzDao platzDao) {
		this.platzDao = platzDao;
	}



}

