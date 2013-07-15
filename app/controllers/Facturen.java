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

import models.*;
import play.*;
import play.data.Form;
import static play.data.Form.*;
import play.data.DynamicForm;
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
      return lijst(0, "datum", "asc", "", "", ""); 
    }

    public static Result lijst(int page, String sortBy, String order, String
    filter, String jaarFilter, String betaaldFilter) {
        int jaar;
        try {
            jaar = Integer.parseInt(jaarFilter);
        } catch (NumberFormatException e) {
            jaar = -1;
        }
        Page<Factuur> currentPage = Factuur.page(page, 15, sortBy, order, filter, jaar, betaaldFilter);
        //Logger.info("sortBy: " + sortBy);
        return ok(
            facturenlijst.render(
                currentPage,
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
}
