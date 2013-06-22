package controllers;

import java.io.*;
import java.lang.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DecimalFormat;

import akka.japi.Function;

import au.com.bytecode.opencsv.CSVReader;

import models.*;
import play.*;
import play.data.DynamicForm;
import com.avaje.ebean.*;
import play.libs.F.Promise;
import play.mvc.*;

import views.html.*;

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
      FactuurContributie fact = FactuurContributie.find.byId(id);
      return ok(factuur.render(id, fact));
    }

    public static Result markeerBetaling(Long factuurId, Long betalingId) {
      FactuurContributie factuur = FactuurContributie.find.byId(factuurId);
      Afschrift afschrift  = Afschrift.find.byId(betalingId);
      Logger.info("Markeer betaling voor factuur "+factuur.lid.getFirstName()+ " voor " +factuur.jaar);
      factuur.betaling = afschrift;
      afschrift.betaaldeFacturen.add(factuur);
      factuur.update();
      afschrift.update();
      Logger.info("Afschrift betaaldeFacturen: " + afschrift.betaaldeFacturen.size()); 
      return redirect(routes.Facturen.toon(factuurId));
    }

    public static Result wisBetaling(Long factuurId) {
      FactuurContributie factuur = FactuurContributie.find.byId(factuurId);
      Logger.info("Wis betaling voor factuur "+factuur.lid.getFirstName()+ " voor " +factuur.jaar);
      Afschrift betaling = factuur.betaling;
      factuur.betaling = null;
      Logger.info("Betaling aantal facturen: "+betaling.betaaldeFacturen.size());
      Logger.info("Betaling bevat factuur: "+betaling.betaaldeFacturen.contains(factuur));
      factuur.save();
      return redirect(routes.Facturen.toon(factuurId));
    }

    public static Result saveFactuur(Long factuurId) {
      Factuur factuur = FactuurContributie.find.byId(factuurId);
      String[] postAction = request().body().asFormUrlEncoded().get("action");
      if (postAction == null || postAction.length == 0) {
          return badRequest("You must provide a valid action");
      } else {
         Logger.info(postAction[0]);
      }
      return TODO;
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
      List<Factuur> facturen = FactuurContributie.generateInvoices(year, amount);
      for(Factuur f: facturen) {
        f.save();
      }
      flash("success", facturen.size()+" facturen gegenereerd");
      return redirect(routes.Facturen.list()); 
    }
}
