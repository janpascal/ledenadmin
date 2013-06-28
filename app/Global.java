import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.*;

import com.avaje.ebean.Ebean;

import controllers.Leden;
import controllers.Afschriften;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
        InitialData.insert(app);
    }
    
    static class InitialData {
        
        public static void insert(Application app) {
            if(Ebean.find(Lid.class).findRowCount() == 0) {
                if(Play.isDev()) {
                    System.out.println("Seeding members");
                    try {
                        Leden.perform_csvimport(new File("data/leden.csv"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    System.out.println("Seeding bankafschriften");
                    try {
                        Afschriften.perform_csvimport(new File("data/afschriften.csv"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                  
                    System.out.println("Seeding contributiefacturen");
                    int aantalFacturen = 0;
                    for(int jaar=2009; jaar<=2013; jaar++) {
                        for( Lid lid: Lid.find.all()) {
                            Factuur factuur = new Factuur(new Date(), lid, new BigDecimal(20), jaar);
                            factuur.save();
                            aantalFacturen++;
                        }
                    }

                    System.out.println("Generated "+aantalFacturen+" facturen");

                    Logger.info("Seeding payments");
                    try {
                        Afschriften.perform_betaling_csvimport(new File("data/betalingen.csv"), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }
        }
        
    }
    
}


