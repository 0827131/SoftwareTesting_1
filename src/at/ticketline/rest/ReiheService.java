package at.ticketline.rest;

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

import com.sun.jersey.spi.inject.Inject;

import at.ticketline.dao.interfaces.ReiheDao;
import at.ticketline.entity.Platz;
import at.ticketline.entity.Reihe;


/**
 * Implements the RESTFUL service for Rows.
 * 
 * @author Florian Westreicher aka meredrica
 */
@Component
@Scope("request")
@Path("reihe")
public class ReiheService {

	private ReiheDao reiheDao;
	
	
	
	@GET
	@Path("list")
	@Produces("application/json")
	public JSONArray getReihen() throws Exception {
		JSONArray toReturn = new JSONArray();
		
		List<Reihe> list = this.reiheDao.findAll();
		for (Reihe r: list) {
			toReturn.put(JSONHelper.fromReihe(r));
		}
		return toReturn;
	}
	
	
	@GET
	@Produces("application/json")
	@Path("{id}")
	@Transactional
	public JSONObject getReihe(@PathParam("id") Integer id) throws Exception {
		JSONObject toReturn = new JSONObject();
		Reihe r = this.reiheDao.findById(id);
		if (r == null) {
			return toReturn;
		} else {
			toReturn = JSONHelper.fromReihe(r);
			JSONArray plaetze = new JSONArray();
			
			for (Platz p : r.getPlaetze()) {
				plaetze.put(JSONHelper.fromPlatz(p));
			}
			toReturn.put("plaetze", plaetze);
			
			return toReturn;
		}
	}

	
	@Inject("reiheDao")
	public void setReiheDao(ReiheDao reiheDao) {
		this.reiheDao = reiheDao;
	}
	
	public ReiheDao getReiheDao() {
		return this.reiheDao;
	}
}
