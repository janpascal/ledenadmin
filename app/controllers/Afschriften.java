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

import com.avaje.ebean.*;
import models.*;
import play.*;
import play.libs.F.Promise;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;

import views.html.*;

public class Afschriften extends Controller {
  
/*    public static Result lijst() {
        List<Afschrift> Afschriften = Afschrift.find.all();
        return ok(afschriftenlijst.render(Afschriften));
    }
  */  
    public static Result list() {
      return lijst(0, "datum", "asc", "", "", ""); 
    }

    public static Result lijst(int page, String sortBy, String order, 
        String filter, String jaarFilter, String verantwoordFilter) {

        int jaar;
        try {
            jaar = Integer.parseInt(jaarFilter);
        } catch (NumberFormatException e) {
            jaar = -1;
        }
        Page<Afschrift> currentPage = Afschrift.page(page, 15, sortBy, order, filter, jaar, verantwoordFilter);
        //Logger.info("sortBy: " + sortBy);
        return ok(
            afschriftenlijst.render(
                currentPage,
                sortBy, order, filter, jaarFilter, verantwoordFilter
            )
        );
    }

    public static Result toon(Long id) {
      Afschrift a= Afschrift.find.byId(id);
      return ok(afschrift.render(id, a));
    }

    public static Result csvimport() {
        return ok(csvafschriftenimport.render());
   }
    
    public static Result csvBetalingenImport() {
        return ok(betalingenimport.render());
   }
    
    public static Result upload() {
        RequestBody mainbody = request().body();
        MultipartFormData body = mainbody.asMultipartFormData();
        if(body==null) {
            flash("error", "Missing file");
            return redirect(routes.Application.index());
        }
        FilePart part = body.getFile("bestand");
        if (part != null) {
          String fileName = part.getFilename();
          String contentType = part.getContentType(); 
          File file = part.getFile();
          try {
            perform_csvimport(file);
          } catch (Exception e) {
            e.printStackTrace();
            flash("error", "File not found of andere fout: "+e.getMessage());
            return redirect(routes.Afschriften.list());    
          }
          flash("succes", "Afschriftenlijst geïmporteerd");
          return redirect(routes.Afschriften.list());    
        } else {
          flash("error", "Missing file");
          return redirect(routes.Afschriften.list());    
        }
      }

    public static void perform_csvimport(File file) throws
    FileNotFoundException, ParseException, NumberFormatException, IOException {
        System.out.println("Importing CSV afschriften");
        CSVReader reader = new CSVReader(new FileReader(file), ',');
        String [] nextLine;
        // Read and check header line
        nextLine = reader.readNext();
        // Maybe ING CSV format?
        if(nextLine.length==9) {
          if("Datum".equals(nextLine[0]) && 
            "Naam / Omschrijving".equals(nextLine[1]) && 
            "Rekening".equals(nextLine[2]) && 
            "Tegenrekening".equals(nextLine[3]) && 
            "Code".equals(nextLine[4]) && 
            "Af Bij".equals(nextLine[5]) && 
            "Bedrag (EUR)".equals(nextLine[6]) && 
            "MutatieSoort".equals(nextLine[7]) && 
            "Mededelingen".equals(nextLine[8])) {
            System.out.println("Importing ING file");
            perform_INGimport(reader);
          }
        }
    }

    @com.avaje.ebean.annotation.Transactional
    public static void perform_INGimport(CSVReader reader) throws ParseException, NumberFormatException, IOException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        DecimalFormat currencyFormat = new DecimalFormat("0.00");
        currencyFormat.setParseBigDecimal(true);
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String datumString = nextLine[0];
            String naam = nextLine[1];
            String rekening = nextLine[2];
            String tegenrekening = nextLine[3];
            String code = nextLine[4];
            String afbijString = nextLine[5];
            String bedragString = nextLine[6];
            String soort = nextLine[7];
            String mededelingen = nextLine[8];

            BigDecimal bedrag = (BigDecimal) currencyFormat.parse(bedragString);
            Date datum;
            if (datumString.isEmpty()) {
              datum = null;
            } else {
              datum = dateFormatter.parse(datumString);
            }
            Afschrift.AfBij afbij;
            if(afbijString.equals("Af")) {
              afbij=Afschrift.AfBij.AF;
            } else if(afbijString.equals("Bij")) {
              afbij=Afschrift.AfBij.BIJ; 
            } else {
              throw new IOException("Error parsing afbij value "+afbijString);
            }

