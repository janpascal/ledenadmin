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
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;

import views.html.*;

public class Afschriften extends Controller {
  
    public static Result lijst() {
        List<Afschrift> Afschriften = Afschrift.find.all();
        return ok(afschriftenlijst.render(Afschriften));
    }
    
    public static Result csvimport() {
        return ok(csvafschriftenimport.render());
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
            flash("error", "File not found of andere fout");
            return redirect(routes.Afschriften.lijst());    
          }
          flash("message", "Afschriftenlijst geïmporteerd");
          return redirect(routes.Afschriften.lijst());    
        } else {
          flash("error", "Missing file");
          return redirect(routes.Afschriften.lijst());    
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

            Afschrift Afschrift = new Afschrift(datum, naam, bedrag, afbij, tegenrekening, mededelingen);
            Afschrift.create(Afschrift);
        }
    }
}
