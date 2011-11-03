package at.ticketline.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ticketline.dao.interfaces.AuffuehrungDao;
import at.ticketline.entity.Auffuehrung;
import at.ticketline.entity.Platz;
import at.ticketline.entity.PlatzStatus;
import at.ticketline.entity.PreisKategorie;
import at.ticketline.entity.Reihe;

import com.sun.jersey.spi.inject.Inject;

@Component
@Scope("request")
@Path("auffuehrung")
public class AuffuehrungService {
	
	private AuffuehrungDao auffuehrungDao;

	
	@GET
	@Path("list")
	@Produces("application/json")
	public JSONArray getAuffuehrungen() throws Exception {
		JSONArray toReturn = new JSONArray();
		List<Auffuehrung> list = this.auffuehrungDao.findAll();
		for (Auffuehrung a : list){
			toReturn.put(JSONHelper.fromAuffuehrung(a));
		}
		return toReturn;
	}
	
	@GET
	@Produces("application/json")
	@Path("{id}")
	@Transactional
	public JSONObject getAuffuehrung(@PathParam("id") Integer id) throws Exception {
		Auffuehrung a = this.auffuehrungDao.findById(id);
		JSONObject toReturn = new JSONObject();
		if (a == null) {
			return toReturn;
		} else {
			toReturn = JSONHelper.fromAuffuehrung(a);
			
		}
		
		JSONObject saal = JSONHelper.fromSaal(a.getSaal());
		
		JSONArray reihen = new JSONArray();
		for (Reihe r : a.getSaal().getReihen()) {
			JSONObject reihe = JSONHelper.fromReihe(r);
			reihen.put(reihe);
		}
		saal.put("reihen", reihen);
		toReturn.put("saal", saal);
		
		JSONArray plaetze = new JSONArray();
		for (Platz p : a.getPlaetze()) {
			plaetze.put(JSONHelper.fromPlatz(p));
		}
		toReturn.put("plaetze", plaetze);
    	return toReturn;

	}
	
	@GET
	@Produces("application/json")
	@Path("belegung/{id}")
	@Transactional
	public JSONObject getAuffuehrungBelegung(@PathParam("id") Integer id) throws Exception {
		Auffuehrung a = this.auffuehrungDao.findById(id);
		JSONObject result = new JSONObject();
		if (a == null) {
			return result;
		} else {
			result = JSONHelper.fromAuffuehrung(a);
		}
		
		result.put("veranstaltung", JSONHelper.fromVeranstaltung(a.getVeranstaltung()));
		JSONObject saal = JSONHelper.fromSaal(a.getSaal());
		saal.put("reihen", a.getSaal().getReihen().size());
		result.put("saal", saal);
		result.put("ort", JSONHelper.fromOrt(a.getSaal().getOrt()));
		
		List<JSONObject> belegung = new ArrayList<JSONObject>();
		
		// Compute maximal Platz count
		int maxPlaetze = 0;
		for (Reihe r : a.getSaal().getReihen()) {
			if (r.getStartplatz() + r.getAnzplaetze() > maxPlaetze)
				maxPlaetze = r.getStartplatz() + r.getAnzplaetze() - 1;
		}
		
		for (Reihe r : a.getSaal().getReihen()) {
			JSONObject reihe = new JSONObject();
			reihe.put("id", r.getId());
			reihe.put("bezeichnung", r.getBezeichnung());
			reihe.put("startplatz", r.getStartplatz());
			reihe.put("kategorie_id", r.getKategorie().getId());
			reihe.put("kategorie_bezeichnung", r.getKategorie().getBezeichnung());
			switch (a.getPreis()) {
			case MAXIMALPREIS:
				reihe.put("kategorie_preis", r.getKategorie().getPreismax());
				break;
			case MINDESTPREIS:
				reihe.put("kategorie_preis", r.getKategorie().getPreismin());
				break;
			case STANDARDPREIS:
				reihe.put("kategorie_preis", r.getKategorie().getPreisstd());
				break;
			}

			List<JSONObject> plaetze = new ArrayList<JSONObject>();
			int maxPlatzReihe =  r.getStartplatz() + r.getAnzplaetze() - 1;
			// Create list of Plaetze
			for (int i = 1; i <= maxPlaetze; i++) {
				JSONObject p = new JSONObject();
				p.put("id", 0);
				p.put("nummer", i);
				if ((i < r.getStartplatz()) || (i > maxPlatzReihe))
					p.put("status", "LEER");
				else
					p.put("status", PlatzStatus.FREI.toString());
				p.put("transaktion_id", 0);
				plaetze.add(p);
			}
			
			// Set the status of the Plaetzee
			for (Platz platz : r.getPlaetze()) {
				JSONObject p = plaetze.get(platz.getNummer() - 1);
				p.put("id", platz.getId());
				p.put("status", platz.getStatus().toString());
				p.put("transaktion_id", platz.getTransaktion().getId());
			}
			reihe.put("plaetze", plaetze);
			
			belegung.add(reihe);
		}
		saal.put("plaetze", maxPlaetze);
		result.put("belegung", belegung);

    	return result;

	}

	
	@Inject("auffuehrungDao")
	public void setAuffuehrungDao(AuffuehrungDao auffuehrungDao) {
		this.auffuehrungDao = auffuehrungDao;
	}
	
	public AuffuehrungDao getAuffuehrungDao() {
		return auffuehrungDao;
	}
	
}
