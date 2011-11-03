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

import at.ticketline.dao.interfaces.AuffuehrungDao;
import at.ticketline.dao.interfaces.VeranstaltungDao;
import at.ticketline.entity.Auffuehrung;
import at.ticketline.entity.Veranstaltung;

import com.sun.jersey.spi.inject.Inject;

/**
 * Implements the RESTFUL service for events.
 * 
 * @author Florian Westreicher aka meredrica
 */
@Component
@Scope("request")
@Path("veranstaltung")
public class VeranstaltungService {
	
	private VeranstaltungDao veranstaltungDao;
	
	private AuffuehrungDao auffuehrungDao;

    @GET
    @Path("list")
    @Produces("application/json")
    public JSONArray getVeranstaltungen() throws Exception {
    	List<Veranstaltung> veranstaltungen = this.veranstaltungDao.findAll();
		JSONArray toReturn = new JSONArray();

		for (Veranstaltung v : veranstaltungen) {
    		toReturn.put(JSONHelper.fromVeranstaltung(v));
    	}
    	
		return toReturn;
    }
    
    @GET
    @Produces("application/json")
    @Path("{id}")
    @Transactional
    public JSONObject getVeranstaltung(@PathParam("id") Integer id) throws Exception {
    	JSONObject result = new JSONObject();

    	Veranstaltung v = this.veranstaltungDao.findById(id);
    	if (v == null) {
    		return result;
    	} else {
    		result = JSONHelper.fromVeranstaltung(v);

    		List<Object[]> auffuehrungen = this.auffuehrungDao.findByVeranstaltungId(id);
    		JSONArray resultAuffuehrungen = new JSONArray();

    		for (Object[] auffuehrung : auffuehrungen) {
    			Auffuehrung a = (Auffuehrung)auffuehrung[0];
    			resultAuffuehrungen.put(JSONHelper.fromAuffuehrung(a));
    		}
    		result.put("auffuehrungen", resultAuffuehrungen);

    		return result;
    	}
    }

	@Inject("veranstaltungDao")
	public void setVeranstaltungDao(VeranstaltungDao veranstaltungDao) {
		this.veranstaltungDao = veranstaltungDao;
	}

	public VeranstaltungDao getVeranstaltungDao() {
		return veranstaltungDao;
	}
	
	@Inject("auffuehrungDao")
	public void setAuffuehrungDao(AuffuehrungDao auffuehrungDao) {
		this.auffuehrungDao = auffuehrungDao;
	}
	
	public AuffuehrungDao getAuffuehrungDao() {
		return auffuehrungDao;
	}

}

