package controllers;

import java.io.*;
import java.lang.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DecimalFormat;

import au.com.bytecode.opencsv.CSVReader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import models.*;
import play.*;
import play.data.Form;
import static play.data.Form.*;
import play.data.DynamicForm;
import play.db.ebean.*;
import com.avaje.ebean.*;
import play.libs.F.Promise;
import play.libs.F.Function;
import play.libs.*;
import play.mvc.*;

import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;

import views.html.*;

@Restrict({@Group(Persoon.LID_ROLE)})
public class Facturen extends Controller {

    public static Result list() {
      return lijst(0, 15, "datum", "asc", "", "", ""); 
    }

    public static Result lijst(int page, int pageSize, String sortBy, String order, String
    filter, String jaarFilter, String betaaldFilter) {
        int jaar;
        try {
            jaar = Integer.parseInt(jaarFilter);
        } catch (NumberFormatException e) {
            jaar = -1;
        }
        Page<Factuur> currentPage = Factuur.page(page, pageSize, sortBy, order, filter, jaar, betaaldFilter);
        //Logger.info("sortBy: " + sortBy);
        return ok(
            facturenlijst.render(
                currentPage, pageSize,
                sortBy, order, filter, jaarFilter, betaaldFilter
            )
        );
    }

    public static Result toon(Long id) {
      Factuur fact = Factuur.find.byId(id);
      return ok(factuur.render(id, fact));
    }

    public static Result bewerkFactuur(Long id) {
        Form<Factuur> myForm = form(Factuur.class).fill(Factuur.find.byId(id));
        Factuur factuur = Factuur.find.byId(id);
        System.out.println("Editing "+factuur.toString());
        return ok(editfactuur.render(id, myForm));
    }
    
    public static Result saveFactuur(Long id) {
        Form<Factuur> myForm = form(Factuur.class).bindFromRequest();
        if(myForm.hasErrors()) {
            return badRequest(editfactuur.render(id,myForm));
        }
        myForm.get().update(id);
        // Logger.info("Saved invoice: "+myForm.get().omschrijving);
        flash("success", "Factuur opgeslagen");
        return redirect(routes.Facturen.list());
    }

    public static Result markeerBetaling(Long factuurId, Long betalingId) {
      Factuur factuur = Factuur.find.byId(factuurId);
      Afschrift afschrift  = Afschrift.find.byId(betalingId);
      Logger.info("Markeer betaling voor factuur "+factuur.lid.getFirstName()+ " voor " +factuur.toString());
      factuur.markeerBetaling(afschrift);
      factuur.update();
      afschrift.update();
      Logger.info("Afschrift betaaldeFacturen: " + afschrift.betaaldeFacturen.size()); 
      return redirect(routes.Facturen.toon(factuurId));
    }

    public static Result wisBetaling(Long factuurId) {
      Factuur factuur = Factuur.find.byId(factuurId);
      Logger.info("Wis betaling voor factuur "+factuur.lid.getFirstName()+ " voor " +factuur.toString());
      Afschrift betaling = factuur.betaling;
      factuur.betaling = null;
      Logger.info("Betaling aantal facturen: "+betaling.betaaldeFacturen.size());
      Logger.info("Betaling bevat factuur: "+betaling.betaaldeFacturen.contains(factuur));
      factuur.save();
      flash("success", "Betaling gewist");
      return redirect(routes.Facturen.toon(factuurId));
    }

    public static Result deleteInvoice(Long id) {
      Factuur.delete(id);
      Logger.info("Deleted invoice "+id);
      flash("success", "Factuur verwijderd");
      return redirect(routes.Facturen.list());
    }

    public static Result newInvoicesForm() {
      return ok(new_invoices_form.render(new DynamicForm()));
    }

    public static Result createNewInvoices() throws ParseException {
      DynamicForm form = new DynamicForm().bindFromRequest();
      String yearString = form.get("year");
      String amountString = form.get("amount");
      Logger.info("Generating invoices for year "+yearString+" ("+amountString+")");
      int year = Integer.parseInt(yearString);
      DecimalFormat currencyFormat = new DecimalFormat("0.00");
      currencyFormat.setParseBigDecimal(true);
      BigDecimal amount = (BigDecimal) currencyFormat.parse(amountString);
      Logger.info("Generating invoices for year "+year+" ("+amount+")");
      List<Factuur> facturen = Factuur.generateInvoices(year, amount);
      for(Factuur f: facturen) {
        f.save();
      }
      flash("success", facturen.size()+" facturen gegenereerd");
      return redirect(routes.Facturen.list()); 
    }

    @Restrict({@Group(Persoon.BESTUUR_ROLE),@Group(Persoon.ADMIN_ROLE)})
    public static Result showContributieOverzicht(int stateAtYear) throws Exception {
        Map<String, String[]> params = request().queryString();
        if(stateAtYear==0) stateAtYear=2013; // FIXME current year
        List<Integer> years = Factuur.jarenMetContributieFacturen();
        Map<Integer,Factuur.YearSummary> overview = new TreeMap<Integer,Factuur.YearSummary>();
        for(Integer year: years) {
          overview.put(year, Factuur.feesSummary(year, stateAtYear));
        }
        return ok(contributieoverzicht.render(overview, stateAtYear));
    }

    @Restrict({@Group(Persoon.BESTUUR_ROLE),@Group(Persoon.ADMIN_ROLE)})
    public static Result paymentsPerYear() throws Exception {
        Map<String, String[]> params = request().queryString();

        Factuur.FeePayments data = Factuur.feePaymentsOverview();

        ObjectNode result = Json.newObject();
        //result.put("sEcho", Integer.valueOf(params.get("sEcho")[0]));
        //result.put("iTotalRecords", data.paymentYears.size());
        //result.put("iTotalDisplayRecords", data.paymentYears.size());

        ArrayNode an = result.putArray("aaData");
        
        for(Integer year: data.paymentYears) {
            ObjectNode row = Json.newObject();
            row.put("0", year);
            Map<Integer,BigDecimal> foryear = data.payments.get(year);
            if (foryear != null) {
                BigDecimal total = new BigDecimal(0);
                for(int i=0; i<data.feeYears.size(); i++) {
                  Integer feeYear = data.feeYears.get(i);
                  BigDecimal amount = foryear.get(feeYear);
                  row.put(""+(i+1), amount);
                  if (amount!=null) total = total.add(amount);
                }
                row.put(""+(data.feeYears.size()+1), total);
            }
            an.add(row);
        } 

        ArrayNode ac = result.putArray("aoColumnDefs");
        ObjectNode row = Json.newObject();
        row.put("sTitle", "In kalenderjaar");
        ArrayNode targets = row.putArray("aTargets");
        targets.add(0);
        ac.add(row);

        for(int i=0; i<data.feeYears.size(); i++) {
        //for(int i=0; i<2; i++) {
            Integer year = data.feeYears.get(i);
            row = Json.newObject();
            row.put("sTitle", "Betaald voor "+year);
            targets = row.putArray("aTargets");
            targets.add(i+1);
            ac.add(row);
        }

        row = Json.newObject();
        row.put("sTitle", "Totaal betaald");
        targets = row.putArray("aTargets");
        targets.add(data.feeYears.size()+1);
        ac.add(row);
        
        return ok(result);
    }
}
