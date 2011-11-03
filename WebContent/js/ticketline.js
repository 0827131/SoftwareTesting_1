htmlId = {
	AJAX_LOADER : '#ajaxloader',
	TEMPLATES : '.template',
	VERANSTALTUNGEN : '#maincontent_veranstaltungen',
	VERANSTALTUNG : '#maincontent_veranstaltung',
	AUFFUEHRUNG : '#maincontent_auffuehrung',
	TRANSAKTIONEN : '#maincontent_transaktionen',
	BC_VERANSTALTUNG : '#breadcrumb_veranstaltung',
	BC_AUFFUEHRUNG : '#breadcrumb_auffuehrung',
	BC_LINK_VERANSTALTUNG : '#link_veranstaltung',
	BC_LINK_AUFFUEHRUNG : '#link_auffuehrung'
};

reservierung = {
	auffuehrung_id: null,
	plaetze: []
};

$(document).ready(
	function loader() {
		loadVeranstaltungen();
		loadTransaktionen();
	}
);

$(".veranstaltungLink").live('click',function(evt) {
	loadVeranstaltung(parseLinkId(evt.target.id));
});

$(".auffuehrungLink").live('click', function(evt) {
	loadAuffuehrung(parseLinkId(evt.target.id));
});
$(".platz").live('click', function(evt) {
	var platzId = evt.target.id;
	var platz = parsePlatzId(platzId);
	if ($("#" + platzId).hasClass("frei"))
		addReservierung(platz);
	else if ($("#" + platzId).hasClass("markiert"))
		removeReservierung(platz);
});
$("#executeReservierung").live('click', function(evt) {
	executeReservierung();
});

$(".transaktionLink").live('click',function(evt) {
	deleteTransaktion(parseLinkId(evt.target.id));
});

/*
 * Zerlegt eine LinkId und liefert nur die ID zurueck.
 * Eine LinkId setzt sich zusammen aus dem Namen der Entitaet, einem Underscore und der ID
 * zB veranstaltung_12
 */
function parseLinkId(linkId) {
	var parts = linkId.split("_");
	if (parts.length != 2) {
		throw "Invalid id: " + linkId;
		return null;
	}
	return parts[1];
}

function parsePlatzId(platzId) {
	var parts = platzId.split("_");
	if (parts.length != 3) {
		throw "Invalid Platz id: " + platzId;
		return null;
	}
	return { reihe: parts[1], nummer: parts[2]};
}

function addReservierung(platz) {
	reservierung.plaetze.push(platz);
	$("#platz_" + platz.reihe + "_" + platz.nummer).removeClass("frei");
	$("#platz_" + platz.reihe + "_" + platz.nummer).addClass("markiert");
}
function removeReservierung(platz) {
	var newPlaetze = [];
	for (var i = 0; i < reservierung.plaetze.length; i++) {
		var p = reservierung.plaetze[i];
		if ((p.reihe != platz.reihe) || (p.nummer != platz.nummer)) {
			newPlaetze.push(p);
		}
	}
	reservierung.plaetze = newPlaetze;
	$("#platz_" + platz.reihe + "_" + platz.nummer).removeClass("markiert");
	$("#platz_" + platz.reihe + "_" + platz.nummer).addClass("frei");
}

/*
 * AJAX CALLS
 */

function loadVeranstaltungen() {
	showAjaxLoader();
	$.ajax({
		url: "service/veranstaltung/list/",
		cache: false,
		success: function(data, status, request) {
			hideAjaxLoader();
			$('#veranstaltungLinks').empty();
			$('#template_veranstaltungen').tmpl(data).appendTo('#veranstaltungLinks');
		}, 
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		},
		dataType: "json"
	});
}

function loadVeranstaltung(id) {
	showAjaxLoader();
	$.ajax({
		url: "service/veranstaltung/" + id,
		cache: false,
		success: function(data, status, request) {
			// alert(data['id'] + ' ' + data['bezeichnung']);
			hideAjaxLoader();
			$(htmlId.VERANSTALTUNGEN).hide();
			$(htmlId.TRANSAKTIONEN).hide();
			$(htmlId.BC_LINK_VERANSTALTUNG).text(data['bezeichnung']);
			$(htmlId.BC_VERANSTALTUNG).show();
			$(htmlId.VERANSTALTUNG).empty();
			$('#template_veranstaltung').tmpl(data).appendTo(htmlId.VERANSTALTUNG);
			$(htmlId.VERANSTALTUNG).show();
		},
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		},
		dataType: "json"
	});
}