            List<Afschrift> dubbele = Afschrift.find.where()
               .eq("datum", datum)
               .eq("naam", naam)
               .eq("bedrag", bedrag)
               .eq("afbij", afbij)
               .eq("tegenrekening", tegenrekening)
               .eq("mededelingen", mededelingen)
               .findList();
            if (!dubbele.isEmpty()) {
              throw new IOException("Dubbel afschrift");
            }

            Afschrift Afschrift = new Afschrift(datum, naam, bedrag, afbij, tegenrekening, mededelingen);
            Afschrift.create(Afschrift);
        }
    }

    public static Result uploadBetalingen() {
        RequestBody mainbody = request().body();
        MultipartFormData body = mainbody.asMultipartFormData();
        if(body==null) {
            flash("error", "Missing file");
            return redirect(routes.Application.index());
        }
        FilePart part = body.getFile("bestand");
        if (part != null) {
          String fileName = part.getFilename();
          String contentType = part.getContentType(); 
          File file = part.getFile();
          try {
            perform_betaling_csvimport(file);
          } catch (Exception e) {
            e.printStackTrace();
            flash("error", "File not found of andere fout: "+e.getMessage());
            return redirect(routes.Afschriften.list());    
          }
          flash("succes", "Afschriftenlijst geïmporteerd");
          return redirect(routes.Afschriften.list());    
        } else {
          flash("error", "Missing file");
          return redirect(routes.Afschriften.list());    
        }
      }

    @com.avaje.ebean.annotation.Transactional
    public static void perform_betaling_csvimport(File file) throws FileNotFoundException, ParseException, NumberFormatException, IOException {
        System.out.println("Importing CSV betalingen...");
        CSVReader reader = new CSVReader(new FileReader(file), ';');
        String [] nextLine;
        // Read and check header line
        nextLine = reader.readNext();
        // Proper format?
        if(nextLine.length>4) {
          if("Datum".equals(nextLine[0]) && 
            "Lid".equals(nextLine[1]) && 
            "Bedrag".equals(nextLine[2]) && 
            "Jaar".equals(nextLine[3])) {

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
            DecimalFormat currencyFormat = new DecimalFormat("0.00");
            currencyFormat.setParseBigDecimal(true);
            while ((nextLine = reader.readNext()) != null) {
                String datumString = nextLine[0];
                String naam = nextLine[1];
                String bedragString = nextLine[2];
                String jaarString = nextLine[3];
                String mededelingen = nextLine[4];

                if (jaarString.isEmpty()) {
                  continue;
                }
                BigDecimal bedrag = (BigDecimal) currencyFormat.parse(bedragString);
                Date datum;
                if (datumString.isEmpty()) {
                  continue;
                } else {
                  datum = dateFormatter.parse(datumString);
                }
                int jaar = Integer.parseInt(jaarString);

                Logger.info(datumString+","+naam+","+bedragString+","+jaarString+","+mededelingen);

                // Find lid
                List<Lid> misschien = Lid.find.where()
                    .icontains("personen.name", naam)
                    .findList();

                if(misschien.size()!=1) {
                  Logger.warn("-- NUMBER != 1");
                  for( Lid l: misschien ) {
                      Logger.info("  "+l.toString());
                  }
                  continue;
                }

                Lid lid = misschien.get(0);
                //Logger.info("Geselecteerd lid: "+lid);

                List<Factuur> facturen = Factuur.find.where()
                    .eq("lid", lid)
                    .eq("jaar", jaar)
                    .findList();

                if(facturen.size()!=1) {
                  Logger.info("Geselecteerd lid: "+lid);
                  Logger.warn("-- Verkeerd aantal facturen");
                  for( Factuur f: facturen ) {
                      Logger.info("  "+f.toString());
                  }
                  continue;
                }

                Factuur factuur = facturen.get(0);
                //Logger.info("Geselecteerde factuur: "+factuur);

                List<Afschrift> afschriften = factuur.possiblePayments();
                Afschrift afschrift = null;
                for(Afschrift a: afschriften) {
                  if (a.datum.equals(datum)) {
                    afschrift = a;
                    break;
                  }
                }
                if (afschrift==null) {
                  Logger.info("Geselecteerd lid: "+lid);
                  Logger.info("Geselecteerde factuur: "+factuur);
                  Logger.warn("-- Geen afschrift gevonden");
                  for( Afschrift a: afschriften ) {
                      Logger.info("  "+a.toString());
                  }
                  continue;
                }

                //Logger.info("Geselecteerd afschrift: "+afschrift);

                factuur.betaling = afschrift;
                afschrift.betaaldeFacturen.add(factuur);
                factuur.update();
                afschrift.update();

                //Logger.info("Factuur "+factuur+" betaald door "+afschrift);
            }
          }
        }
    }
}
