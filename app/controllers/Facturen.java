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
import play.libs.F.Promise;
import play.mvc.*;

import views.html.*;

public class Facturen extends Controller {
  
    public static Result lijst() {
        List<FactuurContributie> facturen = FactuurContributie.find.all();
        return ok(facturenlijst.render(facturen));
    }

    public static Result toon(Long id) {
      FactuurContributie fact = FactuurContributie.find.byId(id);
      return ok(factuur.render(fact));
    }

    public static Result markeerBetaling(Long factuurId, Long betalingId) {
      FactuurContributie factuur = FactuurContributie.find.byId(factuurId);
      Afschrift afschrift  = Afschrift.find.byId(betalingId);
      factuur.betaling = afschrift;
      factuur.save();
      return redirect(routes.Facturen.toon(factuurId));
    }

    public static Result wisBetaling(Long factuurId) {
      FactuurContributie factuur = FactuurContributie.find.byId(factuurId);
      factuur.betaling = null;
      factuur.save();
      return redirect(routes.Facturen.toon(factuurId));
    }
}
