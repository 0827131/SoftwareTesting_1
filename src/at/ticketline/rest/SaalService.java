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

import at.ticketline.dao.interfaces.SaalDao;
import at.ticketline.entity.Reihe;
import at.ticketline.entity.Saal;

import com.sun.jersey.spi.inject.Inject;

/**
 * Implements the RESTFUL service for halls.
 * 
 * @author Florian Westreicher aka meredrica
 */
@Component
@Scope("request")
@Path("saal")
public class SaalService {

	private SaalDao saalDao;

	@GET
	@Path("list")
	@Produces("application/json")
	public JSONArray getSaele() throws Exception {
		List<Saal> list = this.saalDao.findAll();
		JSONArray toReturn = new JSONArray();
		for (Saal s : list) {
			toReturn.put(JSONHelper.fromSaal(s));
		}
		return toReturn;
	}
	
	@GET
    @Produces("application/json")
    @Path("{id}")
    @Transactional
	public JSONObject getSaal(@PathParam("id") Integer id) throws Exception {
		Saal s = this.saalDao.findById(id);
		
		if (s == null) {
			return new JSONObject();
		} else {
			JSONObject toReturn = JSONHelper.fromSaal(s);
			JSONArray array = new JSONArray();
			for (Reihe r : s.getReihen()) {
				array.put(JSONHelper.fromReihe(r));
			}
			toReturn.put("reihen", array);
			return toReturn;
		}
		
	}
	
	@Inject("saalDao")
	public void setSaalDao(SaalDao saalDao) {
		this.saalDao = saalDao;
	}

	public SaalDao getSaalDao() {
		return saalDao;
	}
}