function loadAuffuehrung(id) {
	showAjaxLoader();
	$.ajax({
		url: "service/auffuehrung/belegung/" + id,
		cache: false,
		success: function(data, status, request) {
			// alert(data['id'] + ' ' + data['ort']);
			hideAjaxLoader();
			$(htmlId.VERANSTALTUNG).hide();
			$(htmlId.BC_LINK_AUFFUEHRUNG).text(data.ort.bezeichnung + ' ' + data.saal.bezeichnung + ' ' + data.datum + ' ' + data.uhrzeit);
			$(htmlId.BC_AUFFUEHRUNG).show();
			$(htmlId.AUFFUEHRUNG).empty();
			$('#template_auffuehrung').tmpl(data).appendTo(htmlId.AUFFUEHRUNG);
			$(htmlId.AUFFUEHRUNG).show();
			$("#success_message").hide();
			$("#error_message").hide();
			reservierung = {};
			reservierung['auffuehrung_id'] = data.id;
			reservierung['plaetze'] = [];
		},
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		},
		dataType: "json"
	});
}

function executeReservierung() {
	showAjaxLoader();
	$.ajax({
		url: "service/transaktion/createReservierung",
		data: JSON.stringify(reservierung),
		processData: false,
		type: "POST",
		contentType: "application/json",
		dataType: "json",
		cache: false,
		success: function(data, status, request) {
			hideAjaxLoader();
			if (data.status == "ERFOLGREICH")
				successReservierung(data.transaktion_id);
			else
				failedReservierung(data.plaetze);
		}, 
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		}
	});
}

function successReservierung(transaktionId) {
	$(".markiert").addClass("reserviert");
	$(".markiert").removeClass("markiert");
	$("#success_message").html("Die Reservierung wurde erfolgreich durchgeführt<br />Reservierungsnummer: " + transaktionId);
	$("#success_message").show();
}

function failedReservierung(belegtePlaetze) {
	var msg = "Folgende Pl&auml;tze konnten nicht reserviert werden:<br />";
	for (var p in belegtePlaetze) {
		msg += "Reihe " + p.reihe + ", Nummer " + p.nummer + "<br />";
	}
	$("#error_message").html(msg);
	$("#error_message").show();
}

function loadTransaktionen() {
	showAjaxLoader();
	$.ajax({
		url: "service/transaktion/list/",
		cache: false,
		success: function(data, status, request) {
			hideAjaxLoader();
			$('#transaktionLinks').empty();
			$('#template_transaktionen').tmpl(data).appendTo('#transaktionLinks');
		}, 
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		},
		dataType: "json"
	});
}

function deleteTransaktion(id) {
	showAjaxLoader();
	
	$.ajax({
		url: "service/transaktion/delete/" + id,
		cache: false,
		success: function(data, status, request) {
			$('#transaktion_' + id).remove();
			hideAjaxLoader();
		},
		error: function(request, status, exception) {
			hideAjaxLoader();
			alert(status + " " + exception);
		},
		dataType: "json"
	});
}

/*
 * BREADCRUMBS
 */
function showVeranstaltungen() {
	$(htmlId.AUFFUEHRUNG).hide();
	$(htmlId.BC_AUFFUEHRUNG).hide();
	$(htmlId.VERANSTALTUNG).hide();
	$(htmlId.BC_VERANSTALTUNG).hide();
	$(htmlId.VERANSTALTUNGEN).show();
	$(htmlId.TRANSAKTIONEN).show();
	loadTransaktionen();
	return true;
}

function showVeranstaltung() {
	$(htmlId.AUFFUEHRUNG).hide();
	$(htmlId.BC_AUFFUEHRUNG).hide();
	$(htmlId.VERANSTALTUNG).show();
	return true;
}

/*
 * AJAXLOADER
 */
function showAjaxLoader() {
	$(htmlId.AJAX_LOADER).show();
}

function hideAjaxLoader() {
	$(htmlId.AJAX_LOADER).hide();
}
